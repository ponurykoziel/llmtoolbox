package com.sheahorn.llmtoolbox.llm;

/**
 * Marker interface for beans that expose tool functions callable by LLM agents.
 * <p>
 * Every resource class that registers {@code @Operation} endpoints intended for
 * tool dispatch must implement this interface. {@code ToolDispatcher} uses it
 * to discover dispatchable beans via CDI.
 * </p>
 */
public interface ToolBean {
}
