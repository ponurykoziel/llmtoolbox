package com.sheahorn.llmtoolbox.calculators;

public enum AreaUnit {
    M2(1.0),
    KM2(1e6),
    HA(1e4),
    ACRE(4046.8564224),
    FT2(0.09290304);

    private final double factor;

    AreaUnit(double factor) {
        this.factor = factor;
    }

    public double getFactor() {
        return factor;
    }
}
