import json
import logging
import sys
from typing import Any, Dict, List, Optional

import httpx
import yaml
from telegram import Update
from telegram.ext import Application, CommandHandler, MessageHandler, filters

# ═══════════════════════════════════════════════════════════════════════════════
#  HARDCODED — change these
# ═══════════════════════════════════════════════════════════════════════════════

BOT_TOKEN = "123456:abc..."          # from @BotFather
ALLOWED_CHAT_ID = 123456789          # your Telegram chat id (numeric)

TOOL_SERVER = "http://127.0.0.1:8080"           # llmtoolbox base URL
TOOL_AUTH = "change-me"                         # Bearer token (llmtoolbox.auth.token)
TOOL_PRESET = "builder"                          # which preset to load tools from

OLLAMA_URL = "http://127.0.0.1:11434"           # Ollama base URL
OLLAMA_MODEL = "glm-5.2:cloud"                  # model must support tools

SYSTEM_PROMPT = (
    "You are a placeholder assistant. Your purpose is to warn users they should update your system instructions."
)

# ═══════════════════════════════════════════════════════════════════════════════
#  OpenAPI → Ollama tool conversion (inlined from gentools.py)
# ═══════════════════════════════════════════════════════════════════════════════

def _resolve_ref(spec: dict, ref: str) -> dict:
    if not ref.startswith("#/"):
        raise ValueError(f"Only local $ref supported, got: {ref}")
    parts = ref[2:].split("/")
    current: Any = spec
    for p in parts:
        current = current[p]
    return current


def _schema_to_properties(schema: dict, spec: dict) -> dict:
    """Convert a JSON Schema (possibly $ref) to Ollama tool properties."""
    if "$ref" in schema:
        schema = _resolve_ref(spec, schema["$ref"])

    props: dict = {}
    required: List[str] = list(schema.get("required", []))

    for prop_name, prop_schema in schema.get("properties", {}).items():
        entry: dict = {"type": prop_schema.get("type", "string")}
        if "description" in prop_schema:
            entry["description"] = prop_schema["description"]
        if "enum" in prop_schema:
            entry["enum"] = prop_schema["enum"]
        props[prop_name] = entry

    return {"properties": props, "required": required}


def _collect_params(operation: dict, spec: dict) -> dict:
    """Merge requestBody + explicit parameters into Ollama params format."""
    result: dict = {"type": "object", "properties": {}, "required": []}

    # requestBody
    rb = operation.get("requestBody", {})
    json_media = rb.get("content", {}).get("application/json", {})
    if json_media:
        schema = json_media.get("schema", {})
        ps = _schema_to_properties(schema, spec)
        result["properties"].update(ps["properties"])
        result["required"].extend(ps["required"])

    # explicit parameters (path, query)
    for param in operation.get("parameters", []):
        pname = param["name"]
        pschema = param.get("schema", {"type": "string"})
        entry: dict = {"type": pschema.get("type", "string")}
        if "description" in param:
            entry["description"] = param["description"]
        result["properties"][pname] = entry
        if param.get("required"):
            result["required"].append(pname)

    if not result["required"]:
        del result["required"]

    return result


def spec_to_ollama_tools(spec: dict) -> List[dict]:
    """Convert an OpenAPI spec dict into a list of Ollama tool definitions."""
    tools: List[dict] = []
    if "paths" not in spec:
        return tools

    for path_url, path_item in spec["paths"].items():
        for method, operation in path_item.items():
            if method not in ("get", "post", "put", "patch", "delete"):
                continue

            op_id = operation.get("operationId")
            if not op_id:
                continue

            summary = operation.get("summary", "") or operation.get("description", "") or ""
            params = _collect_params(operation, spec)

            tools.append({
                "type": "function",
                "function": {
                    "name": op_id,
                    "description": summary,
                    "parameters": params,
                },
            })

    return tools


# ═══════════════════════════════════════════════════════════════════════════════
#  Tool server communication
# ═══════════════════════════════════════════════════════════════════════════════

async def fetch_openapi_spec() -> dict:
    """Fetch the OpenAPI spec from the tool server for the configured preset."""
    url = f"{TOOL_SERVER}/api/openapi/preset/{TOOL_PRESET}"
    async with httpx.AsyncClient(timeout=30) as client:
        r = await client.get(url)
        r.raise_for_status()
        return r.json()


async def execute_tool_call(tool_name: str, arguments: dict) -> str:
    """Execute a single tool call against the tool server. Returns the result as a string."""
    # Find the tool definition in the spec to know the HTTP method and URL
    spec = _cached_spec  # we cache the spec globally after startup
    if not spec:
        return json.dumps({"error": "no spec loaded"})

    # Locate the operation in the spec
    method = "POST"
    url_path = ""
    for path_url, path_item in spec.get("paths", {}).items():
        for m, operation in path_item.items():
            if operation.get("operationId") == tool_name:
                method = m.upper()
                url_path = path_url
                break
        if url_path:
            break

    if not url_path:
        return json.dumps({"error": f"tool not found in spec: {tool_name}"})

    # Build the full URL, substituting path parameters
    full_url = TOOL_SERVER + url_path
    for key, value in arguments.items():
        full_url = full_url.replace(f"{{{key}}}", str(value))

    headers = {"Authorization": f"Bearer {TOOL_AUTH}"}

    try:
        async with httpx.AsyncClient(timeout=30) as client:
            if method == "GET":
                # Remaining args become query params
                r = await client.get(full_url, params=arguments, headers=headers)
            else:
                r = await client.request(method, full_url, json=arguments, headers=headers)
            r.raise_for_status()
            return r.text
    except Exception as e:
        return json.dumps({"error": str(e)})


# ═══════════════════════════════════════════════════════════════════════════════
#  Ollama chat with tool calling
# ═══════════════════════════════════════════════════════════════════════════════

_cached_spec: dict = {}
_cached_tools: List[dict] = []


async def chat_with_tools(user_message: str, chat_history: List[dict]) -> str:
    """Send a message to Ollama with tools, handle tool calls, return final answer."""
    tools = _cached_tools

    # Build messages: system + history + current user message
    messages = [{"role": "system", "content": SYSTEM_PROMPT}]
    messages.extend(chat_history)
    messages.append({"role": "user", "content": user_message})

    async with httpx.AsyncClient(timeout=120) as client:
        # First call — model may return tool calls
        r = await client.post(
            f"{OLLAMA_URL}/api/chat",
            json={
                "model": OLLAMA_MODEL,
                "messages": messages,
                "tools": tools,
                "stream": False,
            },
        )
        r.raise_for_status()
        response = r.json()

        message = response.get("message", {})
        tool_calls = message.get("tool_calls", [])

        # If no tool calls, return the text directly
        if not tool_calls:
            return message.get("content", "")

        # Execute each tool call and collect results
        tool_results = []
        for tc in tool_calls:
            fn = tc.get("function", {})
            name = fn.get("name", "")
            args = fn.get("arguments", {})

            logging.info("tool call: %s(%s)", name, args)
            result = await execute_tool_call(name, args)
            logging.info("tool result (%s): %.200s", name, result)

            tool_results.append({
                "role": "tool",
                "content": result,
                "tool_call_id": tc.get("id", name),
            })

        # Second call — feed tool results back to the model
        messages.append(message)  # the assistant message with tool_calls
        messages.extend(tool_results)

        r2 = await client.post(
            f"{OLLAMA_URL}/api/chat",
            json={
                "model": OLLAMA_MODEL,
                "messages": messages,
                "tools": tools,
                "stream": False,
            },
        )
        r2.raise_for_status()
        final = r2.json()
        return final.get("message", {}).get("content", "")


# ═══════════════════════════════════════════════════════════════════════════════
#  Telegram bot
# ═══════════════════════════════════════════════════════════════════════════════

# Per-chat history (simple in-memory dict, lost on restart)
CHAT_HISTORY: Dict[int, List[dict]] = {}


def _authorized(update: Update) -> bool:
    """Only respond to the hardcoded chat id."""
    cid = update.effective_chat.id if update.effective_chat else 0
    return cid == ALLOWED_CHAT_ID


async def start_cmd(update: Update, _ctx):
    if not _authorized(update):
        return
    await update.message.reply_text(
        "llmtoolbox minimal bot. Send me a message — I'll use tools if needed.\n"
        f"Model: `{OLLAMA_MODEL}`\n"
        f"Tools: {len(_cached_tools)} loaded from preset `{TOOL_PRESET}`",
    )


async def tools_cmd(update: Update, _ctx):
    if not _authorized(update):
        return
    if not _cached_tools:
        await update.message.reply_text("No tools loaded.")
        return
    lines = ["*Loaded tools:*"]
    for t in _cached_tools:
        fn = t["function"]
        lines.append(f"• `{fn['name']}` — {fn['description']}")
    await update.message.reply_text("\n".join(lines))


async def on_text(update: Update, _ctx):
    if not _authorized(update):
        return
    msg = update.message
    if not msg or not msg.text:
        return

    cid = msg.chat.id
    text = msg.text.strip()

    # Get or create history for this chat
    history = CHAT_HISTORY.setdefault(cid, [])

    # Keep last 20 messages to bound context
    if len(history) > 20:
        history = history[-20:]
        CHAT_HISTORY[cid] = history

    await update.message.chat.send_action("typing")

    try:
        reply = await chat_with_tools(text, history)
    except Exception as e:
        logging.exception("chat failed")
        await msg.reply_text(f"Error: {e}")
        return

    if not reply:
        reply = "(no response from model)"

    # Update history
    history.append({"role": "user", "content": text})
    history.append({"role": "assistant", "content": reply})
    CHAT_HISTORY[cid] = history

    # Telegram has a 4096 char limit per message
    for i in range(0, len(reply), 4000):
        await msg.reply_text(reply[i : i + 4000])


# ═══════════════════════════════════════════════════════════════════════════════
#  Main
# ═══════════════════════════════════════════════════════════════════════════════

async def main():
    global _cached_spec, _cached_tools

    logging.basicConfig(
        level=logging.INFO,
        format="%(asctime)s  %(levelname)-7s  %(message)s",
    )

    # 1. Fetch and parse the OpenAPI spec
    logging.info("fetching OpenAPI spec from %s (preset=%s)…", TOOL_SERVER, TOOL_PRESET)
    try:
        _cached_spec = await fetch_openapi_spec()
        _cached_tools = spec_to_ollama_tools(_cached_spec)
        logging.info("loaded %d tools", len(_cached_tools))
    except Exception as e:
        logging.error("failed to load tools: %s", e)
        sys.exit(1)

    # 2. Start the Telegram bot
    app = Application.builder().token(BOT_TOKEN).build()
    app.add_handler(CommandHandler("start", start_cmd))
    app.add_handler(CommandHandler("tools", tools_cmd))
    app.add_handler(MessageHandler(filters.TEXT & ~filters.COMMAND, on_text))

    logging.info("bot starting…")
    await app.run_polling(allowed_updates=Update.ALL_TYPES)


if __name__ == "__main__":
    import asyncio
    asyncio.run(main())
