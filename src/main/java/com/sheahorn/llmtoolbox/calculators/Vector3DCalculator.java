package com.sheahorn.llmtoolbox.calculators;

import com.sheahorn.llmtoolbox.calculators.common.DoubleValueCalculator;

public class Vector3DCalculator extends DoubleValueCalculator {

    public Vector3DCalculator(int roundingDigits) {
        super(roundingDigits);
    }

    public double dotProduct(double x1, double y1, double z1, double x2, double y2, double z2) {
        return round(x1 * x2 + y1 * y2 + z1 * z2);
    }

    public double[] crossProduct(double x1, double y1, double z1, double x2, double y2, double z2) {
        return new double[]{
            round(y1 * z2 - z1 * y2),
            round(z1 * x2 - x1 * z2),
            round(x1 * y2 - y1 * x2)
        };
    }

    public double magnitude(double x, double y, double z) {
        return round(Math.sqrt(x * x + y * y + z * z));
    }

    public double[] normalize(double x, double y, double z) {
        double mag = magnitude(x, y, z);
        if (mag == 0.0) throw new IllegalArgumentException("Zero vector cannot be normalized");
        return new double[]{round(x / mag), round(y / mag), round(z / mag)};
    }

    public double angle(double x1, double y1, double z1, double x2, double y2, double z2) {
        double dot = dotProduct(x1, y1, z1, x2, y2, z2);
        double mag1 = magnitude(x1, y1, z1);
        double mag2 = magnitude(x2, y2, z2);
        if (mag1 == 0.0 || mag2 == 0.0) throw new IllegalArgumentException("Zero vector — angle undefined");
        double cos = dot / (mag1 * mag2);
        cos = Math.max(-1.0, Math.min(1.0, cos));
        return round(Math.acos(cos));
    }

    public double[] add(double x1, double y1, double z1, double x2, double y2, double z2) {
        return new double[]{round(x1 + x2), round(y1 + y2), round(z1 + z2)};
    }

    public double[] subtract(double x1, double y1, double z1, double x2, double y2, double z2) {
        return new double[]{round(x1 - x2), round(y1 - y2), round(z1 - z2)};
    }

    public double[] scale(double x, double y, double z, double scalar) {
        return new double[]{round(x * scalar), round(y * scalar), round(z * scalar)};
    }

    public double distance(double x1, double y1, double z1, double x2, double y2, double z2) {
        double dx = x2 - x1, dy = y2 - y1, dz = z2 - z1;
        return round(Math.sqrt(dx * dx + dy * dy + dz * dz));
    }

    public double[] project(double x1, double y1, double z1, double x2, double y2, double z2) {
        double dot = dotProduct(x1, y1, z1, x2, y2, z2);
        double mag2Sq = x2 * x2 + y2 * y2 + z2 * z2;
        if (mag2Sq == 0.0) throw new IllegalArgumentException("Cannot project onto zero vector");
        double factor = dot / mag2Sq;
        return new double[]{round(x2 * factor), round(y2 * factor), round(z2 * factor)};
    }

    public double scalarTripleProduct(double x1, double y1, double z1,
                                       double x2, double y2, double z2,
                                       double x3, double y3, double z3) {
        double[] cross = crossProduct(x2, y2, z2, x3, y3, z3);
        return round(x1 * cross[0] + y1 * cross[1] + z1 * cross[2]);
    }

    public double[] vectorTripleProduct(double x1, double y1, double z1,
                                         double x2, double y2, double z2,
                                         double x3, double y3, double z3) {
        double[] cross = crossProduct(x2, y2, z2, x3, y3, z3);
        return crossProduct(x1, y1, z1, cross[0], cross[1], cross[2]);
    }

    public double cosineSimilarity(double x1, double y1, double z1, double x2, double y2, double z2) {
        double dot = dotProduct(x1, y1, z1, x2, y2, z2);
        double mag1 = magnitude(x1, y1, z1);
        double mag2 = magnitude(x2, y2, z2);
        if (mag1 == 0.0 || mag2 == 0.0) throw new IllegalArgumentException("Zero vector — cosine similarity undefined");
        return round(dot / (mag1 * mag2));
    }
}
