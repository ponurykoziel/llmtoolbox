package com.sheahorn.llmtoolbox.calculators;

public class AdjustBrightnessRequestDto implements ColorSource {
    public String source_color;
    public Double amount;

    @Override
    public String sourceColor() {
        return source_color;
    }
}
