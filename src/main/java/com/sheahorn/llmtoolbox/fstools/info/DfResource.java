package com.sheahorn.llmtoolbox.fstools.info;

import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import com.sheahorn.llmtoolbox.execution.ExecutionResponse;
import com.sheahorn.llmtoolbox.execution.Executor;
import com.sheahorn.llmtoolbox.execution.ToolSupport;
import org.eclipse.microprofile.openapi.annotations.Operation;

@Path("/api/tools/fs/df")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class DfResource extends FsResourceSupport {

    @Inject
    Executor executor;

    @Operation(
            operationId = "fs_df",
            summary = "Reports filesystem disk space in 1024-byte blocks, optionally scoped to a path inside the allowed root"
    )
    @POST
    @Path("/execute")
    public ExecutionResponse execute(DfRequestDto request) {
        String command;

        if (request != null && request.path != null && !request.path.isBlank()) {
            String path = normalizePath(request.path);
            command = "df -kP -- " + ToolSupport.shellQuote(path);
        } else {
            command = "df -kP";
        }

        return executor.execute(command);
    }
}
