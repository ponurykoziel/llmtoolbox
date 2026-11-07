package com.sheahorn.llmtoolbox.resource;

import com.sheahorn.llmtoolbox.buildtools.AbstractFamilyLockService;
import jakarta.enterprise.inject.Any;
import jakarta.enterprise.inject.Instance;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.SecurityContext;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Manual escape hatch for stuck build locks, for the dashboard only.
 *
 * Deliberately NOT a tool: it is not a ToolBean and carries no @Operation, so it never shows
 * up in the OpenAPI subset and an LLM cannot call it. Self-referring tools get their own
 * category (tracker 000018EF) — until then, a human clicks the button.
 */
@Path("/ui/locks")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class LockAdminResource {

    @Inject
    @Any
    Instance<AbstractFamilyLockService> lockServices;

    @Context
    SecurityContext securityContext;

    /** Body may be empty (clear every family) or {"family":"mvn"}. */
    @POST
    @Path("/clear")
    public Response clearLock(Map<String, String> body) {
        if (securityContext == null || !securityContext.isUserInRole("admin")) {
            return Response.status(Response.Status.FORBIDDEN)
                    .entity(Map.of("error", "admin role required"))
                    .build();
        }

        String requested = body == null ? null : body.get("family");
        boolean all = requested == null || requested.isBlank();

        List<String> cleared = new ArrayList<>();
        List<String> stillHeld = new ArrayList<>();
        boolean enabled = false;
        boolean matched = all;

        for (AbstractFamilyLockService lock : lockServices) {
            if (!all && !requested.equalsIgnoreCase(lock.getFamily())) {
                continue;
            }
            matched = true;
            enabled = lock.isEnabled();
            if (lock.forceRelease() != null) {
                cleared.add(lock.getFamily());
            }
            // A live worker still owns the monitor — clearing the flag does not unblock it.
            if (lock.isHeldByWorker()) {
                stillHeld.add(lock.getFamily());
            }
        }

        if (!matched) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(Map.of("error", "unknown lock family: " + requested))
                    .build();
        }

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("enabled", enabled);
        result.put("cleared", cleared);
        result.put("stillHeld", stillHeld);
        return Response.ok(result).build();
    }
}
