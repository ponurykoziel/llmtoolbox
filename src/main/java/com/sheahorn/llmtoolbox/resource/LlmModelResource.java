package com.sheahorn.llmtoolbox.resource;

import com.sheahorn.llmtoolbox.domain.Model;
import com.sheahorn.llmtoolbox.domain.Provider;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.openapi.annotations.Operation;

import java.util.List;
import java.util.Map;

@Path("/api/llm/models")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class LlmModelResource {

    @Operation(operationId = "llm_models_list", summary = "List all LLM models")
    @GET
    public List<Model> list() {
        return Model.listAll();
    }

    @Operation(operationId = "llm_models_create", summary = "Create a new LLM model")
    @POST
    @Transactional
    public Response create(CreateRequest req) {
        if (req == null || req.niceName == null || req.niceName.isBlank()) {
            return Response.status(400).entity(Map.of("error", "niceName is required")).build();
        }
        if (req.providerName == null || req.providerName.isBlank()) {
            return Response.status(400).entity(Map.of("error", "providerName is required")).build();
        }
        if (req.providerId == null || req.providerId.isBlank()) {
            return Response.status(400).entity(Map.of("error", "providerId is required")).build();
        }

        Provider provider = Provider.findById(req.providerId);
        if (provider == null) {
            return Response.status(400).entity(Map.of("error", "provider not found")).build();
        }

        Model existing = Model.find("niceName", req.niceName).firstResult();
        if (existing != null) {
            return Response.status(409).entity(Map.of("error", "model niceName already exists")).build();
        }

        Model m = Model.create(req.providerId, req.niceName, req.providerName);
        m.persist();
        return Response.ok(m).build();
    }

    @Operation(operationId = "llm_models_get", summary = "Get an LLM model by ID")
    @GET
    @Path("/{id}")
    public Response get(@PathParam("id") String id) {
        Model m = Model.findById(id);
        if (m == null) {
            return Response.status(404).entity(Map.of("error", "model not found")).build();
        }
        return Response.ok(m).build();
    }

    @Operation(operationId = "llm_models_patch", summary = "Patch an LLM model")
    @PATCH
    @Path("/{id}")
    @Transactional
    public Response patch(@PathParam("id") String id, PatchRequest req) {
        Model m = Model.findById(id);
        if (m == null) {
            return Response.status(404).entity(Map.of("error", "model not found")).build();
        }

        if (req.niceName != null) {
            if (req.niceName.isBlank()) {
                return Response.status(400).entity(Map.of("error", "niceName must not be blank")).build();
            }
            Model conflict = Model.find("niceName", req.niceName).firstResult();
            if (conflict != null && !conflict.id.equals(id)) {
                return Response.status(409).entity(Map.of("error", "model niceName already exists")).build();
            }
            m.niceName = req.niceName;
        }
        if (req.providerName != null) {
            if (req.providerName.isBlank()) {
                return Response.status(400).entity(Map.of("error", "providerName must not be blank")).build();
            }
            m.providerName = req.providerName;
        }
        if (req.providerId != null) {
            if (req.providerId.isBlank()) {
                return Response.status(400).entity(Map.of("error", "providerId must not be blank")).build();
            }
            Provider provider = Provider.findById(req.providerId);
            if (provider == null) {
                return Response.status(400).entity(Map.of("error", "provider not found")).build();
            }
            m.providerId = req.providerId;
        }

        m.persist();
        return Response.ok(m).build();
    }

    @Operation(operationId = "llm_models_delete", summary = "Delete an LLM model")
    @DELETE
    @Path("/{id}")
    @Transactional
    public Response delete(@PathParam("id") String id) {
        Model m = Model.findById(id);
        if (m == null) {
            return Response.status(404).entity(Map.of("error", "model not found")).build();
        }
        m.delete();
        return Response.noContent().build();
    }

    public static class CreateRequest {
        public String niceName;
        public String providerName;
        public String providerId;
    }

    public static class PatchRequest {
        public String niceName;
        public String providerName;
        public String providerId;
    }
}
