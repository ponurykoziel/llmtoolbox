package com.sheahorn.llmtoolbox.calculators;

public class HistogramAdjustmentDto implements ColorSource {
    public String source_color;
    public Double blackDelta;
    public Double whiteDelta;
    // NOTE: This field is named "grayDelta" for backward compatibility, but it
    // represents a gamma exponent (power-law curve), not a gray-level offset.
    public Double grayDelta;

    @Override
    public String sourceColor() {
        return source_color;
    }
}
