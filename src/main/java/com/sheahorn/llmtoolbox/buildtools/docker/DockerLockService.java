package com.sheahorn.llmtoolbox.buildtools.docker;

import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.time.Instant;

@ApplicationScoped
public class DockerLockService {

    @ConfigProperty(name = "llmtoolbox.build.lock-mode", defaultValue = "synchronize")
    String mode;

    private final Object monitor = new Object();
    private volatile boolean locked = false;
    private volatile Instant since = null;
    private volatile String operation = null;

    public <T> T runLocked(java.util.concurrent.Callable<T> action) throws Exception {
        if ("reject".equalsIgnoreCase(mode)) {
            synchronized (monitor) {
                if (locked) {
                    throw new IllegalStateException("docker is busy — lock held since " + since);
                }
                locked = true;
                since = Instant.now();
                operation = Thread.currentThread().getName();
            }
            try {
                return action.call();
            } finally {
                locked = false;
                since = null;
                operation = null;
            }
        }

        // synchronize (default)
        synchronized (monitor) {
            locked = true;
            since = Instant.now();
            operation = Thread.currentThread().getName();
            try {
                return action.call();
            } finally {
                locked = false;
                since = null;
                operation = null;
            }
        }
    }

    public boolean isLocked() { return locked; }
    public Instant getSince() { return since; }
    public String getOperation() { return operation; }
    public String getMode() { return mode; }
}
