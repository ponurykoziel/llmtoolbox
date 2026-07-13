package com.sheahorn.llmtoolbox.calculators.common;

public abstract class DoubleValueCalculator implements Calculator {

    protected final int roundingDigits;

    protected DoubleValueCalculator(int roundingDigits) {
        this.roundingDigits = roundingDigits;
    }

    protected double round(double value) {
        if (roundingDigits < 0) {
            return value;
        }
        if (Double.isNaN(value) || Double.isInfinite(value)) {
            return value;
        }
        double factor = Math.pow(10, roundingDigits);
        // Avoid overflow: Math.round takes a double but returns a long.
        // If |value| * factor exceeds Long.MAX_VALUE, Math.round saturates
        // to Long.MAX_VALUE, producing wrong results. Return as-is instead.
        if (Math.abs(value) > Long.MAX_VALUE / factor) {
            return value;
        }
        return Math.round(value * factor) / factor;
    }
}
