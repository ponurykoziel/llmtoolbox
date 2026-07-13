package com.sheahorn.llmtoolbox.nettools.hostinfo;

import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;

import com.sheahorn.llmtoolbox.execution.ExecutionResponse;
import com.sheahorn.llmtoolbox.execution.Executor;
import org.eclipse.microprofile.openapi.annotations.Operation;

@Path("/api/tools/net/host")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class HostInfoResource {

    @Inject
    Executor executor;

    @Operation(
            operationId = "net_host_info",
            summary = "Returns the local host's hostname, addresses, routes, neighbors, and resolver config"
    )
    @POST
    @Path("/execute")
    public ExecutionResponse execute(HostInfoRequestDto request) {
        String command = String.join("; ",
                "echo '===== hostname ====='",
                "hostname",
                "echo '===== ip addr ====='",
                "ip addr",
                "echo '===== ip route ====='",
                "ip route",
                "echo '===== ip neigh ====='",
                "ip neigh",
                "echo '===== resolv.conf ====='",
                "cat /etc/resolv.conf"
        );

        return executor.execute(command);
    }
}
