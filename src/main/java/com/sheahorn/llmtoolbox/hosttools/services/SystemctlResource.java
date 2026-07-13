package com.sheahorn.llmtoolbox.hosttools.services;

import com.sheahorn.llmtoolbox.execution.ExecutionResponse;
import com.sheahorn.llmtoolbox.execution.Executor;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import org.eclipse.microprofile.openapi.annotations.Operation;

@Path("/api/tools/host/services")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class SystemctlResource {

    @Inject
    Executor executor;

    @Operation(
            operationId = "host_services_running",
            summary = "Lists active running systemd service units"
    )
    @POST
    @Path("/running")
    public ExecutionResponse running(ServiceRequestDto request) {
        return executor.execute("systemctl list-units --type=service --state=running --no-pager");
    }

    @Operation(
            operationId = "host_services_failed",
            summary = "Lists failed systemd units"
    )
    @POST
    @Path("/failed")
    public ExecutionResponse failed(ServiceRequestDto request) {
        return executor.execute("systemctl list-units --failed --no-pager");
    }
}
