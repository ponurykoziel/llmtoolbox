import com.google.gson.*;
import com.google.gson.reflect.TypeToken;

import java.net.URI;
import java.net.http.*;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpResponse.BodyHandlers;
import java.time.Duration;
import java.util.*;

public class MinimalBot {

    // ═══════════════════════════════════════════════════════════════════════════
    //  HARDCODED — change these
    // ═══════════════════════════════════════════════════════════════════════════

    static final String TOOL_SERVER = "http://127.0.0.1:8080";
    static final String TOOL_AUTH   = "change-me";
    static final String TOOL_PRESET = "builder";

    static final String OLLAMA_URL   = "http://127.0.0.1:11434";
    static final String OLLAMA_MODEL = "qwen2.5:7b";

    static final String SYSTEM_PROMPT =
        "You are a helpful assistant with access to system tools. " +
        "Use tools when appropriate, then respond in plain text. " +
        "Be concise.";

    // ═══════════════════════════════════════════════════════════════════════════
    //  JSON helpers
    // ═══════════════════════════════════════════════════════════════════════════

    static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    static final HttpClient HTTP = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();

    static String jstr(JsonElement e) {
        return e == null || e.isJsonNull() ? "" : e.getAsString();
    }

    static JsonElement jget(JsonElement node, String... path) {
        for (String key : path) {
            if (node == null || !node.isJsonObject()) return null;
            node = node.getAsJsonObject().get(key);
        }
        return node;
    }

    // ═══════════════════════════════════════════════════════════════════════════
    //  OpenAPI → Ollama tool conversion
    // ═══════════════════════════════════════════════════════════════════════════

    static JsonElement resolveRef(JsonObject spec, String ref) {
        if (!ref.startsWith("#/")) throw new IllegalArgumentException("Only local $ref: " + ref);
        JsonElement current = spec;
        for (String part : ref.substring(2).split("/")) {
            if (!current.isJsonObject()) return null;
            current = current.getAsJsonObject().get(part);
            if (current == null) return null;
        }
        return current;
    }

    static JsonObject schemaToParams(JsonElement schemaEl, JsonObject spec) {
        if (schemaEl.isJsonObject() && schemaEl.getAsJsonObject().has("$ref")) {
            schemaEl = resolveRef(spec, schemaEl.getAsJsonObject().get("$ref").getAsString());
        }
        if (!schemaEl.isJsonObject()) return new JsonObject();

        JsonObject schema = schemaEl.getAsJsonObject();
        JsonObject params = new JsonObject();
        params.addProperty("type", schema.has("type") ? schema.get("type").getAsString() : "object");

        JsonObject properties = new JsonObject();
        JsonArray required = new JsonArray();

        JsonObject props = schema.getAsJsonObject("properties");
        if (props != null) {
            for (var entry : props.entrySet()) {
                JsonObject ps = entry.getValue().getAsJsonObject();
                JsonObject prop = new JsonObject();
                prop.addProperty("type", ps.has("type") ? ps.get("type").getAsString() : "string");
                if (ps.has("description")) prop.addProperty("description", ps.get("description").getAsString());
                if (ps.has("enum")) prop.add("enum", ps.get("enum").deepCopy());
                properties.add(entry.getKey(), prop);
            }
        }

        JsonArray req = schema.getAsJsonArray("required");
        if (req != null) for (JsonElement r : req) required.add(r.getAsString());

        params.add("properties", properties);
        if (required.size() > 0) params.add("required", required);
        return params;
    }

    static JsonObject collectParams(JsonObject operation, JsonObject spec) {
        JsonObject params = new JsonObject();
        params.addProperty("type", "object");
        JsonObject properties = new JsonObject();
        JsonArray required = new JsonArray();

        // requestBody
        JsonElement rb = jget(operation, "requestBody", "content", "application/json", "schema");
        if (rb != null) {
            JsonObject ps = schemaToParams(rb, spec);
            JsonObject psProps = ps.getAsJsonObject("properties");
            if (psProps != null) {
                for (var e : psProps.entrySet()) properties.add(e.getKey(), e.getValue());
            }
            JsonArray psReq = ps.getAsJsonArray("required");
            if (psReq != null) for (JsonElement r : psReq) required.add(r.getAsString());
        }

        // explicit parameters
        JsonArray parameters = operation.getAsJsonArray("parameters");
        if (parameters != null) {
            for (JsonElement p : parameters) {
                JsonObject param = p.getAsJsonObject();
                String name = param.get("name").getAsString();
                JsonObject pschema = param.has("schema") ? param.getAsJsonObject("schema") : new JsonObject();
                if (!pschema.has("type")) pschema.addProperty("type", "string");

                JsonObject entry = new JsonObject();
                entry.addProperty("type", pschema.get("type").getAsString());
                if (param.has("description")) entry.addProperty("description", param.get("description").getAsString());
                properties.add(name, entry);
                if (param.has("required") && param.get("required").getAsBoolean()) {
                    required.add(name);
                }
            }
        }

        params.add("properties", properties);
        if (required.size() > 0) params.add("required", required);
        return params;
    }

    static JsonArray specToOllamaTools(JsonObject spec) {
        JsonArray tools = new JsonArray();
        JsonObject paths = spec.getAsJsonObject("paths");
        if (paths == null) return tools;

        for (var pathEntry : paths.entrySet()) {
            JsonObject pathItem = pathEntry.getValue().getAsJsonObject();
            for (String method : List.of("get", "post", "put", "patch", "delete")) {
                JsonElement opEl = pathItem.get(method);
                if (opEl == null || !opEl.isJsonObject()) continue;
                JsonObject op = opEl.getAsJsonObject();

                String opId = jstr(op.get("operationId"));
                if (opId.isEmpty()) continue;

                String summary = jstr(op.get("summary"));
                if (summary.isEmpty()) summary = jstr(op.get("description"));

                JsonObject tool = new JsonObject();
                tool.addProperty("type", "function");

                JsonObject func = new JsonObject();
                func.addProperty("name", opId);
                func.addProperty("description", summary);
                func.add("parameters", collectParams(op, spec));
                tool.add("function", func);

                tools.add(tool);
            }
        }
        return tools;
    }

    // ═══════════════════════════════════════════════════════════════════════════
    //  HTTP helpers
    // ═══════════════════════════════════════════════════════════════════════════

    static HttpRequest.Builder apiRequest(String path) {
        return HttpRequest.newBuilder()
                .uri(URI.create(TOOL_SERVER + path))
                .header("Authorization", "Bearer " + TOOL_AUTH)
                .header("Content-Type", "application/json")
                .timeout(Duration.ofSeconds(30));
    }

    static JsonElement getJson(String path) throws Exception {
        var req = apiRequest(path).GET().build();
        var resp = HTTP.send(req, BodyHandlers.ofString());
        if (resp.statusCode() >= 400) {
            throw new RuntimeException("HTTP " + resp.statusCode() + ": " + resp.body());
        }
        return JsonParser.parseString(resp.body());
    }

    static JsonElement postJson(String path, String body) throws Exception {
        var req = apiRequest(path)
                .POST(body == null ? BodyPublishers.noBody() : BodyPublishers.ofString(body))
                .build();
        var resp = HTTP.send(req, BodyHandlers.ofString());
        if (resp.statusCode() >= 400) {
            throw new RuntimeException("HTTP " + resp.statusCode() + ": " + resp.body());
        }
        return JsonParser.parseString(resp.body());
    }

    // ═══════════════════════════════════════════════════════════════════════════
    //  Tool server communication
    // ═══════════════════════════════════════════════════════════════════════════

    static JsonObject cachedSpec;
    static JsonArray cachedTools;

    static JsonObject fetchOpenApiSpec() throws Exception {
        return getJson("/api/openapi/preset/" + TOOL_PRESET).getAsJsonObject();
    }

    static String executeToolCall(String toolName, JsonObject arguments) throws Exception {
        // Locate the operation in the cached spec
        String method = "POST";
        String urlPath = "";
        JsonObject paths = cachedSpec.getAsJsonObject("paths");
        if (paths != null) {
            outer:
            for (var entry : paths.entrySet()) {
                JsonObject pathItem = entry.getValue().getAsJsonObject();
                for (String m : List.of("get", "post", "put", "patch", "delete")) {
                    JsonElement opEl = pathItem.get(m);
                    if (opEl != null && opEl.isJsonObject()
                            && toolName.equals(jstr(opEl.getAsJsonObject().get("operationId")))) {
                        method = m.toUpperCase();
                        urlPath = entry.getKey();
                        break outer;
                    }
                }
            }
        }

        if (urlPath.isEmpty()) {
            return "{\"error\":\"tool not found in spec: " + toolName + "\"}";
        }

        // Substitute path parameters
        String fullPath = urlPath;
        if (arguments != null) {
            for (var e : arguments.entrySet()) {
                fullPath = fullPath.replace("{" + e.getKey() + "}", e.getValue().getAsString());
            }
        }

        if ("GET".equals(method)) {
            // Build query string from remaining args
            StringBuilder qs = new StringBuilder();
            if (arguments != null) {
                for (var e : arguments.entrySet()) {
                    if (!urlPath.contains("{" + e.getKey() + "}")) {
                        if (qs.length() > 0) qs.append("&");
                        qs.append(e.getKey()).append("=").append(e.getValue().getAsString());
                    }
                }
            }
            if (qs.length() > 0) fullPath += "?" + qs;
            return getJson(fullPath).toString();
        } else {
            String body = arguments == null ? "{}" : GSON.toJson(arguments);
            return postJson(fullPath, body).toString();
        }
    }

    // ═══════════════════════════════════════════════════════════════════════════
    //  Ollama chat with tool calling
    // ═══════════════════════════════════════════════════════════════════════════

    static final List<JsonObject> chatHistory = new ArrayList<>();

    static String chatWithTools(String userMessage) throws Exception {
        // Build messages array
        JsonArray messages = new JsonArray();

        JsonObject sys = new JsonObject();
        sys.addProperty("role", "system");
        sys.addProperty("content", SYSTEM_PROMPT);
        messages.add(sys);

        for (JsonObject h : chatHistory) messages.add(h.deepCopy());

        JsonObject user = new JsonObject();
        user.addProperty("role", "user");
        user.addProperty("content", userMessage);
        messages.add(user);

        // Build request body
        JsonObject body = new JsonObject();
        body.addProperty("model", OLLAMA_MODEL);
        body.add("messages", messages);
        body.add("tools", cachedTools);
        body.addProperty("stream", false);

        // First call
        var req = HttpRequest.newBuilder()
                .uri(URI.create(OLLAMA_URL + "/api/chat"))
                .header("Content-Type", "application/json")
                .POST(BodyPublishers.ofString(GSON.toJson(body)))
                .timeout(Duration.ofSeconds(120))
                .build();
        var resp = HTTP.send(req, BodyHandlers.ofString());
        if (resp.statusCode() >= 400) {
            return "Ollama error " + resp.statusCode() + ": " + resp.body();
        }

        JsonObject response = JsonParser.parseString(resp.body()).getAsJsonObject();
        JsonObject message = response.getAsJsonObject("message");
        JsonArray toolCalls = message.getAsJsonArray("tool_calls");

        // No tool calls → return text
        if (toolCalls == null || toolCalls.size() == 0) {
            String content = jstr(message.get("content"));
            chatHistory.add(user);
            chatHistory.add(message.deepCopy());
            return content;
        }

        // Execute each tool call
        messages.add(message.deepCopy());
        for (JsonElement tcEl : toolCalls) {
            JsonObject tc = tcEl.getAsJsonObject();
            JsonObject fn = tc.getAsJsonObject("function");
            String name = fn.get("name").getAsString();
            JsonObject args = fn.getAsJsonObject("arguments");

            System.out.println("  [tool] " + name + "(" + GSON.toJson(args) + ")");
            String result = executeToolCall(name, args);
            String preview = result.length() > 200 ? result.substring(0, 200) + "…" : result;
            System.out.println("  [tool] → " + preview);

            JsonObject toolMsg = new JsonObject();
            toolMsg.addProperty("role", "tool");
            toolMsg.addProperty("content", result);
            toolMsg.addProperty("tool_call_id", jstr(tc.get("id")));
            messages.add(toolMsg);
        }

        // Second call — feed tool results back
        body.add("messages", messages);
        var req2 = HttpRequest.newBuilder()
                .uri(URI.create(OLLAMA_URL + "/api/chat"))
                .header("Content-Type", "application/json")
                .POST(BodyPublishers.ofString(GSON.toJson(body)))
                .timeout(Duration.ofSeconds(120))
                .build();
        var resp2 = HTTP.send(req2, BodyHandlers.ofString());
        if (resp2.statusCode() >= 400) {
            return "Ollama error " + resp2.statusCode() + ": " + resp2.body();
        }

        JsonObject finalResp = JsonParser.parseString(resp2.body()).getAsJsonObject();
        String finalContent = jstr(jget(finalResp, "message", "content"));

        // Update history
        chatHistory.add(user);
        JsonObject assistantMsg = new JsonObject();
        assistantMsg.addProperty("role", "assistant");
        assistantMsg.addProperty("content", finalContent);
        chatHistory.add(assistantMsg);

        // Trim history to last 20 messages
        while (chatHistory.size() > 20) chatHistory.remove(0);

        return finalContent;
    }

    // ═══════════════════════════════════════════════════════════════════════════
    //  Main — console chat loop
    // ═══════════════════════════════════════════════════════════════════════════

    public static void main(String[] args) throws Exception {
        // 1. Fetch and parse the OpenAPI spec
        System.out.println("Fetching OpenAPI spec from " + TOOL_SERVER + " (preset=" + TOOL_PRESET + ")…");
        cachedSpec = fetchOpenApiSpec();
        cachedTools = specToOllamaTools(cachedSpec);
        System.out.println("Loaded " + cachedTools.size() + " tools.\n");

        // 2. Console chat loop
        try (var scanner = new Scanner(System.in)) {
            while (true) {
                System.out.print("> ");
                String line = scanner.nextLine();
                if (line.isBlank()) continue;
                if ("/exit".equals(line) || "/quit".equals(line)) break;
                if ("/tools".equals(line)) {
                    for (JsonElement t : cachedTools) {
                        JsonObject fn = t.getAsJsonObject().getAsJsonObject("function");
                        System.out.println("  " + fn.get("name").getAsString() + " — " + fn.get("description").getAsString());
                    }
                    continue;
                }
                if ("/clear".equals(line)) {
                    chatHistory.clear();
                    System.out.println("  history cleared.");
                    continue;
                }

                try {
                    String reply = chatWithTools(line);
                    System.out.println(reply);
                    System.out.println();
                } catch (Exception e) {
                    System.err.println("Error: " + e.getMessage());
                    e.printStackTrace();
                }
            }
        }
    }
}
