package com.sheahorn.llmtoolbox.browser;

import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

/**
 * Single endpoint to read and update the centralized screenshot settings.
 * All settings (format, compression, maxBytes) are updated atomically.
 */
@Path("/api/tools/browser/screenshot-settings")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class ScreenshotSettingsResource {

    @Inject
    ScreenshotSettingsService service;

    @GET
    public ScreenshotSettings get() {
        return service.get();
    }

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
