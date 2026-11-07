package com.sheahorn.llmtoolbox.execution;

import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Runs a shell command and never blocks past its timeout.
 *
 * Three things this must get right, in this order of nastiness:
 *
 * 1. Drain the pipes while the process runs, not after. A child that writes more than the
 *    kernel pipe buffer (~64 KB) blocks on write, so a post-waitFor() read deadlocks: the
 *    child waits for us to read, we wait for the child to exit. `mvn`, `docker logs` and
 *    friends hit this constantly.
 *
 * 2. On timeout, kill the whole process tree, not just `sh`. `destroyForcibly()` on the
 *    shell leaves the real work (forked JVM, docker client, gradle daemon) alive, holding
 *    the pipes open.
 *
 * 3. Return no matter what. Callers hold build locks in a finally block; if execute() hangs,
 *    that finally never runs and the lock is stuck until the app restarts.
 *
 * Worst-case wall clock is therefore timeout + ~4s (2s to die, 2s to drain), never infinity.
 */
@ApplicationScoped
public class Executor {

    /** How long we wait for the output pumps to reach EOF once the process is done or killed. */
    private static final long DRAIN_GRACE_MS = 2_000;

    /** How long we wait for a killed process to actually disappear. */
    private static final long KILL_GRACE_MS = 2_000;

    private static final int PUMP_BUFFER_BYTES = 8 * 1024;

    @ConfigProperty(name = "llmtoolbox.command.timeout-seconds", defaultValue = "30")
    long timeoutSeconds;

    @ConfigProperty(name = "llmtoolbox.command.max-output-bytes", defaultValue = "1048576")
    long maxOutputBytes;

    public ExecutionResponse execute(String command) {
        Instant start = Instant.now();

        ExecutionResponse response = new ExecutionResponse();
        response.command = command;

        OutputBuffer stdout = new OutputBuffer(maxOutputBytes);
        OutputBuffer stderr = new OutputBuffer(maxOutputBytes);

        Process process = null;
        Thread stdoutPump = null;
        Thread stderrPump = null;

        try {
            process = new ProcessBuilder("sh", "-c", command).start();

            // Nothing feeds stdin. Leaving it open makes anything interactive (cat, less,
            // ssh, a git credential prompt) hang until the timeout.
            closeQuietly(process.getOutputStream());

            stdoutPump = pump(process.getInputStream(), stdout, "llmtoolbox-stdout-pump");
            stderrPump = pump(process.getErrorStream(), stderr, "llmtoolbox-stderr-pump");

            boolean finished = process.waitFor(timeoutSeconds, TimeUnit.SECONDS);

            if (finished) {
                response.exitCode = process.exitValue();
            } else {
                response.timedOut = true;
                response.exitCode = null;
                killTree(process);
            }

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            response.exitCode = null;
            response.stderr = "interrupted: " + e.getMessage();
            if (process != null) {
                killTree(process);
            }
        } catch (Exception e) {
            response.exitCode = null;
            response.stderr = e.getMessage();
        } finally {
            joinQuietly(stdoutPump, DRAIN_GRACE_MS);
            joinQuietly(stderrPump, DRAIN_GRACE_MS);

            if (process != null) {
                // A pump still blocked in read() (survivor holding the pipe) is released by
                // closing the stream — it then returns whatever it already captured.
                closeQuietly(process.getInputStream());
                closeQuietly(process.getErrorStream());
                closeQuietly(process.getOutputStream());
            }

            response.stdout = stdout.text();
            response.stderr = mergeStderr(response.stderr, stderr.text());
            response.truncated = stdout.truncated() || stderr.truncated();
            response.outputBytes = stdout.total() + stderr.total();
            response.durationMs = Duration.between(start, Instant.now()).toMillis();
        }

        return response;
    }

    private String mergeStderr(String executorMessage, String captured) {
        boolean hasMessage = executorMessage != null && !executorMessage.isBlank();
        boolean hasCaptured = captured != null && !captured.isEmpty();

        if (hasMessage && hasCaptured) {
            return executorMessage + "\n" + captured;
        }
        if (hasMessage) {
            return executorMessage;
        }
        return captured == null ? "" : captured;
    }

    private Thread pump(InputStream inputStream, OutputBuffer buffer, String name) {
        Thread thread = new Thread(() -> {
            byte[] chunk = new byte[PUMP_BUFFER_BYTES];
            try {
                int read;
                while ((read = inputStream.read(chunk)) != -1) {
                    buffer.write(chunk, 0, read);
                }
            } catch (IOException e) {
                // Stream closed by us on shutdown, or the writer died. Nothing to salvage.
            }
        }, name);
        thread.setDaemon(true);
        thread.start();
        return thread;
    }

    /**
     * Kills the process and every descendant. The tree is snapshotted first — once the root
     * dies its children are reparented to init and become unreachable.
     */
    private void killTree(Process process) {
        List<ProcessHandle> tree;
        try {
            tree = breadthFirst(process.toHandle());
        } catch (Exception e) {
            tree = List.of();
        }

        Collections.reverse(tree); // deepest first
        for (ProcessHandle handle : tree) {
            try {
                handle.destroyForcibly();
            } catch (Exception ignored) {
                // already gone
            }
        }

        try {
            process.destroyForcibly();
            process.waitFor(KILL_GRACE_MS, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } catch (Exception ignored) {
            // already gone
        }
    }

    private List<ProcessHandle> breadthFirst(ProcessHandle root) {
        List<ProcessHandle> ordered = new ArrayList<>();
        Deque<ProcessHandle> queue = new ArrayDeque<>(root.children().toList());

        while (!queue.isEmpty()) {
            ProcessHandle handle = queue.poll();
            ordered.add(handle);
            queue.addAll(handle.children().toList());
        }

        return ordered;
    }

    private void joinQuietly(Thread thread, long millis) {
        if (thread == null) {
            return;
        }
        try {
            thread.join(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private void closeQuietly(Closeable closeable) {
        if (closeable == null) {
            return;
        }
        try {
            closeable.close();
        } catch (IOException ignored) {
            // nothing useful to do
        }
    }

}
