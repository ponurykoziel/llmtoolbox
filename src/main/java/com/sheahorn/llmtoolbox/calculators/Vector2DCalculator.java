package com.sheahorn.llmtoolbox.calculators;

import com.sheahorn.llmtoolbox.calculators.common.DoubleValueCalculator;

public class Vector2DCalculator extends DoubleValueCalculator {

    public Vector2DCalculator(int roundingDigits) {
        super(roundingDigits);
    }

    public double dotProduct(double x1, double y1, double x2, double y2) {
        return round(x1 * x2 + y1 * y2);
    }

    public double crossProduct(double x1, double y1, double x2, double y2) {
        return round(x1 * y2 - y1 * x2);
    }

    public double magnitude(double x, double y) {
        return round(Math.sqrt(x * x + y * y));
    }

    public double[] normalize(double x, double y) {
        double mag = magnitude(x, y);
        if (mag == 0.0) throw new IllegalArgumentException("Zero vector cannot be normalized");
        return new double[]{round(x / mag), round(y / mag)};
    }

    public double angle(double x1, double y1, double x2, double y2) {
        double dot = dotProduct(x1, y1, x2, y2);
        double mag1 = magnitude(x1, y1);
        double mag2 = magnitude(x2, y2);
        if (mag1 == 0.0 || mag2 == 0.0) throw new IllegalArgumentException("Zero vector — angle undefined");
        double cos = dot / (mag1 * mag2);
        cos = Math.max(-1.0, Math.min(1.0, cos));
        return round(Math.acos(cos));
    }

    public double[] add(double x1, double y1, double x2, double y2) {
        return new double[]{round(x1 + x2), round(y1 + y2)};
    }

    public double[] subtract(double x1, double y1, double x2, double y2) {
        return new double[]{round(x1 - x2), round(y1 - y2)};
    }

    public double[] scale(double x, double y, double scalar) {
        return new double[]{round(x * scalar), round(y * scalar)};
    }

    public double distance(double x1, double y1, double x2, double y2) {
        return round(Math.sqrt((x2 - x1) * (x2 - x1) + (y2 - y1) * (y2 - y1)));
    }

    public double[] project(double x1, double y1, double x2, double y2) {
        double dot = dotProduct(x1, y1, x2, y2);
        double mag2Sq = x2 * x2 + y2 * y2;
        if (mag2Sq == 0.0) throw new IllegalArgumentException("Cannot project onto zero vector");
        double factor = dot / mag2Sq;
        return new double[]{round(x2 * factor), round(y2 * factor)};
    }

    public double cosineSimilarity(double x1, double y1, double x2, double y2) {
        double dot = dotProduct(x1, y1, x2, y2);
        double mag1 = magnitude(x1, y1);
        double mag2 = magnitude(x2, y2);
        if (mag1 == 0.0 || mag2 == 0.0) throw new IllegalArgumentException("Zero vector — cosine similarity undefined");
        return round(dot / (mag1 * mag2));
    }
}
