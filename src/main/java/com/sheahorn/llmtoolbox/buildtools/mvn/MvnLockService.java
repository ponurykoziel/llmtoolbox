package com.sheahorn.llmtoolbox.buildtools.mvn;

import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class MvnLockService {

    private final Object monitor = new Object();

    public <T> T runLocked(java.util.concurrent.Callable<T> action) throws Exception {
        synchronized (monitor) {
            return action.call();
        }
    }
}
