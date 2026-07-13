package com.sheahorn.llmtoolbox.calculators;

import com.sheahorn.llmtoolbox.calculators.Vector2DCalculator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class Vector2DCalculatorTest {

    private Vector2DCalculator calc;

    @BeforeEach
    void setUp() {
        calc = new Vector2DCalculator(8);
    }

    // ── dotProduct ──────────────────────────────────────────────────────

    @Test
    void dotProduct_normal() {
        assertEquals(11.0, calc.dotProduct(1, 2, 3, 4));
    }

    @Test
    void dotProduct_orthogonal() {
        assertEquals(0.0, calc.dotProduct(1, 0, 0, 1));
    }

    @Test
    void dotProduct_parallel() {
        assertEquals(6.0, calc.dotProduct(2, 0, 3, 0));
    }

    @Test
    void dotProduct_negative() {
        assertEquals(-11.0, calc.dotProduct(-1, 2, 3, -4));
    }

    @Test
    void dotProduct_zeroVectors() {
        assertEquals(0.0, calc.dotProduct(0, 0, 0, 0));
    }

    @Test
    void dotProduct_largeValues() {
        // Use values that won't overflow Math.round (Long.MAX_VALUE ≈ 9.22e18)
        // 10_000 * 20_000 = 200_000_000, well within safe range
        assertEquals(200_000_000.0, calc.dotProduct(10_000, 0, 20_000, 0));
    }

    // ── crossProduct ───────────────────────────────────────────────────

    @Test
    void crossProduct_normal() {
        // (1,2) × (3,4) = 1*4 - 2*3 = -2
        assertEquals(-2.0, calc.crossProduct(1, 2, 3, 4));
    }

    @Test
    void crossProduct_parallel() {
        assertEquals(0.0, calc.crossProduct(2, 0, 3, 0));
    }

    @Test
    void crossProduct_orthogonal() {
        // (1,0) × (0,1) = 1*1 - 0*0 = 1
        assertEquals(1.0, calc.crossProduct(1, 0, 0, 1));
    }

    @Test
    void crossProduct_zeroVectors() {
        assertEquals(0.0, calc.crossProduct(0, 0, 0, 0));
    }

    @Test
    void crossProduct_negative() {
        // (-1,2) × (3,-4) = (-1)*(-4) - 2*3 = 4 - 6 = -2
        assertEquals(-2.0, calc.crossProduct(-1, 2, 3, -4));
    }

    @Test
    void crossProduct_anticommutative() {
        // (3,4) × (1,2) = 3*2 - 4*1 = 2, opposite sign of (1,2)×(3,4) = -2
        assertEquals(2.0, calc.crossProduct(3, 4, 1, 2));
    }

    // ── magnitude ──────────────────────────────────────────────────────

    @Test
    void magnitude_normal() {
        assertEquals(5.0, calc.magnitude(3, 4));
    }

    @Test
    void magnitude_zero() {
        assertEquals(0.0, calc.magnitude(0, 0));
    }

    @Test
    void magnitude_negative() {
        assertEquals(5.0, calc.magnitude(-3, -4));
    }

    @Test
    void magnitude_unitX() {
        assertEquals(1.0, calc.magnitude(1, 0));
    }

    @Test
    void magnitude_irrational() {
        // √2 ≈ 1.41421356
        assertEquals(1.41421356, calc.magnitude(1, 1), 1e-8);
    }

    @Test
    void magnitude_large() {
        // √(1e12 + 1e12) = √(2e12) ≈ 1414213.5623731
        assertEquals(1414213.5623731, calc.magnitude(1_000_000, 1_000_000), 1e-8);
    }

    // ── normalize ──────────────────────────────────────────────────────

    @Test
    void normalize_normal() {
        double[] result = calc.normalize(3, 4);
        assertEquals(0.6, result[0]);
        assertEquals(0.8, result[1]);
    }

    @Test
    void normalize_unitVector() {
        double[] result = calc.normalize(1, 0);
        assertEquals(1.0, result[0]);
        assertEquals(0.0, result[1]);
    }

    @Test
    void normalize_negative() {
        double[] result = calc.normalize(-3, -4);
        assertEquals(-0.6, result[0]);
        assertEquals(-0.8, result[1]);
    }

    @Test
    void normalize_irrational() {
        // (1,1) → (1/√2, 1/√2) ≈ (0.70710678, 0.70710678)
        double[] result = calc.normalize(1, 1);
        assertEquals(0.70710678, result[0], 1e-8);
        assertEquals(0.70710678, result[1], 1e-8);
    }

    @Test
    void normalize_zeroVector_throws() {
        assertThrows(IllegalArgumentException.class, () -> calc.normalize(0, 0));
    }

    @Test
    void normalize_resultIsUnit() {
        double[] result = calc.normalize(7, 24);
        double mag = Math.sqrt(result[0] * result[0] + result[1] * result[1]);
        assertEquals(1.0, mag, 1e-8);
    }

    // ── angle ──────────────────────────────────────────────────────────

    @Test
    void angle_orthogonal() {
        // (1,0) vs (0,1) → π/2 ≈ 1.57079633
        assertEquals(1.57079633, calc.angle(1, 0, 0, 1), 1e-8);
    }

    @Test
    void angle_parallelSame() {
        assertEquals(0.0, calc.angle(1, 0, 2, 0));
    }

    @Test
    void angle_parallelOpposite() {
        // (1,0) vs (-1,0) → π ≈ 3.14159265
        assertEquals(3.14159265, calc.angle(1, 0, -1, 0), 1e-8);
    }

    @Test
    void angle_45degrees() {
        // (1,0) vs (1,1) → π/4 ≈ 0.78539816
        assertEquals(0.78539816, calc.angle(1, 0, 1, 1), 1e-8);
    }

    @Test
    void angle_firstZero_throws() {
        assertThrows(IllegalArgumentException.class, () -> calc.angle(0, 0, 1, 0));
    }

    @Test
    void angle_secondZero_throws() {
        assertThrows(IllegalArgumentException.class, () -> calc.angle(1, 0, 0, 0));
    }

    @Test
    void angle_clamping() {
        // Due to floating point, cos might be 1.0000000000000002; clamping prevents NaN
        double result = calc.angle(1, 0, 1, 0);
        assertEquals(0.0, result);
    }

    @Test
    void angle_acute() {
        // (1,0) vs (1,0.1) → small angle
        double result = calc.angle(1, 0, 1, 0.1);
        assertTrue(result > 0.0 && result < 0.1);
    }

    // ── add ────────────────────────────────────────────────────────────

    @Test
    void add_normal() {
        double[] result = calc.add(1, 2, 3, 4);
        assertEquals(4.0, result[0]);
        assertEquals(6.0, result[1]);
    }

    @Test
    void add_negative() {
        double[] result = calc.add(-1, -2, 3, 4);
        assertEquals(2.0, result[0]);
        assertEquals(2.0, result[1]);
    }

    @Test
    void add_zero() {
        double[] result = calc.add(0, 0, 1, 2);
        assertEquals(1.0, result[0]);
        assertEquals(2.0, result[1]);
    }

    @Test
    void add_cancel() {
        double[] result = calc.add(1, 2, -1, -2);
        assertEquals(0.0, result[0]);
        assertEquals(0.0, result[1]);
    }

    @Test
    void add_fractional() {
        double[] result = calc.add(0.5, 0.25, 0.25, 0.75);
        assertEquals(0.75, result[0]);
        assertEquals(1.0, result[1]);
    }

    // ── subtract ───────────────────────────────────────────────────────

    @Test
    void subtract_normal() {
        double[] result = calc.subtract(3, 4, 1, 2);
        assertEquals(2.0, result[0]);
        assertEquals(2.0, result[1]);
    }

    @Test
    void subtract_zeroResult() {
        double[] result = calc.subtract(1, 2, 1, 2);
        assertEquals(0.0, result[0]);
        assertEquals(0.0, result[1]);
    }

    @Test
    void subtract_negativeResult() {
        double[] result = calc.subtract(1, 2, 3, 4);
        assertEquals(-2.0, result[0]);
        assertEquals(-2.0, result[1]);
    }

    @Test
    void subtract_zero() {
        double[] result = calc.subtract(0, 0, 1, 2);
        assertEquals(-1.0, result[0]);
        assertEquals(-2.0, result[1]);
    }

    // ── scale ──────────────────────────────────────────────────────────

    @Test
    void scale_normal() {
        double[] result = calc.scale(1, 2, 3);
        assertEquals(3.0, result[0]);
        assertEquals(6.0, result[1]);
    }

    @Test
    void scale_zeroScalar() {
        double[] result = calc.scale(1, 2, 0);
        assertEquals(0.0, result[0]);
        assertEquals(0.0, result[1]);
    }

    @Test
    void scale_negativeScalar() {
        double[] result = calc.scale(1, 2, -1);
        assertEquals(-1.0, result[0]);
        assertEquals(-2.0, result[1]);
    }

    @Test
    void scale_fractional() {
        double[] result = calc.scale(1, 2, 0.5);
        assertEquals(0.5, result[0]);
        assertEquals(1.0, result[1]);
    }

    @Test
    void scale_largeScalar() {
        double[] result = calc.scale(1, 1, 1_000_000);
        assertEquals(1_000_000.0, result[0]);
        assertEquals(1_000_000.0, result[1]);
    }

    // ── distance ───────────────────────────────────────────────────────

    @Test
    void distance_normal() {
        assertEquals(5.0, calc.distance(0, 0, 3, 4));
    }

    @Test
    void distance_samePoint() {
        assertEquals(0.0, calc.distance(1, 2, 1, 2));
    }

    @Test
    void distance_negative() {
        assertEquals(5.0, calc.distance(-1, -2, -4, -6));
    }

    @Test
    void distance_irrational() {
        assertEquals(1.41421356, calc.distance(0, 0, 1, 1), 1e-8);
    }

    @Test
    void distance_reverseOrder() {
        // distance is symmetric
        assertEquals(5.0, calc.distance(3, 4, 0, 0));
    }

    // ── project ────────────────────────────────────────────────────────

    @Test
    void project_normal() {
        // (3,4) onto (1,0): dot=3, mag2Sq=1, factor=3 → (3,0)
        double[] result = calc.project(3, 4, 1, 0);
        assertEquals(3.0, result[0]);
        assertEquals(0.0, result[1]);
    }

    @Test
    void project_orthogonal() {
        // (0,1) onto (1,0): dot=0 → (0,0)
        double[] result = calc.project(0, 1, 1, 0);
        assertEquals(0.0, result[0]);
        assertEquals(0.0, result[1]);
    }

    @Test
    void project_parallel() {
        // (2,0) onto (1,0): dot=2, mag2Sq=1, factor=2 → (2,0)
        double[] result = calc.project(2, 0, 1, 0);
        assertEquals(2.0, result[0]);
        assertEquals(0.0, result[1]);
    }

    @Test
    void project_ontoZero_throws() {
        assertThrows(IllegalArgumentException.class, () -> calc.project(1, 0, 0, 0));
    }

    @Test
    void project_ontoItself() {
        // (3,4) onto (3,4): dot=25, mag2Sq=25, factor=1 → (3,4)
        double[] result = calc.project(3, 4, 3, 4);
        assertEquals(3.0, result[0]);
        assertEquals(4.0, result[1]);
    }

    @Test
    void project_negativeOnto() {
        // (3,4) onto (-1,0): dot=-3, mag2Sq=1, factor=-3 → (3,0) ... wait
        // dot = 3*(-1) + 4*0 = -3, factor = -3/1 = -3, result = (-1*-3, 0*-3) = (3,0)
        double[] result = calc.project(3, 4, -1, 0);
        assertEquals(3.0, result[0]);
        assertEquals(0.0, result[1]);
    }

    // ── cosineSimilarity ───────────────────────────────────────────────

    @Test
    void cosineSimilarity_sameDirection() {
        assertEquals(1.0, calc.cosineSimilarity(1, 0, 2, 0));
    }

    @Test
    void cosineSimilarity_orthogonal() {
        assertEquals(0.0, calc.cosineSimilarity(1, 0, 0, 1));
    }

    @Test
    void cosineSimilarity_opposite() {
        assertEquals(-1.0, calc.cosineSimilarity(1, 0, -1, 0));
    }

    @Test
    void cosineSimilarity_45degrees() {
        // 1/√2 ≈ 0.70710678
        assertEquals(0.70710678, calc.cosineSimilarity(1, 0, 1, 1), 1e-8);
    }

    @Test
    void cosineSimilarity_zeroVector_throws() {
        assertThrows(IllegalArgumentException.class, () -> calc.cosineSimilarity(0, 0, 1, 0));
    }

    @Test
    void cosineSimilarity_secondZero_throws() {
        assertThrows(IllegalArgumentException.class, () -> calc.cosineSimilarity(1, 0, 0, 0));
    }

    @Test
    void cosineSimilarity_acute() {
        // (1,0) vs (1,0.01) → cos ≈ 0.99995
        double result = calc.cosineSimilarity(1, 0, 1, 0.01);
        assertTrue(result > 0.999 && result < 1.0);
    }

    // ── floating-point edge cases: angle ────────────────────────────────

    @Test
    void angle_nearParallel() {
        // (1, 1e-4) vs (1, 0): angle should be very small (~1e-4 rad)
        double result = calc.angle(1, 1e-4, 1, 0);
        assertTrue(result > 0.0 && result < 1e-3, "near-parallel angle should be tiny");
    }

    @Test
    void angle_nearOpposite() {
        // (1, 0) vs (-1, 1e-4): angle should be near π
        double result = calc.angle(1, 0, -1, 1e-4);
        assertTrue(result > 3.14 && result <= 3.14159265, "near-opposite angle should be near π");
    }

    @Test
    void angle_nearOrthogonal() {
        // (1, 0) vs (1e-4, 1): angle should be near π/2
        double result = calc.angle(1, 0, 1e-4, 1);
        assertTrue(result > 1.57 && result < 1.571, "near-orthogonal angle should be near π/2");
    }

    @Test
    void angle_veryLargeVectors() {
        // (1e4, 0) vs (0, 1e4): orthogonal, angle = π/2
        double result = calc.angle(1e4, 0, 0, 1e4);
        assertEquals(1.57079633, result, 1e-8);
    }

    @Test
    void angle_verySmallVectors() {
        // (1e-4, 0) vs (0, 1e-4): orthogonal, angle = π/2
        double result = calc.angle(1e-4, 0, 0, 1e-4);
        assertEquals(1.57079633, result, 1e-8);
    }

    @Test
    void angle_identicalVerySmall() {
        // (1e-4, 1e-4) vs (2e-4, 2e-4): parallel, angle ≈ 0
        // Rounding cascades through dotProduct, magnitude, and acos
        double result = calc.angle(1e-4, 1e-4, 2e-4, 2e-4);
        assertTrue(result < 0.01, "parallel vectors should have near-zero angle");
    }

    // ── floating-point edge cases: cosineSimilarity ─────────────────────

    @Test
    void cosineSimilarity_nearParallel() {
        // (1, 1e-4) vs (1, 0): cos ≈ 0.999999995
        double result = calc.cosineSimilarity(1, 1e-4, 1, 0);
        assertTrue(result > 0.999999 && result <= 1.0);
    }

    @Test
    void cosineSimilarity_nearOpposite() {
        // (1, 0) vs (-1, 1e-4): cos ≈ -0.999999995
        double result = calc.cosineSimilarity(1, 0, -1, 1e-4);
        assertTrue(result < -0.999999 && result >= -1.0);
    }

    @Test
    void cosineSimilarity_nearOrthogonal() {
        // (1, 0) vs (1e-4, 1): cos ≈ 1e-4
        double result = calc.cosineSimilarity(1, 0, 1e-4, 1);
        assertTrue(Math.abs(result) < 1e-3);
    }

    @Test
    void cosineSimilarity_veryLargeVectors() {
        // (1e4, 0) vs (1e4, 1e4): cos = 1/√2 ≈ 0.70710678
        double result = calc.cosineSimilarity(1e4, 0, 1e4, 1e4);
        assertEquals(0.70710678, result, 1e-8);
    }

    @Test
    void cosineSimilarity_verySmallVectors() {
        // (1e-4, 0) vs (1e-4, 1e-4): cos = 1/√2 ≈ 0.70710678
        // Double rounding (dotProduct + magnitude + division) causes small deviations
        double result = calc.cosineSimilarity(1e-4, 0, 1e-4, 1e-4);
        assertEquals(0.70710678, result, 1e-5);
    }

    @Test
    void cosineSimilarity_orthogonalLarge() {
        // (1e4, 0) vs (0, 1e4): orthogonal, cos = 0
        double result = calc.cosineSimilarity(1e4, 0, 0, 1e4);
        assertEquals(0.0, result, 1e-8);
    }

    // ── floating-point edge cases: magnitude ────────────────────────────

    @Test
    void magnitude_largeButSafe() {
        // √(1e4² + 1e4²) = 1e4 * √2 ≈ 14142.135623731
        double result = calc.magnitude(1e4, 1e4);
        assertEquals(14142.13562373, result, 1e-8);
    }

    @Test
    void magnitude_smallButSafe() {
        // √(1e-4² + 1e-4²) = 1e-4 * √2 ≈ 0.00014142136
        double result = calc.magnitude(1e-4, 1e-4);
        assertEquals(0.00014142, result, 1e-8);
    }

    // ── floating-point edge cases: normalize ────────────────────────────

    @Test
    void normalize_smallButSafe() {
        // (1e-4, 1e-4): magnitude ≈ 1.414e-4, normalized ≈ (0.7071, 0.7071)
        // Rounding cascades: magnitude is rounded first, then division is rounded again
        double[] result = calc.normalize(1e-4, 1e-4);
        assertEquals(0.70710678, result[0], 1e-5);
        assertEquals(0.70710678, result[1], 1e-5);
    }

    @Test
    void normalize_largeButSafe() {
        // (1e4, 1e4): magnitude ≈ 1.414e4, normalized ≈ (0.7071, 0.7071)
        double[] result = calc.normalize(1e4, 1e4);
        assertEquals(0.70710678, result[0], 1e-8);
        assertEquals(0.70710678, result[1], 1e-8);
    }

    // ── floating-point edge cases: dotProduct ───────────────────────────

    @Test
    void dotProduct_largeButSafe() {
        // 1e4 * 1e4 = 1e8, well within safe range
        double result = calc.dotProduct(1e4, 0, 1e4, 0);
        assertEquals(1e8, result, 1e-8);
    }

    @Test
    void dotProduct_smallButSafe() {
        // 1e-4 * 1e-4 = 1e-8
        double result = calc.dotProduct(1e-4, 0, 1e-4, 0);
        assertEquals(1e-8, result, 1e-8);
    }

    // ── rounding precision ─────────────────────────────────────────────

    @Test
    void rounding_8digits() {
        double result = calc.magnitude(1.0 / 3.0, 0);
        // 1/3 ≈ 0.33333333
        assertEquals(0.33333333, result);
    }
}
