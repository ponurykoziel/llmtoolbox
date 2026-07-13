package com.sheahorn.llmtoolbox.hosttools.sysinfo;

import com.sheahorn.llmtoolbox.execution.ExecutionResponse;
import com.sheahorn.llmtoolbox.execution.Executor;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import org.eclipse.microprofile.openapi.annotations.Operation;

@Path("/api/tools/host/sysinfo/system")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class SystemResource {

    @Inject
    Executor executor;

    @Operation(
            operationId = "host_sysinfo_free",
            summary = "Shows human-readable memory usage via `free -h`"
    )
    @POST
    @Path("/free")
    public ExecutionResponse free(SysInfoRequestDto request) {
        return executor.execute("free -h");
    }

    @Operation(
            operationId = "host_sysinfo_uptime",
            summary = "Shows system uptime and load averages via `uptime`"
    )
    @POST
    @Path("/uptime")
    public ExecutionResponse uptime(SysInfoRequestDto request) {
        return executor.execute("uptime");
    }

    @Operation(
            operationId = "host_sysinfo_who",
            summary = "Lists logged-in users via `who`"
    )
    @POST
    @Path("/who")
    public ExecutionResponse who(SysInfoRequestDto request) {
        return executor.execute("who");
    }
}
