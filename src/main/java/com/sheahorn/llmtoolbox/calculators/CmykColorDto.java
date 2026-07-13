package com.sheahorn.llmtoolbox.calculators;

public class CmykColorDto implements ColorDto {
    public final double c, m, y, k;
    public CmykColorDto(double c, double m, double y, double k) {
        this.c = c;
        this.m = m;
        this.y = y;
        this.k = k;
    }
}
