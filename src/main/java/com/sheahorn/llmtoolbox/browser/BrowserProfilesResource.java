package com.sheahorn.llmtoolbox.browser;

import com.sheahorn.llmtoolbox.llm.ToolBean;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import org.eclipse.microprofile.openapi.annotations.Operation;

/**
 * Proxy for browser profile CRUD endpoints on the Python sidecar.
 */
@Path("/api/tools/browser/profiles")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class BrowserProfilesResource extends BrowserResourceSupport implements ToolBean {

    @Operation(
            operationId = "browser_profiles_list",
            summary = "List all browser profiles with their isolated storage directories."
    )
    @GET
    public String list() throws Exception {
        return get("/profiles");
    }

    @Operation(
            operationId = "browser_profiles_create",
            summary = "Create a new browser profile with an isolated user data directory for persistent cookies and storage."
    )
    @POST
    public String create(ProfileCreateRequest request) throws Exception {
        if (request == null || request.name == null || request.name.isBlank()) {
            throw new IllegalArgumentException("name is required");
        }
        return post("/profiles", request);
    }

    @Operation(
            operationId = "browser_profiles_delete",
            summary = "Delete a profile and all its stored data, closing any active sessions using it."
    )
    @DELETE
    @Path("/{name}")
    public String delete(@PathParam("name") String name) throws Exception {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("name is required");
        }
        return super.delete("/profiles/" + name);
    }

    // ── DTO ──────────────────────────────────────────────────

    public static class ProfileCreateRequest {
        public String name;
    }
}
