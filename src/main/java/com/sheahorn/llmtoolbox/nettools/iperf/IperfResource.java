package com.sheahorn.llmtoolbox.nettools.iperf;

import com.sheahorn.llmtoolbox.execution.ExecutionResponse;
import com.sheahorn.llmtoolbox.execution.Executor;
import com.sheahorn.llmtoolbox.execution.ToolSupport;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import org.eclipse.microprofile.openapi.annotations.Operation;

@Path("/api/tools/net/iperf")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class IperfResource {

    @Inject
    Executor executor;

    @Operation(
            operationId = "net_iperf",
            summary = "Runs an iperf3 client test against a remote host"
    )
    @POST
    @Path("/execute")
    public ExecutionResponse execute(IperfRequestDto request) {
        validate(request);

        int port = request.port == null ? 5201 : request.port;
        int seconds = request.seconds == null ? 10 : request.seconds;
        boolean json = request.json == null || request.json;

        StringBuilder command = new StringBuilder();

        command.append("iperf3")
                .append(" -c ").append(ToolSupport.shellQuote(request.host))
                .append(" -p ").append(port)
                .append(" -t ").append(seconds);

        if (Boolean.TRUE.equals(request.udp)) {
            command.append(" -u");

            if (request.bandwidth != null && !request.bandwidth.isBlank()) {
                command.append(" -b ").append(ToolSupport.shellQuote(request.bandwidth));
            }
        }

        if (Boolean.TRUE.equals(request.reverse)) {
            command.append(" -R");
        }

        if (json) {
            command.append(" -J");
        }

        return executor.execute(command.toString());
    }

    private void validate(IperfRequestDto request) {
        if (request == null) {
            throw new IllegalArgumentException("Request is required");
        }

        request.host = ToolSupport.sanitizeSafeChars(request.host);
        ToolSupport.validateHost(request.host);

        if (request.port != null) {
            ToolSupport.validatePort(request.port);
        }

        if (request.seconds != null && (request.seconds < 1 || request.seconds > 30)) {
            throw new IllegalArgumentException("seconds must be between 1 and 30");
        }

        if (request.bandwidth != null && !request.bandwidth.isBlank()) {
            if (!request.bandwidth.matches("[0-9]+[KMG]?")) {
                throw new IllegalArgumentException("bandwidth must look like 10M, 500K, or 1G");
            }
        }

        if (request.bandwidth != null && !request.bandwidth.isBlank()
                && !Boolean.TRUE.equals(request.udp)) {
            throw new IllegalArgumentException("bandwidth is only supported for UDP");
        }
    }
}
