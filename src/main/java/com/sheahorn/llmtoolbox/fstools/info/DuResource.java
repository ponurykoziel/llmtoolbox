package com.sheahorn.llmtoolbox.fstools.info;

import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import com.sheahorn.llmtoolbox.execution.ExecutionResponse;
import com.sheahorn.llmtoolbox.execution.Executor;
import com.sheahorn.llmtoolbox.execution.ToolSupport;
import org.eclipse.microprofile.openapi.annotations.Operation;

@Path("/api/tools/fs/du")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class DuResource extends FsResourceSupport {

    @Inject
    Executor executor;

    @Operation(
            operationId = "fs_du",
            summary = "Reports the disk usage in 1024-byte blocks for a path inside the allowed root"
    )
    @POST
    @Path("/execute")
    public ExecutionResponse execute(DuRequestDto request) {
        if (request == null) {
            throw new IllegalArgumentException("Request is required");
        }

        String path = normalizePath(request.path);

        String command = "du -sk -- " + ToolSupport.shellQuote(path);

        return executor.execute(command);
    }
}
