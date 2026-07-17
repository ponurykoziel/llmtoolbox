package com.sheahorn.llmtoolbox.buildtools.git;

import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class GitLockService {

    private final Object monitor = new Object();

    public <T> T runLocked(java.util.concurrent.Callable<T> action) throws Exception {
        synchronized (monitor) {
            return action.call();
        }
    }
}
