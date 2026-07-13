package com.sheahorn.llmtoolbox.calculators;

public class HsvColorDto implements ColorDto {
    public final double h, s, v;
    public HsvColorDto(double h, double s, double v) {
        this.h = h;
        this.s = s;
        this.v = v;
    }
}
