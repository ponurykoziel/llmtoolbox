package com.sheahorn.llmtoolbox.hosttools.sysinfo;

import com.sheahorn.llmtoolbox.execution.ExecutionResponse;
import com.sheahorn.llmtoolbox.execution.Executor;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import org.eclipse.microprofile.openapi.annotations.Operation;

@Path("/api/tools/host/sysinfo/network")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class NetworkResource {

    @Inject
    Executor executor;

    @Operation(
            operationId = "host_sysinfo_ss_tnlp",
            summary = "Lists TCP sockets in the listen state with owning process info via `ss -tnlp`"
    )
    @POST
    @Path("/ss-tnlp")
    public ExecutionResponse ssTnlp(SysInfoRequestDto request) {
        return executor.execute("ss -tnlp");
    }

    @Operation(
            operationId = "host_sysinfo_ip_addr",
            summary = "Dumps interface addresses via `ip addr`"
    )
    @POST
    @Path("/ip-addr")
    public ExecutionResponse ipAddr(SysInfoRequestDto request) {
        return executor.execute("ip addr");
    }

    @Operation(
            operationId = "host_sysinfo_ip_route",
            summary = "Dumps the routing table via `ip route`"
    )
    @POST
    @Path("/ip-route")
    public ExecutionResponse ipRoute(SysInfoRequestDto request) {
        return executor.execute("ip route");
    }

    @Operation(
            operationId = "host_sysinfo_ip_neigh",
            summary = "Dumps the ARP/ND neighbor table via `ip neigh`"
    )
    @POST
    @Path("/ip-neigh")
    public ExecutionResponse ipNeigh(SysInfoRequestDto request) {
        return executor.execute("ip neigh");
    }
}
