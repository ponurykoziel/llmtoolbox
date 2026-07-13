package com.sheahorn.llmtoolbox.calculators;

public class OkLabColorDto implements ColorDto {
    public final double L, a, b;
    public OkLabColorDto(double L, double a, double b) {
        this.L = L;
        this.a = a;
        this.b = b;
    }
}
