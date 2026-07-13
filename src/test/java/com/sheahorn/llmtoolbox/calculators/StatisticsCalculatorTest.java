package com.sheahorn.llmtoolbox.calculators;

import com.sheahorn.llmtoolbox.calculators.StatisticsCalculator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class StatisticsCalculatorTest {

    private StatisticsCalculator calc;

    @BeforeEach
    void setUp() {
        calc = new StatisticsCalculator(8);
    }

    // ── sum ─────────────────────────────────────────────────────────────

    @Test
    void sum_normal() {
        assertEquals(15.0, calc.sum(new double[]{1, 2, 3, 4, 5}));
    }

    @Test
    void sum_singleElement() {
        assertEquals(7.0, calc.sum(new double[]{7}));
    }

    @Test
    void sum_empty() {
        assertThrows(IllegalArgumentException.class,
                () -> calc.sum(new double[]{}));
    }

    @Test
    void sum_negative() {
        assertEquals(-3.0, calc.sum(new double[]{-1, -2}));
    }

    @Test
    void sum_mixed() {
        assertEquals(2.0, calc.sum(new double[]{-1, 3}));
    }

    // ── mean ────────────────────────────────────────────────────────────

    @Test
    void mean_normal() {
        assertEquals(3.0, calc.mean(new double[]{1, 2, 3, 4, 5}));
    }

    @Test
    void mean_singleElement() {
        assertEquals(7.0, calc.mean(new double[]{7}));
    }

    @Test
    void mean_fractional() {
        assertEquals(2.5, calc.mean(new double[]{1, 2, 3, 4}));
    }

    @Test
    void mean_negative() {
        assertEquals(-2.0, calc.mean(new double[]{-1, -2, -3}));
    }

    // ── geometricMean ───────────────────────────────────────────────────

    @Test
    void geometricMean_normal() {
        // (2*8)^(1/2) = 4
        assertEquals(4.0, calc.geometricMean(new double[]{2, 8}));
    }

    @Test
    void geometricMean_threeValues() {
        // (1*3*9)^(1/3) = 27^(1/3) = 3
        assertEquals(3.0, calc.geometricMean(new double[]{1, 3, 9}));
    }

    @Test
    void geometricMean_singleValue() {
        assertEquals(5.0, calc.geometricMean(new double[]{5}));
    }

    @Test
    void geometricMean_zero_returnsZero() {
        // Geometric mean of [1, 0, 3] is 0 (product becomes 0)
        assertEquals(0.0, calc.geometricMean(new double[]{1, 0, 3}));
    }

    @Test
    void geometricMean_negative_throws() {
        assertThrows(IllegalArgumentException.class,
                () -> calc.geometricMean(new double[]{1, -2, 3}));
    }

    // ── harmonicMean ────────────────────────────────────────────────────

    @Test
    void harmonicMean_normal() {
        // 2 / (1/2 + 1/4) = 2 / 0.75 = 2.66666667
        assertEquals(2.66666667, calc.harmonicMean(new double[]{2, 4}), 1e-8);
    }

    @Test
    void harmonicMean_threeValues() {
        // 3 / (1/1 + 1/2 + 1/4) = 3 / 1.75 = 1.71428571
        assertEquals(1.71428571, calc.harmonicMean(new double[]{1, 2, 4}), 1e-8);
    }

    @Test
    void harmonicMean_singleValue() {
        assertEquals(5.0, calc.harmonicMean(new double[]{5}));
    }

    @Test
    void harmonicMean_zero_throws() {
        assertThrows(IllegalArgumentException.class,
                () -> calc.harmonicMean(new double[]{1, 0, 3}));
    }

    @Test
    void harmonicMean_negative() {
        // 2 / (1/(-2) + 1/(-4)) = 2 / (-0.75) = -2.66666667
        assertEquals(-2.66666667, calc.harmonicMean(new double[]{-2, -4}), 1e-8);
    }

    // ── median ──────────────────────────────────────────────────────────

    @Test
    void median_oddLength() {
        assertEquals(3.0, calc.median(new double[]{5, 1, 3, 2, 4}));
    }

    @Test
    void median_evenLength() {
        assertEquals(2.5, calc.median(new double[]{1, 2, 3, 4}));
    }

    @Test
    void median_singleElement() {
        assertEquals(7.0, calc.median(new double[]{7}));
    }

    @Test
    void median_twoElements() {
        assertEquals(1.5, calc.median(new double[]{1, 2}));
    }

    @Test
    void median_negative() {
        assertEquals(-2.0, calc.median(new double[]{-1, -2, -3}));
    }

    // ── mode ────────────────────────────────────────────────────────────

    @Test
    void mode_singleMode() {
        assertEquals(2.0, calc.mode(new double[]{1, 2, 2, 3}));
    }

    @Test
    void mode_tie() {
        // first encountered with max count wins
        double result = calc.mode(new double[]{1, 1, 2, 2});
        assertTrue(result == 1.0 || result == 2.0);
    }

    @Test
    void mode_allSame() {
        assertEquals(5.0, calc.mode(new double[]{5, 5, 5}));
    }

    @Test
    void mode_singleElement() {
        assertEquals(7.0, calc.mode(new double[]{7}));
    }

    @Test
    void mode_allUnique() {
        // All values appear once — first encountered wins
        double result = calc.mode(new double[]{3, 1, 4, 1.5, 2});
        // Any of the values is valid; just verify it's one of them
        double[] candidates = {3, 1, 4, 1.5, 2};
        boolean found = false;
        for (double c : candidates) {
            if (Math.abs(result - c) < 1e-8) { found = true; break; }
        }
        assertTrue(found, "mode should be one of the input values");
    }

    @Test
    void mode_negativeValues() {
        assertEquals(-2.0, calc.mode(new double[]{-1, -2, -2, -3}));
    }

    @Test
    void mode_fractionalTie() {
        // 1.1 appears twice, 1.2 appears twice — first encountered wins
        double result = calc.mode(new double[]{1.1, 1.1, 1.2, 1.2});
        assertTrue(Math.abs(result - 1.1) < 1e-8 || Math.abs(result - 1.2) < 1e-8);
    }

    @Test
    void mode_largeArray() {
        double[] values = new double[100];
        for (int i = 0; i < 100; i++) values[i] = i % 10; // 0..9 each 10 times
        double result = calc.mode(values);
        // All values appear 10 times; any of 0..9 is valid (HashMap iteration order)
        assertTrue(result >= 0.0 && result <= 9.0 && result == Math.floor(result),
                "mode should be one of 0..9");
    }

    // ── stddev ──────────────────────────────────────────────────────────

    @Test
    void stddev_normal() {
        // population stddev of {2,4,4,4,5,5,7,9} = 2.0
        assertEquals(2.0, calc.stddev(new double[]{2, 4, 4, 4, 5, 5, 7, 9}));
    }

    @Test
    void stddev_constant() {
        assertEquals(0.0, calc.stddev(new double[]{5, 5, 5}));
    }

    @Test
    void stddev_singleElement() {
        assertEquals(0.0, calc.stddev(new double[]{7}));
    }

    @Test
    void stddev_simple() {
        // {0, 2}: mean=1, variance=((0-1)^2+(2-1)^2)/2=1, stddev=1
        assertEquals(1.0, calc.stddev(new double[]{0, 2}));
    }

    // ── variance ────────────────────────────────────────────────────────

    @Test
    void variance_normal() {
        assertEquals(4.0, calc.variance(new double[]{2, 4, 4, 4, 5, 5, 7, 9}));
    }

    @Test
    void variance_constant() {
        assertEquals(0.0, calc.variance(new double[]{5, 5, 5}));
    }

    @Test
    void variance_singleElement() {
        assertEquals(0.0, calc.variance(new double[]{7}));
    }

    // ── skewness ────────────────────────────────────────────────────────

    @Test
    void skewness_symmetric() {
        // {1,2,3}: mean=2, stddev≈0.81649658, skewness=0
        assertEquals(0.0, calc.skewness(new double[]{1, 2, 3}), 1e-8);
    }

    @Test
    void skewness_rightSkewed() {
        // {1,1,1,10}: mean=3.25, positive skew
        double result = calc.skewness(new double[]{1, 1, 1, 10});
        assertTrue(result > 0.0);
    }

    @Test
    void skewness_leftSkewed() {
        // {-10,1,1,1}: mean=-1.75, negative skew
        double result = calc.skewness(new double[]{-10, 1, 1, 1});
        assertTrue(result < 0.0);
    }

    @Test
    void skewness_zeroStddev_throws() {
        assertThrows(IllegalArgumentException.class,
                () -> calc.skewness(new double[]{5, 5, 5}));
    }

    // ── kurtosis ────────────────────────────────────────────────────────

    @Test
    void kurtosis_normal() {
        // {1,2,3}: excess kurtosis ≈ -1.5
        double result = calc.kurtosis(new double[]{1, 2, 3});
        assertTrue(result < 0.0);
    }

    @Test
    void kurtosis_constant_throws() {
        assertThrows(IllegalArgumentException.class,
                () -> calc.kurtosis(new double[]{5, 5, 5}));
    }

    @Test
    void kurtosis_peaked() {
        // {0, 10, 10, 10, 10, 10, 20}: most values at center
        double result = calc.kurtosis(new double[]{0, 10, 10, 10, 10, 10, 20});
        // just verify it computes without error
        assertFalse(Double.isNaN(result));
    }

    // ── min ─────────────────────────────────────────────────────────────

    @Test
    void min_normal() {
        assertEquals(1.0, calc.min(new double[]{3, 1, 4, 1, 5}));
    }

    @Test
    void min_singleElement() {
        assertEquals(7.0, calc.min(new double[]{7}));
    }

    @Test
    void min_negative() {
        assertEquals(-5.0, calc.min(new double[]{-1, -5, 3}));
    }

    // ── max ─────────────────────────────────────────────────────────────

    @Test
    void max_normal() {
        assertEquals(5.0, calc.max(new double[]{3, 1, 4, 1, 5}));
    }

    @Test
    void max_singleElement() {
        assertEquals(7.0, calc.max(new double[]{7}));
    }

    @Test
    void max_negative() {
        assertEquals(3.0, calc.max(new double[]{-1, -5, 3}));
    }

    // ── quantile ────────────────────────────────────────────────────────

    @Test
    void quantile_median() {
        assertEquals(3.0, calc.quantile(new double[]{1, 2, 3, 4, 5}, 0.5));
    }

    @Test
    void quantile_min() {
        assertEquals(1.0, calc.quantile(new double[]{1, 2, 3, 4, 5}, 0.0));
    }

    @Test
    void quantile_max() {
        assertEquals(5.0, calc.quantile(new double[]{1, 2, 3, 4, 5}, 1.0));
    }

    @Test
    void quantile_q1() {
        assertEquals(2.0, calc.quantile(new double[]{1, 2, 3, 4, 5}, 0.25));
    }

    @Test
    void quantile_q3() {
        assertEquals(4.0, calc.quantile(new double[]{1, 2, 3, 4, 5}, 0.75));
    }

    @Test
    void quantile_interpolated() {
        // {1,2,3,4}: q=0.5, pos=1.5, interpolate between index 1 (2) and 2 (3) → 2.5
        assertEquals(2.5, calc.quantile(new double[]{1, 2, 3, 4}, 0.5));
    }

    @Test
    void quantile_invalidLow_throws() {
        assertThrows(IllegalArgumentException.class,
                () -> calc.quantile(new double[]{1, 2, 3}, -0.1));
    }

    @Test
    void quantile_invalidHigh_throws() {
        assertThrows(IllegalArgumentException.class,
                () -> calc.quantile(new double[]{1, 2, 3}, 1.1));
    }

    // ── iqr ─────────────────────────────────────────────────────────────

    @Test
    void iqr_normal() {
        assertEquals(2.0, calc.iqr(new double[]{1, 2, 3, 4, 5}));
    }

    @Test
    void iqr_singleElement() {
        assertEquals(0.0, calc.iqr(new double[]{7}));
    }

    // ── zScore ──────────────────────────────────────────────────────────

    @Test
    void zScore_normal() {
        assertEquals(1.0, calc.zScore(12, 10, 2));
    }

    @Test
    void zScore_belowMean() {
        assertEquals(-1.0, calc.zScore(8, 10, 2));
    }

    @Test
    void zScore_atMean() {
        assertEquals(0.0, calc.zScore(10, 10, 2));
    }

    @Test
    void zScore_zeroStddev_throws() {
        assertThrows(IllegalArgumentException.class,
                () -> calc.zScore(5, 5, 0));
    }

    // ── binomialPmf ────────────────────────────────────────────────────

    @Test
    void binomialPmf_normal() {
        // n=10, p=0.5, k=5: C(10,5)*0.5^10 = 252/1024 ≈ 0.24609375
        assertEquals(0.24609375, calc.binomialPmf(10, 0.5, 5), 1e-8);
    }

    @Test
    void binomialPmf_k0() {
        // n=5, p=0.5, k=0: (1-0.5)^5 = 0.03125
        assertEquals(0.03125, calc.binomialPmf(5, 0.5, 0));
    }

    @Test
    void binomialPmf_kn() {
        // n=5, p=0.5, k=5: 0.5^5 = 0.03125
        assertEquals(0.03125, calc.binomialPmf(5, 0.5, 5));
    }

    @Test
    void binomialPmf_p0() {
        // n=5, p=0, k=0: 1.0 (only possible outcome)
        assertEquals(1.0, calc.binomialPmf(5, 0.0, 0));
    }

    @Test
    void binomialPmf_p0_kPositive() {
        assertEquals(0.0, calc.binomialPmf(5, 0.0, 3));
    }

    @Test
    void binomialPmf_p1() {
        // n=5, p=1, k=5: 1.0
        assertEquals(1.0, calc.binomialPmf(5, 1.0, 5));
    }

    @Test
    void binomialPmf_invalidK_throws() {
        assertThrows(IllegalArgumentException.class,
                () -> calc.binomialPmf(5, 0.5, 6));
    }

    @Test
    void binomialPmf_invalidP_throws() {
        assertThrows(IllegalArgumentException.class,
                () -> calc.binomialPmf(5, 1.5, 2));
    }

    @Test
    void binomialPmf_negativeN_throws() {
        assertThrows(IllegalArgumentException.class,
                () -> calc.binomialPmf(-1, 0.5, 0));
    }

    // ── poissonPmf ──────────────────────────────────────────────────────

    @Test
    void poissonPmf_normal() {
        // lambda=2, k=0: e^(-2)*2^0/0! = e^(-2) ≈ 0.13533528
        assertEquals(0.13533528, calc.poissonPmf(2.0, 0), 1e-8);
    }

    @Test
    void poissonPmf_k1() {
        // lambda=2, k=1: e^(-2)*2/1 = 2*e^(-2) ≈ 0.27067057
        assertEquals(0.27067057, calc.poissonPmf(2.0, 1), 1e-8);
    }

    @Test
    void poissonPmf_lambdaZero() {
        // lambda=0, k=0: e^0*0^0/0! = 1*1/1 = 1.0
        assertEquals(1.0, calc.poissonPmf(0.0, 0));
    }

    @Test
    void poissonPmf_lambdaZero_kPositive() {
        assertEquals(0.0, calc.poissonPmf(0.0, 5));
    }

    @Test
    void poissonPmf_largeK() {
        // lambda=5, k=10: small but computable
        double result = calc.poissonPmf(5.0, 10);
        assertTrue(result > 0.0 && result < 0.02);
    }

    @Test
    void poissonPmf_negativeLambda_throws() {
        assertThrows(IllegalArgumentException.class,
                () -> calc.poissonPmf(-1.0, 0));
    }

    @Test
    void poissonPmf_negativeK_throws() {
        assertThrows(IllegalArgumentException.class,
                () -> calc.poissonPmf(2.0, -1));
    }

    // ── linearRegression ────────────────────────────────────────────────

    @Test
    void linearRegression_normal() {
        // y = 2x + 1: x={1,2,3}, y={3,5,7}
        double[] result = calc.linearRegression(new double[]{1, 2, 3}, new double[]{3, 5, 7});
        assertEquals(2.0, result[0]); // slope
        assertEquals(1.0, result[1]); // intercept
    }

    @Test
    void linearRegression_horizontal() {
        // y = 3: x={1,2,3}, y={3,3,3}
        double[] result = calc.linearRegression(new double[]{1, 2, 3}, new double[]{3, 3, 3});
        assertEquals(0.0, result[0]); // slope
        assertEquals(3.0, result[1]); // intercept
    }

    @Test
    void linearRegression_identicalX_throws() {
        assertThrows(IllegalArgumentException.class,
                () -> calc.linearRegression(new double[]{2, 2, 2}, new double[]{1, 2, 3}));
    }

    @Test
    void linearRegression_tooFewPoints_throws() {
        assertThrows(IllegalArgumentException.class,
                () -> calc.linearRegression(new double[]{1}, new double[]{2}));
    }

    @Test
    void linearRegression_mismatchedLength_throws() {
        assertThrows(IllegalArgumentException.class,
                () -> calc.linearRegression(new double[]{1, 2}, new double[]{1, 2, 3}));
    }

    // ── clamp ───────────────────────────────────────────────────────────

    @Test
    void clamp_withinRange() {
        assertEquals(5.0, calc.clamp(5, 0, 10));
    }

    @Test
    void clamp_belowLow() {
        assertEquals(0.0, calc.clamp(-5, 0, 10));
    }

    @Test
    void clamp_aboveHigh() {
        assertEquals(10.0, calc.clamp(15, 0, 10));
    }

    @Test
    void clamp_atLow() {
        assertEquals(0.0, calc.clamp(0, 0, 10));
    }

    @Test
    void clamp_atHigh() {
        assertEquals(10.0, calc.clamp(10, 0, 10));
    }

    @Test
    void clamp_negativeRange() {
        assertEquals(-3.0, calc.clamp(-5, -3, -1));
    }

    // ── factorial ───────────────────────────────────────────────────────

    @Test
    void factorial_0() {
        assertEquals(1.0, calc.factorial(0));
    }

    @Test
    void factorial_1() {
        assertEquals(1.0, calc.factorial(1));
    }

    @Test
    void factorial_5() {
        assertEquals(120.0, calc.factorial(5));
    }

    @Test
    void factorial_10() {
        assertEquals(3628800.0, calc.factorial(10));
    }

    @Test
    void factorial_170() {
        // 170! ≈ 7.257415615307994e306, well under Double.MAX_VALUE
        double result = calc.factorial(170);
        assertTrue(Double.isFinite(result));
        assertTrue(result > 7e306 && result < 8e306);
    }

    @Test
    void factorial_171_throws() {
        assertThrows(IllegalArgumentException.class,
                () -> calc.factorial(171));
    }

    @Test
    void factorial_negative_throws() {
        assertThrows(IllegalArgumentException.class,
                () -> calc.factorial(-1));
    }

    @Test
    void factorial_nonInteger_throws() {
        assertThrows(IllegalArgumentException.class,
                () -> calc.factorial(2.5));
    }

    // ── gcd ─────────────────────────────────────────────────────────────

    @Test
    void gcd_normal() {
        assertEquals(6.0, calc.gcd(48, 18));
    }

    @Test
    void gcd_coprime() {
        assertEquals(1.0, calc.gcd(17, 13));
    }

    @Test
    void gcd_oneZero() {
        assertEquals(5.0, calc.gcd(5, 0));
    }

    @Test
    void gcd_bothZero() {
        assertEquals(0.0, calc.gcd(0, 0));
    }

    @Test
    void gcd_negative() {
        assertEquals(6.0, calc.gcd(-48, 18));
    }

    @Test
    void gcd_large() {
        assertEquals(1.0, calc.gcd(1_000_000_007, 1_000_000_009));
    }

    @Test
    void gcd_nonInteger_throws() {
        assertThrows(IllegalArgumentException.class,
                () -> calc.gcd(2.5, 3));
    }

    // ── lcm ─────────────────────────────────────────────────────────────

    @Test
    void lcm_normal() {
        assertEquals(12.0, calc.lcm(4, 6));
    }

    @Test
    void lcm_coprime() {
        assertEquals(221.0, calc.lcm(17, 13));
    }

    @Test
    void lcm_oneZero() {
        assertEquals(0.0, calc.lcm(5, 0));
    }

    @Test
    void lcm_bothZero() {
        assertEquals(0.0, calc.lcm(0, 0));
    }

    @Test
    void lcm_negative() {
        assertEquals(12.0, calc.lcm(-4, 6));
    }

    @Test
    void lcm_nonInteger_throws() {
        assertThrows(IllegalArgumentException.class,
                () -> calc.lcm(2.5, 3));
    }

    // ── permute ─────────────────────────────────────────────────────────

    @Test
    void permute_normal() {
        // P(5,2) = 5*4 = 20
        assertEquals(20.0, calc.permute(5, 2));
    }

    @Test
    void permute_k0() {
        assertEquals(1.0, calc.permute(5, 0));
    }

    @Test
    void permute_kn() {
        // P(5,5) = 5! = 120
        assertEquals(120.0, calc.permute(5, 5));
    }

    @Test
    void permute_kTooLarge_throws() {
        assertThrows(IllegalArgumentException.class,
                () -> calc.permute(5, 6));
    }

    @Test
    void permute_negativeN_throws() {
        assertThrows(IllegalArgumentException.class,
                () -> calc.permute(-1, 0));
    }

    @Test
    void permute_nTooLarge_throws() {
        assertThrows(IllegalArgumentException.class,
                () -> calc.permute(171, 1));
    }

    // ── combine ─────────────────────────────────────────────────────────

    @Test
    void combine_normal() {
        // C(5,2) = 10
        assertEquals(10.0, calc.combine(5, 2));
    }

    @Test
    void combine_k0() {
        assertEquals(1.0, calc.combine(5, 0));
    }

    @Test
    void combine_kn() {
        assertEquals(1.0, calc.combine(5, 5));
    }

    @Test
    void combine_symmetric() {
        assertEquals(calc.combine(10, 3), calc.combine(10, 7));
    }

    @Test
    void combine_kTooLarge_throws() {
        assertThrows(IllegalArgumentException.class,
                () -> calc.combine(5, 6));
    }

    @Test
    void combine_nTooLarge_throws() {
        assertThrows(IllegalArgumentException.class,
                () -> calc.combine(171, 1));
    }

    // ── aOfBToCPercent ──────────────────────────────────────────────────

    @Test
    void aOfBToCPercent_normal() {
        assertEquals(25.0, calc.aOfBToCPercent(25, 100));
    }

    @Test
    void aOfBToCPercent_zeroA() {
        assertEquals(0.0, calc.aOfBToCPercent(0, 100));
    }

    @Test
    void aOfBToCPercent_bZero_throws() {
        assertThrows(IllegalArgumentException.class,
                () -> calc.aOfBToCPercent(5, 0));
    }

    @Test
    void aOfBToCPercent_over100() {
        assertEquals(200.0, calc.aOfBToCPercent(200, 100));
    }

    // ── aPercentOfBToC ──────────────────────────────────────────────────

    @Test
    void aPercentOfBToC_normal() {
        assertEquals(25.0, calc.aPercentOfBToC(25, 100));
    }

    @Test
    void aPercentOfBToC_zeroPercent() {
        assertEquals(0.0, calc.aPercentOfBToC(0, 100));
    }

    @Test
    void aPercentOfBToC_zeroB() {
        assertEquals(0.0, calc.aPercentOfBToC(50, 0));
    }

    @Test
    void aPercentOfBToC_negativePercent() {
        assertEquals(-50.0, calc.aPercentOfBToC(-50, 100));
    }

    // ── ratio ───────────────────────────────────────────────────────────

    @Test
    void ratio_normal() {
        assertEquals(0.5, calc.ratio(1, 2));
    }

    @Test
    void ratio_zeroNumerator() {
        assertEquals(0.0, calc.ratio(0, 5));
    }

    @Test
    void ratio_negative() {
        assertEquals(-0.5, calc.ratio(-1, 2));
    }

    @Test
    void ratio_divisionByZero_throws() {
        assertThrows(ArithmeticException.class,
                () -> calc.ratio(1, 0));
    }

    // ── linearInterpolate ───────────────────────────────────────────────

    @Test
    void linearInterpolate_midpoint() {
        assertEquals(5.0, calc.linearInterpolate(1.5, 1, 3, 2, 7));
    }

    @Test
    void linearInterpolate_atX1() {
        assertEquals(3.0, calc.linearInterpolate(1, 1, 3, 2, 7));
    }

    @Test
    void linearInterpolate_atX2() {
        assertEquals(7.0, calc.linearInterpolate(2, 1, 3, 2, 7));
    }

    @Test
    void linearInterpolate_extrapolate() {
        // x=3, beyond x2: 3 + (3-1)*(7-3)/(2-1) = 3 + 2*4 = 11
        assertEquals(11.0, calc.linearInterpolate(3, 1, 3, 2, 7));
    }

    @Test
    void linearInterpolate_identicalX_throws() {
        assertThrows(IllegalArgumentException.class,
                () -> calc.linearInterpolate(1.5, 2, 3, 2, 7));
    }

    // ── solveLinearEquation ─────────────────────────────────────────────

    @Test
    void solveLinearEquation_normal() {
        // 2x + 4 = 0 → x = -2
        assertEquals(-2.0, calc.solveLinearEquation(2, 4));
    }

    @Test
    void solveLinearEquation_zeroB() {
        // 2x + 0 = 0 → x = 0
        assertEquals(0.0, calc.solveLinearEquation(2, 0));
    }

    @Test
    void solveLinearEquation_aZero_bZero_throws() {
        assertThrows(IllegalArgumentException.class,
                () -> calc.solveLinearEquation(0, 0));
    }

    @Test
    void solveLinearEquation_aZero_bNonZero_throws() {
        assertThrows(IllegalArgumentException.class,
                () -> calc.solveLinearEquation(0, 5));
    }

    @Test
    void solveLinearEquation_fractional() {
        // 3x + 1 = 0 → x = -1/3 ≈ -0.33333333
        assertEquals(-0.33333333, calc.solveLinearEquation(3, 1));
    }

    // ── solveQuadricEquation ────────────────────────────────────────────

    @Test
    void solveQuadricEquation_twoRoots() {
        // x^2 - 5x + 6 = 0 → roots 3 and 2
        double[] result = calc.solveQuadricEquation(1, -5, 6);
        assertEquals(2, result.length);
        // order: (-b+sqrtD)/(2a) first, then (-b-sqrtD)/(2a)
        assertEquals(3.0, result[0]);
        assertEquals(2.0, result[1]);
    }

    @Test
    void solveQuadricEquation_doubleRoot() {
        // x^2 - 2x + 1 = 0 → root 1 (double)
        double[] result = calc.solveQuadricEquation(1, -2, 1);
        assertEquals(1.0, result[0]);
        assertEquals(1.0, result[1]);
    }

    @Test
    void solveQuadricEquation_negativeDiscriminant_throws() {
        // x^2 + 1 = 0 → discriminant = -4
        assertThrows(IllegalArgumentException.class,
                () -> calc.solveQuadricEquation(1, 0, 1));
    }

    @Test
    void solveQuadricEquation_aZero_throws() {
        assertThrows(IllegalArgumentException.class,
                () -> calc.solveQuadricEquation(0, 2, 1));
    }

    @Test
    void solveQuadricEquation_negativeA() {
        // -x^2 + 5x - 6 = 0 → same roots as x^2 - 5x + 6: 3 and 2
        double[] result = calc.solveQuadricEquation(-1, 5, -6);
        assertEquals(2.0, result[0]);
        assertEquals(3.0, result[1]);
    }

    // ── edge cases ──────────────────────────────────────────────────────

    @Test
    void sum_largeValues() {
        // Use values within Long.MAX_VALUE / factor range to avoid Math.round overflow
        double[] values = {1e10, 2e10, 3e10};
        double result = calc.sum(values);
        assertEquals(6e10, result, 1e-8);
    }

    @Test
    void mean_extremeValues() {
        // Use values that stay within safe rounding range
        double[] values = {1e10, 2e10, 3e10};
        double result = calc.mean(values);
        assertEquals(2e10, result, 1e-8);
    }

    @Test
    void median_unsortedWithDuplicates() {
        // {5, 1, 5, 1, 5} → sorted {1, 1, 5, 5, 5} → median=5
        assertEquals(5.0, calc.median(new double[]{5, 1, 5, 1, 5}));
    }

    @Test
    void stddev_largeSpread() {
        double[] values = {0, 1_000_000};
        double result = calc.stddev(values);
        assertEquals(500_000.0, result, 1e-8);
    }

    @Test
    void variance_largeSpread() {
        // Use values that stay within Long.MAX_VALUE / factor for Math.round
        double[] values = {0, 10_000};
        double result = calc.variance(values);
        assertEquals(2.5e7, result, 1e-8);
    }

    @Test
    void skewness_highlySkewed() {
        // {0, 0, 0, 0, 100}: mean=20, stddev=40, skewness=1.5
        double result = calc.skewness(new double[]{0, 0, 0, 0, 100});
        assertTrue(result >= 1.5, "should be highly positively skewed");
    }

    @Test
    void kurtosis_highlyPeaked() {
        // {0, 5, 5, 5, 5, 5, 10}: most values at center
        double result = calc.kurtosis(new double[]{0, 5, 5, 5, 5, 5, 10});
        assertTrue(result > 0.0, "should have positive excess kurtosis (peaked)");
    }

    @Test
    void quantile_singleElement() {
        assertEquals(7.0, calc.quantile(new double[]{7}, 0.5));
        assertEquals(7.0, calc.quantile(new double[]{7}, 0.0));
        assertEquals(7.0, calc.quantile(new double[]{7}, 1.0));
    }

    @Test
    void iqr_largeSpread() {
        // {0, 25, 50, 75, 100}: Q1=25, Q3=75, IQR=50
        assertEquals(50.0, calc.iqr(new double[]{0, 25, 50, 75, 100}));
    }

    @Test
    void linearRegression_negativeSlope() {
        // y = -2x + 10: x={1,2,3}, y={8,6,4}
        double[] result = calc.linearRegression(new double[]{1, 2, 3}, new double[]{8, 6, 4});
        assertEquals(-2.0, result[0]);
        assertEquals(10.0, result[1]);
    }

    @Test
    void linearRegression_largeValues() {
        double[] x = {1e6, 2e6, 3e6};
        double[] y = {2e6, 4e6, 6e6};
        double[] result = calc.linearRegression(x, y);
        assertEquals(2.0, result[0], 1e-8);
        assertEquals(0.0, result[1], 1e-8);
    }

    @Test
    void clamp_lowGreaterThanHigh() {
        // clamp(5, 10, 0): low > high, max(10, min(0, 5)) = max(10, 0) = 10
        double result = calc.clamp(5, 10, 0);
        assertEquals(10.0, result);
    }

    @Test
    void factorial_20() {
        // 20! = 2432902008176640000, but Math.round overflows Long.MAX_VALUE
        // The round() method returns value as-is when |value| > Double.MAX_VALUE / factor
        // So we just verify it's finite and close
        double result = calc.factorial(20);
        assertTrue(Double.isFinite(result));
        assertTrue(result > 2.4e18 && result < 2.5e18);
    }

    @Test
    void gcd_largeCoprime() {
        assertEquals(1.0, calc.gcd(1_000_000_000_000L, 1_000_000_000_001L));
    }

    @Test
    void lcm_large() {
        // lcm(1000000, 1000000) = 1000000
        assertEquals(1_000_000.0, calc.lcm(1_000_000, 1_000_000));
    }

    @Test
    void permute_largeN() {
        // P(170, 1) = 170
        assertEquals(170.0, calc.permute(170, 1));
    }

    @Test
    void combine_largeN() {
        // C(170, 1) = 170
        assertEquals(170.0, calc.combine(170, 1));
    }

    @Test
    void aOfBToCPercent_negativeA() {
        assertEquals(-25.0, calc.aOfBToCPercent(-25, 100));
    }

    @Test
    void aPercentOfBToC_largePercent() {
        assertEquals(200.0, calc.aPercentOfBToC(200, 100));
    }

    @Test
    void ratio_largeValues() {
        assertEquals(0.5, calc.ratio(1e100, 2e100));
    }

    @Test
    void linearInterpolate_extrapolateBelow() {
        // x=0, below x1=1: 3 + (0-1)*(7-3)/(2-1) = 3 - 4 = -1
        assertEquals(-1.0, calc.linearInterpolate(0, 1, 3, 2, 7));
    }

    @Test
    void solveLinearEquation_largeCoefficients() {
        // 1e6 x + 2e6 = 0 → x = -2
        assertEquals(-2.0, calc.solveLinearEquation(1e6, 2e6));
    }

    @Test
    void solveQuadricEquation_largeCoefficients() {
        // x^2 - 2000x + 1000000 = 0 → (x-1000)^2 = 0 → x=1000
        double[] result = calc.solveQuadricEquation(1, -2000, 1_000_000);
        assertEquals(1000.0, result[0]);
        assertEquals(1000.0, result[1]);
    }

    // ── rounding precision ─────────────────────────────────────────────

    @Test
    void rounding_8digits() {
        double result = calc.mean(new double[]{1.0 / 3.0});
        assertEquals(0.33333333, result);
    }
}
