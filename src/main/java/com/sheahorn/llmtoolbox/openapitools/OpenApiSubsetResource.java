package com.sheahorn.llmtoolbox.openapitools;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.*;

import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import com.sheahorn.llmtoolbox.auth.NoBearerAuth;
import com.sheahorn.llmtoolbox.basics.presets.PresetDefaults;

@Path("/api/openapi")
@Produces(MediaType.APPLICATION_JSON)
@NoBearerAuth
public class OpenApiSubsetResource {

    private static final ObjectMapper MAPPER = new ObjectMapper();
    private volatile JsonNode cachedFull;
    private static final String[] OPENAPI_FILES = {
        "/META-INF/openapi.yaml",
        "/META-INF/openapi.yml",
        "/META-INF/openapi.json"
    };

    // ── select (wildcard-aware, comma-separated) ──────────────

    @GET
    @Path("/select/{selectors}")
    public Response bySelectors(@PathParam("selectors") String selectors) {
        if (selectors == null || selectors.isBlank()) {
            return error("selectors path param is required (comma-separated). Use abc_* for prefix, abc_def for exact.");
        }
        JsonNode full = resolveOpenApi();
        if (full == null) {
            return error("OpenAPI document not available");
        }
        Set<String> opIds = new LinkedHashSet<>();
        for (String raw : selectors.split(",")) {
            String s = raw.trim();
            if (s.isEmpty()) continue;
            if (s.endsWith("*")) {
                String prefix = s.substring(0, s.length() - 1);
                if (!prefix.isEmpty()) {
                    collectByPrefix(full, prefix, opIds);
                }
            } else {
                opIds.add(s);
            }
        }
        return buildSubset(full, opIds);
    }

    @GET
    @Path("/preset/{name}")
    public Response byPreset(@PathParam("name") String name) {
        if (name == null || name.isBlank()) {
            return error("preset name is required");
        }

        List<String> prefixes = PresetDefaults.resolve(name);
        if (prefixes == null) {
            List<String> available = new ArrayList<>(PresetDefaults.allNames());
            available.sort(null);
            return error("Unknown preset: " + name + ". Available: " + available);
        }

        JsonNode full = resolveOpenApi();
        if (full == null) {
            return error("OpenAPI document not available");
        }

        Set<String> opIds = new LinkedHashSet<>();
        for (String p : prefixes) {
            if (p.equals("*")) {
                // "all" preset — collect every operationId
                collectAll(full, opIds);
            } else if (p.endsWith("*")) {
                p = p.substring(0, p.length() - 1);
                collectByPrefix(full, p, opIds);
            } else {
                opIds.add(p);
            }
        }
        return buildSubset(full, opIds);
    }

    // ── builder ────────────────────────────────────────────────

    private Response buildSubset(JsonNode full, Set<String> selectedOpIds) {
        ObjectNode subset = MAPPER.createObjectNode();
        subset.put("openapi", "3.1.0");
        subset.set("info", full.get("info").deepCopy());

        ObjectNode subsetComponents = MAPPER.createObjectNode();
        ObjectNode subsetSchemas = MAPPER.createObjectNode();
        Set<String> neededSchemas = new LinkedHashSet<>();
        ObjectNode subsetPaths = MAPPER.createObjectNode();

        JsonNode paths = full.get("paths");
        if (paths != null) {
            var pathFields = paths.fields();
            while (pathFields.hasNext()) {
                var entry = pathFields.next();
                String pathUrl = entry.getKey();
                JsonNode pathItem = entry.getValue();
                JsonNode slimmed = slimPathItem(pathItem, selectedOpIds);
                if (slimmed != null && !slimmed.isEmpty()) {
                    subsetPaths.set(pathUrl, slimmed);
                    collectRefs(slimmed, neededSchemas);
                }
            }
        }

        // Transitive closure
        JsonNode fullSchemas = full.at("/components/schemas");
        boolean changed = true;
        while (changed) {
            changed = false;
            for (String schemaName : new ArrayList<>(neededSchemas)) {
                if (subsetSchemas.has(schemaName)) continue;
                if (fullSchemas != null && fullSchemas.has(schemaName)) {
                    JsonNode schema = fullSchemas.get(schemaName);
                    subsetSchemas.set(schemaName, schema.deepCopy());
                    int before = neededSchemas.size();
                    collectRefsFromSchema(schema, neededSchemas);
                    if (neededSchemas.size() > before) changed = true;
                }
            }
        }

        if (!subsetSchemas.isEmpty()) {
            subsetComponents.set("schemas", subsetSchemas);
            subset.set("components", subsetComponents);
        }
        subset.set("paths", subsetPaths);

        try {
            String json = MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(subset);
            return Response.ok(json).build();
        } catch (Exception e) {
            return error("Serialization failed: " + e.getMessage());
        }
    }

    private Response error(String msg) {
        return Response.status(400).entity(Map.of("error", msg)).build();
    }

    private JsonNode resolveOpenApi() {
        if (cachedFull != null) return cachedFull;
        synchronized (this) {
            if (cachedFull != null) return cachedFull;
            for (String file : OPENAPI_FILES) {
                try (InputStream is = getClass().getResourceAsStream(file)) {
                    if (is == null) continue;
                    String raw = new String(is.readAllBytes(), StandardCharsets.UTF_8);
                    cachedFull = MAPPER.readTree(raw);
                    return cachedFull;
                } catch (Exception e) {
                    // try next
                }
            }
            return null;
        }
    }

    // ── helpers (JsonNode-based) ──────────────────────────────

    private void collectAll(JsonNode full, Set<String> out) {
        JsonNode paths = full.get("paths");
        if (paths == null) return;
        var it = paths.fields();
        while (it.hasNext()) {
            JsonNode pi = it.next().getValue();
            for (JsonNode op : allOperations(pi)) {
                JsonNode opId = op.get("operationId");
                if (opId != null) {
                    out.add(opId.asText());
                }
            }
        }
    }

    private void collectByPrefix(JsonNode full, String prefix, Set<String> out) {
        JsonNode paths = full.get("paths");
        if (paths == null) return;
        var it = paths.fields();
        while (it.hasNext()) {
            JsonNode pi = it.next().getValue();
            for (JsonNode op : allOperations(pi)) {
                JsonNode opId = op.get("operationId");
                if (opId != null && opId.asText().startsWith(prefix)) {
                    out.add(opId.asText());
                }
            }
        }
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

    /** Keep only operations whose operationId is in the selected set. */
    private JsonNode slimPathItem(JsonNode pi, Set<String> selectedOpIds) {
        ObjectNode slim = MAPPER.createObjectNode();
        boolean any = false;
        String[] methods = {"get", "post", "put", "delete", "patch", "options", "head", "trace"};
        for (String m : methods) {
            JsonNode op = pi.get(m);
            if (op != null && !op.isNull()) {
                JsonNode opId = op.get("operationId");
                if (opId != null && selectedOpIds.contains(opId.asText())) {
                    slim.set(m, op.deepCopy());
                    any = true;
                }
            }
        }
        return any ? slim : null;
    }

    private void collectRefs(JsonNode pi, Set<String> out) {
        for (JsonNode op : allOperations(pi)) {
            JsonNode reqBody = op.at("/requestBody/content");
            if (!reqBody.isMissingNode()) collectRefsFromContent(reqBody, out);
            JsonNode responses = op.at("/responses");
            if (!responses.isMissingNode()) {
                var rit = responses.fields();
                while (rit.hasNext()) {
                    JsonNode resp = rit.next().getValue();
                    JsonNode content = resp.at("/content");
                    if (!content.isMissingNode()) collectRefsFromContent(content, out);
                }
            }
            JsonNode params = op.get("parameters");
            if (params != null && params.isArray()) {
                for (JsonNode param : params) {
                    JsonNode schema = param.at("/schema");
                    if (!schema.isMissingNode()) collectRefsFromSchema(schema, out);
                    JsonNode pcontent = param.at("/content");
                    if (!pcontent.isMissingNode()) collectRefsFromContent(pcontent, out);
                }
            }
        }
    }

    private void collectRefsFromContent(JsonNode content, Set<String> out) {
        if (content == null || content.isMissingNode()) return;
        var it = content.fields();
        while (it.hasNext()) {
            JsonNode mt = it.next().getValue();
            JsonNode schema = mt.get("schema");
            if (schema != null && !schema.isMissingNode()) {
                collectRefsFromSchema(schema, out);
            }
        }
    }

    private void collectRefsFromSchema(JsonNode schema, Set<String> out) {
        if (schema == null || schema.isMissingNode()) return;
        JsonNode ref = schema.get("$ref");
        if (ref != null) {
            String refStr = ref.asText();
            if (refStr.startsWith("#/components/schemas/")) {
                out.add(refStr.substring("#/components/schemas/".length()));
            }
        }
        JsonNode props = schema.get("properties");
        if (props != null) {
            var it = props.fields();
            while (it.hasNext()) collectRefsFromSchema(it.next().getValue(), out);
        }
        JsonNode items = schema.get("items");
        if (items != null) collectRefsFromSchema(items, out);
        JsonNode addProps = schema.get("additionalProperties");
        if (addProps != null && addProps.isObject()) collectRefsFromSchema(addProps, out);
        for (String key : List.of("allOf", "oneOf", "anyOf")) {
            JsonNode arr = schema.get(key);
            if (arr != null && arr.isArray()) {
                for (JsonNode s : arr) collectRefsFromSchema(s, out);
            }
        }
    }
}
