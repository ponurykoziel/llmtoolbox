package com.sheahorn.llmtoolbox.buildtools;

import com.sheahorn.llmtoolbox.buildtools.mvn.MvnLockService;
import org.junit.jupiter.api.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Behaviour of the shared family lock: modes, the off switch, and forceRelease.
 * Plain JUnit — no CDI, the settings are passed in explicitly.
 */
class BuildLockServiceTest {

    private MvnLockService lock(String mode, boolean enabled) {
        return new MvnLockService(new BuildLockSettings(mode, enabled));
    }

    @Test
    void synchronizeModeSerializesTwoWorkers() throws Exception {
        MvnLockService lock = lock("synchronize", true);
        AtomicInteger concurrent = new AtomicInteger();
        AtomicInteger maxConcurrent = new AtomicInteger();
        CountDownLatch bothDone = new CountDownLatch(2);

        Runnable work = () -> {
            try {
                lock.runLocked(() -> {
                    maxConcurrent.accumulateAndGet(concurrent.incrementAndGet(), Math::max);
                    Thread.sleep(80);
                    concurrent.decrementAndGet();
                    return null;
                });
            } catch (Exception e) {
                fail(e);
            } finally {
                bothDone.countDown();
            }
        };

        Thread a = new Thread(work);
        Thread b = new Thread(work);
        a.start();
        b.start();

        assertTrue(bothDone.await(5, TimeUnit.SECONDS), "both workers should finish");
        assertEquals(1, maxConcurrent.get(), "synchronize mode must not let two calls run at once");
        assertFalse(lock.isLocked());
    }

    @Test
    void rejectModeThrowsWhileHeld() throws Exception {
        MvnLockService lock = lock("reject", true);
        CountDownLatch inside = new CountDownLatch(1);
        CountDownLatch release = new CountDownLatch(1);
        AtomicReference<Throwable> workerError = new AtomicReference<>();

        Thread worker = new Thread(() -> {
            try {
                lock.runLocked(() -> {
                    inside.countDown();
                    release.await(5, TimeUnit.SECONDS);
                    return null;
                });
            } catch (Throwable t) {
                workerError.set(t);
            }
        });
        worker.start();

        assertTrue(inside.await(5, TimeUnit.SECONDS), "worker never entered the lock");
        IllegalStateException busy = assertThrows(IllegalStateException.class,
                () -> lock.runLocked(() -> "nope"));
        assertTrue(busy.getMessage().startsWith("mvn is busy"), busy.getMessage());

        release.countDown();
        worker.join(5000);
        assertNull(workerError.get());
        assertFalse(lock.isLocked());
    }

    @Test
    void disabledSwitchMakesItAPassThrough() throws Exception {
        MvnLockService lock = lock("synchronize", false);
        AtomicInteger concurrent = new AtomicInteger();
        AtomicInteger maxConcurrent = new AtomicInteger();
        CountDownLatch bothDone = new CountDownLatch(2);

        Runnable work = () -> {
            try {
                lock.runLocked(() -> {
                    maxConcurrent.accumulateAndGet(concurrent.incrementAndGet(), Math::max);
                    Thread.sleep(120);
                    concurrent.decrementAndGet();
                    return null;
                });
            } catch (Exception e) {
                fail(e);
            } finally {
                bothDone.countDown();
            }
        };

        Thread a = new Thread(work);
        Thread b = new Thread(work);
        a.start();
        b.start();

        assertTrue(bothDone.await(5, TimeUnit.SECONDS));
        assertEquals(2, maxConcurrent.get(), "locks disabled must let calls run in parallel");
        assertFalse(lock.isEnabled());
        assertEquals("disabled", lock.getMode());
        assertFalse(lock.isLocked());
    }

    @Test
    void disabledSwitchNeverRejects() throws Exception {
        MvnLockService lock = lock("reject", false);
        CountDownLatch inside = new CountDownLatch(1);
        CountDownLatch release = new CountDownLatch(1);

        Thread worker = new Thread(() -> {
            try {
                lock.runLocked(() -> {
                    inside.countDown();
                    release.await(5, TimeUnit.SECONDS);
                    return null;
                });
            } catch (Exception ignored) {
                // not under test here
            }
        });
        worker.start();

        assertTrue(inside.await(5, TimeUnit.SECONDS));
        assertEquals("ok", lock.runLocked(() -> "ok"), "disabled must ignore reject mode");

        release.countDown();
        worker.join(5000);
    }

    @Test
    void forceReleaseClearsStaleState() throws Exception {
        MvnLockService lock = lock("synchronize", true);
        assertNull(lock.forceRelease(), "a free lock has nothing to clear");

        CountDownLatch inside = new CountDownLatch(1);
        CountDownLatch release = new CountDownLatch(1);

        Thread worker = new Thread(() -> {
            try {
                lock.runLocked(() -> {
                    inside.countDown();
                    release.await(5, TimeUnit.SECONDS);
                    return null;
                });
            } catch (Exception ignored) {
                // not under test here
            }
        });
        worker.start();

        assertTrue(inside.await(5, TimeUnit.SECONDS));
        assertTrue(lock.isLocked());
        assertNotNull(lock.forceRelease(), "the held lock should report the operation it cleared");
        assertFalse(lock.isLocked());
        assertTrue(lock.isHeldByWorker(), "the monitor is still owned by the running worker");

        release.countDown();
        worker.join(5000);
        assertFalse(lock.isHeldByWorker());
    }
}
