package com.sheahorn.llmtoolbox.resource;

import com.sheahorn.llmtoolbox.domain.LlmExecuteRequest;
import com.sheahorn.llmtoolbox.domain.LlmExecuteResponse;
import com.sheahorn.llmtoolbox.llm.LlmExecutionService;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.openapi.annotations.Operation;
import com.sheahorn.llmtoolbox.llm.ToolBean;

import java.util.Map;

@Path("/api/llm/execute")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class LlmExecutionResource implements ToolBean {

    @Inject
    LlmExecutionService service;

    @Operation(
            operationId = "llm_execute_request",
            summary = "Executes an LLM request using a configured agent with tool support"
    )
    @POST
    public Response execute(LlmExecuteRequest request) {
        if (request == null || request.agentName == null || request.agentName.isBlank()) {
            return Response.status(400)
                    .entity(Map.of("error", "agentName is required"))
                    .build();
        }
        if (request.requestPrompt == null || request.requestPrompt.isBlank()) {
            return Response.status(400)
                    .entity(Map.of("error", "requestPrompt is required"))
                    .build();
        }

        LlmExecuteResponse result = service.execute(request);
        return Response.ok(result).build();
    }
}
