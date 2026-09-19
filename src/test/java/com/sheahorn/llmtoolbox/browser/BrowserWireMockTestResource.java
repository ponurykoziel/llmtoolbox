package com.sheahorn.llmtoolbox.browser;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import io.quarkus.test.common.QuarkusTestResourceLifecycleManager;

import java.util.Map;

/**
 * WireMock test resource that stubs the Python browser sidecar.
 * Overrides browser config to point at the WireMock server.
 */
public class BrowserWireMockTestResource implements QuarkusTestResourceLifecycleManager {

    private WireMockServer wireMock;

    @Override
    public Map<String, String> start() {
        wireMock = new WireMockServer(new WireMockConfiguration().dynamicPort());
        wireMock.start();
        return Map.of(
                "llmtoolbox.browser.enabled", "true",
                "llmtoolbox.browser.port", String.valueOf(wireMock.port()),
                "llmtoolbox.browser.token", ""
        );
    }

    @Override
    public void stop() {
        if (wireMock != null) {
            wireMock.stop();
        }
    }
}
