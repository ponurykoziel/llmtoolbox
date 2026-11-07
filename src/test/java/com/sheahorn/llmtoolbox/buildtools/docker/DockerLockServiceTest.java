package com.sheahorn.llmtoolbox.buildtools.docker;

import com.sheahorn.llmtoolbox.buildtools.BuildLockSettings;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class DockerLockServiceTest {

    private DockerLockService lock() {
        return new DockerLockService(new BuildLockSettings("synchronize", true));
    }

    @Test
    void testRunLockedReturnsValue() throws Exception {
        DockerLockService lock = lock();
        String result = lock.runLocked(() -> "hello");
        assertEquals("hello", result);
    }

    @Test
    void testRunLockedPropagatesException() {
        DockerLockService lock = lock();
        assertThrows(RuntimeException.class, () ->
                lock.runLocked(() -> { throw new RuntimeException("boom"); }));
    }

    @Test
    void testRunLockedSerializesAccess() throws Exception {
        DockerLockService lock = lock();
        int[] counter = {0};

        lock.runLocked(() -> { counter[0]++; return null; });
        lock.runLocked(() -> { counter[0]++; return null; });

        assertEquals(2, counter[0]);
    }
}
