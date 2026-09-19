package com.sheahorn.llmtoolbox.calculators.common;

import com.sheahorn.llmtoolbox.llm.ToolBean;

/**
 * Marker interface for all calculator classes in the {@code com.sheahorn.llmtoolbox.calculators} package.
 * <p>
 * Extends {@link ToolBean} so all calculators are automatically discoverable
 * by the LLM tool dispatcher without additional per-class changes.
 * </p>
 */
public interface Calculator extends ToolBean {
}
