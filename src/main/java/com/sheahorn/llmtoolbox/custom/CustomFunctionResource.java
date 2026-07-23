package com.sheahorn.llmtoolbox.custom;

import com.sheahorn.llmtoolbox.execution.ExecutionResponse;
import com.sheahorn.llmtoolbox.execution.Executor;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.openapi.annotations.Operation;

import java.util.List;
import java.util.Map;

@Path("/api/tools/functions")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class CustomFunctionResource {

    @Inject
    Executor executor;

    @GET
    public List<CustomFunction> list() {
        return CustomFunction.listAll();
    }

    @POST
    @Transactional
    public Response create(CreateRequest request) {
        if (request == null || request.operationId == null || request.operationId.isBlank()) {
            return Response.status(400).entity(Map.of("error", "operationId is required")).build();
        }
        if (request.shellCommand == null || request.shellCommand.isBlank()) {
            return Response.status(400).entity(Map.of("error", "shellCommand is required")).build();
        }

        CustomFunction existing = CustomFunction.find("operationId", request.operationId).firstResult();
        if (existing != null) {
            return Response.status(409).entity(Map.of("error", "operationId already exists")).build();
        }

        CustomFunction f = CustomFunction.create(
                request.operationId,
                request.description,
                request.shellCommand
        );
        f.persist();
        return Response.ok(f).build();
    }

    @GET
    @Path("/{id}")
    public Response get(@PathParam("id") String id) {
        CustomFunction f = CustomFunction.findById(id);
        if (f == null) {
            return Response.status(404).entity(Map.of("error", "function not found")).build();
        }
        return Response.ok(f).build();
    }

    @PATCH
    @Path("/{id}")
    @Transactional
    public Response patch(@PathParam("id") String id, PatchRequest request) {
        CustomFunction f = CustomFunction.findById(id);
        if (f == null) {
            return Response.status(404).entity(Map.of("error", "function not found")).build();
        }

        if (request.operationId != null) {
            if (request.operationId.isBlank()) {
                return Response.status(400).entity(Map.of("error", "operationId must not be blank")).build();
            }
            CustomFunction conflict = CustomFunction.find("operationId", request.operationId).firstResult();
            if (conflict != null && !conflict.id.equals(id)) {
                return Response.status(409).entity(Map.of("error", "operationId already exists")).build();
            }
            f.operationId = request.operationId;
        }
        if (request.description != null) {
            f.description = request.description.isBlank() ? null : request.description;
        }
        if (request.shellCommand != null) {
            if (request.shellCommand.isBlank()) {
                return Response.status(400).entity(Map.of("error", "shellCommand must not be blank")).build();
            }
            f.shellCommand = request.shellCommand;
        }

        f.persist();
        return Response.ok(f).build();
    }

    @DELETE
    @Path("/{id}")
    @Transactional
    public Response delete(@PathParam("id") String id) {
        CustomFunction f = CustomFunction.findById(id);
        if (f == null) {
            return Response.status(404).entity(Map.of("error", "function not found")).build();
        }
        f.delete();
        return Response.noContent().build();
    }

    @Operation(
            operationId = "custom_function_execute",
            summary = "Executes a custom function's shell command and returns the result"
    )
    @POST
    @Path("/{id}/execute")
    public Response execute(@PathParam("id") String id) {
        CustomFunction f = CustomFunction.findById(id);
        if (f == null) {
            return Response.status(404).entity(Map.of("error", "function not found")).build();
        }

        ExecutionResponse result = executor.execute(f.shellCommand);
        return Response.ok(result).build();
    }

    @POST
    @Path("/custom/{operationId}")
    public Response executeByOperationId(@PathParam("operationId") String operationId) {
        CustomFunction f = CustomFunction.find("operationId", operationId).firstResult();
        if (f == null) {
            return Response.status(404).entity(Map.of("error", "function not found")).build();
        }

        ExecutionResponse result = executor.execute(f.shellCommand);
        return Response.ok(result).build();
    }

    public static class CreateRequest {
        public String operationId;
        public String description;
        public String shellCommand;
    }

    public static class PatchRequest {
        public String operationId;
        public String description;
        public String shellCommand;
    }
}
