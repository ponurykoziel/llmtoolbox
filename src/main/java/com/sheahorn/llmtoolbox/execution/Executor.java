package com.sheahorn.llmtoolbox.execution;

import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.TimeUnit;

@ApplicationScoped
public class Executor {

    @ConfigProperty(name = "llmtoolbox.command.timeout-seconds", defaultValue = "30")
    long timeoutSeconds;

    public ExecutionResponse execute(String command) {
        Instant start = Instant.now();

        ExecutionResponse response = new ExecutionResponse();
        response.command = command;

        Process process = null;

        try {
            ProcessBuilder builder = new ProcessBuilder("sh", "-c", command);
            process = builder.start();

            boolean finished = process.waitFor(timeoutSeconds, TimeUnit.SECONDS);

            if (!finished) {
                process.destroyForcibly();
                response.timedOut = true;
                response.exitCode = null;
            } else {
                response.exitCode = process.exitValue();
            }

            response.stdout = read(process.getInputStream());
            response.stderr = read(process.getErrorStream());

        } catch (Exception e) {
            response.exitCode = null;
            response.stdout = "";
            response.stderr = e.getMessage();
            response.timedOut = false;
        } finally {
            response.durationMs = Duration.between(start, Instant.now()).toMillis();
        }

        return response;
    }

    private String read(InputStream inputStream) throws Exception {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        inputStream.transferTo(buffer);
        return buffer.toString(StandardCharsets.UTF_8);
    }
}


