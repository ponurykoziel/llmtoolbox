package com.sheahorn.llmtoolbox.calculators;

public enum VolumeUnit {
    ML(1e-3),
    L(1.0),
    M3(1e3),
    GAL(3.785411784),
    QT(0.946352946),
    PT(0.473176473),
    CUP(0.2365882365),
    FLOZ(0.0295735295625);

    private final double factor;

    VolumeUnit(double factor) {
        this.factor = factor;
    }

    public double getFactor() {
        return factor;
    }
}
