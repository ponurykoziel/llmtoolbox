package com.sheahorn.llmtoolbox.calculators;

public enum LengthUnit {
    NM(1e-9),
    UM(1e-6),
    MM(1e-3),
    CM(1e-2),
    M(1.0),
    KM(1e3),
    IN(0.0254),
    FT(0.3048),
    YD(0.9144),
    MI(1609.344);

    private final double factor;

    LengthUnit(double factor) {
        this.factor = factor;
    }

    public double getFactor() {
        return factor;
    }
}