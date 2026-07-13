package com.sheahorn.llmtoolbox.resource;

import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.SecurityContext;
import org.thymeleaf.TemplateEngine;

@Path("")
public class LlmtoolsPageResource {

    @Inject
    TemplateEngine templateEngine;

    @jakarta.ws.rs.core.Context
    SecurityContext securityContext;

    @GET
    @Path("/login.html")
    @Produces(MediaType.TEXT_HTML)
    public Response loginPage(@QueryParam("error") String error) {
        org.thymeleaf.context.Context ctx = new org.thymeleaf.context.Context();
        if (error != null && !error.isBlank()) {
            ctx.setVariable("error", error);
        }
        return Response.ok(templateEngine.process("login", ctx)).build();
    }

    @GET
    @Path("/login-failed.html")
    @Produces(MediaType.TEXT_HTML)
    public Response loginFailedPage(@QueryParam("reason") String reason) {
        org.thymeleaf.context.Context ctx = new org.thymeleaf.context.Context();
        String message = "Your session expired or credentials are invalid.";
        if ("deleted".equals(reason)) {
            message = "Your account has been deleted.";
        } else if ("expired".equals(reason)) {
            message = "Your session has expired.";
        }
        ctx.setVariable("message", message);
        return Response.ok(templateEngine.process("login-failed", ctx)).build();
    }

    @GET
    @Path("/")
    @Produces(MediaType.TEXT_HTML)
    public Response dashboard() {
        org.thymeleaf.context.Context ctx = new org.thymeleaf.context.Context();
        ctx.setVariable("isAdmin", isAdmin());
        return Response.ok(templateEngine.process("dashboard", ctx)).build();
    }

    @GET
    @Path("/ui/account")
    @Produces(MediaType.TEXT_HTML)
    public Response account() {
        org.thymeleaf.context.Context ctx = new org.thymeleaf.context.Context();
        ctx.setVariable("isAdmin", isAdmin());
        return Response.ok(templateEngine.process("account", ctx)).build();
    }

    @GET
    @Path("/ui/presets")
    @Produces(MediaType.TEXT_HTML)
    public Response presets() {
        org.thymeleaf.context.Context ctx = new org.thymeleaf.context.Context();
        ctx.setVariable("isAdmin", isAdmin());
        return Response.ok(templateEngine.process("presets", ctx)).build();
    }

    @GET
    @Path("/ui/presets/new")
    @Produces(MediaType.TEXT_HTML)
    public Response createPreset() {
        org.thymeleaf.context.Context ctx = new org.thymeleaf.context.Context();
        ctx.setVariable("isAdmin", isAdmin());
        return Response.ok(templateEngine.process("createPreset", ctx)).build();
    }

    @GET
    @Path("/ui/presets/{name}")
    @Produces(MediaType.TEXT_HTML)
    public Response presetDetail(@PathParam("name") String name) {
        org.thymeleaf.context.Context ctx = new org.thymeleaf.context.Context();
        ctx.setVariable("presetName", name);
        ctx.setVariable("isAdmin", isAdmin());
        return Response.ok(templateEngine.process("presetDetail", ctx)).build();
    }

    @GET
    @Path("/ui/functions")
    @Produces(MediaType.TEXT_HTML)
    public Response functions() {
        org.thymeleaf.context.Context ctx = new org.thymeleaf.context.Context();
        ctx.setVariable("isAdmin", isAdmin());
        return Response.ok(templateEngine.process("functions", ctx)).build();
    }

    @GET
    @Path("/ui/functions/new")
    @Produces(MediaType.TEXT_HTML)
    public Response createFunction() {
        org.thymeleaf.context.Context ctx = new org.thymeleaf.context.Context();
        ctx.setVariable("isAdmin", isAdmin());
        return Response.ok(templateEngine.process("createFunction", ctx)).build();
    }

    @GET
    @Path("/ui/functions/{id}")
    @Produces(MediaType.TEXT_HTML)
    public Response functionDetail(@PathParam("id") String id) {
        org.thymeleaf.context.Context ctx = new org.thymeleaf.context.Context();
        ctx.setVariable("functionId", id);
        ctx.setVariable("isAdmin", isAdmin());
        return Response.ok(templateEngine.process("functionDetail", ctx)).build();
    }

    @GET
    @Path("/ui/search")
    @Produces(MediaType.TEXT_HTML)
    public Response search() {
        org.thymeleaf.context.Context ctx = new org.thymeleaf.context.Context();
        ctx.setVariable("isAdmin", isAdmin());
        return Response.ok(templateEngine.process("search", ctx)).build();
    }

    @POST
    @Path("/logout")
    @Produces(MediaType.TEXT_HTML)
    public Response logout() {
        return Response.status(Response.Status.FOUND)
                .header("Location", "/login.html?error=Logged+out")
                .header("Set-Cookie", "llmtoolbox_session=; Max-Age=0; Path=/; HttpOnly; SameSite=Lax")
                .build();
    }

    private boolean isAdmin() {
        return securityContext != null && securityContext.isUserInRole("admin");
    }
}
