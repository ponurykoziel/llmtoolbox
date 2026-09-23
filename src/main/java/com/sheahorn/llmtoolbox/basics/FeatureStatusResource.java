package com.sheahorn.llmtoolbox.basics;

import com.sheahorn.llmtoolbox.browser.BrowserSidecar;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Exposes the on/off state of optional features (terminal, browser) for the dashboard.
 */
@Path("/api/tools/features")
@Produces(MediaType.APPLICATION_JSON)
public class FeatureStatusResource {

    @ConfigProperty(name = "llmtoolbox.terminal.allow", defaultValue = "false")
    boolean terminalEnabled;

    @ConfigProperty(name = "llmtoolbox.browser.enabled", defaultValue = "false")
    boolean browserEnabled;

    @Inject
    BrowserSidecar browserSidecar;

    @GET
    public Map<String, Object> status() {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("terminal", Map.of("enabled", terminalEnabled));

        Map<String, Object> browser = new LinkedHashMap<>();
        browser.put("enabled", browserEnabled);
        browser.put("status", browserSidecar.status());
        browser.put("venvPresent", browserSidecar.isVenvPresent());
        result.put("browser", browser);
        return result;
    }
}
