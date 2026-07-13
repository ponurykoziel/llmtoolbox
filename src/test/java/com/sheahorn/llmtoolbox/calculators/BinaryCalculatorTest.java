package com.sheahorn.llmtoolbox.calculators;

import com.sheahorn.llmtoolbox.calculators.BinaryCalculator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class BinaryCalculatorTest {

    private BinaryCalculator calc;

    @BeforeEach
    void setUp() {
        calc = new BinaryCalculator(8);
    }

    // ── add ──────────────────────────────────────────────────────────────

    @Test
    void add_normal() {
        assertEquals(7.0, calc.add(3, 4));
    }

    @Test
    void add_negative() {
        assertEquals(-1.0, calc.add(3, -4));
    }

    @Test
    void add_zero() {
        assertEquals(5.0, calc.add(5, 0));
    }

    @Test
    void add_fractional() {
        assertEquals(0.7, calc.add(0.3, 0.4));
    }

    @Test
    void add_large() {
        assertEquals(2_000_000.0, calc.add(1_000_000, 1_000_000));
    }

    // ── subtract ─────────────────────────────────────────────────────────

    @Test
    void subtract_normal() {
        assertEquals(1.0, calc.subtract(5, 4));
    }

    @Test
    void subtract_negativeResult() {
        assertEquals(-1.0, calc.subtract(3, 4));
    }

    @Test
    void subtract_zero() {
        assertEquals(5.0, calc.subtract(5, 0));
    }

    @Test
    void subtract_fractional() {
        assertEquals(0.1, calc.subtract(0.5, 0.4));
    }

    @Test
    void subtract_self() {
        assertEquals(0.0, calc.subtract(7, 7));
    }

    // ── multiply ─────────────────────────────────────────────────────────

    @Test
    void multiply_normal() {
        assertEquals(12.0, calc.multiply(3, 4));
    }

    @Test
    void multiply_negative() {
        assertEquals(-12.0, calc.multiply(3, -4));
    }

    @Test
    void multiply_doubleNegative() {
        assertEquals(12.0, calc.multiply(-3, -4));
    }

    @Test
    void multiply_zero() {
        assertEquals(0.0, calc.multiply(5, 0));
    }

    @Test
    void multiply_fractional() {
        assertEquals(0.12, calc.multiply(0.3, 0.4));
    }

    @Test
    void multiply_large() {
        // Use values that won't overflow Math.round (Long.MAX_VALUE ≈ 9.22e18)
        assertEquals(200_000_000.0, calc.multiply(10_000, 20_000));
    }

    // ── divide ───────────────────────────────────────────────────────────

    @Test
    void divide_normal() {
        assertEquals(2.5, calc.divide(5, 2));
    }

    @Test
    void divide_negative() {
        assertEquals(-2.5, calc.divide(5, -2));
    }

    @Test
    void divide_fractional() {
        assertEquals(0.75, calc.divide(0.3, 0.4));
    }

    @Test
    void divide_byZero_throws() {
        assertThrows(ArithmeticException.class, () -> calc.divide(5, 0));
    }

    @Test
    void divide_zeroNumerator() {
        assertEquals(0.0, calc.divide(0, 5));
    }

    @Test
    void divide_one() {
        assertEquals(5.0, calc.divide(5, 1));
    }

    // ── power ────────────────────────────────────────────────────────────

    @Test
    void power_normal() {
        assertEquals(8.0, calc.power(2, 3));
    }

    @Test
    void power_zeroExponent() {
        assertEquals(1.0, calc.power(5, 0));
    }

    @Test
    void power_oneExponent() {
        assertEquals(5.0, calc.power(5, 1));
    }

    @Test
    void power_negativeBase() {
        assertEquals(-8.0, calc.power(-2, 3));
    }

    @Test
    void power_negativeBaseEvenExponent() {
        assertEquals(4.0, calc.power(-2, 2));
    }

    @Test
    void power_fractionalExponent() {
        // sqrt(4) = 2
        assertEquals(2.0, calc.power(4, 0.5));
    }

    @Test
    void power_zeroBase() {
        assertEquals(0.0, calc.power(0, 5));
    }

    @Test
    void power_zeroBaseZeroExponent() {
        // Math.pow(0,0) = 1.0 in Java
        assertEquals(1.0, calc.power(0, 0));
    }

    // ── moduloDivision ──────────────────────────────────────────────────

    @Test
    void moduloDivision_normal() {
        // floorMod-style: ((7 % 3) + 3) % 3 = (1+3)%3 = 1
        assertEquals(1.0, calc.moduloDivision(7, 3));
    }

    @Test
    void moduloDivision_negativeDividend() {
        // ((-7 % 3) + 3) % 3 = (-1+3)%3 = 2
        assertEquals(2.0, calc.moduloDivision(-7, 3));
    }

    @Test
    void moduloDivision_negativeDivisor() {
        // ((7 % -3) + -3) % -3 = (1-3)%-3 = -2%-3 = -2... wait
        // Java: 7 % -3 = 1, (1 + (-3)) % (-3) = -2 % -3 = -2
        assertEquals(-2.0, calc.moduloDivision(7, -3));
    }

    @Test
    void moduloDivision_bothNegative() {
        // ((-7 % -3) + -3) % -3 = (-1-3)%-3 = -4%-3 = -1
        assertEquals(-1.0, calc.moduloDivision(-7, -3));
    }

    @Test
    void moduloDivision_exactMultiple() {
        assertEquals(0.0, calc.moduloDivision(6, 3));
    }

    @Test
    void moduloDivision_byZero_throws() {
        assertThrows(ArithmeticException.class, () -> calc.moduloDivision(5, 0));
    }

    @Test
    void moduloDivision_fractional() {
        // ((2.5 % 1.2) + 1.2) % 1.2 = (0.1+1.2)%1.2 = 1.3%1.2 = 0.1
        assertEquals(0.1, calc.moduloDivision(2.5, 1.2), 1e-8);
    }

    // ── divisionRemainder ───────────────────────────────────────────────

    @Test
    void divisionRemainder_normal() {
        assertEquals(1.0, calc.divisionRemainder(7, 3));
    }

    @Test
    void divisionRemainder_negativeDividend() {
        // -7 % 3 = -1 in Java
        assertEquals(-1.0, calc.divisionRemainder(-7, 3));
    }

    @Test
    void divisionRemainder_exactMultiple() {
        assertEquals(0.0, calc.divisionRemainder(6, 3));
    }

    @Test
    void divisionRemainder_byZero_throws() {
        assertThrows(ArithmeticException.class, () -> calc.divisionRemainder(5, 0));
    }

    @Test
    void divisionRemainder_fractional() {
        assertEquals(0.1, calc.divisionRemainder(2.5, 1.2), 1e-8);
    }

    // ── roundTo ─────────────────────────────────────────────────────────

    @Test
    void roundTo_positiveDigits() {
        assertEquals(3.14, calc.roundTo(Math.PI, 2));
    }

    @Test
    void roundTo_zeroDigits() {
        // digits <= 0 returns the value after initial 8-digit round
        assertEquals(3.14159265, calc.roundTo(Math.PI, 0), 1e-8);
    }

    @Test
    void roundTo_negativeDigits() {
        // digits <= 0 returns the value after initial 8-digit round
        double result = calc.roundTo(Math.PI, -2);
        assertEquals(3.14159265, result, 1e-8);
    }

    @Test
    void roundTo_manyDigits() {
        assertEquals(3.14159265, calc.roundTo(Math.PI, 8), 1e-8);
    }

    @Test
    void roundTo_NaN() {
        double result = calc.roundTo(Double.NaN, 5);
        assertTrue(Double.isNaN(result));
    }

    @Test
    void roundTo_infinity() {
        double result = calc.roundTo(Double.POSITIVE_INFINITY, 5);
        assertTrue(Double.isInfinite(result));
    }

    // ── NaN / Infinity edge cases ──────────────────────────────────────

    @Test
    void add_NaN() {
        assertTrue(Double.isNaN(calc.add(Double.NaN, 5)));
    }

    @Test
    void add_infinity() {
        assertTrue(Double.isInfinite(calc.add(Double.POSITIVE_INFINITY, 1)));
    }

    @Test
    void divide_NaN() {
        assertTrue(Double.isNaN(calc.divide(Double.NaN, 5)));
    }

    @Test
    void divide_infinityByInfinity() {
        assertTrue(Double.isNaN(calc.divide(Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY)));
    }

    @Test
    void power_negativeBaseFractionalExponent() {
        // (-2)^0.5 = NaN (complex result)
        assertTrue(Double.isNaN(calc.power(-2, 0.5)));
    }

    @Test
    void power_largeExponent() {
        // 2^1000 is huge but finite
        double result = calc.power(2, 1000);
        assertTrue(Double.isFinite(result));
    }

    // ── rounding precision ──────────────────────────────────────────────

    @Test
    void rounding_8digits() {
        assertEquals(0.66666667, calc.divide(2.0, 3.0), 1e-8);
    }
}
