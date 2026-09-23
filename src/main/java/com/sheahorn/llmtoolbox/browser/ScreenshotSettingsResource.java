package com.sheahorn.llmtoolbox.browser;

import com.sheahorn.llmtoolbox.llm.ToolBean;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import org.eclipse.microprofile.openapi.annotations.Operation;

/**
 * Single endpoint to read and update the centralized screenshot settings.
 * All settings (format, compression, maxBytes) are updated atomically.
 */
@Path("/api/tools/browser/screenshot-settings")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class ScreenshotSettingsResource implements ToolBean {

    @Inject
    ScreenshotSettingsService service;

    @Operation(
            operationId = "browser_settings_screenshot_get",
            summary = "Get the current screenshot settings (format, compression, max byte cap)."
    )
    @GET
    public ScreenshotSettings get() {
        return service.get();
    }

    @Operation(
            operationId = "browser_settings_screenshot_set",
            summary = "Update screenshot settings atomically. format: 'jpg' (compression 1-100) or 'png' (compression 0-9, 0=uncompressed). maxBytes: hard byte cap, 0 = no limit."
    )
    @PUT
    public ScreenshotSettings update(SettingsRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("request body is required");
        }
        return service.update(request.format, request.compression, request.maxBytes);
    }

    public static class SettingsRequest {
        public String format;
        public int compression;
        public long maxBytes;
    }
}
