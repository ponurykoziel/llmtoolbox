package com.sheahorn.llmtoolbox.calculators;

public class OkLchColorDto implements ColorDto {
    public final double L, C, h;
    public OkLchColorDto(double L, double C, double h) {
        this.L = L;
        this.C = C;
        this.h = h;
    }
}
