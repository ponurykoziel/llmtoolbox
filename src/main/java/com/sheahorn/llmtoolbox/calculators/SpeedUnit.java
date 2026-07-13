package com.sheahorn.llmtoolbox.calculators;

public enum SpeedUnit {
    MS(1.0),
    KMH(0.2777777777777778),
    MPH(0.44704),
    KNOT(0.5144444444444445);

    private final double factor;

    SpeedUnit(double factor) {
        this.factor = factor;
    }

    public double getFactor() {
        return factor;
    }
}
