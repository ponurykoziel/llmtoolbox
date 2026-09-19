package com.sheahorn.llmtoolbox.resource;

import com.sheahorn.llmtoolbox.domain.Agent;
import com.sheahorn.llmtoolbox.domain.Model;
import com.sheahorn.llmtoolbox.domain.Personality;
import com.sheahorn.llmtoolbox.domain.Provider;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.openapi.annotations.Operation;

import java.util.List;
import java.util.Map;

@Path("/api/llm/agents")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class LlmAgentResource {

    @Operation(operationId = "llm_agents_list", summary = "List all LLM agents")
    @GET
    public List<Agent> list() {
        return Agent.listAll();
    }

    @Operation(operationId = "llm_agents_create", summary = "Create a new LLM agent")
    @POST
    @Transactional
    public Response create(CreateRequest req) {
        if (req == null || req.niceName == null || req.niceName.isBlank()) {
            return Response.status(400).entity(Map.of("error", "niceName is required")).build();
        }
        if (req.providerId == null || req.providerId.isBlank()) {
            return Response.status(400).entity(Map.of("error", "providerId is required")).build();
        }
        if (req.modelId == null || req.modelId.isBlank()) {
            return Response.status(400).entity(Map.of("error", "modelId is required")).build();
        }
        if (req.personalityId == null || req.personalityId.isBlank()) {
            return Response.status(400).entity(Map.of("error", "personalityId is required")).build();
        }

        if (Provider.findById(req.providerId) == null) {
            return Response.status(400).entity(Map.of("error", "provider not found")).build();
        }
        if (Model.findById(req.modelId) == null) {
            return Response.status(400).entity(Map.of("error", "model not found")).build();
        }
        if (Personality.findById(req.personalityId) == null) {
            return Response.status(400).entity(Map.of("error", "personality not found")).build();
        }

        Agent existing = Agent.find("niceName", req.niceName).firstResult();
        if (existing != null) {
            return Response.status(409).entity(Map.of("error", "agent niceName already exists")).build();
        }

        Agent a = Agent.create(req.niceName, req.providerId, req.modelId, req.personalityId, req.toolPreset);
        a.persist();
        return Response.ok(a).build();
    }

    @Operation(operationId = "llm_agents_get", summary = "Get an LLM agent by ID")
    @GET
    @Path("/{id}")
    public Response get(@PathParam("id") String id) {
        Agent a = Agent.findById(id);
        if (a == null) {
            return Response.status(404).entity(Map.of("error", "agent not found")).build();
        }
        return Response.ok(a).build();
    }

    @Operation(operationId = "llm_agents_patch", summary = "Patch an LLM agent")
    @PATCH
    @Path("/{id}")
    @Transactional
    public Response patch(@PathParam("id") String id, PatchRequest req) {
        Agent a = Agent.findById(id);
        if (a == null) {
            return Response.status(404).entity(Map.of("error", "agent not found")).build();
        }

        if (req.niceName != null) {
            if (req.niceName.isBlank()) {
                return Response.status(400).entity(Map.of("error", "niceName must not be blank")).build();
            }
            Agent conflict = Agent.find("niceName", req.niceName).firstResult();
            if (conflict != null && !conflict.id.equals(id)) {
                return Response.status(409).entity(Map.of("error", "agent niceName already exists")).build();
            }
            a.niceName = req.niceName;
        }
        if (req.providerId != null) {
            if (req.providerId.isBlank()) {
                return Response.status(400).entity(Map.of("error", "providerId must not be blank")).build();
            }
            if (Provider.findById(req.providerId) == null) {
                return Response.status(400).entity(Map.of("error", "provider not found")).build();
            }
            a.providerId = req.providerId;
        }
        if (req.modelId != null) {
            if (req.modelId.isBlank()) {
                return Response.status(400).entity(Map.of("error", "modelId must not be blank")).build();
            }
            if (Model.findById(req.modelId) == null) {
                return Response.status(400).entity(Map.of("error", "model not found")).build();
            }
            a.modelId = req.modelId;
        }
        if (req.personalityId != null) {
            if (req.personalityId.isBlank()) {
                return Response.status(400).entity(Map.of("error", "personalityId must not be blank")).build();
            }
            if (Personality.findById(req.personalityId) == null) {
                return Response.status(400).entity(Map.of("error", "personality not found")).build();
            }
            a.personalityId = req.personalityId;
        }
        if (req.toolPreset != null) {
            a.toolPreset = req.toolPreset.isBlank() ? null : req.toolPreset;
        }

        a.persist();
        return Response.ok(a).build();
    }

    @Operation(operationId = "llm_agents_delete", summary = "Delete an LLM agent")
    @DELETE
    @Path("/{id}")
    @Transactional
    public Response delete(@PathParam("id") String id) {
        Agent a = Agent.findById(id);
        if (a == null) {
            return Response.status(404).entity(Map.of("error", "agent not found")).build();
        }
        a.delete();
        return Response.noContent().build();
    }

    public static class CreateRequest {
        public String niceName;
        public String providerId;
        public String modelId;
        public String personalityId;
        public String toolPreset;
    }

    public static class PatchRequest {
        public String niceName;
        public String providerId;
        public String modelId;
        public String personalityId;
        public String toolPreset;
    }
}
