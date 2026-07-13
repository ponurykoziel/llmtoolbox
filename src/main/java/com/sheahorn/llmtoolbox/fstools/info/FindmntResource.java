package com.sheahorn.llmtoolbox.fstools.info;

import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import com.sheahorn.llmtoolbox.execution.ExecutionResponse;
import com.sheahorn.llmtoolbox.execution.Executor;
import org.eclipse.microprofile.openapi.annotations.Operation;

@Path("/api/tools/fs/findmnt")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class FindmntResource {

    @Inject
    Executor executor;

    @Operation(
            operationId = "fs_findmnt",
            summary = "Lists all mounted filesystems via `findmnt`"
    )
    @POST
    @Path("/execute")
    public ExecutionResponse execute(FindmntRequestDto request) {
        return executor.execute("findmnt");
    }
}
