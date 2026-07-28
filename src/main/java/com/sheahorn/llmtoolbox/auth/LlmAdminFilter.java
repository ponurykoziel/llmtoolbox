package com.sheahorn.llmtoolbox.auth;

import jakarta.annotation.Priority;
import jakarta.ws.rs.Priorities;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.SecurityContext;
import jakarta.ws.rs.ext.Provider;

import java.util.Map;

/**
 * Blocks non-admin access to /api/llm/* CRUD endpoints.
 * The /api/llm/execute endpoint is excluded — it's open for tool dispatch.
 * Runs after BearerAuthFilter (which sets SecurityContext).
 */
@Provider
@Priority(Priorities.AUTHORIZATION)
public class LlmAdminFilter implements ContainerRequestFilter {

    @Override
    public void filter(ContainerRequestContext requestContext) {
        String path = requestContext.getUriInfo().getPath();
        if (path == null) return;

        if (path.startsWith("/")) path = path.substring(1);

        // Only guard /api/llm/* but NOT /api/llm/execute
        if (!path.startsWith("api/llm/") || path.equals("api/llm/execute")) return;

        SecurityContext sec = requestContext.getSecurityContext();
        if (sec == null || sec.getUserPrincipal() == null || !sec.isUserInRole("admin")) {
            requestContext.abortWith(Response.status(Response.Status.FORBIDDEN)
                    .entity(Map.of("error", "admin access required"))
                    .build());
        }
    }
}
