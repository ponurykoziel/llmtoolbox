package com.sheahorn.llmtoolbox.hosttools.monitor;

import com.sheahorn.llmtoolbox.execution.ExecutionResponse;
import com.sheahorn.llmtoolbox.execution.Executor;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import org.eclipse.microprofile.openapi.annotations.Operation;

@Path("/api/tools/host/monitor")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class MonitorResource {

    @Inject
    Executor executor;

    @Operation(
            operationId = "host_monitor_off",
            summary = "Forces the display off via xset dpms"
    )
    @POST
    @Path("/off")
    public ExecutionResponse off(MonitorRequestDto request) {
        return executor.execute("xset dpms force off");
    }

    @Operation(
            operationId = "host_monitor_on",
            summary = "Wakes the display via xset dpms force on and nudges the mouse via xdotool"
    )
    @POST
    @Path("/on")
    public ExecutionResponse on(MonitorRequestDto request) {
        return executor.execute("xset dpms force on; xdotool mousemove_relative 1 0");
    }
}
