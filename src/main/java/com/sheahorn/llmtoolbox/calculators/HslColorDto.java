package com.sheahorn.llmtoolbox.calculators;

public class HslColorDto implements ColorDto {
    public final double h, s, l;
    public HslColorDto(double h, double s, double l) {
        this.h = h;
        this.s = s;
        this.l = l;
    }
}
