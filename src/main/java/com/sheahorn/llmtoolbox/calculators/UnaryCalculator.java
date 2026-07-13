package com.sheahorn.llmtoolbox.calculators;

import com.sheahorn.llmtoolbox.calculators.common.DoubleValueCalculator;

public class UnaryCalculator extends DoubleValueCalculator {

    // TODO: Every method rounds the input before operating, then rounds the result.
    // This causes unnecessary precision loss (e.g. sqrt(2.0000000001) truncates to 2.0
    // before computing). Removing input rounding could break code that relies on
    // double-equality after rounding (e.g. zero checks, sign checks). Needs discussion
    // on whether to keep input rounding or switch to output-only rounding.

    public UnaryCalculator(int roundingDigits) {
        super(roundingDigits);
    }

    public double sqrt(double a) {
        double ra = round(a);
        if (ra < 0.0) {
            throw new IllegalArgumentException("Cannot take square root of negative number");
        }
        return round(Math.sqrt(ra));
    }

    public double cbrt(double a) {
        return round(Math.cbrt(round(a)));
    }

    public double log2(double a) {
        double ra = round(a);
        if (ra <= 0.0) {
            throw new IllegalArgumentException("Logarithm undefined for non-positive value");
        }
        return round(Math.log(ra) / Math.log(2));
    }

    public double log10(double a) {
        double ra = round(a);
        if (ra <= 0.0) {
            throw new IllegalArgumentException("Logarithm undefined for non-positive value");
        }
        return round(Math.log10(ra));
    }

    public double ln(double a) {
        double ra = round(a);
        if (ra <= 0.0) {
            throw new IllegalArgumentException("Logarithm undefined for non-positive value");
        }
        return round(Math.log(ra));
    }

    public double sin(double a) {
        return round(Math.sin(round(a)));
    }

    public double cos(double a) {
        return round(Math.cos(round(a)));
    }

    public double tan(double a) {
        return round(Math.tan(round(a)));
    }

    public int sign(double a) {
        return (int) Math.signum(round(a));
    }

    public double abs(double a) {
        return round(Math.abs(round(a)));
    }

    public int magnitude(double a) {
        double ra = round(a);
        if (ra == 0.0) {
            throw new IllegalArgumentException("Magnitude undefined for zero");
        }
        return (int) Math.floor(Math.log10(Math.abs(ra)));
    }

    public double nearestInt(double a) {
        return round(Math.round(round(a)));
    }

    public double ceil(double a) {
        return round(Math.ceil(round(a)));
    }

    public double floor(double a) {
        return round(Math.floor(round(a)));
    }

    public double trunc(double a) {
        double ra = round(a);
        return round(ra >= 0.0 ? Math.floor(ra) : Math.ceil(ra));
    }
}
