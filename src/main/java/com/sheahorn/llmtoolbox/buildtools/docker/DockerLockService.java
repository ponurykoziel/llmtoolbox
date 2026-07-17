package com.sheahorn.llmtoolbox.buildtools.docker;

import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class DockerLockService {

    private final Object monitor = new Object();

    public <T> T runLocked(java.util.concurrent.Callable<T> action) throws Exception {
        synchronized (monitor) {
            return action.call();
        }
    }
}
