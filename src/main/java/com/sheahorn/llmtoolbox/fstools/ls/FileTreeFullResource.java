package com.sheahorn.llmtoolbox.fstools.ls;

import com.sheahorn.llmtoolbox.execution.ExecutionResponse;
import com.sheahorn.llmtoolbox.execution.Executor;
import com.sheahorn.llmtoolbox.execution.ToolSupport;
import com.sheahorn.llmtoolbox.fstools.info.FsResourceSupport;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import org.eclipse.microprofile.openapi.annotations.Operation;

@Path("/api/tools/fs/ls")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class FileTreeFullResource extends FsResourceSupport {

    @Inject
    Executor executor;

    @Operation(
            operationId = "fs_ls_full",
            summary = "Lists the complete relative file tree under a path inside the allowed root (recursive), including dotfiles"
    )
    @POST
    @Path("/full")
    public ExecutionResponse execute(FileTreeRequestDto request) {
        if (request == null || request.path == null || request.path.isBlank()) {
            throw new IllegalArgumentException("path is required");
        }

        String path = normalizePath(request.path);

        String command = "find "
                + ToolSupport.shellQuote(path)
                + " -mindepth 1"
                + " -printf '%P\\n'";

        return executor.execute(command);
    }
}
