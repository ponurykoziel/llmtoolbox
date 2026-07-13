package com.sheahorn.llmtoolbox.execution;

public class ExecutionResponse {
    public String command;
    public Integer exitCode;
    public String stdout;
    public String stderr;
    public boolean timedOut;
    public long durationMs;
}
