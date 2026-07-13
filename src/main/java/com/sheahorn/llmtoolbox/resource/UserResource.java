package com.sheahorn.llmtoolbox.resource;

import com.sheahorn.llmtoolbox.domain.User;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.SecurityContext;
import io.quarkus.elytron.security.common.BcryptUtil;
import org.jboss.logging.Logger;

import java.util.Map;

@Path("/api/users")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class UserResource {

    private static final Logger LOG = Logger.getLogger(UserResource.class);

    @Context
    SecurityContext securityContext;

    @GET
    @Path("/me")
    public Response me() {
        if (securityContext == null || securityContext.getUserPrincipal() == null) {
            return Response.status(Response.Status.UNAUTHORIZED).build();
        }
        String username = securityContext.getUserPrincipal().getName();
        User user = User.findByUsername(username);
        if (user == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        return Response.ok(Map.of("id", user.id, "username", user.username, "role", user.role)).build();
    }

    @PATCH
    @Path("/{id}/password")
    @Transactional
    public Response changePassword(@PathParam("id") String id, Map<String, Object> body) {
        String newPassword = (String) body.get("password");
        if (newPassword == null || newPassword.isBlank()) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(Map.of("error", "password is required"))
                    .build();
        }

        // Non-admin users can only change their own password
        boolean isAdmin = securityContext != null && securityContext.isUserInRole("admin");
        if (!isAdmin) {
            String currentUsername = securityContext != null && securityContext.getUserPrincipal() != null
                    ? securityContext.getUserPrincipal().getName() : null;
            User target = User.findById(id);
            if (target == null || !target.username.equals(currentUsername)) {
                return Response.status(Response.Status.FORBIDDEN)
                        .entity(Map.of("error", "you can only change your own password"))
                        .build();
            }
        }

        User user = User.findById(id);
        if (user == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(Map.of("error", "user not found"))
                    .build();
        }
        user.password = BcryptUtil.bcryptHash(newPassword);
        user.persist();
        return Response.ok(Map.of("id", user.id, "username", user.username)).build();
    }
}
