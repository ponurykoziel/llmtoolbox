package com.sheahorn.llmtoolbox.nettools.tls;

import com.sheahorn.llmtoolbox.execution.ExecutionResponse;
import com.sheahorn.llmtoolbox.execution.Executor;
import com.sheahorn.llmtoolbox.execution.ToolSupport;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import org.eclipse.microprofile.openapi.annotations.Operation;

@Path("/api/tools/net/tls-check")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class TlsCheckResource {

    @Inject
    Executor executor;

    @Operation(
            operationId = "net_tls_check",
            summary = "Inspects a TLS certificate by initiating a brief openssl s_client handshake"
    )
    @POST
    @Path("/execute")
    public ExecutionResponse execute(TlsCheckRequestDto request) {
        validate(request);

        int port = request.port == null ? 443 : request.port;

        String connect = request.host + ":" + port;

        String command = "printf '' | timeout 5 openssl s_client"
                + " -connect " + ToolSupport.shellQuote(connect)
                + " -servername " + ToolSupport.shellQuote(request.host)
                + " -showcerts";

        return executor.execute(command);
    }

    private void validate(TlsCheckRequestDto request) {
        if (request == null) {
            throw new IllegalArgumentException("Request is required");
        }

        request.host = ToolSupport.sanitizeSafeChars(request.host);
        ToolSupport.validateHost(request.host);

        if (request.port != null) {
            ToolSupport.validatePort(request.port);
        }
    }
}
