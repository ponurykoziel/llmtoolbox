package com.sheahorn.llmtoolbox.buildtools.git;

import com.sheahorn.llmtoolbox.buildtools.BuildLockSettings;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class GitLockServiceTest {

    private GitLockService lock() {
        return new GitLockService(new BuildLockSettings("synchronize", true));
    }

    @Test
    void testRunLockedReturnsValue() throws Exception {
        GitLockService lock = lock();
        String result = lock.runLocked(() -> "hello");
        assertEquals("hello", result);
    }

    @Test
    void testRunLockedPropagatesException() {
        GitLockService lock = lock();
        assertThrows(RuntimeException.class, () ->
                lock.runLocked(() -> { throw new RuntimeException("boom"); }));
    }

    @Test
    void testRunLockedReleasesOnException() throws Exception {
        GitLockService lock = lock();
        assertThrows(RuntimeException.class, () ->
                lock.runLocked(() -> { throw new RuntimeException("boom"); }));

        assertFalse(lock.isLocked(), "a throwing action must not leave the lock flagged");
        assertFalse(lock.isHeldByWorker());
        // still usable afterwards
        assertEquals("ok", lock.runLocked(() -> "ok"));
    }

    @Test
    void testRunLockedSerializesAccess() throws Exception {
        GitLockService lock = lock();
        int[] counter = {0};

        // Two callables that increment the counter — if not serialized, could race
        lock.runLocked(() -> { counter[0]++; return null; });
        lock.runLocked(() -> { counter[0]++; return null; });

        assertEquals(2, counter[0]);
    }

    @Test
    void testFamilyAndModeSurfaced() {
        GitLockService lock = lock();
        assertEquals("git", lock.getFamily());
        assertTrue(lock.isEnabled());
        assertEquals("synchronize", lock.getMode());
    }
}
