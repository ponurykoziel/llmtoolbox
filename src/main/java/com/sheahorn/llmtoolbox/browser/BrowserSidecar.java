package com.sheahorn.llmtoolbox.browser;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logging.Logger;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Path;
import java.time.Duration;
import java.util.concurrent.TimeUnit;

/**
 * Manages the headless_browser Python sidecar process lifecycle.
 * <p>
 * On startup (if enabled), launches {@code browser/run.sh} which activates the
 * Python venv and starts the FastAPI server. Polls {@code /openapi.json} until
 * the server responds, then marks the sidecar as ready.
 * </p>
 * <p>
 * On shutdown, destroys the Python process and waits for graceful termination.
 * </p>
 */
@ApplicationScoped
public class BrowserSidecar {

    private static final Logger LOG = Logger.getLogger(BrowserSidecar.class);

    @ConfigProperty(name = "llmtoolbox.browser.enabled", defaultValue = "false")
    boolean enabled;

    @ConfigProperty(name = "llmtoolbox.browser.port", defaultValue = "9020")
    int port;

    @ConfigProperty(name = "llmtoolbox.browser.python.path", defaultValue = "browser")
    String browserDir;

    private Process process;
    private volatile boolean ready = false;

    @PostConstruct
    void start() {
        if (!enabled) {
            LOG.info("Browser sidecar is disabled (llmtoolbox.browser.enabled=false). Skipping startup.");
            return;
        }

        try {
            Path dir = Path.of(browserDir).toAbsolutePath().normalize();
            LOG.infof("Starting browser sidecar from %s on port %d...", dir, port);

            ProcessBuilder pb = new ProcessBuilder("bash", "run.sh")
                    .directory(dir.toFile())
                    .redirectErrorStream(true);

            process = pb.start();

            // Drain stdout in a background thread so the process doesn't block
            Thread drainer = new Thread(() -> {
                try (var reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        LOG.debugf("[browser] %s", line);
                    }
                } catch (Exception e) {
                    LOG.debugf(e, "[browser] stdout drainer error");
                }
            }, "browser-stdout-drainer");
            drainer.setDaemon(true);
            drainer.start();

            // Health check: poll /openapi.json until the server responds
            waitForReady();

            LOG.info("Browser sidecar is ready.");
        } catch (Exception e) {
            LOG.errorf(e, "Failed to start browser sidecar");
            if (process != null) {
                process.destroyForcibly();
            }
        }
    }

    @PreDestroy
    void stop() {
        if (process == null || !process.isAlive()) {
            return;
        }

        LOG.info("Stopping browser sidecar...");
        process.destroy();
        try {
            boolean terminated = process.waitFor(10, TimeUnit.SECONDS);
            if (!terminated) {
                LOG.warn("Browser sidecar did not terminate gracefully, force-killing.");
                process.destroyForcibly();
                process.waitFor(5, TimeUnit.SECONDS);
            }
            LOG.info("Browser sidecar stopped.");
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            process.destroyForcibly();
        }
    }

    private void waitForReady() {
        HttpClient client = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(2))
                .build();

        String healthUrl = "http://127.0.0.1:" + port + "/openapi.json";

        int maxAttempts = 30;
        for (int i = 0; i < maxAttempts; i++) {
            // Check if process died prematurely
            if (process != null && !process.isAlive()) {
                int exitCode = process.exitValue();
                throw new RuntimeException("Browser sidecar process exited with code " + exitCode + " before becoming ready");
            }

            try {
                HttpRequest req = HttpRequest.newBuilder()
                        .uri(URI.create(healthUrl))
                        .timeout(Duration.ofSeconds(2))
                        .GET()
                        .build();

                HttpResponse<String> resp = client.send(req, HttpResponse.BodyHandlers.ofString());
                if (resp.statusCode() == 200) {
                    ready = true;
                    return;
                }
            } catch (Exception e) {
                // Server not ready yet
            }

            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new RuntimeException("Interrupted while waiting for browser sidecar", e);
            }
        }

        throw new RuntimeException("Browser sidecar did not become ready within " + maxAttempts + " seconds");
    }

    /**
     * Returns the base URL of the Python sidecar (http://127.0.0.1:{port}).
     */
    public String baseUrl() {
        return "http://127.0.0.1:" + port;
    }

    /**
     * Returns whether the sidecar is enabled and ready to accept requests.
     */
    public boolean isReady() {
        return enabled && ready;
    }
}
