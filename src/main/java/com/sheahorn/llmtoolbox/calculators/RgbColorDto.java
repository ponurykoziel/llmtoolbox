package com.sheahorn.llmtoolbox.calculators;

public class RgbColorDto implements ColorDto {
    public final int r, g, b;
    public RgbColorDto(int r, int g, int b) {
        this.r = r;
        this.g = g;
        this.b = b;
    }
}
