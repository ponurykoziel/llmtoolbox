package com.sheahorn.llmtoolbox.domain;

public class LlmExecuteResponse {
    public String agent;
    public String status;   // success, error, timeout
    public String payload;  // LLM response text with tool annotations
}
