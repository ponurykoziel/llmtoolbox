package com.sheahorn.llmtoolbox.fstools.info;

import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import com.sheahorn.llmtoolbox.execution.ExecutionResponse;
import com.sheahorn.llmtoolbox.execution.Executor;
import com.sheahorn.llmtoolbox.execution.ToolSupport;
import org.eclipse.microprofile.openapi.annotations.Operation;

@Path("/api/tools/fs/sha256sum")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class Sha256sumResource extends FsResourceSupport {

    @Inject
    Executor executor;

    @Operation(
            operationId = "fs_sha256sum",
            summary = "Computes a SHA-256 digest for a file, falling back to `sha256 -r` if sha256sum is missing"
    )
    @POST
    @Path("/execute")
    public ExecutionResponse execute(Sha256sumRequestDto request) {
        if (request == null) {
            throw new IllegalArgumentException("Request is required");
        }

        String path = normalizePath(request.path);
        String quotedPath = ToolSupport.shellQuote(path);

        String command = "if command -v sha256sum >/dev/null 2>&1; then "
                + "sha256sum -- " + quotedPath
                + "; else "
                + "sha256 -r -- " + quotedPath
                + "; fi";

        return executor.execute(command);
    }
}
