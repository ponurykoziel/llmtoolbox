package com.sheahorn.llmtoolbox.calculators;

public class LchColorDto implements ColorDto {
    public final double L, C, h;
    public LchColorDto(double L, double C, double h) {
        this.L = L;
        this.C = C;
        this.h = h;
    }
}
