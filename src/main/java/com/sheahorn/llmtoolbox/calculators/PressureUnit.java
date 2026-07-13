package com.sheahorn.llmtoolbox.calculators;

public enum PressureUnit {
    PA(1.0),
    KPA(1e3),
    BAR(1e5),
    ATM(101325.0),
    PSI(6894.757293168361);

    private final double factor;

    PressureUnit(double factor) {
        this.factor = factor;
    }

    public double getFactor() {
        return factor;
    }
}
