package com.sheahorn.llmtoolbox.calculators;

public class DeltaERequestDto implements ColorSource {
    public String source_color;
    public String compare_color;
    public String formula; // CIE76, CIE94, CIEDE2000

    @Override
    public String sourceColor() {
        return source_color;
    }
}
