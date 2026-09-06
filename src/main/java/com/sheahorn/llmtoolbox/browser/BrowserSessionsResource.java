package com.sheahorn.llmtoolbox.browser;

import com.sheahorn.llmtoolbox.llm.ToolBean;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import org.eclipse.microprofile.openapi.annotations.Operation;

/**
 * Proxy for browser session management endpoints on the Python sidecar.
 */
@Path("/api/tools/browser/sessions")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class BrowserSessionsResource extends BrowserResourceSupport implements ToolBean {

    @Operation(
            operationId = "browser_session_list",
            summary = "List all currently active browsing sessions."
    )
    @GET
    public String list() throws Exception {
        return get("/sessions");
    }

    @Operation(
            operationId = "browser_session_open",
            summary = "Open a new browsing session, either tied to a persistent profile or as an anonymous throwaway context."
    )
    @POST
    public String open(SessionOpenRequest request) throws Exception {
        return post("/sessions", request != null ? request : new SessionOpenRequest());
    }

    @Operation(
            operationId = "browser_session_close",
            summary = "Close a browsing session, persisting storage state for profile-backed sessions."
    )
    @DELETE
    @Path("/{session_id}")
    public String close(@PathParam("session_id") String sessionId) throws Exception {
        if (sessionId == null || sessionId.isBlank()) {
            throw new IllegalArgumentException("session_id is required");
        }
        return super.delete("/sessions/" + sessionId);
    }

    // ── DTO ──────────────────────────────────────────────────

    public static class SessionOpenRequest {
        public String profile;
    }
}
