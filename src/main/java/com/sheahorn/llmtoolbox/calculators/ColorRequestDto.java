package com.sheahorn.llmtoolbox.calculators;

public class ColorRequestDto implements ColorSource {
    public String source_color;

    @Override
    public String sourceColor() {
        return source_color;
    }
}
