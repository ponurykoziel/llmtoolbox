package com.sheahorn.llmtoolbox.browser;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sheahorn.llmtoolbox.fstools.info.FsResourceSupport;
import jakarta.inject.Inject;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;

/**
 * Base class for browser proxy resources that forward requests to the Python sidecar.
 */
public abstract class BrowserResourceSupport extends FsResourceSupport {

    protected static final ObjectMapper MAPPER = new ObjectMapper();
    protected static final HttpClient HTTP = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(5))
            .version(HttpClient.Version.HTTP_1_1)
            .build();

    @Inject
    BrowserConfig config;

    @Inject
    BrowserSidecar sidecar;

    protected void requireReady() {
        if (!sidecar.isReady()) {
            throw new IllegalStateException("Browser sidecar is not ready. Ensure llmtoolbox.browser.enabled=true and the Python process is running.");
        }
    }

    protected String baseUrl() {
        return config.baseUrl();
    }

    protected String token() {
        return config.getToken();
    }

    protected HttpRequest.Builder auth(HttpRequest.Builder builder) {
        String t = token();
        if (t != null && !t.isBlank() && !"change-me-to-a-random-secret".equals(t)) {
            builder.header("Authorization", "Bearer " + t);
        }
        return builder;
    }

    protected String get(String path) throws Exception {
        requireReady();
        HttpRequest req = auth(HttpRequest.newBuilder()
                .uri(URI.create(baseUrl() + path))
                .timeout(Duration.ofSeconds(30))
                .GET())
                .build();
        HttpResponse<String> resp = HTTP.send(req, HttpResponse.BodyHandlers.ofString());
        if (resp.statusCode() >= 400) {
            throw new RuntimeException("Browser sidecar error (" + resp.statusCode() + "): " + resp.body());
        }
        return resp.body();
    }

    protected String post(String path, Object body) throws Exception {
        requireReady();
        String json = MAPPER.writeValueAsString(body);
        HttpRequest req = auth(HttpRequest.newBuilder()
                .uri(URI.create(baseUrl() + path))
                .timeout(Duration.ofSeconds(30))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json)))
                .build();
        HttpResponse<String> resp = HTTP.send(req, HttpResponse.BodyHandlers.ofString());
        if (resp.statusCode() >= 400) {
            throw new RuntimeException("Browser sidecar error (" + resp.statusCode() + "): " + resp.body());
        }
        return resp.body();
    }

    protected byte[] postBytes(String path, Object body) throws Exception {
        requireReady();
        String json = MAPPER.writeValueAsString(body);
        HttpRequest req = auth(HttpRequest.newBuilder()
                .uri(URI.create(baseUrl() + path))
                .timeout(Duration.ofSeconds(30))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json)))
                .build();
        HttpResponse<byte[]> resp = HTTP.send(req, HttpResponse.BodyHandlers.ofByteArray());
        if (resp.statusCode() >= 400) {
            throw new RuntimeException("Browser sidecar error (" + resp.statusCode() + "): "
                    + new String(resp.body(), StandardCharsets.UTF_8));
        }
        return resp.body();
    }

    protected String delete(String path) throws Exception {
        requireReady();
        HttpRequest req = auth(HttpRequest.newBuilder()
                .uri(URI.create(baseUrl() + path))
                .timeout(Duration.ofSeconds(30))
                .DELETE())
                .build();
        HttpResponse<String> resp = HTTP.send(req, HttpResponse.BodyHandlers.ofString());
        if (resp.statusCode() >= 400) {
            throw new RuntimeException("Browser sidecar error (" + resp.statusCode() + "): " + resp.body());
        }
        return resp.body();
    }
}
