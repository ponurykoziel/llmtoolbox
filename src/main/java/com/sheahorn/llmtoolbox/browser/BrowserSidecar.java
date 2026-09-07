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
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.concurrent.TimeUnit;

/**
 * Manages the headless_browser Python sidecar process lifecycle.
 * <p>
 * On startup (if enabled), it ensures the Python venv exists (auto-creating it via
 * {@code browser/setup.sh} when missing), then launches {@code browser/run.sh} which
 * activates the venv and starts the FastAPI server. Polls {@code /openapi.json} until
 * the server responds, then marks the sidecar as ready.
 * </p>
 * <p>
 * On shutdown, destroys the Python process and waits for graceful termination.
 * </p>
 * <p>
 * The sidecar exposes a coarse status string for the dashboard:
 * <ul>
 *   <li>{@code off} — disabled ({@code llmtoolbox.browser.enabled=false})</li>
 *   <li>{@code init} — enabled but no scripts found ({@code browser/server.py} missing)</li>
 *   <li>{@code no venv} — enabled, scripts found, but the venv could not be created / sidecar failed to start</li>
 *   <li>{@code on} — enabled and ready</li>
 * </ul>
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
    private volatile String status = "off";

    @PostConstruct
    void start() {
        if (!enabled) {
            status = "off";
            LOG.info("Browser sidecar is disabled (llmtoolbox.browser.enabled=false). Skipping startup.");
            return;
        }

        Path dir = Path.of(browserDir).toAbsolutePath().normalize();
        Path serverPy = dir.resolve("server.py");
        Path venv = dir.resolve(".venv");

        if (!Files.exists(serverPy)) {
            status = "init";
            LOG.warnf("Browser sidecar is enabled but no scripts found at %s (server.py missing). Status: init.", dir);
            return;
        }

        if (!Files.isDirectory(venv)) {
            LOG.infof("No venv found at %s. Running setup.sh to create it...", venv);
            if (!runSetup(dir)) {
                status = "no venv";
                LOG.errorf("Failed to create browser venv at %s. Status: no venv.", venv);
                return;
            }
        }

        try {
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

            status = "on";
            LOG.info("Browser sidecar is ready.");
        } catch (Exception e) {
            status = "no venv";
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

    /**
     * Runs {@code browser/setup.sh} to create the venv and install dependencies.
     * Returns true on success (exit code 0).
     */
    private boolean runSetup(Path dir) {
        try {
            ProcessBuilder pb = new ProcessBuilder("bash", "setup.sh")
                    .directory(dir.toFile())
                    .redirectErrorStream(true);
            Process p = pb.start();

            try (var reader = new BufferedReader(new InputStreamReader(p.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    LOG.infof("[browser-setup] %s", line);
                }
            }

            int exit = p.waitFor();
            if (exit != 0) {
                LOG.errorf("setup.sh exited with code %d", exit);
                return false;
            }
            return true;
        } catch (Exception e) {
            LOG.errorf(e, "setup.sh failed");
            return false;
        }
    }

    private void waitForReady() {
        HttpClient client = HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_1_1)
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

    /**
     * Returns the coarse status string for the dashboard: {@code off}, {@code init},
     * {@code no venv}, or {@code on}.
     */
    public String status() {
        return status;
    }
}
