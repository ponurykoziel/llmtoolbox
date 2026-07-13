package com.sheahorn.llmtoolbox.fstools.tail;

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

@Path("/api/tools/fs/head")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class HeadResource extends FsResourceSupport {

    private static final int DEFAULT_LINES = 200;
    private static final int MAX_LINES = 5000;

    @Inject
    Executor executor;

    @Operation(
            operationId = "fs_head",
            summary = "Returns the first N lines (default 200, max 5000) of a regular file inside the allowed root"
    )
    @POST
    @Path("/execute")
    public ExecutionResponse execute(TailRequestDto request) {
        validate(request);

        int lines = request.lines == null ? DEFAULT_LINES : request.lines;
        lines = Math.max(1, Math.min(MAX_LINES, lines));

        String path = normalizePath(request.path);
        String command = "head -n " + lines + " -- " + ToolSupport.shellQuote(path);

        return executor.execute(command);
    }

    private void validate(TailRequestDto request) {
        if (request == null || request.path == null || request.path.isBlank()) {
            throw new IllegalArgumentException("path is required");
        }
    }
}
