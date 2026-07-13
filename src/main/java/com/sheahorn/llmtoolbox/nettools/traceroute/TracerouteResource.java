package com.sheahorn.llmtoolbox.nettools.traceroute;

import com.sheahorn.llmtoolbox.execution.ExecutionResponse;
import com.sheahorn.llmtoolbox.execution.Executor;
import com.sheahorn.llmtoolbox.execution.ToolSupport;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import org.eclipse.microprofile.openapi.annotations.Operation;

@Path("/api/tools/net/traceroute")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class TracerouteResource {

    @Inject
    Executor executor;

    @Operation(
            operationId = "net_traceroute",
            summary = "Runs traceroute to a host, with short per-hop timeouts and a single probe"
    )
    @POST
    @Path("/execute")
    public ExecutionResponse execute(TracerouteRequestDto request) {
        validate(request);

        String command = "traceroute"
                + " -w 5"
                + " -q 1"
                + " " + ToolSupport.shellQuote(request.host);

        return executor.execute(command);
    }

    private void validate(TracerouteRequestDto request) {
        if (request == null) {
            throw new IllegalArgumentException("Request is required");
        }

        request.host = ToolSupport.sanitizeSafeChars(request.host);
        ToolSupport.validateHost(request.host);
    }
}
