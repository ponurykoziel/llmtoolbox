package com.sheahorn.llmtoolbox.nettools.tcpconnect;

import com.sheahorn.llmtoolbox.execution.ExecutionResponse;
import com.sheahorn.llmtoolbox.execution.Executor;
import com.sheahorn.llmtoolbox.execution.ToolSupport;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import org.eclipse.microprofile.openapi.annotations.Operation;

@Path("/api/tools/net/tcp-connect")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class TcpConnectResource {

    @Inject
    Executor executor;

    @Operation(
            operationId = "net_tcp_connect",
            summary = "Tests TCP connectivity to a host and port using netcat"
    )
    @POST
    @Path("/execute")
    public ExecutionResponse execute(TcpConnectRequestDto request) {
        validate(request);

        String command = "nc"
                + " -vz"
                + " -w 5"
                + " " + ToolSupport.shellQuote(request.host)
                + " " + ToolSupport.shellQuote(String.valueOf(request.port));

        return executor.execute(command);
    }

    private void validate(TcpConnectRequestDto request) {
        if (request == null) {
            throw new IllegalArgumentException("Request is required");
        }

        request.host = ToolSupport.sanitizeSafeChars(request.host);
        ToolSupport.validateHost(request.host);
        ToolSupport.validatePort(request.port);
    }
}
