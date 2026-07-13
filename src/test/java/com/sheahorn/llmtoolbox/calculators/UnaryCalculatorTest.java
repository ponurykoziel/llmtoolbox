package com.sheahorn.llmtoolbox.calculators;

import com.sheahorn.llmtoolbox.calculators.UnaryCalculator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class UnaryCalculatorTest {

    private UnaryCalculator calc;

    @BeforeEach
    void setUp() {
        calc = new UnaryCalculator(8);
    }

    // ── sqrt ────────────────────────────────────────────────────────────

    @Test
    void sqrt_positive() {
        assertEquals(3.0, calc.sqrt(9));
    }

    @Test
    void sqrt_zero() {
        assertEquals(0.0, calc.sqrt(0));
    }

    @Test
    void sqrt_one() {
        assertEquals(1.0, calc.sqrt(1));
    }

    @Test
    void sqrt_irrational() {
        assertEquals(1.41421356, calc.sqrt(2), 1e-8);
    }

    @Test
    void sqrt_fractional() {
        assertEquals(0.5, calc.sqrt(0.25));
    }

    @Test
    void sqrt_negative_throws() {
        assertThrows(IllegalArgumentException.class, () -> calc.sqrt(-1));
    }

    @Test
    void sqrt_large() {
        assertEquals(10000.0, calc.sqrt(100_000_000));
    }

    // ── cbrt ────────────────────────────────────────────────────────────

    @Test
    void cbrt_positive() {
        assertEquals(3.0, calc.cbrt(27));
    }

    @Test
    void cbrt_negative() {
        assertEquals(-3.0, calc.cbrt(-27));
    }

    @Test
    void cbrt_zero() {
        assertEquals(0.0, calc.cbrt(0));
    }

    @Test
    void cbrt_one() {
        assertEquals(1.0, calc.cbrt(1));
    }

    @Test
    void cbrt_irrational() {
        assertEquals(1.25992105, calc.cbrt(2), 1e-8);
    }

    // ── log2 ────────────────────────────────────────────────────────────

    @Test
    void log2_powerOfTwo() {
        assertEquals(3.0, calc.log2(8));
    }

    @Test
    void log2_one() {
        assertEquals(0.0, calc.log2(1));
    }

    @Test
    void log2_fractional() {
        assertEquals(-1.0, calc.log2(0.5));
    }

    @Test
    void log2_large() {
        assertEquals(10.0, calc.log2(1024));
    }

    @Test
    void log2_zero_throws() {
        assertThrows(IllegalArgumentException.class, () -> calc.log2(0));
    }

    @Test
    void log2_negative_throws() {
        assertThrows(IllegalArgumentException.class, () -> calc.log2(-1));
    }

    // ── log10 ───────────────────────────────────────────────────────────

    @Test
    void log10_powerOfTen() {
        assertEquals(3.0, calc.log10(1000));
    }

    @Test
    void log10_one() {
        assertEquals(0.0, calc.log10(1));
    }

    @Test
    void log10_fractional() {
        assertEquals(-1.0, calc.log10(0.1));
    }

    @Test
    void log10_zero_throws() {
        assertThrows(IllegalArgumentException.class, () -> calc.log10(0));
    }

    @Test
    void log10_negative_throws() {
        assertThrows(IllegalArgumentException.class, () -> calc.log10(-1));
    }

    // ── ln ──────────────────────────────────────────────────────────────

    @Test
    void ln_e() {
        assertEquals(1.0, calc.ln(Math.E));
    }

    @Test
    void ln_one() {
        assertEquals(0.0, calc.ln(1));
    }

    @Test
    void ln_fractional() {
        // ln(1/e) = -1
        assertEquals(-1.0, calc.ln(1.0 / Math.E), 1e-8);
    }

    @Test
    void ln_zero_throws() {
        assertThrows(IllegalArgumentException.class, () -> calc.ln(0));
    }

    @Test
    void ln_negative_throws() {
        assertThrows(IllegalArgumentException.class, () -> calc.ln(-1));
    }

    // ── sin ─────────────────────────────────────────────────────────────

    @Test
    void sin_zero() {
        assertEquals(0.0, calc.sin(0));
    }

    @Test
    void sin_piHalf() {
        assertEquals(1.0, calc.sin(Math.PI / 2));
    }

    @Test
    void sin_pi() {
        assertEquals(0.0, calc.sin(Math.PI), 1e-8);
    }

    @Test
    void sin_negative() {
        assertEquals(-1.0, calc.sin(-Math.PI / 2));
    }

    // ── cos ─────────────────────────────────────────────────────────────

    @Test
    void cos_zero() {
        assertEquals(1.0, calc.cos(0));
    }

    @Test
    void cos_piHalf() {
        assertEquals(0.0, calc.cos(Math.PI / 2), 1e-8);
    }

    @Test
    void cos_pi() {
        assertEquals(-1.0, calc.cos(Math.PI));
    }

    @Test
    void cos_negative() {
        assertEquals(1.0, calc.cos(-Math.PI * 2));
    }

    // ── tan ─────────────────────────────────────────────────────────────

    @Test
    void tan_zero() {
        assertEquals(0.0, calc.tan(0));
    }

    @Test
    void tan_piQuarter() {
        // tan(π/4) ≈ 0.99999999 due to rounding of π/4 before tan
        assertEquals(0.99999999, calc.tan(Math.PI / 4), 1e-8);
    }

    @Test
    void tan_negative() {
        assertEquals(-0.99999999, calc.tan(-Math.PI / 4), 1e-8);
    }

    // ── sign ────────────────────────────────────────────────────────────

    @Test
    void sign_positive() {
        assertEquals(1, calc.sign(42));
    }

    @Test
    void sign_negative() {
        assertEquals(-1, calc.sign(-42));
    }

    @Test
    void sign_zero() {
        assertEquals(0, calc.sign(0));
    }

    @Test
    void sign_fractional() {
        assertEquals(1, calc.sign(0.001));
    }

    @Test
    void sign_negativeFractional() {
        assertEquals(-1, calc.sign(-0.001));
    }

    // ── abs ─────────────────────────────────────────────────────────────

    @Test
    void abs_positive() {
        assertEquals(42.0, calc.abs(42));
    }

    @Test
    void abs_negative() {
        assertEquals(42.0, calc.abs(-42));
    }

    @Test
    void abs_zero() {
        assertEquals(0.0, calc.abs(0));
    }

    @Test
    void abs_fractional() {
        assertEquals(3.14, calc.abs(-3.14));
    }

    // ── magnitude ───────────────────────────────────────────────────────

    @Test
    void magnitude_500() {
        // log10(500) ≈ 2.69897, floor = 2
        assertEquals(2, calc.magnitude(500));
    }

    @Test
    void magnitude_0_007() {
        // log10(0.007) ≈ -2.1549, floor = -3
        assertEquals(-3, calc.magnitude(0.007));
    }

    @Test
    void magnitude_one() {
        assertEquals(0, calc.magnitude(1));
    }

    @Test
    void magnitude_ten() {
        assertEquals(1, calc.magnitude(10));
    }

    @Test
    void magnitude_negative() {
        // log10(|-500|) = log10(500) ≈ 2.69897, floor = 2
        assertEquals(2, calc.magnitude(-500));
    }

    @Test
    void magnitude_large() {
        assertEquals(6, calc.magnitude(1_000_000));
    }

    @Test
    void magnitude_zero_throws() {
        assertThrows(IllegalArgumentException.class, () -> calc.magnitude(0));
    }

    // ── nearestInt ──────────────────────────────────────────────────────

    @Test
    void nearestInt_roundDown() {
        assertEquals(3.0, calc.nearestInt(3.2));
    }

    @Test
    void nearestInt_roundUp() {
        assertEquals(4.0, calc.nearestInt(3.7));
    }

    @Test
    void nearestInt_exactHalf() {
        // Math.round(3.5) = 4 in Java (round half up)
        assertEquals(4.0, calc.nearestInt(3.5));
    }

    @Test
    void nearestInt_negative() {
        assertEquals(-3.0, calc.nearestInt(-3.2));
    }

    @Test
    void nearestInt_negativeHalf() {
        // Math.round(-3.5) = -3 in Java (round half up toward +inf)
        assertEquals(-3.0, calc.nearestInt(-3.5));
    }

    @Test
    void nearestInt_integer() {
        assertEquals(7.0, calc.nearestInt(7));
    }

    // ── ceil ─────────────────────────────────────────────────────────────

    @Test
    void ceil_positive() {
        assertEquals(4.0, calc.ceil(3.2));
    }

    @Test
    void ceil_negative() {
        assertEquals(-3.0, calc.ceil(-3.2));
    }

    @Test
    void ceil_integer() {
        assertEquals(7.0, calc.ceil(7));
    }

    @Test
    void ceil_justAboveInteger() {
        assertEquals(8.0, calc.ceil(7.00000001));
    }

    // ── floor ────────────────────────────────────────────────────────────

    @Test
    void floor_positive() {
        assertEquals(3.0, calc.floor(3.7));
    }

    @Test
    void floor_negative() {
        assertEquals(-4.0, calc.floor(-3.2));
    }

    @Test
    void floor_integer() {
        assertEquals(7.0, calc.floor(7));
    }

    @Test
    void floor_justBelowInteger() {
        assertEquals(6.0, calc.floor(6.99999999));
    }

    // ── trunc ────────────────────────────────────────────────────────────

    @Test
    void trunc_positive() {
        assertEquals(3.0, calc.trunc(3.7));
    }

    @Test
    void trunc_negative() {
        assertEquals(-3.0, calc.trunc(-3.7));
    }

    @Test
    void trunc_integer() {
        assertEquals(7.0, calc.trunc(7));
    }

    @Test
    void trunc_zero() {
        assertEquals(0.0, calc.trunc(0));
    }

    // ── tan asymptote ──────────────────────────────────────────────────

    @Test
    void tan_nearPiHalf() {
        // tan near π/2 produces a very large value, not an exception
        double result = calc.tan(Math.PI / 2 - 1e-10);
        assertTrue(Math.abs(result) > 1e6);
    }

    // ── large inputs ───────────────────────────────────────────────────

    @Test
    void sin_largeInput() {
        // sin(1000π) ≈ 0 (periodic)
        double result = calc.sin(1000 * Math.PI);
        assertEquals(0.0, result, 1e-8);
    }

    @Test
    void cos_largeInput() {
        double result = calc.cos(1000 * Math.PI);
        assertEquals(1.0, result, 1e-8);
    }

    // ── NaN / Infinity ─────────────────────────────────────────────────

    @Test
    void sqrt_NaN() {
        assertTrue(Double.isNaN(calc.sqrt(Double.NaN)));
    }

    @Test
    void sqrt_infinity() {
        assertTrue(Double.isInfinite(calc.sqrt(Double.POSITIVE_INFINITY)));
    }

    @Test
    void log2_NaN() {
        assertTrue(Double.isNaN(calc.log2(Double.NaN)));
    }

    @Test
    void sin_NaN() {
        assertTrue(Double.isNaN(calc.sin(Double.NaN)));
    }

    @Test
    void nearestInt_NaN() {
        // Math.round(NaN) returns 0 in Java, so nearestInt(NaN) = round(0) = 0.0
        assertEquals(0.0, calc.nearestInt(Double.NaN));
    }

    @Test
    void ceil_NaN() {
        assertTrue(Double.isNaN(calc.ceil(Double.NaN)));
    }

    @Test
    void floor_NaN() {
        assertTrue(Double.isNaN(calc.floor(Double.NaN)));
    }

    @Test
    void trunc_NaN() {
        assertTrue(Double.isNaN(calc.trunc(Double.NaN)));
    }

    // ── rounding precision ──────────────────────────────────────────────

    @Test
    void rounding_8digits() {
        assertEquals(0.57735027, calc.sqrt(1.0 / 3.0), 1e-8);
    }
}
