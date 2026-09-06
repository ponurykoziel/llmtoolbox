"""
Headless Browser — REST API bridge for Playwright + Chromium.

Profiles: isolated user-data-dirs, persistent cookies/storage.
Anonymous: throwaway contexts, no persistence.

All endpoints bound to 127.0.0.1 only.
"""

import asyncio
import json
import os
import shutil
import uuid
from pathlib import Path
from typing import Optional

import yaml
from fastapi import FastAPI, HTTPException, Request
from fastapi.responses import JSONResponse
from pydantic import BaseModel
from playwright.async_api import async_playwright, Browser, BrowserContext, Page

from stealth import (
    STEALTH_INIT_SCRIPT,
    STEALTH_CONTEXT_CONFIG,
    STEALTH_LAUNCH_ARGS,
)

# ── Config ──────────────────────────────────────────────────────────────────
BASE_DIR = Path(__file__).resolve().parent
PROFILES_DIR = BASE_DIR / "profiles"
PROFILES_DIR.mkdir(exist_ok=True)

CONFIG_PATH = BASE_DIR / "config.yaml"


def _load_config() -> dict:
    """Load configuration from config.yaml, falling back to defaults."""
    if CONFIG_PATH.exists():
        with open(CONFIG_PATH) as f:
            return yaml.safe_load(f) or {}
    return {}


_config = _load_config()
HOST = _config.get("host", "127.0.0.1")
PORT = int(_config.get("port", 9020))
AUTH_TOKEN = _config.get("token", "")

# ── State ───────────────────────────────────────────────────────────────────
app = FastAPI(title="Headless Browser Bridge")

# Paths that do not require authentication
_OPEN_PATHS = {"/docs", "/openapi.json", "/redoc"}


@app.middleware("http")
async def auth_middleware(request: Request, call_next):
    """Global bearer-token guard. Skips OpenAPI/docs paths."""
    if request.url.path in _OPEN_PATHS:
        return await call_next(request)

    if not AUTH_TOKEN or AUTH_TOKEN == "change-me-to-a-random-secret":
        # No token configured or still the placeholder — allow all requests (dev mode)
        return await call_next(request)

    auth_header = request.headers.get("Authorization", "")
    if not auth_header.startswith("Bearer "):
        return JSONResponse(
            status_code=401,
            content={"detail": "Missing or invalid Authorization header. Use: Bearer <token>"},
        )

    provided = auth_header[len("Bearer "):]
    if provided != AUTH_TOKEN:
        return JSONResponse(
            status_code=403,
            content={"detail": "Invalid bearer token."},
        )

    return await call_next(request)

playwright_instance = None
browser: Optional[Browser] = None

# Active sessions: session_id -> {context, page, profile_name}
sessions: dict[str, dict] = {}

# ── Models ──────────────────────────────────────────────────────────────────


class ProfileCreate(BaseModel):
    name: str


class ProfileInfo(BaseModel):
    name: str
    path: str


class SessionOpen(BaseModel):
    profile: Optional[str] = None  # None = anonymous


class GotoRequest(BaseModel):
    url: str
    wait_until: str = "load"  # load | domcontentloaded | networkidle


class ClickRequest(BaseModel):
    selector: str
    timeout: int = 10_000


class ExecuteRequest(BaseModel):
    script: str


class TypeRequest(BaseModel):
    selector: str
    text: str
    method: str = "type"  # "type" = char-by-char (realistic, triggers keyboard hooks), "fill" = fast clear+fill


class ScreenshotRequest(BaseModel):
    full_page: bool = False


# ── Lifecycle ───────────────────────────────────────────────────────────────


@app.on_event("startup")
async def startup():
    global playwright_instance, browser
    playwright_instance = await async_playwright().start()
    browser = await playwright_instance.chromium.launch(
        headless=True,
        args=STEALTH_LAUNCH_ARGS,
    )
    print(f"Browser launched. Profiles dir: {PROFILES_DIR}")


@app.on_event("shutdown")
async def shutdown():
    global browser, playwright_instance
    for sid in list(sessions):
        await _close_session(sid)
    if browser:
        await browser.close()
    if playwright_instance:
        await playwright_instance.stop()
    print("Shutdown complete.")


# ── Helpers ─────────────────────────────────────────────────────────────────


def _profile_dir(name: str) -> Path:
    """Sanitize and return profile directory path."""
    safe = name.replace("/", "_").replace("\\", "_").strip()
    if not safe:
        raise HTTPException(400, "Invalid profile name")
    return PROFILES_DIR / safe


async def _close_session(session_id: str):
    s = sessions.pop(session_id, None)
    if s is None:
        return
    ctx: BrowserContext = s["context"]
    await ctx.close()


def _get_session(session_id: str) -> dict:
    s = sessions.get(session_id)
    if s is None:
        raise HTTPException(404, "Session not found")
    return s


# ── Profile endpoints ───────────────────────────────────────────────────────


@app.get("/profiles", operation_id="browser_profiles_list", description="List all browser profiles with their isolated storage directories.")
async def list_profiles():
    if not PROFILES_DIR.exists():
        return []
    return [
        {"name": d.name}
        for d in PROFILES_DIR.iterdir()
        if d.is_dir()
    ]


@app.post("/profiles", status_code=201, operation_id="browser_profiles_create", description="Create a new browser profile with an isolated user data directory for persistent cookies and storage.")
async def create_profile(body: ProfileCreate):
    path = _profile_dir(body.name)
    if path.exists():
        raise HTTPException(409, "Profile already exists")
    path.mkdir(parents=True)
    return {"name": body.name, "path": str(path)}


@app.delete("/profiles/{name}", operation_id="browser_profiles_delete", description="Delete a profile and all its stored data, closing any active sessions using it.")
async def delete_profile(name: str):
    path = _profile_dir(name)
    if not path.exists():
        raise HTTPException(404, "Profile not found")
    # Close any sessions using this profile
    for sid in list(sessions):
        if sessions[sid].get("profile_name") == name:
            await _close_session(sid)
    shutil.rmtree(path)
    return {"deleted": name}


# ── Session endpoints ───────────────────────────────────────────────────────


@app.post("/sessions", status_code=201, operation_id="browser_session_open", description="Open a new browsing session, either tied to a persistent profile or as an anonymous throwaway context.")
async def open_session(body: SessionOpen):
    global browser

    session_id = uuid.uuid4().hex[:12]

    if body.profile:
        # Persistent profile
        path = _profile_dir(body.profile)
        if not path.exists():
            raise HTTPException(404, "Profile not found — create it first")
        context = await browser.new_context(
            **STEALTH_CONTEXT_CONFIG,
            storage_state=str(path / "state.json")
            if (path / "state.json").exists()
            else None,
        )
        profile_name = body.profile
    else:
        # Anonymous — throwaway context
        context = await browser.new_context(**STEALTH_CONTEXT_CONFIG)
        profile_name = None

    page = await context.new_page()

    # Full stealth init script
    await page.add_init_script(STEALTH_INIT_SCRIPT)

    sessions[session_id] = {
        "context": context,
        "page": page,
        "profile_name": profile_name,
    }

    return {"session_id": session_id, "profile": profile_name}


@app.delete("/sessions/{session_id}", operation_id="browser_session_close", description="Close a browsing session, persisting storage state for profile-backed sessions.")
async def close_session(session_id: str):
    s = _get_session(session_id)
    ctx: BrowserContext = s["context"]
    profile_name = s.get("profile_name")

    if profile_name:
        path = _profile_dir(profile_name)
        state = await ctx.storage_state()
        (path / "state.json").write_text(json.dumps(state, indent=2))

    await _close_session(session_id)
    return {"closed": session_id}


@app.get("/sessions", operation_id="browser_session_list", description="List all currently active browsing sessions.")
async def list_sessions():
    return [
        {"session_id": sid, "profile": s["profile_name"]}
        for sid, s in sessions.items()
    ]


# ── Browser primitives ──────────────────────────────────────────────────────


@app.post("/sessions/{session_id}/goto", operation_id="browser_interact_page_navigate_to", description="Navigate the session's page to a URL, waiting for the page to load.")
async def goto(session_id: str, body: GotoRequest):
    s = _get_session(session_id)
    page: Page = s["page"]
    try:
        await page.goto(body.url, wait_until=body.wait_until, timeout=30_000)
        return {"url": page.url, "title": await page.title()}
    except Exception as e:
        raise HTTPException(500, f"Navigation failed: {e}")


@app.get("/sessions/{session_id}/content", operation_id="browser_interact_page_get_html", description="Retrieve the full HTML content of the current page.")
async def get_content(session_id: str):
    s = _get_session(session_id)
    page: Page = s["page"]
    html = await page.content()
    return {"url": page.url, "html": html}


@app.get("/sessions/{session_id}/text", operation_id="browser_interact_page_get_visible_text", description="Extract visible text from the current page, suitable for LLM consumption.")
async def get_text(session_id: str):
    s = _get_session(session_id)
    page: Page = s["page"]
    text = await page.inner_text("body")
    return {"url": page.url, "text": text}


@app.post("/sessions/{session_id}/click", operation_id="browser_interact_page_click_element", description="Click an element on the page by CSS selector.")
async def click(session_id: str, body: ClickRequest):
    s = _get_session(session_id)
    page: Page = s["page"]
    try:
        await page.click(body.selector, timeout=body.timeout)
        return {"clicked": body.selector, "url": page.url}
    except Exception as e:
        raise HTTPException(500, f"Click failed: {e}")


@app.post("/sessions/{session_id}/screenshot_base64", operation_id="browser_interact_page_capture_screenshot_base64", description="Capture a screenshot of the current page, returned as a base64-encoded PNG string.")
async def screenshot_base64(session_id: str, body: ScreenshotRequest = ScreenshotRequest()):
    import base64

    s = _get_session(session_id)
    page: Page = s["page"]
    img_bytes = await page.screenshot(full_page=body.full_page)
    b64 = base64.b64encode(img_bytes).decode()
    return {"screenshot_b64": b64}


@app.post("/sessions/{session_id}/screenshot_png", operation_id="browser_interact_page_capture_screenshot_png", description="Capture a screenshot of the current page, returned as raw PNG bytes (image/png).")
async def screenshot_png(session_id: str, body: ScreenshotRequest = ScreenshotRequest()):
    from fastapi.responses import Response

    s = _get_session(session_id)
    page: Page = s["page"]
    img_bytes = await page.screenshot(full_page=body.full_page)
    return Response(content=img_bytes, media_type="image/png")


@app.post("/sessions/{session_id}/execute", operation_id="browser_interact_page_execute_javascript", description="Execute arbitrary JavaScript in the page context and return the result.")
async def execute(session_id: str, body: ExecuteRequest):
    s = _get_session(session_id)
    page: Page = s["page"]
    try:
        result = await page.evaluate(body.script)
        return {"result": result}
    except Exception as e:
        raise HTTPException(500, f"Script failed: {e}")


@app.post("/sessions/{session_id}/type", operation_id="browser_interact_page_type_text", description="Type text into an input field using Playwright's trusted input pipeline (isTrusted=true). Use method='fill' for fast clear+fill, or method='type' for character-by-character typing.")
async def type_text(session_id: str, body: TypeRequest):
    s = _get_session(session_id)
    page: Page = s["page"]
    try:
        if body.method == "type":
            await page.type(body.selector, body.text, timeout=10_000)
        else:
            await page.fill(body.selector, body.text, timeout=10_000)
        return {"typed": body.selector, "text": body.text, "method": body.method, "url": page.url}
    except Exception as e:
        raise HTTPException(500, f"Type failed: {e}")


@app.get("/sessions/{session_id}/url", operation_id="browser_session_url_print", description="Get the current URL of the session's page.")
async def current_url(session_id: str):
    s = _get_session(session_id)
    page: Page = s["page"]
    return {"url": page.url}


# ── Entrypoint ──────────────────────────────────────────────────────────────

if __name__ == "__main__":
    import uvicorn

    uvicorn.run(app, host=HOST, port=PORT)
