package com.sheahorn.llmtoolbox.fstools.info;

import com.sheahorn.llmtoolbox.execution.ExecutionResponse;
import com.sheahorn.llmtoolbox.execution.Executor;
import com.sheahorn.llmtoolbox.execution.ToolSupport;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import org.eclipse.microprofile.openapi.annotations.Operation;

@Path("/api/tools/fs/ls-info")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class LsInfoResource extends FsResourceSupport {

    @Inject
    Executor executor;

    @Operation(
            operationId = "fs_ls_info",
            summary = "Returns a detailed `ls -la` listing for a path inside the allowed root"
    )
    @POST
    @Path("/execute")
    public ExecutionResponse execute(LsInfoRequestDto request) {
        if (request == null) {
            throw new IllegalArgumentException("Request is required");
        }

        String path = normalizePath(request.path);

        String command = "ls -la -- " + ToolSupport.shellQuote(path);

        return executor.execute(command);
    }
}
