package com.sheahorn.llmtoolbox.buildtools.docker;

import jakarta.enterprise.context.ApplicationScoped;

import java.time.Instant;

@ApplicationScoped
public class DockerLockService {

    private final Object monitor = new Object();
    private volatile boolean locked = false;
    private volatile Instant since = null;
    private volatile String operation = null;

    public <T> T runLocked(java.util.concurrent.Callable<T> action) throws Exception {
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
}
