package com.sheahorn.llmtoolbox.buildtools.git;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class GitLockServiceTest {

    @Test
    void testRunLockedReturnsValue() throws Exception {
        GitLockService lock = new GitLockService();
        String result = lock.runLocked(() -> "hello");
        assertEquals("hello", result);
    }

    @Test
    void testRunLockedPropagatesException() {
        GitLockService lock = new GitLockService();
        assertThrows(RuntimeException.class, () ->
                lock.runLocked(() -> { throw new RuntimeException("boom"); }));
    }

    @Test
    void testRunLockedSerializesAccess() throws Exception {
        GitLockService lock = new GitLockService();
        int[] counter = {0};

        // Two callables that increment the counter — if not serialized, could race
        lock.runLocked(() -> { counter[0]++; return null; });
        lock.runLocked(() -> { counter[0]++; return null; });

        assertEquals(2, counter[0]);
    }
}
