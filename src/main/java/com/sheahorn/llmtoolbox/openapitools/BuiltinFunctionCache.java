package com.sheahorn.llmtoolbox.openapitools;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
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
                }
            }
        }

        loaded = true;
        LOG.infof("Cached %d built-in functions from OpenAPI spec.", functions.size());
    }

    public Map<String, String> all() {
        if (!loaded) load();
        return Collections.unmodifiableMap(functions);
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
