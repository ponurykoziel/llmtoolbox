package com.sheahorn.llmtoolbox.calculators;

public enum DataUnit {
    B(1.0),
    KB(1e3),
    KIB(1024.0),
    MB(1e6),
    MIB(1048576.0),
    GB(1e9),
    GIB(1073741824.0),
    TB(1e12),
    TIB(1099511627776.0);

    private final double factor;

    DataUnit(double factor) {
        this.factor = factor;
    }

    public double getFactor() {
        return factor;
    }
}