package com.sheahorn.llmtoolbox.calculators;

public class LabColorDto implements ColorDto {
    public final double L, a, b;
    public LabColorDto(double L, double a, double b) {
        this.L = L;
        this.a = a;
        this.b = b;
    }
}
