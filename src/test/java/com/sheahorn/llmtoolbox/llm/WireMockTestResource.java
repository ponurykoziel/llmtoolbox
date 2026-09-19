package com.sheahorn.llmtoolbox.llm;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import io.quarkus.test.common.QuarkusTestResourceLifecycleManager;

import java.util.Map;

public class WireMockTestResource implements QuarkusTestResourceLifecycleManager {

    private WireMockServer wireMock;

    @Override
    public Map<String, String> start() {
        wireMock = new WireMockServer(new WireMockConfiguration().dynamicPort());
        wireMock.start();
        return Map.of("wiremock.port", String.valueOf(wireMock.port()));
    }

    @Override
    public void stop() {
        if (wireMock != null) {
            wireMock.stop();
        }
    }
}
