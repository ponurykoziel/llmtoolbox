package com.sheahorn.llmtoolbox.execution;

import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.QuarkusTestProfile;
import io.quarkus.test.junit.TestProfile;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * The contract that matters: execute() always returns, never deadlocks on a full pipe, kills
 * the whole process tree on timeout, and bounds captured output.
 *
 * Profile keeps the timeout at 2s so the timeout tests stay fast.
 */
@QuarkusTest
@TestProfile(ExecutorTest.FastProfile.class)
class ExecutorTest {

    public static class FastProfile implements QuarkusTestProfile {
        @Override
        public Map<String, String> getConfigOverrides() {
            return Map.of(
                    "llmtoolbox.command.timeout-seconds", "2",
                    "llmtoolbox.command.max-output-bytes", "4096"
            );
        }
    }

    @Inject
    Executor executor;

    @Test
    void capturesStdoutAndExitCode() {
        ExecutionResponse response = executor.execute("echo hello");

        assertEquals(0, (int) response.exitCode);
        assertEquals("hello\n", response.stdout);
        assertFalse(response.timedOut);
        assertFalse(response.truncated);
    }

    @Test
    void capturesStderrAndNonZeroExit() {
        ExecutionResponse response = executor.execute("echo oops >&2; exit 3");

        assertEquals(3, (int) response.exitCode);
        assertTrue(response.stderr.contains("oops"), response.stderr);
        assertFalse(response.timedOut);
    }

    @Test
    void closesStdinSoInteractiveCommandsDoNotHang() {
        // `cat` with an open stdin would block until the timeout. With stdin closed it sees
        // EOF immediately.
        ExecutionResponse response = executor.execute("cat");

        assertFalse(response.timedOut, "stdin should be closed, cat must not wait for input");
        assertEquals(0, (int) response.exitCode);
        assertTrue(response.stdout.isEmpty());
        assertTrue(response.durationMs < 1500, "took " + response.durationMs + "ms");
    }

    @Test
    void largeOutputDoesNotDeadlock() {
        // ~200 KB is well past the kernel pipe buffer: reading after waitFor() deadlocks here.
        ExecutionResponse response = executor.execute("head -c 200000 /dev/zero | tr '\\0' 'a'");

        assertFalse(response.timedOut, "large output must not deadlock the reader");
        assertEquals(0, (int) response.exitCode);
        assertTrue(response.outputBytes >= 200_000, "captured total was " + response.outputBytes);
        assertTrue(response.durationMs < 2000, "took " + response.durationMs + "ms");
    }

    @Test
    void truncatesToTheTailAndFlagsIt() {
        ExecutionResponse response = executor.execute("head -c 200000 /dev/zero | tr '\\0' 'a'");

        assertTrue(response.truncated);
        assertTrue(response.stdout.length() <= 4096 + 200,
                "kept " + response.stdout.length() + " chars, cap was 4096");
        assertTrue(response.stdout.startsWith("[... output truncated"), "marker missing");
    }

    @Test
    void timeoutReturnsInsteadOfBlocking() {
        long start = System.currentTimeMillis();
        ExecutionResponse response = executor.execute("sleep 60");
        long elapsed = System.currentTimeMillis() - start;

        assertTrue(response.timedOut);
        assertNull(response.exitCode);
        assertTrue(elapsed < 8000, "execute() must return shortly after the timeout, took " + elapsed + "ms");
    }

    @Test
    void timeoutKillsTheWholeProcessTree() {
        ExecutionResponse response = executor.execute("sleep 60 & echo $!; wait");

        assertTrue(response.timedOut);

        String firstLine = response.stdout.trim().split("\\R")[0].trim();
        long pid = Long.parseLong(firstLine);

        assertTrue(waitUntilDead(pid, 3000),
                "grandchild " + pid + " survived the timeout — destroyForcibly only hit sh");
    }

    private boolean waitUntilDead(long pid, long millis) {
        long deadline = System.currentTimeMillis() + millis;
        while (System.currentTimeMillis() < deadline) {
            Optional<ProcessHandle> handle = ProcessHandle.of(pid);
            if (handle.isEmpty() || !handle.get().isAlive()) {
                return true;
            }
            try {
                Thread.sleep(50);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return false;
            }
        }
        return false;
    }
}
