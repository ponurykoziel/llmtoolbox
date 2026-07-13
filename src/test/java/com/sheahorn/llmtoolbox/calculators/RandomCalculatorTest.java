package com.sheahorn.llmtoolbox.calculators;

import com.sheahorn.llmtoolbox.calculators.RandomCalculator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class RandomCalculatorTest {

    private RandomCalculator calc;

    @BeforeEach
    void setUp() {
        calc = new RandomCalculator();
    }

    // ── randomValue: byte ───────────────────────────────────────────────

    @Test
    void randomValue_byte() {
        String result = calc.randomValue("byte", null, null, null, null);
        byte b = Byte.parseByte(result);
        // any byte value is valid
        assertTrue(b >= -128 && b <= 127);
    }

    // ── randomValue: boolean ────────────────────────────────────────────

    @Test
    void randomValue_boolean() {
        String result = calc.randomValue("boolean", null, null, null, null);
        assertTrue("true".equals(result) || "false".equals(result));
    }

    // ── randomValue: int ────────────────────────────────────────────────

    @Test
    void randomValue_int_unbounded() {
        String result = calc.randomValue("int", null, null, null, null);
        int val = Integer.parseInt(result);
        // any int is valid
        assertTrue(val >= Integer.MIN_VALUE && val <= Integer.MAX_VALUE);
    }

    @Test
    void randomValue_int_bounded() {
        for (int i = 0; i < 20; i++) {
            String result = calc.randomValue("int", 10L, 20L, null, null);
            long val = Long.parseLong(result);
            assertTrue(val >= 10 && val <= 20, "value " + val + " not in [10,20]");
        }
    }

    @Test
    void randomValue_int_minEqualsMax() {
        String result = calc.randomValue("int", 5L, 5L, null, null);
        assertEquals("5", result);
    }

    @Test
    void randomValue_int_minGreaterThanMax_throws() {
        assertThrows(IllegalArgumentException.class,
                () -> calc.randomValue("int", 10L, 5L, null, null));
    }

    @Test
    void randomValue_int_maxTooLarge_throws() {
        assertThrows(IllegalArgumentException.class,
                () -> calc.randomValue("int", 0L, Long.MAX_VALUE, null, null));
    }

    // ── randomValue: long ───────────────────────────────────────────────

    @Test
    void randomValue_long() {
        String result = calc.randomValue("long", null, null, null, null);
        long val = Long.parseLong(result);
        // any long is valid
        assertTrue(val >= Long.MIN_VALUE && val <= Long.MAX_VALUE);
    }

    // ── randomValue: float ──────────────────────────────────────────────

    @Test
    void randomValue_float() {
        String result = calc.randomValue("float", null, null, null, null);
        float val = Float.parseFloat(result);
        assertTrue(val >= 0.0f && val < 1.0f);
    }

    // ── randomValue: double ─────────────────────────────────────────────

    @Test
    void randomValue_double() {
        String result = calc.randomValue("double", null, null, null, null);
        double val = Double.parseDouble(result);
        assertTrue(val >= 0.0 && val < 1.0);
    }

    // ── randomValue: gaussian ───────────────────────────────────────────

    @Test
    void randomValue_gaussian_default() {
        String result = calc.randomValue("gaussian", null, null, null, null);
        double val = Double.parseDouble(result);
        // mean=0, stddev=1: most values within [-5, 5], but don't assert range
        assertFalse(Double.isNaN(val));
        assertFalse(Double.isInfinite(val));
    }

    @Test
    void randomValue_gaussian_custom() {
        String result = calc.randomValue("gaussian", null, null, 100.0, 0.0);
        // mean=100, stddev=0 → always exactly 100
        assertEquals("100.0", result);
    }

    @Test
    void randomValue_gaussian_negativeStddev_throws() {
        assertThrows(IllegalArgumentException.class,
                () -> calc.randomValue("gaussian", null, null, 0.0, -1.0));
    }

    // ── randomValue: uuid ───────────────────────────────────────────────

    @Test
    void randomValue_uuid() {
        String result = calc.randomValue("uuid", null, null, null, null);
        // UUID format: 8-4-4-4-12 hex digits
        assertTrue(result.matches("[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}"));
    }

    @Test
    void randomValue_uuid_unique() {
        String r1 = calc.randomValue("uuid", null, null, null, null);
        String r2 = calc.randomValue("uuid", null, null, null, null);
        assertNotEquals(r1, r2);
    }

    // ── randomValue: invalid type ───────────────────────────────────────

    @Test
    void randomValue_invalidType_throws() {
        assertThrows(IllegalArgumentException.class,
                () -> calc.randomValue("nonexistent", null, null, null, null));
    }

    @Test
    void randomValue_caseInsensitive() {
        String result = calc.randomValue("UUID", null, null, null, null);
        assertTrue(result.matches("[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}"));
    }

    // ── randomValue: int partial min/max ────────────────────────────────

    @Test
    void randomValue_int_onlyMin_throws() {
        assertThrows(IllegalArgumentException.class,
                () -> calc.randomValue("int", 5L, null, null, null));
    }

    @Test
    void randomValue_int_onlyMax_throws() {
        assertThrows(IllegalArgumentException.class,
                () -> calc.randomValue("int", null, 10L, null, null));
    }

    @Test
    void randomValue_int_negativeRange() {
        for (int i = 0; i < 20; i++) {
            String result = calc.randomValue("int", -100L, -50L, null, null);
            long val = Long.parseLong(result);
            assertTrue(val >= -100 && val <= -50, "value " + val + " not in [-100,-50]");
        }
    }

    @Test
    void randomValue_int_wideRange() {
        for (int i = 0; i < 20; i++) {
            String result = calc.randomValue("int", -1_000_000L, 1_000_000L, null, null);
            long val = Long.parseLong(result);
            assertTrue(val >= -1_000_000 && val <= 1_000_000);
        }
    }

    // ── randomValue: gaussian edge cases ────────────────────────────────

    @Test
    void randomValue_gaussian_onlyMean() {
        // mean=50, default stddev=1
        String result = calc.randomValue("gaussian", null, null, 50.0, null);
        double val = Double.parseDouble(result);
        assertFalse(Double.isNaN(val));
        assertFalse(Double.isInfinite(val));
    }

    @Test
    void randomValue_gaussian_onlyStddev() {
        // default mean=0, stddev=5
        String result = calc.randomValue("gaussian", null, null, null, 5.0);
        double val = Double.parseDouble(result);
        assertFalse(Double.isNaN(val));
        assertFalse(Double.isInfinite(val));
    }

    @Test
    void randomValue_gaussian_largeStddev() {
        String result = calc.randomValue("gaussian", null, null, 0.0, 1000.0);
        double val = Double.parseDouble(result);
        assertFalse(Double.isNaN(val));
        assertFalse(Double.isInfinite(val));
    }

    @Test
    void randomValue_gaussian_negativeMean() {
        String result = calc.randomValue("gaussian", null, null, -10.0, 0.5);
        double val = Double.parseDouble(result);
        assertFalse(Double.isNaN(val));
        assertFalse(Double.isInfinite(val));
    }

    @Test
    void randomValue_gaussian_zeroStddev() {
        // stddev=0 → always exactly the mean
        String result = calc.randomValue("gaussian", null, null, 42.0, 0.0);
        assertEquals("42.0", result);
    }

    // ── randomText ──────────────────────────────────────────────────────

    @Test
    void randomText_lengthMatches() {
        String result = calc.randomText(10, null, null);
        assertEquals(10, result.length());
    }

    @Test
    void randomText_defaultCharset() {
        String result = calc.randomText(100, null, null);
        // should only contain letters and numbers
        assertTrue(result.matches("[a-zA-Z0-9]+"));
    }

    @Test
    void randomText_letters() {
        String result = calc.randomText(100, "letters", null);
        assertTrue(result.matches("[a-zA-Z]+"));
    }

    @Test
    void randomText_numbers() {
        String result = calc.randomText(100, "numbers", null);
        assertTrue(result.matches("[0-9]+"));
    }

    @Test
    void randomText_lettersNumbers() {
        String result = calc.randomText(100, "letters_numbers", null);
        assertTrue(result.matches("[a-zA-Z0-9]+"));
    }

    @Test
    void randomText_custom() {
        String result = calc.randomText(100, "custom", "ABC123");
        assertTrue(result.matches("[ABC123]+"));
    }

    @Test
    void randomText_customMissingChars_throws() {
        assertThrows(IllegalArgumentException.class,
                () -> calc.randomText(10, "custom", null));
    }

    @Test
    void randomText_customEmptyChars_throws() {
        assertThrows(IllegalArgumentException.class,
                () -> calc.randomText(10, "custom", ""));
    }

    @Test
    void randomText_lengthZero_throws() {
        assertThrows(IllegalArgumentException.class,
                () -> calc.randomText(0, null, null));
    }

    @Test
    void randomText_lengthNegative_throws() {
        assertThrows(IllegalArgumentException.class,
                () -> calc.randomText(-1, null, null));
    }

    @Test
    void randomText_lengthTooLarge_throws() {
        assertThrows(IllegalArgumentException.class,
                () -> calc.randomText(10001, null, null));
    }

    @Test
    void randomText_length1() {
        String result = calc.randomText(1, null, null);
        assertEquals(1, result.length());
    }

    @Test
    void randomText_length10000() {
        String result = calc.randomText(10000, null, null);
        assertEquals(10000, result.length());
    }

    @Test
    void randomText_unknownCharset_throws() {
        assertThrows(IllegalArgumentException.class,
                () -> calc.randomText(10, "unknown", null));
    }

    @Test
    void randomText_caseInsensitiveCharset() {
        String result = calc.randomText(100, "LETTERS", null);
        assertTrue(result.matches("[a-zA-Z]+"));
    }
}
