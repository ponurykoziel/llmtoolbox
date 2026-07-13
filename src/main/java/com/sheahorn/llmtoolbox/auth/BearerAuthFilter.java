package com.sheahorn.llmtoolbox.auth;

import com.sheahorn.llmtoolbox.security.ApiKeyResolver;
import jakarta.annotation.Priority;
import jakarta.inject.Inject;
import jakarta.ws.rs.Priorities;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.container.ResourceInfo;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.ext.Provider;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.io.IOException;
import java.lang.reflect.Method;

@Provider
@Priority(Priorities.AUTHENTICATION)
public class BearerAuthFilter implements ContainerRequestFilter {

    @ConfigProperty(name = "llmtoolbox.auth.token")
    String token;

    @Inject
    ApiKeyResolver apiKeyResolver;

    @Context
    ResourceInfo resourceInfo;

    @Override
    public void filter(ContainerRequestContext requestContext) throws IOException {
        if (isNoAuthEndpoint()) {
            return;
        }

        String path = requestContext.getUriInfo().getPath();

        // Skip Bearer auth for non-API paths (UI pages, login, etc.)
        // These are protected by Quarkus form-auth instead
        if (!path.startsWith("api/") && !path.equals("api")) {
            return;
        }

        // Check for valid session cookie (browser users already logged in via form)
        var cookies = requestContext.getCookies();
        if (cookies.containsKey("llmtoolbox_session")) {
            return;
        }

        String authorization = requestContext.getHeaderString("Authorization");
        if (authorization == null) {
            requestContext.abortWith(
                    jakarta.ws.rs.core.Response.status(401)
                            .entity("Unauthorized")
                            .build()
            );
            return;
        }

        // Try static config token first
        if (authorization.equals("Bearer " + token)) {
            return;
        }

        // Try API key resolver (static access key + user-created keys)
        if (authorization.startsWith("Bearer ")) {
            String providedKey = authorization.substring(7);
            if (apiKeyResolver.resolve(providedKey).isPresent()) {
                return;
            }
        }

        requestContext.abortWith(
                jakarta.ws.rs.core.Response.status(401)
                        .entity("Unauthorized")
                        .build()
        );
    }

    private boolean isNoAuthEndpoint() {
        Class<?> resourceClass = resourceInfo.getResourceClass();
        Method resourceMethod = resourceInfo.getResourceMethod();

        return resourceClass != null && resourceClass.isAnnotationPresent(NoBearerAuth.class)
                || resourceMethod != null && resourceMethod.isAnnotationPresent(NoBearerAuth.class);
    }
}
