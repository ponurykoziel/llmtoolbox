package com.sheahorn.llmtoolbox.basics.clipboard;

import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import org.eclipse.microprofile.openapi.annotations.Operation;

@Path("/api/tools/clipboard")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class ClipboardResource {

    @Inject
    ClipboardCache cache;

    @Operation(
            operationId = "clipboard_read",
            summary = "Reads the current clipboard content"
    )
    @GET
    @Path("/read")
    public ClipboardResponse read() {
        ClipboardResponse r = new ClipboardResponse();
        r.content = cache.read();
        return r;
    }

    @Operation(
            operationId = "clipboard_write",
            summary = "Overwrites the clipboard with new content"
    )
    @POST
    @Path("/write")
    public ClipboardResponse write(ClipboardWriteRequestDto request) {
        if (request == null) {
            throw new IllegalArgumentException("Request is required");
        }
        cache.write(request.content);
        ClipboardResponse r = new ClipboardResponse();
        r.content = cache.read();
        return r;
    }

    @Operation(
            operationId = "clipboard_append",
            summary = "Appends content to the clipboard"
    )
    @POST
    @Path("/append")
    public ClipboardResponse append(ClipboardWriteRequestDto request) {
        if (request == null) {
            throw new IllegalArgumentException("Request is required");
        }
        ClipboardResponse r = new ClipboardResponse();
        r.content = cache.append(request.content);
        return r;
    }
}
