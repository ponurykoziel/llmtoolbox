package com.sheahorn.llmtoolbox.buildtools.docker;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class DockerLockServiceTest {

    @Test
    void testRunLockedReturnsValue() throws Exception {
        DockerLockService lock = new DockerLockService();
        String result = lock.runLocked(() -> "hello");
        assertEquals("hello", result);
    }

    @Test
    void testRunLockedPropagatesException() {
        DockerLockService lock = new DockerLockService();
        assertThrows(RuntimeException.class, () ->
                lock.runLocked(() -> { throw new RuntimeException("boom"); }));
    }

    @Test
    void testRunLockedSerializesAccess() throws Exception {
        DockerLockService lock = new DockerLockService();
        int[] counter = {0};

        lock.runLocked(() -> { counter[0]++; return null; });
        lock.runLocked(() -> { counter[0]++; return null; });

        assertEquals(2, counter[0]);
    }
}
