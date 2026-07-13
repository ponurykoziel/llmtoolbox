package com.sheahorn.llmtoolbox.nettools.ping;

import com.sheahorn.llmtoolbox.execution.ExecutionResponse;
import com.sheahorn.llmtoolbox.execution.Executor;
import com.sheahorn.llmtoolbox.execution.ToolSupport;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import org.eclipse.microprofile.openapi.annotations.Operation;

@Path("/api/tools/net/ping")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class PingResource {

    @Inject
    Executor executor;

    @Operation(
            operationId = "net_ping",
            summary = "Sends a bounded number of ICMP echo requests to a host"
    )
    @POST
    @Path("/execute")
    public ExecutionResponse execute(PingRequestDto request) {
        validate(request);

        int count = request.count == null ? 4 : request.count;

        String command = "ping"
                + " -c " + count
                + " -W 5"
                + " " + ToolSupport.shellQuote(request.host);

        return executor.execute(command);
    }

    private void validate(PingRequestDto request) {
        if (request == null) {
            throw new IllegalArgumentException("Request is required");
        }

        request.host = ToolSupport.sanitizeSafeChars(request.host);
        ToolSupport.validateHost(request.host);

        if (request.count != null && (request.count < 1 || request.count > 10)) {
            throw new IllegalArgumentException("count must be between 1 and 10");
        }
    }
}
