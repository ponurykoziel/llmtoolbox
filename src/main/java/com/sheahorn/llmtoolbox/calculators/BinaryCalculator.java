package com.sheahorn.llmtoolbox.calculators;

import com.sheahorn.llmtoolbox.calculators.common.DoubleValueCalculator;

public class BinaryCalculator extends DoubleValueCalculator {

    // TODO: Every method rounds both inputs before operating, then rounds the result.
    // This causes unnecessary precision loss. Removing input rounding could break code
    // that relies on double-equality after rounding (e.g. zero checks in divide,
    // moduloDivision, divisionRemainder). Needs discussion on whether to keep input
    // rounding or switch to output-only rounding.

    public BinaryCalculator(int roundingDigits) {
        super(roundingDigits);
    }

    public double add(double a, double b) {
        return round(round(a) + round(b));
    }

    public double subtract(double a, double b) {
        return round(round(a) - round(b));
    }

    public double multiply(double a, double b) {
        return round(round(a) * round(b));
    }

    public double divide(double a, double b) {
        double rb = round(b);
        if (rb == 0.0) {
            throw new ArithmeticException("Division by zero");
        }
        return round(round(a) / rb);
    }

    public double power(double a, double b) {
        return round(Math.pow(round(a), round(b)));
    }

    public double moduloDivision(double a, double b) {
        double ra = round(a);
        double rb = round(b);
        if (rb == 0.0) {
            throw new ArithmeticException("Division by zero");
        }
        return round(((ra % rb) + rb) % rb);
    }

    public double divisionRemainder(double a, double b) {
        double ra = round(a);
        double rb = round(b);
        if (rb == 0.0) {
            throw new ArithmeticException("Division by zero");
        }
        return round(ra % rb);
    }

    public double roundTo(double a, int digits) {
        double ra = round(a);
        if (digits <= 0) {
            return ra;
        }
        if (Double.isNaN(ra) || Double.isInfinite(ra)) {
            return ra;
        }
        double factor = Math.pow(10, digits);
        // Avoid overflow: if |value| is too large to multiply by factor, return as-is
        if (Math.abs(ra) > Double.MAX_VALUE / factor) {
            return ra;
        }
        return Math.round(ra * factor) / factor;
    }
}
