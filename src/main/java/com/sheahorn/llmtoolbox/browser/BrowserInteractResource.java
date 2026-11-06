package com.sheahorn.llmtoolbox.browser;

import com.sheahorn.llmtoolbox.llm.ToolBean;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.openapi.annotations.Operation;

import java.util.Base64;

/**
 * Proxy for browser interaction endpoints on the Python sidecar.
 * Covers navigation, content extraction, screenshots, JS execution, and typing.
 */
@Path("/api/tools/browser/interact")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class BrowserInteractResource extends BrowserResourceSupport implements ToolBean {

    @Inject
    ScreenshotSettingsService screenshotSettings;

    @Operation(
            operationId = "browser_interact_page_navigate_to",
            summary = "Navigate the session's page to a URL, waiting for the page to load."
    )
    @POST
    @Path("/{session_id}/navigate")
    public String navigateTo(@PathParam("session_id") String sessionId, GotoRequest request) throws Exception {
        validateSessionId(sessionId);
        if (request == null || request.url == null || request.url.isBlank()) {
            throw new IllegalArgumentException("url is required");
        }
        return post("/sessions/" + sessionId + "/goto", request);
    }

    @Operation(
            operationId = "browser_interact_page_get_html",
            summary = "Retrieve the full HTML content of the current page."
    )
    @GET
    @Path("/{session_id}/html")
    public String getHtml(@PathParam("session_id") String sessionId) throws Exception {
        validateSessionId(sessionId);
        return get("/sessions/" + sessionId + "/content");
    }

    @Operation(
            operationId = "browser_interact_page_get_html_file",
            summary = "Retrieve the full HTML content of the current page and write it to a file, returning only the path and size."
    )
    @POST
    @Path("/{session_id}/html_file")
    public String getHtmlFile(@PathParam("session_id") String sessionId, HtmlFileRequest request) throws Exception {
        validateSessionId(sessionId);
        if (request == null || request.path == null || request.path.isBlank()) {
            throw new IllegalArgumentException("path is required");
        }

        String contentJson = get("/sessions/" + sessionId + "/content");
        String html = MAPPER.readTree(contentJson).path("html").asText();

        java.nio.file.Path target = resolvePath(request.path);
        java.nio.file.Files.writeString(target, html, java.nio.charset.StandardCharsets.UTF_8);

        return MAPPER.writeValueAsString(java.util.Map.of(
                "path", target.toString(),
                "bytes", html.getBytes(java.nio.charset.StandardCharsets.UTF_8).length
        ));
    }

    @Operation(
            operationId = "browser_interact_page_get_visible_text",
            summary = "Extract visible text from the current page, suitable for LLM consumption."
    )
    @GET
    @Path("/{session_id}/text")
    public String getVisibleText(@PathParam("session_id") String sessionId) throws Exception {
        validateSessionId(sessionId);
        return get("/sessions/" + sessionId + "/text");
    }

    @Operation(
            operationId = "browser_interact_page_click_element",
            summary = "Click an element on the page by CSS selector."
    )
    @POST
    @Path("/{session_id}/click")
    public String clickElement(@PathParam("session_id") String sessionId, ClickRequest request) throws Exception {
        validateSessionId(sessionId);
        if (request == null || request.selector == null || request.selector.isBlank()) {
            throw new IllegalArgumentException("selector is required");
        }
        return post("/sessions/" + sessionId + "/click", request);
    }

    @Operation(
            operationId = "browser_interact_page_capture_screenshot_base64",
            summary = "Capture a screenshot of the current page, returned as a base64-encoded string in the configured format."
    )
    @POST
    @Path("/{session_id}/screenshot_base64")
    public String captureScreenshotBase64(@PathParam("session_id") String sessionId, ScreenshotRequest request) throws Exception {
        validateSessionId(sessionId);
        byte[] png = capturePng(sessionId, request);
        byte[] transformed = screenshotSettings.get().transform(png);
        String b64 = Base64.getEncoder().encodeToString(transformed);
        return MAPPER.writeValueAsString(java.util.Map.of(
                "screenshot_b64", b64,
                "format", screenshotSettings.get().getFormat()
        ));
    }

    @Operation(
            operationId = "browser_interact_page_capture_screenshot_png",
            summary = "Capture a screenshot of the current page, returned as raw image bytes in the configured format."
    )
    @POST
    @Path("/{session_id}/screenshot_png")
    public Response captureScreenshotPng(@PathParam("session_id") String sessionId, ScreenshotRequest request) throws Exception {
        validateSessionId(sessionId);
        byte[] png = capturePng(sessionId, request);
        ScreenshotSettings settings = screenshotSettings.get();
        byte[] transformed = settings.transform(png);
        String mediaType = ScreenshotSettings.FORMAT_JPG.equals(settings.getFormat())
                ? "image/jpeg" : "image/png";
        return Response.ok(transformed).type(mediaType).build();
    }

    @Operation(
            operationId = "browser_interact_page_capture_screenshot_file",
            summary = "Capture a screenshot of the current page and write it to a file in the configured format, returning only the path and size."
    )
    @POST
    @Path("/{session_id}/screenshot_file")
    public String captureScreenshotFile(@PathParam("session_id") String sessionId, ScreenshotFileRequest request) throws Exception {
        validateSessionId(sessionId);
        if (request == null || request.path == null || request.path.isBlank()) {
            throw new IllegalArgumentException("path is required");
        }

        ScreenshotRequest screenshot = new ScreenshotRequest();
        screenshot.full_page = request.full_page;
        byte[] png = capturePng(sessionId, screenshot);
        ScreenshotSettings settings = screenshotSettings.get();
        byte[] transformed = settings.transform(png);

        java.nio.file.Path target = resolvePath(request.path);
        java.nio.file.Files.write(target, transformed);

        return MAPPER.writeValueAsString(java.util.Map.of(
                "path", target.toString(),
                "bytes", transformed.length,
                "format", settings.getFormat()
        ));
    }

    @Operation(
            operationId = "browser_interact_page_execute_javascript",
            summary = "Execute arbitrary JavaScript in the page context and return the result."
    )
    @POST
    @Path("/{session_id}/execute")
    public String executeJavascript(@PathParam("session_id") String sessionId, ExecuteRequest request) throws Exception {
        validateSessionId(sessionId);
        if (request == null || request.script == null || request.script.isBlank()) {
            throw new IllegalArgumentException("script is required");
        }
        return post("/sessions/" + sessionId + "/execute", request);
    }

    @Operation(
            operationId = "browser_interact_page_type_text",
            summary = "Type text into an input field using Playwright's trusted input pipeline (isTrusted=true). Use method='fill' for fast clear+fill, or method='type' for character-by-character typing."
    )
    @POST
    @Path("/{session_id}/type")
    public String typeText(@PathParam("session_id") String sessionId, TypeRequest request) throws Exception {
        validateSessionId(sessionId);
        if (request == null || request.selector == null || request.selector.isBlank()) {
            throw new IllegalArgumentException("selector is required");
        }
        if (request.text == null) {
            throw new IllegalArgumentException("text is required");
        }
        return post("/sessions/" + sessionId + "/type", request);
    }

    @Operation(
            operationId = "browser_session_url_print",
            summary = "Get the current URL of the session's page."
    )
    @GET
    @Path("/{session_id}/url")
    public String urlPrint(@PathParam("session_id") String sessionId) throws Exception {
        validateSessionId(sessionId);
        return get("/sessions/" + sessionId + "/url");
    }

    // ── helpers ──────────────────────────────────────────────

    private void validateSessionId(String sessionId) {
        if (sessionId == null || sessionId.isBlank()) {
            throw new IllegalArgumentException("session_id is required");
        }
    }

    /**
     * Captures a lossless PNG from the Python sidecar (single source of truth
     * for all screenshot tools). Transformation happens in Java via settings.
     */
    private byte[] capturePng(String sessionId, ScreenshotRequest request) throws Exception {
        ScreenshotRequest screenshot = request != null ? request : new ScreenshotRequest();
        return postBytes("/sessions/" + sessionId + "/screenshot_png", screenshot);
    }

    // ── DTOs ─────────────────────────────────────────────────

    public static class GotoRequest {
        public String url;
        public String wait_until;
    }

    public static class ClickRequest {
        public String selector;
        public Integer timeout;
    }

    public static class ExecuteRequest {
        public String script;
    }

    public static class TypeRequest {
        public String selector;
        public String text;
        public String method;
    }

    public static class ScreenshotRequest {
        public Boolean full_page;
    }

    public static class ScreenshotFileRequest {
        public String path;
        public Boolean full_page;
    }

    public static class HtmlFileRequest {
        public String path;
    }
}
