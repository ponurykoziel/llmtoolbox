package com.sheahorn.llmtoolbox.hosttools.sysinfo;

import com.sheahorn.llmtoolbox.execution.ExecutionResponse;
import com.sheahorn.llmtoolbox.execution.Executor;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import org.eclipse.microprofile.openapi.annotations.Operation;

@Path("/api/tools/host/sysinfo/processes")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class ProcessResource {

    @Inject
    Executor executor;

    @Operation(
            operationId = "host_sysinfo_ps",
            summary = "Lists all processes with `ps aux`"
    )
    @POST
    @Path("/ps")
    public ExecutionResponse ps(SysInfoRequestDto request) {
        return executor.execute("ps aux");
    }

    @Operation(
            operationId = "host_sysinfo_top",
            summary = "Takes a single non-interactive `top` snapshot via `top -bn1`"
    )
    @POST
    @Path("/top")
    public ExecutionResponse top(SysInfoRequestDto request) {
        return executor.execute("top -bn1");
    }
}
