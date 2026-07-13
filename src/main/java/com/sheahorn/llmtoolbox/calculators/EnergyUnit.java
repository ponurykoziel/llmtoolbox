package com.sheahorn.llmtoolbox.calculators;

public enum EnergyUnit {
    J(1.0),
    KJ(1e3),
    CAL(4.184),
    KCAL(4184.0),
    WH(3600.0),
    KWH(3.6e6);

    private final double factor;

    EnergyUnit(double factor) {
        this.factor = factor;
    }

    public double getFactor() {
        return factor;
    }
}