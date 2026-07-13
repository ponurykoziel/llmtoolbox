package com.sheahorn.llmtoolbox.terminal;

import com.sheahorn.llmtoolbox.execution.ExecutionResponse;
import com.sheahorn.llmtoolbox.execution.Executor;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.openapi.annotations.Operation;

@Path("/api/tools/terminal")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class TerminalResource {

    @Inject
    Executor executor;

    @ConfigProperty(name = "llmtoolbox.terminal.allow", defaultValue = "false")
    boolean terminalAllowed;

    @Operation(
            operationId = "terminal_execute",
            summary = "Executes an arbitrary shell command; requires llmtoolbox.terminal.allow=true"
    )
    @POST
    @Path("/execute")
    public Response execute(TerminalRequestDto request) {
        if (!terminalAllowed) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new TerminalErrorResponse("terminal is not enabled; set llmtoolbox.terminal.allow=true"))
                    .build();
        }

        if (request == null || request.cmd == null || request.cmd.isBlank()) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new TerminalErrorResponse("cmd is required"))
                    .build();
        }

        ExecutionResponse result = executor.execute(request.cmd);
        return Response.ok(result).build();
    }

    public static class TerminalErrorResponse {
        public String error;

        public TerminalErrorResponse(String error) {
            this.error = error;
        }
    }
}
