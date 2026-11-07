package com.sheahorn.llmtoolbox.execution;

public class ExecutionResponse {
    public String command;
    public Integer exitCode;
    public String stdout;
    public String stderr;
    public boolean timedOut;
    public long durationMs;

    /** True when stdout or stderr hit llmtoolbox.command.max-output-bytes (only the tail is kept). */
    public boolean truncated;

    /** Total bytes the process produced across both streams, before truncation. */
    public long outputBytes;
}
