package com.sheahorn.llmtoolbox.calculators;

public enum MassUnit {
    MG(1e-3),
    G(1.0),
    KG(1e3),
    T(1e6),
    OZ(28.349523125),
    LB(453.59237);

    private final double factor;

    MassUnit(double factor) {
        this.factor = factor;
    }

    public double getFactor() {
        return factor;
    }
}
