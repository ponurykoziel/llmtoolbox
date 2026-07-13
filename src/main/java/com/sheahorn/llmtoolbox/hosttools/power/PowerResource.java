package com.sheahorn.llmtoolbox.hosttools.power;

import com.sheahorn.llmtoolbox.execution.ExecutionResponse;
import com.sheahorn.llmtoolbox.execution.Executor;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import org.eclipse.microprofile.openapi.annotations.Operation;

@Path("/api/tools/host/power")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class PowerResource {

    @Inject
    Executor executor;

    @Operation(
            operationId = "host_power_shutdown",
            summary = "Powers off the host immediately via systemctl"
    )
    @POST
    @Path("/shutdown")
    public ExecutionResponse shutdown(PowerRequestDto request) {
        return executor.execute("systemctl poweroff");
    }

    @Operation(
            operationId = "host_power_restart",
            summary = "Reboots the host immediately via systemctl"
    )
    @POST
    @Path("/restart")
    public ExecutionResponse restart(PowerRequestDto request) {
        return executor.execute("systemctl reboot");
    }
}
