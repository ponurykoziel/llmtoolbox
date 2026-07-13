package com.sheahorn.llmtoolbox.fstools.info;

import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import com.sheahorn.llmtoolbox.execution.ExecutionResponse;
import com.sheahorn.llmtoolbox.execution.Executor;
import org.eclipse.microprofile.openapi.annotations.Operation;

@Path("/api/tools/fs/lsblk")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class LsblkResource {

    @Inject
    Executor executor;

    @Operation(
            operationId = "fs_lsblk",
            summary = "Lists block devices along with filesystem and mount info via `lsblk -f`"
    )
    @POST
    @Path("/execute")
    public ExecutionResponse execute(LsblkRequestDto request) {
        return executor.execute("lsblk -f");
    }
}
