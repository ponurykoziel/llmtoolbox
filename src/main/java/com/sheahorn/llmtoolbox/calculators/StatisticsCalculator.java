package com.sheahorn.llmtoolbox.calculators;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import com.sheahorn.llmtoolbox.calculators.common.DoubleValueCalculator;

public class StatisticsCalculator extends DoubleValueCalculator {

    public StatisticsCalculator(int roundingDigits) {
        super(roundingDigits);
    }

    private void validateNotEmpty(double[] values) {
        if (values == null || values.length == 0) {
            throw new IllegalArgumentException("values array must not be empty");
        }
    }

    public double sum(double[] values) {
        validateNotEmpty(values);
        double s = 0.0;
        for (double v : values) s += v;
        return round(s);
    }

    public double mean(double[] values) {
        validateNotEmpty(values);
        double s = 0.0;
        for (double v : values) s += v;
        return round(s / values.length);
    }

    public double geometricMean(double[] values) {
        validateNotEmpty(values);
        double product = 1.0;
        for (double v : values) {
            if (v < 0.0) throw new IllegalArgumentException("All values must be non-negative for geometric mean");
            product *= v;
        }
        return round(Math.pow(product, 1.0 / values.length));
    }

    public double harmonicMean(double[] values) {
        validateNotEmpty(values);
        double sumRecip = 0.0;
        for (double v : values) {
            if (v == 0.0) throw new IllegalArgumentException("Values must be non-zero for harmonic mean");
            sumRecip += 1.0 / v;
        }
        return round(values.length / sumRecip);
    }

    public double median(double[] values) {
        validateNotEmpty(values);
        double[] sorted = new double[values.length];
        for (int i = 0; i < values.length; i++) sorted[i] = values[i];
        Arrays.sort(sorted);
        int n = sorted.length;
        if (n % 2 == 1) {
            return round(sorted[n / 2]);
        } else {
            return round((sorted[n / 2 - 1] + sorted[n / 2]) / 2.0);
        }
    }

    public double mode(double[] values) {
        validateNotEmpty(values);
        // Use String.format with the configured rounding precision as keys
        // to avoid Double.equals() fragility with floating-point values.
        String fmt = "%." + roundingDigits + "f";
        Map<String, Integer> counts = new HashMap<>();
        for (double v : values) {
            String key = String.format(fmt, v);
            counts.put(key, counts.getOrDefault(key, 0) + 1);
        }
        String bestKey = String.format(fmt, values[0]);
        int bestCount = 0;
        for (Map.Entry<String, Integer> e : counts.entrySet()) {
            if (e.getValue() > bestCount) {
                bestCount = e.getValue();
                bestKey = e.getKey();
            }
        }
        return round(Double.parseDouble(bestKey));
    }

    public double stddev(double[] values) {
        validateNotEmpty(values);
        double m = mean(values);
        double sumSq = 0.0;
        for (double v : values) sumSq += (v - m) * (v - m);
        return round(Math.sqrt(sumSq / values.length));
    }

    public double variance(double[] values) {
        validateNotEmpty(values);
        double m = mean(values);
        double sumSq = 0.0;
        for (double v : values) sumSq += (v - m) * (v - m);
        return round(sumSq / values.length);
    }

    public double skewness(double[] values) {
        validateNotEmpty(values);
        double m = mean(values);
        double s = stddev(values);
        if (s == 0.0) throw new IllegalArgumentException("Standard deviation is zero, skewness undefined");
        double sumCubed = 0.0;
        for (double v : values) sumCubed += Math.pow((v - m) / s, 3);
        return round(sumCubed / values.length);
    }

    public double kurtosis(double[] values) {
        validateNotEmpty(values);
        double m = mean(values);
        double s = stddev(values);
        if (s == 0.0) throw new IllegalArgumentException("Standard deviation is zero, kurtosis undefined");
        double sumFourth = 0.0;
        for (double v : values) sumFourth += Math.pow((v - m) / s, 4);
        return round(sumFourth / values.length - 3.0); // excess kurtosis
    }

    public double min(double[] values) {
        validateNotEmpty(values);
        double r = values[0];
        for (double v : values) if (v < r) r = v;
        return round(r);
    }

    public double max(double[] values) {
        validateNotEmpty(values);
        double r = values[0];
        for (double v : values) if (v > r) r = v;
        return round(r);
    }

    public double quantile(double[] values, double q) {
        validateNotEmpty(values);
        if (q < 0.0 || q > 1.0) {
            throw new IllegalArgumentException("q must be between 0 and 1");
        }
        double[] sorted = new double[values.length];
        for (int i = 0; i < values.length; i++) sorted[i] = values[i];
        Arrays.sort(sorted);
        double pos = q * (sorted.length - 1);
        int lo = (int) Math.floor(pos);
        int hi = (int) Math.ceil(pos);
        if (lo == hi) {
            return round(sorted[lo]);
        } else {
            double frac = pos - lo;
            return round(sorted[lo] + frac * (sorted[hi] - sorted[lo]));
        }
    }

    public double iqr(double[] values) {
        validateNotEmpty(values);
        return round(quantile(values, 0.75) - quantile(values, 0.25));
    }

    public double zScore(double value, double mean, double stddev) {
        if (stddev == 0.0) throw new IllegalArgumentException("stddev must not be zero");
        return round((value - mean) / stddev);
    }

    public double binomialPmf(int n, double p, int k) {
        if (n < 0 || k < 0 || k > n) throw new IllegalArgumentException("Requires 0 <= k <= n");
        if (p < 0.0 || p > 1.0) throw new IllegalArgumentException("p must be between 0 and 1");
        return round(combine(n, k) * Math.pow(p, k) * Math.pow(1.0 - p, n - k));
    }

    public double poissonPmf(double lambda, int k) {
        if (lambda < 0.0) throw new IllegalArgumentException("lambda must be non-negative");
        if (k < 0) throw new IllegalArgumentException("k must be non-negative");
        return round(Math.pow(lambda, k) * Math.exp(-lambda) / factorial(k));
    }

    public double[] linearRegression(double[] x, double[] y) {
        if (x.length != y.length) throw new IllegalArgumentException("x and y must have the same length");
        if (x.length < 2) throw new IllegalArgumentException("At least 2 points required");

        double sumX = 0.0, sumY = 0.0, sumXY = 0.0, sumXX = 0.0;
        int n = x.length;
        for (int i = 0; i < n; i++) {
            sumX += x[i];
            sumY += y[i];
            sumXY += x[i] * y[i];
            sumXX += x[i] * x[i];
        }

        double denom = n * sumXX - sumX * sumX;
        if (denom == 0.0) throw new IllegalArgumentException("All x values are identical, regression undefined");

        double slope = (n * sumXY - sumX * sumY) / denom;
        double intercept = (sumY - slope * sumX) / n;

        return new double[]{round(slope), round(intercept)};
    }

    public double clamp(double value, double low, double high) {
        return round(Math.max(low, Math.min(high, value)));
    }

    public double factorial(double a) {
        if (a < 0.0 || a != Math.floor(a)) {
            throw new IllegalArgumentException("a must be a non-negative integer");
        }
        int n = (int) Math.round(a);
        if (n > 170) {
            throw new IllegalArgumentException("a too large (max 170 to avoid overflow)");
        }
        double f = 1.0;
        for (int i = 2; i <= n; i++) f *= i;
        return round(f);
    }

    public double gcd(double a, double b) {
        if (a != Math.floor(a) || b != Math.floor(b)) {
            throw new IllegalArgumentException("Both a and b must be integers");
        }
        long x = Math.abs(Math.round(a));
        long y = Math.abs(Math.round(b));
        return round(gcdLong(x, y));
    }

    public double lcm(double a, double b) {
        if (a != Math.floor(a) || b != Math.floor(b)) {
            throw new IllegalArgumentException("Both a and b must be integers");
        }
        long x = Math.abs(Math.round(a));
        long y = Math.abs(Math.round(b));
        if (x == 0 || y == 0) {
            return 0.0;
        }
        long g = gcdLong(x, y);
        // Avoid long overflow: compute as double
        return round((double) x / (double) g * (double) y);
    }

    public double permute(double a, double b) {
        if (a != Math.floor(a) || b != Math.floor(b)) {
            throw new IllegalArgumentException("Both a and b must be integers");
        }
        int n = (int) Math.round(a);
        int k = (int) Math.round(b);
        if (n < 0 || k < 0 || k > n) {
            throw new IllegalArgumentException("Requires 0 <= k <= n");
        }
        if (n > 170) {
            throw new IllegalArgumentException("n too large (max 170)");
        }
        double p = 1.0;
        for (int i = 0; i < k; i++) p *= (n - i);
        return round(p);
    }

    public double combine(double a, double b) {
        if (a != Math.floor(a) || b != Math.floor(b)) {
            throw new IllegalArgumentException("Both a and b must be integers");
        }
        int n = (int) Math.round(a);
        int k = (int) Math.round(b);
        if (n < 0 || k < 0 || k > n) {
            throw new IllegalArgumentException("Requires 0 <= k <= n");
        }
        if (n > 170) {
            throw new IllegalArgumentException("n too large (max 170)");
        }
        if (k > n - k) k = n - k;
        double c = 1.0;
        for (int i = 0; i < k; i++) {
            c = c * (n - i) / (i + 1);
        }
        return round(c);
    }

    public double aOfBToCPercent(double a, double b) {
        if (b == 0.0) {
            throw new IllegalArgumentException("b must not be zero");
        }
        return round((a / b) * 100.0);
    }

    public double aPercentOfBToC(double a, double b) {
        return round((a / 100.0) * b);
    }

    public double ratio(double a, double b) {
        if (b == 0.0) {
            throw new ArithmeticException("Division by zero");
        }
        return round(a / b);
    }

    public double linearInterpolate(double x, double x1, double y1, double x2, double y2) {
        if (x1 == x2) {
            throw new IllegalArgumentException("x1 and x2 must be distinct");
        }
        return round(y1 + (x - x1) * (y2 - y1) / (x2 - x1));
    }

    public double solveLinearEquation(double a, double b) {
        if (a == 0.0) {
            if (b == 0.0) {
                throw new IllegalArgumentException("Infinite solutions (a=0, b=0)");
            } else {
                throw new IllegalArgumentException("No solution (a=0, b≠0)");
            }
        }
        return round(-b / a);
    }

    public double[] solveQuadricEquation(double a, double b, double c) {
        if (a == 0.0) {
            throw new IllegalArgumentException("a must not be zero (not a quadratic equation)");
        }
        double discriminant = b * b - 4.0 * a * c;
        if (discriminant < 0.0) {
            throw new IllegalArgumentException("No real roots (discriminant < 0)");
        }
        double sqrtD = Math.sqrt(discriminant);
        return new double[] {
            round((-b + sqrtD) / (2.0 * a)),
            round((-b - sqrtD) / (2.0 * a))
        };
    }

    private long gcdLong(long a, long b) {
        while (b != 0) {
            long t = b;
            b = a % b;
            a = t;
        }
        return a;
    }
}
