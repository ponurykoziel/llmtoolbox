package com.sheahorn.llmtoolbox.openapitools;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.quarkus.runtime.StartupEvent;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import org.jboss.logging.Logger;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.*;

@ApplicationScoped
public class BuiltinFunctionCache {

    private static final Logger LOG = Logger.getLogger(BuiltinFunctionCache.class);
    private static final ObjectMapper MAPPER = new ObjectMapper();

    private final Map<String, String> functions = new LinkedHashMap<>();
    private final Map<String, JsonNode> schemas = new LinkedHashMap<>();
    private volatile boolean loaded = false;

    void init(@Observes StartupEvent event) {
        load();
    }

    private synchronized void load() {
        if (loaded) return;
        JsonNode spec = loadOpenApi();
        if (spec == null) {
            LOG.warn("No OpenAPI spec found; built-in function list will be empty.");
            loaded = true;
            return;
        }

        JsonNode paths = spec.get("paths");
        if (paths == null) { loaded = true; return; }

        var pathIt = paths.fields();
        while (pathIt.hasNext()) {
            var entry = pathIt.next();
            JsonNode pathItem = entry.getValue();
            for (JsonNode op : allOperations(pathItem)) {
                JsonNode opId = op.get("operationId");
                if (opId != null) {
                    String id = opId.asText();
                    String desc = null;
                    JsonNode summary = op.get("summary");
                    if (summary != null && !summary.isNull()) desc = summary.asText();
                    if (desc == null || desc.isBlank()) {
                        JsonNode description = op.get("description");
                        if (description != null && !description.isNull()) desc = description.asText();
                    }
                    functions.put(id, desc != null ? desc : "");

                    JsonNode schema = extractRequestSchema(op, spec);
                    if (schema != null) {
                        schemas.put(id, schema);
                    }
                }
            }
        }

        loaded = true;
        LOG.infof("Cached %d built-in functions from OpenAPI spec (%d with parameter schemas).",
                functions.size(), schemas.size());
    }

    public Map<String, String> all() {
        if (!loaded) load();
        return Collections.unmodifiableMap(functions);
    }

    /**
     * Returns the fully-resolved JSON Schema for the request body of the given
     * operationId, or null if the operation has no request body schema.
     * All {@code $ref} references are inlined so the schema is self-contained
     * and directly usable as an LLM function-calling {@code parameters} object.
     */
    public JsonNode schema(String operationId) {
        if (!loaded) load();
        return schemas.get(operationId);
    }

    private JsonNode extractRequestSchema(JsonNode op, JsonNode spec) {
        JsonNode requestBody = op.get("requestBody");
        if (requestBody == null || !requestBody.isObject()) return null;
        JsonNode content = requestBody.get("content");
        if (content == null || !content.isObject()) return null;
        JsonNode appJson = content.get("application/json");
        if (appJson == null || !appJson.isObject()) return null;
        JsonNode schema = appJson.get("schema");
        if (schema == null || schema.isMissingNode()) return null;
        JsonNode resolved = resolveRefs(schema, spec);
        return markAllRequired(resolved);
    }

    /**
     * All request-body parameters are required by convention (ADR-0013 rule 4:
     * every tool parameter is a body field). Inject a {@code required} array
     * listing every top-level property so LLMs know they must all be supplied.
     */
    private JsonNode markAllRequired(JsonNode schema) {
        if (schema == null || !schema.isObject()) return schema;
        JsonNode props = schema.get("properties");
        if (props == null || !props.isObject() || props.isEmpty()) return schema;

        ObjectNode result = ((ObjectNode) schema).deepCopy();
        ArrayNode required = MAPPER.createArrayNode();
        var it = props.fields();
        while (it.hasNext()) {
            required.add(it.next().getKey());
        }
        result.set("required", required);
        return result;
    }

    private JsonNode resolveRefs(JsonNode node, JsonNode spec) {
        return resolveRefs(node, spec, new HashSet<>(), 0);
    }

    private JsonNode resolveRefs(JsonNode node, JsonNode spec, Set<String> seen, int depth) {
        if (node == null || node.isMissingNode()) return null;
        if (depth > 32) return node.deepCopy();

        if (node.isObject()) {
            JsonNode ref = node.get("$ref");
            if (ref != null && ref.isTextual()) {
                String refStr = ref.asText();
                if (refStr.startsWith("#/components/schemas/")) {
                    String name = refStr.substring("#/components/schemas/".length());
                    if (seen.contains(name)) {
                        return node.deepCopy();
                    }
                    seen.add(name);
                    JsonNode target = spec.at("/components/schemas/" + name);
                    if (target != null && !target.isMissingNode()) {
                        return resolveRefs(target, spec, seen, depth + 1);
                    }
                }
                return node.deepCopy();
            }

            ObjectNode result = MAPPER.createObjectNode();
            var it = node.fields();
            while (it.hasNext()) {
                var e = it.next();
                JsonNode resolved = resolveRefs(e.getValue(), spec, seen, depth + 1);
                if (resolved != null) {
                    result.set(e.getKey(), resolved);
                }
            }
            return result;
        }

        if (node.isArray()) {
            ArrayNode result = MAPPER.createArrayNode();
            for (JsonNode child : node) {
                JsonNode resolved = resolveRefs(child, spec, seen, depth + 1);
                if (resolved != null) {
                    result.add(resolved);
                }
            }
            return result;
        }

        return node.deepCopy();
    }

    private JsonNode loadOpenApi() {
        String[] files = {"/META-INF/openapi.yaml", "/META-INF/openapi.yml", "/META-INF/openapi.json"};
        for (String file : files) {
            try (InputStream is = getClass().getResourceAsStream(file)) {
                if (is == null) continue;
                String raw = new String(is.readAllBytes(), StandardCharsets.UTF_8);
                return MAPPER.readTree(raw);
            } catch (Exception e) {
                // try next
            }
        }
        return null;
    }

    private List<JsonNode> allOperations(JsonNode pi) {
        List<JsonNode> ops = new ArrayList<>();
        String[] methods = {"get", "post", "put", "delete", "patch", "options", "head", "trace"};
        for (String m : methods) {
            JsonNode op = pi.get(m);
            if (op != null && !op.isNull()) ops.add(op);
        }
        return ops;
    }
}
