package com.sheahorn.llmtoolbox.buildtools;

import java.time.Instant;
import java.util.concurrent.Callable;
import java.util.concurrent.locks.ReentrantLock;

/**
 * One global lock per build-tool family (mvn, gradle, git, docker, cargo).
 *
 * Why a ReentrantLock and not `synchronized`: isLocked() lets us report honestly whether a
 * worker is still inside a call, which is exactly the difference between "the flag is stale,
 * clearing it fixes things" and "a call is genuinely running, clearing it only hides that".
 *
 * Release is unconditional: the finally below runs even when the action throws or times out.
 * Together with Executor's bounded timeout, a lock can no longer be held forever by a worker.
 *
 * llmtoolbox.build.locks.enabled=false short-circuits everything into a pass-through.
 */
public abstract class AbstractFamilyLockService {

    private final String family;
    private final BuildLockSettings settings;
    private final ReentrantLock lock = new ReentrantLock();

    private volatile boolean locked;
    private volatile Instant since;
    private volatile String operation;

    protected AbstractFamilyLockService(String family) {
        this(family, new BuildLockSettings());
    }

    protected AbstractFamilyLockService(String family, BuildLockSettings settings) {
        this.family = family;
        this.settings = settings;
    }

    public <T> T runLocked(Callable<T> action) throws Exception {
        if (!settings.enabled()) {
            return action.call();
        }

        if (settings.reject()) {
            if (!lock.tryLock()) {
                throw new IllegalStateException(family + " is busy — lock held since " + since
                        + " by " + operation);
            }
            try {
                return markAndRun(action);
            } finally {
                // clear before unlock: the next waiter must not have its state wiped
                clear();
                lock.unlock();
            }
        }

        lock.lock();
        try {
            return markAndRun(action);
        } finally {
            clear();
            lock.unlock();
        }
    }

    private <T> T markAndRun(Callable<T> action) throws Exception {
        locked = true;
        since = Instant.now();
        operation = Thread.currentThread().getName();
        return action.call();
    }

    private void clear() {
        locked = false;
        since = null;
        operation = null;
    }

    /**
     * Drops the visible lock state without touching the monitor. Returns the operation that
     * was cleared, or null when the lock was already free.
     *
     * Cannot release a monitor held by a live worker — that is what isHeldByWorker() is for.
     */
    public String forceRelease() {
        String previous = operation;
        clear();
        return previous;
    }

    public String getFamily() {
        return family;
    }

    public boolean isEnabled() {
        return settings.enabled();
    }

    /** Effective mode: the configured one, or "disabled" when locks are switched off. */
    public String getMode() {
        return settings.enabled() ? settings.mode() : BuildLockSettings.MODE_DISABLED;
    }

    public boolean isLocked() {
        return locked;
    }

    /** True while a worker is actually inside runLocked() — the flag alone can be stale. */
    public boolean isHeldByWorker() {
        return lock.isLocked();
    }

    public Instant getSince() {
        return since;
    }

    public String getOperation() {
        return operation;
    }
}
