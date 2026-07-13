package com.sheahorn.llmtoolbox.calculators;

public class ContrastRequestDto implements ColorSource {
    public String source_color;
    public String compare_color;

    @Override
    public String sourceColor() {
        return source_color;
    }
}
