package com.sheahorn.llmtoolbox.calculators;

public class HwbColorDto implements ColorDto {
    public final double h, w, b;
    public HwbColorDto(double h, double w, double b) {
        this.h = h;
        this.w = w;
        this.b = b;
    }
}
