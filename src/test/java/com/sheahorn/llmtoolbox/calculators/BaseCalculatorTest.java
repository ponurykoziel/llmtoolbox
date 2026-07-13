package com.sheahorn.llmtoolbox.calculators;

import com.sheahorn.llmtoolbox.calculators.BaseCalculator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class BaseCalculatorTest {

    private BaseCalculator calc;

    @BeforeEach
    void setUp() {
        calc = new BaseCalculator();
    }

    // ── baseConversion ──────────────────────────────────────────────────

    @Test
    void baseConversion_binToHex() {
        assertEquals("A", calc.baseConversion("1010", 2, 16));
    }

    @Test
    void baseConversion_hexToDec() {
        assertEquals("255", calc.baseConversion("FF", 16, 10));
    }

    @Test
    void baseConversion_decToBin() {
        assertEquals("1010", calc.baseConversion("10", 10, 2));
    }

    @Test
    void baseConversion_octToDec() {
        assertEquals("64", calc.baseConversion("100", 8, 10));
    }

    @Test
    void baseConversion_base36() {
        assertEquals("Z", calc.baseConversion("35", 10, 36));
    }

    @Test
    void baseConversion_sameBase() {
        assertEquals("42", calc.baseConversion("42", 10, 10));
    }

    @Test
    void baseConversion_zero() {
        assertEquals("0", calc.baseConversion("0", 10, 2));
    }

    @Test
    void baseConversion_largeValue() {
        assertEquals("7FFFFFFF", calc.baseConversion("2147483647", 10, 16));
    }

    @Test
    void baseConversion_fromBaseTooLow_throws() {
        assertThrows(IllegalArgumentException.class,
                () -> calc.baseConversion("10", 1, 10));
    }

    @Test
    void baseConversion_fromBaseTooHigh_throws() {
        assertThrows(IllegalArgumentException.class,
                () -> calc.baseConversion("10", 37, 10));
    }

    @Test
    void baseConversion_toBaseTooLow_throws() {
        assertThrows(IllegalArgumentException.class,
                () -> calc.baseConversion("10", 10, 1));
    }

    @Test
    void baseConversion_toBaseTooHigh_throws() {
        assertThrows(IllegalArgumentException.class,
                () -> calc.baseConversion("10", 10, 37));
    }

    @Test
    void baseConversion_invalidDigitForBase_throws() {
        // 'G' is not valid in base 16
        assertThrows(NumberFormatException.class,
                () -> calc.baseConversion("G", 16, 10));
    }

    @Test
    void baseConversion_negativeValue() {
        // -10 in decimal to hex
        assertEquals("-A", calc.baseConversion("-10", 10, 16));
    }

    @Test
    void baseConversion_negativeValueToBinary() {
        assertEquals("-1010", calc.baseConversion("-10", 10, 2));
    }

    // ── decomposeBytes ──────────────────────────────────────────────────

    @Test
    void decomposeBytes_zero() {
        long[] result = calc.decomposeBytes(0);
        assertEquals(0, result[0]);
        assertEquals(0, result[1]);
        assertEquals(0, result[2]);
        assertEquals(0, result[3]);
    }

    @Test
    void decomposeBytes_maxLong() {
        long[] result = calc.decomposeBytes(Long.MAX_VALUE);
        // Long.MAX_VALUE = 0x7FFFFFFFFFFFFFFF
        // >> 24 gives 0x7FFFFFFFFF, bottom byte = 0xFF
        assertEquals(0xFF, result[0]);
        assertEquals(0xFF, result[1]);
        assertEquals(0xFF, result[2]);
        assertEquals(0xFF, result[3]);
    }

    @Test
    void decomposeBytes_knownValue() {
        // 0x01020304 = 16909060
        long[] result = calc.decomposeBytes(0x01020304);
        assertEquals(1, result[0]);
        assertEquals(2, result[1]);
        assertEquals(3, result[2]);
        assertEquals(4, result[3]);
    }

    @Test
    void decomposeBytes_negativeOne() {
        // -1 = 0xFFFFFFFFFFFFFFFF, top 4 bytes: FF FF FF FF
        long[] result = calc.decomposeBytes(-1);
        assertEquals(0xFF, result[0]);
        assertEquals(0xFF, result[1]);
        assertEquals(0xFF, result[2]);
        assertEquals(0xFF, result[3]);
    }

    @Test
    void decomposeBytes_allBitsSet() {
        // 0xFFFFFFFF = 4294967295
        long[] result = calc.decomposeBytes(0xFFFFFFFFL);
        assertEquals(0xFF, result[0]);
        assertEquals(0xFF, result[1]);
        assertEquals(0xFF, result[2]);
        assertEquals(0xFF, result[3]);
    }

    // ── composeBytes ────────────────────────────────────────────────────

    @Test
    void composeBytes_zero() {
        assertEquals(0, calc.composeBytes(0, 0, 0, 0));
    }

    @Test
    void composeBytes_max() {
        assertEquals(0xFFFFFFFFL, calc.composeBytes(255, 255, 255, 255));
    }

    @Test
    void composeBytes_knownValue() {
        assertEquals(0x01020304, calc.composeBytes(1, 2, 3, 4));
    }

    @Test
    void composeBytes_roundtrip() {
        long original = 0x12345678;
        long[] bytes = calc.decomposeBytes(original);
        long back = calc.composeBytes(bytes[0], bytes[1], bytes[2], bytes[3]);
        assertEquals(original, back);
    }

    @Test
    void composeBytes_byteTooHigh_throws() {
        assertThrows(IllegalArgumentException.class,
                () -> calc.composeBytes(256, 0, 0, 0));
    }

    @Test
    void composeBytes_byteNegative_throws() {
        assertThrows(IllegalArgumentException.class,
                () -> calc.composeBytes(-1, 0, 0, 0));
    }

    @Test
    void composeBytes_anyByteInvalid_throws() {
        assertThrows(IllegalArgumentException.class,
                () -> calc.composeBytes(0, 0, 0, 256));
    }

    // ── bitwiseOps ──────────────────────────────────────────────────────

    @Test
    void bitwiseOps_and() {
        // 12 & 10 = 8
        assertEquals("8", calc.bitwiseOps("and", 12L, 10L));
    }

    @Test
    void bitwiseOps_or() {
        assertEquals("14", calc.bitwiseOps("or", 12L, 10L));
    }

    @Test
    void bitwiseOps_xor() {
        assertEquals("6", calc.bitwiseOps("xor", 12L, 10L));
    }

    @Test
    void bitwiseOps_not() {
        assertEquals(String.valueOf(~42L), calc.bitwiseOps("not", 42L, null));
    }

    @Test
    void bitwiseOps_shl() {
        assertEquals("16", calc.bitwiseOps("shl", 4L, 2L));
    }

    @Test
    void bitwiseOps_shr() {
        assertEquals("2", calc.bitwiseOps("shr", 8L, 2L));
    }

    @Test
    void bitwiseOps_shrNegative() {
        // -8 >> 2 = -2
        assertEquals("-2", calc.bitwiseOps("shr", -8L, 2L));
    }

    @Test
    void bitwiseOps_not_negative() {
        assertEquals(String.valueOf(~(-1L)), calc.bitwiseOps("not", -1L, null));
    }

    @Test
    void bitwiseOps_caseInsensitive() {
        assertEquals("8", calc.bitwiseOps("AND", 12L, 10L));
    }

    @Test
    void bitwiseOps_unknownOp_throws() {
        assertThrows(IllegalArgumentException.class,
                () -> calc.bitwiseOps("nand", 1L, 1L));
    }

    @Test
    void bitwiseOps_notMissingA_throws() {
        assertThrows(IllegalArgumentException.class,
                () -> calc.bitwiseOps("not", null, null));
    }

    @Test
    void bitwiseOps_andMissingA_throws() {
        assertThrows(IllegalArgumentException.class,
                () -> calc.bitwiseOps("and", null, 1L));
    }

    @Test
    void bitwiseOps_andMissingB_throws() {
        assertThrows(IllegalArgumentException.class,
                () -> calc.bitwiseOps("and", 1L, null));
    }

    @Test
    void bitwiseOps_shl_largeShift() {
        // 1 << 63 = Long.MIN_VALUE
        assertEquals(String.valueOf(1L << 63), calc.bitwiseOps("shl", 1L, 63L));
    }

    @Test
    void bitwiseOps_shr_largeShift() {
        // -1 >> 63 = -1 (arithmetic shift fills with 1s)
        assertEquals("-1", calc.bitwiseOps("shr", -1L, 63L));
    }
}
