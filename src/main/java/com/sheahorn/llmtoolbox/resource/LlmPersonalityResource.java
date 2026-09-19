package com.sheahorn.llmtoolbox.resource;

import com.sheahorn.llmtoolbox.domain.Personality;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.openapi.annotations.Operation;

import java.util.List;
import java.util.Map;

@Path("/api/llm/personalities")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class LlmPersonalityResource {

    @Operation(operationId = "llm_personalities_list", summary = "List all LLM personalities")
    @GET
    public List<Personality> list() {
        return Personality.listAll();
    }

    @Operation(operationId = "llm_personalities_create", summary = "Create a new LLM personality")
    @POST
    @Transactional
    public Response create(CreateRequest req) {
        if (req == null || req.niceName == null || req.niceName.isBlank()) {
            return Response.status(400).entity(Map.of("error", "niceName is required")).build();
        }

        Personality existing = Personality.find("niceName", req.niceName).firstResult();
        if (existing != null) {
            return Response.status(409).entity(Map.of("error", "personality niceName already exists")).build();
        }

        Personality p = Personality.create(req.niceName);
        applyFields(p, req);
        p.persist();
        return Response.ok(p).build();
    }

    @Operation(operationId = "llm_personalities_get", summary = "Get an LLM personality by ID")
    @GET
    @Path("/{id}")
    public Response get(@PathParam("id") String id) {
        Personality p = Personality.findById(id);
        if (p == null) {
            return Response.status(404).entity(Map.of("error", "personality not found")).build();
        }
        return Response.ok(p).build();
    }

    @Operation(operationId = "llm_personalities_patch", summary = "Patch an LLM personality")
    @PATCH
    @Path("/{id}")
    @Transactional
    public Response patch(@PathParam("id") String id, PatchRequest req) {
        Personality p = Personality.findById(id);
        if (p == null) {
            return Response.status(404).entity(Map.of("error", "personality not found")).build();
        }

        if (req.niceName != null) {
            if (req.niceName.isBlank()) {
                return Response.status(400).entity(Map.of("error", "niceName must not be blank")).build();
            }
            Personality conflict = Personality.find("niceName", req.niceName).firstResult();
            if (conflict != null && !conflict.id.equals(id)) {
                return Response.status(409).entity(Map.of("error", "personality niceName already exists")).build();
            }
            p.niceName = req.niceName;
        }

        applyFields(p, req);
        p.persist();
        return Response.ok(p).build();
    }

    @Operation(operationId = "llm_personalities_delete", summary = "Delete an LLM personality")
    @DELETE
    @Path("/{id}")
    @Transactional
    public Response delete(@PathParam("id") String id) {
        Personality p = Personality.findById(id);
        if (p == null) {
            return Response.status(404).entity(Map.of("error", "personality not found")).build();
        }
        p.delete();
        return Response.noContent().build();
    }

    private void applyFields(Personality p, PersonalityFields req) {
        if (req.systemPrompt != null) {
            p.systemPrompt = req.systemPrompt.isBlank() ? null : req.systemPrompt;
        }
        if (req.temperature != null) p.temperature = Personality.sanitizeParam(req.temperature);
        if (req.minP != null) p.minP = Personality.sanitizeParam(req.minP);
        if (req.topP != null) p.topP = Personality.sanitizeParam(req.topP);
        if (req.topK != null) p.topK = Personality.sanitizeParam(req.topK);
        if (req.frequencyPenalty != null) p.frequencyPenalty = Personality.sanitizeParam(req.frequencyPenalty);
        if (req.presencePenalty != null) p.presencePenalty = Personality.sanitizeParam(req.presencePenalty);
        if (req.reasoningEffort != null) {
            p.reasoningEffort = req.reasoningEffort.isBlank() ? null : req.reasoningEffort;
        }
    }

    public static class CreateRequest extends PersonalityFields {
        public String niceName;
    }

    public static class PatchRequest extends PersonalityFields {
        public String niceName;
    }

    public static class PersonalityFields {
        public String systemPrompt;
        public Double temperature;
        public Double minP;
        public Double topP;
        public Double topK;
        public Double frequencyPenalty;
        public Double presencePenalty;
        public String reasoningEffort;
    }
}
