package com.sheahorn.llmtoolbox.calculators;

/**
 * Marker interface for DTOs that carry a source_color field.
 * Used by ColorResource to extract the color string polymorphically
 * instead of an instanceof chain over Object.
 */
public interface ColorSource {
    String sourceColor();
}
