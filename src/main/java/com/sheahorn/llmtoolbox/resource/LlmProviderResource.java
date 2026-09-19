package com.sheahorn.llmtoolbox.resource;

import com.sheahorn.llmtoolbox.domain.ApiMode;
import com.sheahorn.llmtoolbox.domain.ApiType;
import com.sheahorn.llmtoolbox.domain.Provider;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.openapi.annotations.Operation;
import com.sheahorn.llmtoolbox.llm.ToolBean;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;

@Path("/api/llm/providers")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class LlmProviderResource implements ToolBean {

    @Operation(operationId = "llm_providers_list", summary = "List all LLM providers")
    @GET
    public List<Provider> list() {
        return Provider.listAll();
    }

    @Operation(operationId = "llm_providers_create", summary = "Create a new LLM provider")
    @POST
    @Transactional
    public Response create(CreateRequest req) {
        if (req == null || req.name == null || req.name.isBlank()) {
            return Response.status(400).entity(Map.of("error", "name is required")).build();
        }
        if (req.baseUrl == null || req.baseUrl.isBlank()) {
            return Response.status(400).entity(Map.of("error", "baseUrl is required")).build();
        }
        if (req.apiType == null) {
            return Response.status(400).entity(Map.of("error", "apiType is required (ollama, openai, or openwebui)")).build();
        }

        Provider existing = Provider.find("name", req.name).firstResult();
        if (existing != null) {
            return Response.status(409).entity(Map.of("error", "provider name already exists")).build();
        }

        Provider p = Provider.create(req.name, req.baseUrl, req.apiKey, req.apiType, req.apiMode);
        p.persist();
        return Response.ok(p).build();
    }

    @Operation(operationId = "llm_providers_get", summary = "Get an LLM provider by ID")
    @POST
    @Path("/get")
    public Response get(ProviderIdRequest request) {
        if (request == null || request.id == null || request.id.isBlank()) {
            return Response.status(400).entity(Map.of("error", "id is required")).build();
        }

        Provider p = Provider.findById(request.id);
        if (p == null) {
            return Response.status(404).entity(Map.of("error", "provider not found")).build();
        }
        return Response.ok(p).build();
    }

    @Operation(operationId = "llm_providers_patch", summary = "Patch an LLM provider")
    @POST
    @Path("/patch")
    @Transactional
    public Response patch(PatchRequest req) {
        if (req == null || req.id == null || req.id.isBlank()) {
            return Response.status(400).entity(Map.of("error", "id is required")).build();
        }

        Provider p = Provider.findById(req.id);
        if (p == null) {
            return Response.status(404).entity(Map.of("error", "provider not found")).build();
        }

        if (req.name != null) {
            if (req.name.isBlank()) {
                return Response.status(400).entity(Map.of("error", "name must not be blank")).build();
            }
            Provider conflict = Provider.find("name", req.name).firstResult();
            if (conflict != null && !conflict.id.equals(req.id)) {
                return Response.status(409).entity(Map.of("error", "provider name already exists")).build();
            }
            p.name = req.name;
        }
        if (req.baseUrl != null) {
            if (req.baseUrl.isBlank()) {
                return Response.status(400).entity(Map.of("error", "baseUrl must not be blank")).build();
            }
            p.baseUrl = req.baseUrl;
        }
        if (req.apiKey != null) {
            p.apiKey = req.apiKey.isBlank() ? null : req.apiKey;
        }
        if (req.apiType != null) {
            p.apiType = req.apiType;
        }
        if (req.apiMode != null) {
            p.apiMode = req.apiMode;
        }

        p.persist();
        return Response.ok(p).build();
    }

    @Operation(operationId = "llm_providers_test", summary = "Test connectivity to an LLM provider")
    @POST
    @Path("/test")
    public Response test(ProviderIdRequest request) {
        if (request == null || request.id == null || request.id.isBlank()) {
            return Response.status(400).entity(Map.of("error", "id is required")).build();
        }

        Provider p = Provider.findById(request.id);
        if (p == null) {
            return Response.status(404).entity(Map.of("error", "provider not found")).build();
        }

        Instant start = Instant.now();
        try {
            var client = HttpClient.newBuilder()
                    .connectTimeout(Duration.ofSeconds(5))
                    .build();

            HttpRequest.Builder req = HttpRequest.newBuilder()
                    .uri(URI.create(p.baseUrl))
                    .method("HEAD", HttpRequest.BodyPublishers.noBody())
                    .timeout(Duration.ofSeconds(5));

            if (p.apiKey != null && !p.apiKey.isBlank()) {
                req.header("Authorization", "Bearer " + p.apiKey);
            }

            var resp = client.send(req.build(), HttpResponse.BodyHandlers.discarding());
            long ms = Duration.between(start, Instant.now()).toMillis();

            boolean authorized = resp.statusCode() != 401 && resp.statusCode() != 403;
            return Response.ok(Map.of(
                    "reachable", true,
                    "statusCode", resp.statusCode(),
                    "authorized", authorized,
                    "durationMs", ms
            )).build();
        } catch (Exception e) {
            long ms = Duration.between(start, Instant.now()).toMillis();
            return Response.ok(Map.of(
                    "reachable", false,
                    "error", e.getClass().getSimpleName() + ": " + e.getMessage(),
                    "durationMs", ms
            )).build();
        }
    }

    @Operation(operationId = "llm_providers_delete", summary = "Delete an LLM provider")
    @POST
    @Path("/delete")
    @Transactional
    public Response delete(ProviderIdRequest request) {
        if (request == null || request.id == null || request.id.isBlank()) {
            return Response.status(400).entity(Map.of("error", "id is required")).build();
        }

        Provider p = Provider.findById(request.id);
        if (p == null) {
            return Response.status(404).entity(Map.of("error", "provider not found")).build();
        }
        p.delete();
        return Response.noContent().build();
    }

    public static class CreateRequest {
        public String name;
        public String baseUrl;
        public String apiKey;
        public ApiType apiType;
        public ApiMode apiMode;
    }

    public static class PatchRequest {
        public String id;
        public String name;
        public String baseUrl;
        public String apiKey;
        public ApiType apiType;
        public ApiMode apiMode;
    }
}
