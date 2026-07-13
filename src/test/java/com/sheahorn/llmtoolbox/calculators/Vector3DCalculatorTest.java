package com.sheahorn.llmtoolbox.calculators;

import com.sheahorn.llmtoolbox.calculators.Vector3DCalculator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class Vector3DCalculatorTest {

    private Vector3DCalculator calc;

    @BeforeEach
    void setUp() {
        calc = new Vector3DCalculator(8);
    }

    // ── dotProduct ──────────────────────────────────────────────────────

    @Test
    void dotProduct_normal() {
        // (1,2,3)·(4,5,6) = 4+10+18 = 32
        assertEquals(32.0, calc.dotProduct(1, 2, 3, 4, 5, 6));
    }

    @Test
    void dotProduct_orthogonal() {
        assertEquals(0.0, calc.dotProduct(1, 0, 0, 0, 1, 0));
    }

    @Test
    void dotProduct_parallel() {
        assertEquals(6.0, calc.dotProduct(2, 0, 0, 3, 0, 0));
    }

    @Test
    void dotProduct_negative() {
        // (-1,2,3)·(4,-5,6) = -4 -10 +18 = 4
        assertEquals(4.0, calc.dotProduct(-1, 2, 3, 4, -5, 6));
    }

    @Test
    void dotProduct_zeroVectors() {
        assertEquals(0.0, calc.dotProduct(0, 0, 0, 0, 0, 0));
    }

    @Test
    void dotProduct_allAxes() {
        // (1,2,3)·(1,2,3) = 1+4+9 = 14
        assertEquals(14.0, calc.dotProduct(1, 2, 3, 1, 2, 3));
    }

    // ── crossProduct ───────────────────────────────────────────────────

    @Test
    void crossProduct_normal() {
        // i × j = k: (1,0,0)×(0,1,0) = (0,0,1)
        double[] result = calc.crossProduct(1, 0, 0, 0, 1, 0);
        assertEquals(0.0, result[0]);
        assertEquals(0.0, result[1]);
        assertEquals(1.0, result[2]);
    }

    @Test
    void crossProduct_jki() {
        // j × k = i: (0,1,0)×(0,0,1) = (1,0,0)
        double[] result = calc.crossProduct(0, 1, 0, 0, 0, 1);
        assertEquals(1.0, result[0]);
        assertEquals(0.0, result[1]);
        assertEquals(0.0, result[2]);
    }

    @Test
    void crossProduct_kij() {
        // k × i = j: (0,0,1)×(1,0,0) = (0,1,0)
        double[] result = calc.crossProduct(0, 0, 1, 1, 0, 0);
        assertEquals(0.0, result[0]);
        assertEquals(1.0, result[1]);
        assertEquals(0.0, result[2]);
    }

    @Test
    void crossProduct_parallel() {
        double[] result = calc.crossProduct(2, 0, 0, 3, 0, 0);
        assertEquals(0.0, result[0]);
        assertEquals(0.0, result[1]);
        assertEquals(0.0, result[2]);
    }

    @Test
    void crossProduct_anticommutative() {
        double[] ab = calc.crossProduct(1, 2, 3, 4, 5, 6);
        double[] ba = calc.crossProduct(4, 5, 6, 1, 2, 3);
        assertEquals(-ab[0], ba[0]);
        assertEquals(-ab[1], ba[1]);
        assertEquals(-ab[2], ba[2]);
    }

    @Test
    void crossProduct_zeroVectors() {
        double[] result = calc.crossProduct(0, 0, 0, 0, 0, 0);
        assertEquals(0.0, result[0]);
        assertEquals(0.0, result[1]);
        assertEquals(0.0, result[2]);
    }

    @Test
    void crossProduct_orthogonalToInputs() {
        // result should be orthogonal to both inputs
        double[] result = calc.crossProduct(1, 2, 3, 4, 5, 6);
        double dotA = calc.dotProduct(result[0], result[1], result[2], 1, 2, 3);
        double dotB = calc.dotProduct(result[0], result[1], result[2], 4, 5, 6);
        assertEquals(0.0, dotA, 1e-8);
        assertEquals(0.0, dotB, 1e-8);
    }

    // ── magnitude ──────────────────────────────────────────────────────

    @Test
    void magnitude_normal() {
        assertEquals(5.0, calc.magnitude(3, 4, 0));
    }

    @Test
    void magnitude_zero() {
        assertEquals(0.0, calc.magnitude(0, 0, 0));
    }

    @Test
    void magnitude_negative() {
        assertEquals(5.0, calc.magnitude(-3, -4, 0));
    }

    @Test
    void magnitude_unitAxis() {
        assertEquals(1.0, calc.magnitude(0, 0, 1));
    }

    @Test
    void magnitude_irrational() {
        // √3 ≈ 1.73205081
        assertEquals(1.73205081, calc.magnitude(1, 1, 1), 1e-8);
    }

    @Test
    void magnitude_allAxes() {
        // √(1+4+9) = √14 ≈ 3.74165739
        assertEquals(3.74165739, calc.magnitude(1, 2, 3), 1e-8);
    }

    // ── normalize ──────────────────────────────────────────────────────

    @Test
    void normalize_normal() {
        double[] result = calc.normalize(3, 4, 0);
        assertEquals(0.6, result[0]);
        assertEquals(0.8, result[1]);
        assertEquals(0.0, result[2]);
    }

    @Test
    void normalize_unitVector() {
        double[] result = calc.normalize(0, 0, 1);
        assertEquals(0.0, result[0]);
        assertEquals(0.0, result[1]);
        assertEquals(1.0, result[2]);
    }

    @Test
    void normalize_negative() {
        double[] result = calc.normalize(-3, -4, 0);
        assertEquals(-0.6, result[0]);
        assertEquals(-0.8, result[1]);
        assertEquals(0.0, result[2]);
    }

    @Test
    void normalize_irrational() {
        // (1,1,1) → (1/√3, 1/√3, 1/√3) ≈ (0.57735027, 0.57735027, 0.57735027)
        double[] result = calc.normalize(1, 1, 1);
        assertEquals(0.57735027, result[0], 1e-8);
        assertEquals(0.57735027, result[1], 1e-8);
        assertEquals(0.57735027, result[2], 1e-8);
    }

    @Test
    void normalize_zeroVector_throws() {
        assertThrows(IllegalArgumentException.class, () -> calc.normalize(0, 0, 0));
    }

    @Test
    void normalize_resultIsUnit() {
        double[] result = calc.normalize(7, 24, 0);
        double mag = Math.sqrt(result[0] * result[0] + result[1] * result[1] + result[2] * result[2]);
        assertEquals(1.0, mag, 1e-8);
    }

    // ── angle ──────────────────────────────────────────────────────────

    @Test
    void angle_orthogonal() {
        assertEquals(1.57079633, calc.angle(1, 0, 0, 0, 1, 0), 1e-8);
    }

    @Test
    void angle_parallelSame() {
        assertEquals(0.0, calc.angle(1, 0, 0, 2, 0, 0));
    }

    @Test
    void angle_parallelOpposite() {
        assertEquals(3.14159265, calc.angle(1, 0, 0, -1, 0, 0), 1e-8);
    }

    @Test
    void angle_45degrees() {
        // (1,0,0) vs (1,1,0) → π/4 ≈ 0.78539816
        assertEquals(0.78539816, calc.angle(1, 0, 0, 1, 1, 0), 1e-8);
    }

    @Test
    void angle_firstZero_throws() {
        assertThrows(IllegalArgumentException.class, () -> calc.angle(0, 0, 0, 1, 0, 0));
    }

    @Test
    void angle_secondZero_throws() {
        assertThrows(IllegalArgumentException.class, () -> calc.angle(1, 0, 0, 0, 0, 0));
    }

    @Test
    void angle_clamping() {
        // identical vectors → cos=1.0, no NaN from floating-point overshoot
        assertEquals(0.0, calc.angle(1, 0, 0, 1, 0, 0));
    }

    @Test
    void angle_3dDiagonal() {
        // (1,0,0) vs (1,1,1) → cos = 1/√3, angle ≈ 0.95531662
        assertEquals(0.95531662, calc.angle(1, 0, 0, 1, 1, 1), 1e-8);
    }

    // ── add ────────────────────────────────────────────────────────────

    @Test
    void add_normal() {
        double[] result = calc.add(1, 2, 3, 4, 5, 6);
        assertEquals(5.0, result[0]);
        assertEquals(7.0, result[1]);
        assertEquals(9.0, result[2]);
    }

    @Test
    void add_negative() {
        double[] result = calc.add(-1, -2, -3, 4, 5, 6);
        assertEquals(3.0, result[0]);
        assertEquals(3.0, result[1]);
        assertEquals(3.0, result[2]);
    }

    @Test
    void add_zero() {
        double[] result = calc.add(0, 0, 0, 1, 2, 3);
        assertEquals(1.0, result[0]);
        assertEquals(2.0, result[1]);
        assertEquals(3.0, result[2]);
    }

    @Test
    void add_cancel() {
        double[] result = calc.add(1, 2, 3, -1, -2, -3);
        assertEquals(0.0, result[0]);
        assertEquals(0.0, result[1]);
        assertEquals(0.0, result[2]);
    }

    // ── subtract ───────────────────────────────────────────────────────

    @Test
    void subtract_normal() {
        double[] result = calc.subtract(4, 5, 6, 1, 2, 3);
        assertEquals(3.0, result[0]);
        assertEquals(3.0, result[1]);
        assertEquals(3.0, result[2]);
    }

    @Test
    void subtract_zeroResult() {
        double[] result = calc.subtract(1, 2, 3, 1, 2, 3);
        assertEquals(0.0, result[0]);
        assertEquals(0.0, result[1]);
        assertEquals(0.0, result[2]);
    }

    @Test
    void subtract_negativeResult() {
        double[] result = calc.subtract(1, 2, 3, 4, 5, 6);
        assertEquals(-3.0, result[0]);
        assertEquals(-3.0, result[1]);
        assertEquals(-3.0, result[2]);
    }

    // ── scale ──────────────────────────────────────────────────────────

    @Test
    void scale_normal() {
        double[] result = calc.scale(1, 2, 3, 3);
        assertEquals(3.0, result[0]);
        assertEquals(6.0, result[1]);
        assertEquals(9.0, result[2]);
    }

    @Test
    void scale_zeroScalar() {
        double[] result = calc.scale(1, 2, 3, 0);
        assertEquals(0.0, result[0]);
        assertEquals(0.0, result[1]);
        assertEquals(0.0, result[2]);
    }

    @Test
    void scale_negativeScalar() {
        double[] result = calc.scale(1, 2, 3, -1);
        assertEquals(-1.0, result[0]);
        assertEquals(-2.0, result[1]);
        assertEquals(-3.0, result[2]);
    }

    @Test
    void scale_fractional() {
        double[] result = calc.scale(1, 2, 3, 0.5);
        assertEquals(0.5, result[0]);
        assertEquals(1.0, result[1]);
        assertEquals(1.5, result[2]);
    }

    // ── distance ───────────────────────────────────────────────────────

    @Test
    void distance_normal() {
        assertEquals(5.0, calc.distance(0, 0, 0, 3, 4, 0));
    }

    @Test
    void distance_samePoint() {
        assertEquals(0.0, calc.distance(1, 2, 3, 1, 2, 3));
    }

    @Test
    void distance_irrational() {
        // √3 ≈ 1.73205081
        assertEquals(1.73205081, calc.distance(0, 0, 0, 1, 1, 1), 1e-8);
    }

    @Test
    void distance_reverseOrder() {
        assertEquals(5.0, calc.distance(3, 4, 0, 0, 0, 0));
    }

    @Test
    void distance_allAxes() {
        // (0,0,0)→(1,2,3) = √14 ≈ 3.74165739
        assertEquals(3.74165739, calc.distance(0, 0, 0, 1, 2, 3), 1e-8);
    }

    // ── project ────────────────────────────────────────────────────────

    @Test
    void project_normal() {
        // (3,4,0) onto (1,0,0): dot=3, mag2Sq=1, factor=3 → (3,0,0)
        double[] result = calc.project(3, 4, 0, 1, 0, 0);
        assertEquals(3.0, result[0]);
        assertEquals(0.0, result[1]);
        assertEquals(0.0, result[2]);
    }

    @Test
    void project_orthogonal() {
        // (0,1,0) onto (1,0,0): dot=0 → (0,0,0)
        double[] result = calc.project(0, 1, 0, 1, 0, 0);
        assertEquals(0.0, result[0]);
        assertEquals(0.0, result[1]);
        assertEquals(0.0, result[2]);
    }

    @Test
    void project_ontoZero_throws() {
        assertThrows(IllegalArgumentException.class, () -> calc.project(1, 0, 0, 0, 0, 0));
    }

    @Test
    void project_ontoItself() {
        double[] result = calc.project(3, 4, 5, 3, 4, 5);
        assertEquals(3.0, result[0]);
        assertEquals(4.0, result[1]);
        assertEquals(5.0, result[2]);
    }

    @Test
    void project_negativeOnto() {
        // (3,4,0) onto (-1,0,0): dot=-3, mag2Sq=1, factor=-3 → (3,0,0)
        double[] result = calc.project(3, 4, 0, -1, 0, 0);
        assertEquals(3.0, result[0]);
        assertEquals(0.0, result[1]);
        assertEquals(0.0, result[2]);
    }

    // ── scalarTripleProduct ────────────────────────────────────────────

    @Test
    void scalarTripleProduct_normal() {
        // a=(1,0,0), b=(0,1,0), c=(0,0,1): a·(b×c) = a·i = 1
        assertEquals(1.0, calc.scalarTripleProduct(1, 0, 0, 0, 1, 0, 0, 0, 1));
    }

    @Test
    void scalarTripleProduct_coplanar() {
        // a=(1,1,0), b=(1,0,0), c=(0,1,0): all in xy-plane → 0
        assertEquals(0.0, calc.scalarTripleProduct(1, 1, 0, 1, 0, 0, 0, 1, 0));
    }

    @Test
    void scalarTripleProduct_negative() {
        // swap b and c → sign flips
        double pos = calc.scalarTripleProduct(1, 0, 0, 0, 1, 0, 0, 0, 1);
        double neg = calc.scalarTripleProduct(1, 0, 0, 0, 0, 1, 0, 1, 0);
        assertEquals(-pos, neg);
    }

    @Test
    void scalarTripleProduct_cyclicPermutation() {
        // a·(b×c) = b·(c×a) = c·(a×b)
        double v1 = calc.scalarTripleProduct(1, 0, 0, 0, 1, 0, 0, 0, 1);
        double v2 = calc.scalarTripleProduct(0, 1, 0, 0, 0, 1, 1, 0, 0);
        double v3 = calc.scalarTripleProduct(0, 0, 1, 1, 0, 0, 0, 1, 0);
        assertEquals(v1, v2);
        assertEquals(v1, v3);
    }

    @Test
    void scalarTripleProduct_zeroVector() {
        // a=(0,0,0) → 0
        assertEquals(0.0, calc.scalarTripleProduct(0, 0, 0, 1, 0, 0, 0, 1, 0));
    }

    // ── vectorTripleProduct ────────────────────────────────────────────

    @Test
    void vectorTripleProduct_normal() {
        // a=(1,0,0), b=(0,1,0), c=(0,0,1): a×(b×c) = a×i = 0 (since a∥i)
        double[] result = calc.vectorTripleProduct(1, 0, 0, 0, 1, 0, 0, 0, 1);
        assertEquals(0.0, result[0]);
        assertEquals(0.0, result[1]);
        assertEquals(0.0, result[2]);
    }

    @Test
    void vectorTripleProduct_identity() {
        // a×(b×c) = b(a·c) - c(a·b)
        // a=(1,2,3), b=(4,5,6), c=(7,8,9)
        double[] result = calc.vectorTripleProduct(1, 2, 3, 4, 5, 6, 7, 8, 9);
        double aDotC = calc.dotProduct(1, 2, 3, 7, 8, 9);
        double aDotB = calc.dotProduct(1, 2, 3, 4, 5, 6);
        // b(a·c) - c(a·b)
        double[] expected = calc.subtract(
                4 * aDotC, 5 * aDotC, 6 * aDotC,
                7 * aDotB, 8 * aDotB, 9 * aDotB);
        assertEquals(expected[0], result[0], 1e-8);
        assertEquals(expected[1], result[1], 1e-8);
        assertEquals(expected[2], result[2], 1e-8);
    }

    @Test
    void vectorTripleProduct_parallelBC() {
        // b∥c → b×c=0 → a×0=0
        double[] result = calc.vectorTripleProduct(1, 2, 3, 1, 0, 0, 2, 0, 0);
        assertEquals(0.0, result[0]);
        assertEquals(0.0, result[1]);
        assertEquals(0.0, result[2]);
    }

    @Test
    void vectorTripleProduct_nonZero() {
        // a=(0,1,0), b=(1,0,0), c=(0,0,1): b×c = (0,-1,0), a×(b×c) = (0,1,0)×(0,-1,0) = (0,0,0)
        // Let's use a more interesting case: a=(1,0,0), b=(0,1,0), c=(1,0,0)
        // b×c = (0,1,0)×(1,0,0) = (0,0,-1), a×(b×c) = (1,0,0)×(0,0,-1) = (0,1,0)
        double[] result = calc.vectorTripleProduct(1, 0, 0, 0, 1, 0, 1, 0, 0);
        assertEquals(0.0, result[0], 1e-8);
        assertEquals(1.0, result[1], 1e-8);
        assertEquals(0.0, result[2], 1e-8);
    }

    // ── cosineSimilarity ───────────────────────────────────────────────

    @Test
    void cosineSimilarity_sameDirection() {
        assertEquals(1.0, calc.cosineSimilarity(1, 0, 0, 2, 0, 0));
    }

    @Test
    void cosineSimilarity_orthogonal() {
        assertEquals(0.0, calc.cosineSimilarity(1, 0, 0, 0, 1, 0));
    }

    @Test
    void cosineSimilarity_opposite() {
        assertEquals(-1.0, calc.cosineSimilarity(1, 0, 0, -1, 0, 0));
    }

    @Test
    void cosineSimilarity_45degrees() {
        // 1/√2 ≈ 0.70710678
        assertEquals(0.70710678, calc.cosineSimilarity(1, 0, 0, 1, 1, 0), 1e-8);
    }

    @Test
    void cosineSimilarity_zeroVector_throws() {
        assertThrows(IllegalArgumentException.class, () -> calc.cosineSimilarity(0, 0, 0, 1, 0, 0));
    }

    @Test
    void cosineSimilarity_secondZero_throws() {
        assertThrows(IllegalArgumentException.class, () -> calc.cosineSimilarity(1, 0, 0, 0, 0, 0));
    }

    @Test
    void cosineSimilarity_3dDiagonal() {
        // (1,0,0) vs (1,1,1): cos = 1/√3 ≈ 0.57735027
        assertEquals(0.57735027, calc.cosineSimilarity(1, 0, 0, 1, 1, 1), 1e-8);
    }

    // ── floating-point edge cases: angle ────────────────────────────────

    @Test
    void angle_nearParallel() {
        // (1, 1e-4, 0) vs (1, 0, 0): angle should be very small (~1e-4 rad)
        double result = calc.angle(1, 1e-4, 0, 1, 0, 0);
        assertTrue(result > 0.0 && result < 1e-3, "near-parallel angle should be tiny");
    }

    @Test
    void angle_nearOpposite() {
        // (1, 0, 0) vs (-1, 1e-4, 0): angle should be near π
        double result = calc.angle(1, 0, 0, -1, 1e-4, 0);
        assertTrue(result > 3.14 && result <= 3.14159265, "near-opposite angle should be near π");
    }

    @Test
    void angle_nearOrthogonal() {
        // (1, 0, 0) vs (1e-4, 1, 0): angle should be near π/2
        double result = calc.angle(1, 0, 0, 1e-4, 1, 0);
        assertTrue(result > 1.57 && result < 1.571, "near-orthogonal angle should be near π/2");
    }

    @Test
    void angle_veryLargeVectors() {
        // (1e4, 0, 0) vs (0, 1e4, 0): orthogonal, angle = π/2
        double result = calc.angle(1e4, 0, 0, 0, 1e4, 0);
        assertEquals(1.57079633, result, 1e-8);
    }

    @Test
    void angle_verySmallVectors() {
        // (1e-4, 0, 0) vs (0, 1e-4, 0): orthogonal, angle = π/2
        double result = calc.angle(1e-4, 0, 0, 0, 1e-4, 0);
        assertEquals(1.57079633, result, 1e-8);
    }

    @Test
    void angle_identicalVerySmall() {
        // (1e-4, 1e-4, 1e-4) vs (2e-4, 2e-4, 2e-4): parallel, angle ≈ 0
        // Rounding cascades through dotProduct, magnitude, and acos
        double result = calc.angle(1e-4, 1e-4, 1e-4, 2e-4, 2e-4, 2e-4);
        assertTrue(result < 0.01, "parallel vectors should have near-zero angle");
    }

    // ── floating-point edge cases: cosineSimilarity ─────────────────────

    @Test
    void cosineSimilarity_nearParallel() {
        // (1, 1e-4, 0) vs (1, 0, 0): cos ≈ 0.999999995
        double result = calc.cosineSimilarity(1, 1e-4, 0, 1, 0, 0);
        assertTrue(result > 0.999999 && result <= 1.0);
    }

    @Test
    void cosineSimilarity_nearOpposite() {
        // (1, 0, 0) vs (-1, 1e-4, 0): cos ≈ -0.999999995
        double result = calc.cosineSimilarity(1, 0, 0, -1, 1e-4, 0);
        assertTrue(result < -0.999999 && result >= -1.0);
    }

    @Test
    void cosineSimilarity_nearOrthogonal() {
        // (1, 0, 0) vs (1e-4, 1, 0): cos ≈ 1e-4
        double result = calc.cosineSimilarity(1, 0, 0, 1e-4, 1, 0);
        assertTrue(Math.abs(result) < 1e-3);
    }

    @Test
    void cosineSimilarity_veryLargeVectors() {
        // (1e4, 0, 0) vs (1e4, 1e4, 0): cos = 1/√2 ≈ 0.70710678
        double result = calc.cosineSimilarity(1e4, 0, 0, 1e4, 1e4, 0);
        assertEquals(0.70710678, result, 1e-8);
    }

    @Test
    void cosineSimilarity_verySmallVectors() {
        // (1e-4, 0, 0) vs (1e-4, 1e-4, 0): cos = 1/√2 ≈ 0.70710678
        // Double rounding (dotProduct + magnitude + division) causes small deviations
        double result = calc.cosineSimilarity(1e-4, 0, 0, 1e-4, 1e-4, 0);
        assertEquals(0.70710678, result, 1e-5);
    }

    @Test
    void cosineSimilarity_orthogonalLarge() {
        // (1e4, 0, 0) vs (0, 1e4, 0): orthogonal, cos = 0
        double result = calc.cosineSimilarity(1e4, 0, 0, 0, 1e4, 0);
        assertEquals(0.0, result, 1e-8);
    }

    // ── floating-point edge cases: magnitude ────────────────────────────

    @Test
    void magnitude_largeButSafe() {
        // √(1e4² + 1e4² + 1e4²) = 1e4 * √3 ≈ 17320.508075689
        double result = calc.magnitude(1e4, 1e4, 1e4);
        assertEquals(17320.50807569, result, 1e-8);
    }

    @Test
    void magnitude_smallButSafe() {
        // √(1e-4² + 1e-4² + 1e-4²) = 1e-4 * √3 ≈ 0.00017320508
        double result = calc.magnitude(1e-4, 1e-4, 1e-4);
        assertEquals(0.00017321, result, 1e-8);
    }

    // ── floating-point edge cases: normalize ────────────────────────────

    @Test
    void normalize_smallButSafe() {
        // (1e-4, 1e-4, 1e-4): magnitude ≈ 1.732e-4, normalized ≈ (0.57735, 0.57735, 0.57735)
        // Double rounding (magnitude then division) causes deviations up to ~2e-5
        double[] result = calc.normalize(1e-4, 1e-4, 1e-4);
        assertEquals(0.57735027, result[0], 2e-5);
        assertEquals(0.57735027, result[1], 2e-5);
        assertEquals(0.57735027, result[2], 2e-5);
    }

    @Test
    void normalize_largeButSafe() {
        // (1e4, 1e4, 1e4): magnitude ≈ 1.732e4, normalized ≈ (0.57735, 0.57735, 0.57735)
        double[] result = calc.normalize(1e4, 1e4, 1e4);
        assertEquals(0.57735027, result[0], 1e-8);
        assertEquals(0.57735027, result[1], 1e-8);
        assertEquals(0.57735027, result[2], 1e-8);
    }

    // ── floating-point edge cases: dotProduct ───────────────────────────

    @Test
    void dotProduct_largeButSafe() {
        // 1e4 * 1e4 = 1e8, well within safe range
        double result = calc.dotProduct(1e4, 0, 0, 1e4, 0, 0);
        assertEquals(1e8, result, 1e-8);
    }

    @Test
    void dotProduct_smallButSafe() {
        // 1e-4 * 1e-4 = 1e-8
        double result = calc.dotProduct(1e-4, 0, 0, 1e-4, 0, 0);
        assertEquals(1e-8, result, 1e-8);
    }

    // ── floating-point edge cases: crossProduct ─────────────────────────

    @Test
    void crossProduct_largeButSafe() {
        // (1e4, 0, 0) × (0, 1e4, 0) = (0, 0, 1e8)
        double[] result = calc.crossProduct(1e4, 0, 0, 0, 1e4, 0);
        assertEquals(0.0, result[0], 1e-8);
        assertEquals(0.0, result[1], 1e-8);
        assertEquals(1e8, result[2], 1e-8);
    }

    @Test
    void crossProduct_smallButSafe() {
        // (1e-4, 0, 0) × (0, 1e-4, 0) = (0, 0, 1e-8)
        double[] result = calc.crossProduct(1e-4, 0, 0, 0, 1e-4, 0);
        assertEquals(0.0, result[0], 1e-8);
        assertEquals(0.0, result[1], 1e-8);
        assertEquals(1e-8, result[2], 1e-8);
    }

    // ── rounding precision ─────────────────────────────────────────────

    @Test
    void rounding_8digits() {
        double result = calc.magnitude(1.0 / 3.0, 0, 0);
        assertEquals(0.33333333, result);
    }
}
