package com.sheahorn.llmtoolbox.browser;

import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.util.Optional;

/**
 * Centralized configuration for the browser sidecar and its Java proxy resources.
 */
@ApplicationScoped
public class BrowserConfig {

    @ConfigProperty(name = "llmtoolbox.browser.enabled", defaultValue = "false")
    boolean enabled;

    @ConfigProperty(name = "llmtoolbox.browser.port", defaultValue = "9020")
    int port;

    @ConfigProperty(name = "llmtoolbox.browser.token")
    Optional<String> token;

    public boolean isEnabled() {
        return enabled;
    }

    public int getPort() {
        return port;
    }

    public String getToken() {
        return token.orElse("");
    }

    /**
     * Returns the base URL of the Python sidecar (http://127.0.0.1:{port}).
     */
    public String baseUrl() {
        return "http://127.0.0.1:" + port;
    }
}
