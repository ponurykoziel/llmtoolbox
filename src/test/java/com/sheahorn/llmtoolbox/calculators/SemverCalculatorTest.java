package com.sheahorn.llmtoolbox.calculators;

import com.sheahorn.llmtoolbox.calculators.SemverCalculator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SemverCalculatorTest {

    private SemverCalculator calc;

    @BeforeEach
    void setUp() {
        calc = new SemverCalculator();
    }

    // ── parse ───────────────────────────────────────────────────────────

    @Test
    void parse_simple() {
        SemverCalculator.Parsed p = calc.parse("1.2.3");
        assertEquals(1, p.major);
        assertEquals(2, p.minor);
        assertEquals(3, p.patch);
        assertNull(p.prerelease);
        assertNull(p.build);
    }

    @Test
    void parse_withPrerelease() {
        SemverCalculator.Parsed p = calc.parse("1.2.3-alpha.1");
        assertEquals(1, p.major);
        assertEquals(2, p.minor);
        assertEquals(3, p.patch);
        assertEquals("alpha.1", p.prerelease);
        assertNull(p.build);
    }

    @Test
    void parse_withBuild() {
        SemverCalculator.Parsed p = calc.parse("1.2.3+build.123");
        assertEquals(1, p.major);
        assertEquals(2, p.minor);
        assertEquals(3, p.patch);
        assertNull(p.prerelease);
        assertEquals("build.123", p.build);
    }

    @Test
    void parse_withPrereleaseAndBuild() {
        SemverCalculator.Parsed p = calc.parse("1.2.3-rc.1+build.42");
        assertEquals(1, p.major);
        assertEquals(2, p.minor);
        assertEquals(3, p.patch);
        assertEquals("rc.1", p.prerelease);
        assertEquals("build.42", p.build);
    }

    @Test
    void parse_vPrefix() {
        SemverCalculator.Parsed p = calc.parse("v1.2.3");
        assertEquals(1, p.major);
        assertEquals(2, p.minor);
        assertEquals(3, p.patch);
    }

    @Test
    void parse_vPrefixWithPrerelease() {
        SemverCalculator.Parsed p = calc.parse("v1.2.3-beta");
        assertEquals("beta", p.prerelease);
    }

    @Test
    void parse_zeroMajor() {
        SemverCalculator.Parsed p = calc.parse("0.1.0");
        assertEquals(0, p.major);
        assertEquals(1, p.minor);
        assertEquals(0, p.patch);
    }

    @Test
    void parse_allZeros() {
        SemverCalculator.Parsed p = calc.parse("0.0.0");
        assertEquals(0, p.major);
        assertEquals(0, p.minor);
        assertEquals(0, p.patch);
    }

    @Test
    void parse_largeVersion() {
        SemverCalculator.Parsed p = calc.parse("999.999.999");
        assertEquals(999, p.major);
        assertEquals(999, p.minor);
        assertEquals(999, p.patch);
    }

    @Test
    void parse_invalidFormat_throws() {
        assertThrows(IllegalArgumentException.class,
                () -> calc.parse("1.2"));
    }

    @Test
    void parse_tooManyParts_throws() {
        assertThrows(IllegalArgumentException.class,
                () -> calc.parse("1.2.3.4"));
    }

    @Test
    void parse_negativeMajor_throws() {
        assertThrows(IllegalArgumentException.class,
                () -> calc.parse("-1.2.3"));
    }

    @Test
    void parse_negativeMinor_throws() {
        assertThrows(IllegalArgumentException.class,
                () -> calc.parse("1.-2.3"));
    }

    @Test
    void parse_nonNumeric_throws() {
        assertThrows(IllegalArgumentException.class,
                () -> calc.parse("a.b.c"));
    }

    // ── compare ─────────────────────────────────────────────────────────

    @Test
    void compare_equal() {
        assertEquals(0, calc.compare("1.2.3", "1.2.3"));
    }

    @Test
    void compare_greaterMajor() {
        assertEquals(1, calc.compare("2.0.0", "1.9.9"));
    }

    @Test
    void compare_lesserMajor() {
        assertEquals(-1, calc.compare("1.9.9", "2.0.0"));
    }

    @Test
    void compare_greaterMinor() {
        assertEquals(1, calc.compare("1.3.0", "1.2.9"));
    }

    @Test
    void compare_lesserMinor() {
        assertEquals(-1, calc.compare("1.2.9", "1.3.0"));
    }

    @Test
    void compare_greaterPatch() {
        assertEquals(1, calc.compare("1.2.4", "1.2.3"));
    }

    @Test
    void compare_lesserPatch() {
        assertEquals(-1, calc.compare("1.2.3", "1.2.4"));
    }

    @Test
    void compare_prereleaseLowerThanRelease() {
        // 1.0.0-alpha < 1.0.0
        assertEquals(-1, calc.compare("1.0.0-alpha", "1.0.0"));
    }

    @Test
    void compare_releaseHigherThanPrerelease() {
        assertEquals(1, calc.compare("1.0.0", "1.0.0-alpha"));
    }

    @Test
    void compare_prereleaseNumeric() {
        // 1.0.0-1 < 1.0.0-2
        assertEquals(-1, calc.compare("1.0.0-1", "1.0.0-2"));
    }

    @Test
    void compare_prereleaseAlpha() {
        // 1.0.0-alpha < 1.0.0-beta
        assertEquals(-1, calc.compare("1.0.0-alpha", "1.0.0-beta"));
    }

    @Test
    void compare_prereleaseNumericVsAlpha() {
        // numeric identifiers have lower precedence than alphanumeric
        assertEquals(-1, calc.compare("1.0.0-1", "1.0.0-alpha"));
    }

    @Test
    void compare_prereleaseAlphaVsNumeric() {
        assertEquals(1, calc.compare("1.0.0-alpha", "1.0.0-1"));
    }

    @Test
    void compare_prereleaseMoreFields() {
        // 1.0.0-alpha < 1.0.0-alpha.1 (more fields = higher)
        assertEquals(-1, calc.compare("1.0.0-alpha", "1.0.0-alpha.1"));
    }

    @Test
    void compare_prereleaseFewerFields() {
        assertEquals(1, calc.compare("1.0.0-alpha.1", "1.0.0-alpha"));
    }

    @Test
    void compare_buildIgnored() {
        // Build metadata is ignored in precedence
        assertEquals(0, calc.compare("1.0.0+build.1", "1.0.0+build.2"));
    }

    @Test
    void compare_vPrefix() {
        assertEquals(0, calc.compare("v1.2.3", "1.2.3"));
    }

    // ── satisfies: exact ────────────────────────────────────────────────

    @Test
    void satisfies_exactMatch() {
        assertTrue(calc.satisfies("1.2.3", "1.2.3"));
    }

    @Test
    void satisfies_exactNoMatch() {
        assertFalse(calc.satisfies("1.2.4", "1.2.3"));
    }

    @Test
    void satisfies_exactWithPrerelease() {
        assertTrue(calc.satisfies("1.2.3-alpha", "1.2.3-alpha"));
    }

    // ── satisfies: caret ────────────────────────────────────────────────

    @Test
    void satisfies_caretCompatible() {
        // ^1.2.3 means >=1.2.3 <2.0.0
        assertTrue(calc.satisfies("1.2.3", "^1.2.3"));
        assertTrue(calc.satisfies("1.9.9", "^1.2.3"));
        assertFalse(calc.satisfies("2.0.0", "^1.2.3"));
    }

    @Test
    void satisfies_caretZeroMajorMinor() {
        // ^0.2.3 means >=0.2.3 <0.3.0
        assertTrue(calc.satisfies("0.2.3", "^0.2.3"));
        assertTrue(calc.satisfies("0.2.9", "^0.2.3"));
        assertFalse(calc.satisfies("0.3.0", "^0.2.3"));
        assertFalse(calc.satisfies("0.2.2", "^0.2.3"));
    }

    @Test
    void satisfies_caretZeroMajorMinorPatch() {
        // ^0.0.3 means >=0.0.3 <0.0.4 (effectively =0.0.3 since patch is locked)
        assertTrue(calc.satisfies("0.0.3", "^0.0.3"));
        assertFalse(calc.satisfies("0.0.3-rc", "^0.0.3")); // prerelease < release
        assertFalse(calc.satisfies("0.0.4", "^0.0.3"));
        assertFalse(calc.satisfies("0.0.2", "^0.0.3"));
    }

    @Test
    void satisfies_caretBelowBase() {
        assertFalse(calc.satisfies("1.2.2", "^1.2.3"));
    }

    // ── satisfies: tilde ────────────────────────────────────────────────

    @Test
    void satisfies_tildeApproximately() {
        // ~1.2.3 means >=1.2.3 <1.3.0
        assertTrue(calc.satisfies("1.2.3", "~1.2.3"));
        assertTrue(calc.satisfies("1.2.9", "~1.2.3"));
        assertFalse(calc.satisfies("1.3.0", "~1.2.3"));
    }

    @Test
    void satisfies_tildeBelowBase() {
        assertFalse(calc.satisfies("1.2.2", "~1.2.3"));
    }

    // ── satisfies: comparison operators ─────────────────────────────────

    @Test
    void satisfies_greaterOrEqual() {
        assertTrue(calc.satisfies("1.2.3", ">=1.2.3"));
        assertTrue(calc.satisfies("2.0.0", ">=1.2.3"));
        assertFalse(calc.satisfies("1.2.2", ">=1.2.3"));
    }

    @Test
    void satisfies_lessOrEqual() {
        assertTrue(calc.satisfies("1.2.3", "<=1.2.3"));
        assertTrue(calc.satisfies("1.0.0", "<=1.2.3"));
        assertFalse(calc.satisfies("1.2.4", "<=1.2.3"));
    }

    @Test
    void satisfies_greaterThan() {
        assertTrue(calc.satisfies("1.2.4", ">1.2.3"));
        assertFalse(calc.satisfies("1.2.3", ">1.2.3"));
    }

    @Test
    void satisfies_lessThan() {
        assertTrue(calc.satisfies("1.2.2", "<1.2.3"));
        assertFalse(calc.satisfies("1.2.3", "<1.2.3"));
    }

    // ── satisfies: compound ranges ──────────────────────────────────────

    @Test
    void satisfies_compoundExactRange() {
        // Two exact versions separated by space: both must match (impossible)
        assertFalse(calc.satisfies("1.5.0", "1.0.0 2.0.0"));
    }

    @Test
    void satisfies_compoundExactAndExact() {
        // Same version twice → both match
        assertTrue(calc.satisfies("1.2.3", "1.2.3 1.2.3"));
    }

    @Test
    void satisfies_compoundRange() {
        assertTrue(calc.satisfies("1.5.0", ">=1.2.3 <=1.9.9"));
        assertTrue(calc.satisfies("1.2.3", ">=1.2.3 <=1.9.9"));
        assertTrue(calc.satisfies("1.9.9", ">=1.2.3 <=1.9.9"));
        assertFalse(calc.satisfies("1.1.0", ">=1.2.3 <=1.9.9"));
        assertFalse(calc.satisfies("2.0.0", ">=1.2.3 <=1.9.9"));
    }

    @Test
    void satisfies_compoundWithExactFirst() {
        // "1.2.3 >=1.2.3" — whitespace split → ["1.2.3", ">=1.2.3"]
        // First part exact match, second >= → both satisfied for version 1.2.3
        assertTrue(calc.satisfies("1.2.3", "1.2.3 >=1.2.3"));
        assertFalse(calc.satisfies("1.2.2", "1.2.3 >=1.2.3"));
    }

    // ── satisfies: unknown format ───────────────────────────────────────

    @Test
    void satisfies_unknownFormat_throws() {
        assertThrows(IllegalArgumentException.class,
                () -> calc.satisfies("1.2.3", "??"));
    }

    // ── bump ────────────────────────────────────────────────────────────

    @Test
    void bump_major() {
        assertEquals("2.0.0", calc.bump("1.2.3", "major"));
    }

    @Test
    void bump_minor() {
        assertEquals("1.3.0", calc.bump("1.2.3", "minor"));
    }

    @Test
    void bump_patch() {
        assertEquals("1.2.4", calc.bump("1.2.3", "patch"));
    }

    @Test
    void bump_majorFromZero() {
        assertEquals("1.0.0", calc.bump("0.9.5", "major"));
    }

    @Test
    void bump_minorResetsPatch() {
        assertEquals("2.1.0", calc.bump("2.0.9", "minor"));
    }

    @Test
    void bump_majorResetsAll() {
        assertEquals("3.0.0", calc.bump("2.9.9", "major"));
    }

    @Test
    void bump_caseInsensitive() {
        assertEquals("2.0.0", calc.bump("1.2.3", "MAJOR"));
    }

    @Test
    void bump_invalidBump_throws() {
        assertThrows(IllegalArgumentException.class,
                () -> calc.bump("1.2.3", "pre"));
    }

    @Test
    void bump_stripsPrerelease() {
        // bumping strips prerelease and build
        assertEquals("2.0.0", calc.bump("1.2.3-alpha+build", "major"));
    }

    // ── bump: v-prefix ─────────────────────────────────────────────────

    @Test
    void bump_vPrefix_major() {
        assertEquals("2.0.0", calc.bump("v1.2.3", "major"));
    }

    @Test
    void bump_vPrefix_minor() {
        assertEquals("1.3.0", calc.bump("v1.2.3", "minor"));
    }

    @Test
    void bump_vPrefix_patch() {
        assertEquals("1.2.4", calc.bump("v1.2.3", "patch"));
    }

    @Test
    void bump_vPrefix_withPrerelease() {
        assertEquals("2.0.0", calc.bump("v1.2.3-rc.1", "major"));
    }

    // ── parse: complex prerelease identifiers ───────────────────────────

    @Test
    void parse_complexPrerelease() {
        SemverCalculator.Parsed p = calc.parse("1.0.0-alpha.1.beta.2");
        assertEquals(1, p.major);
        assertEquals(0, p.minor);
        assertEquals(0, p.patch);
        assertEquals("alpha.1.beta.2", p.prerelease);
        assertNull(p.build);
    }

    @Test
    void parse_prereleaseWithLeadingZeros() {
        SemverCalculator.Parsed p = calc.parse("1.0.0-01");
        assertEquals("01", p.prerelease);
    }

    @Test
    void parse_prereleaseHyphenated() {
        SemverCalculator.Parsed p = calc.parse("1.0.0-alpha-beta");
        assertEquals("alpha-beta", p.prerelease);
    }

    @Test
    void parse_buildWithDots() {
        SemverCalculator.Parsed p = calc.parse("1.0.0+build.1.2.3");
        assertEquals("build.1.2.3", p.build);
    }

    // ── compare: complex prerelease ─────────────────────────────────────

    @Test
    void compare_prereleaseComplexNumeric() {
        // 1.0.0-1.2 < 1.0.0-1.10 (numeric comparison per identifier)
        assertEquals(-1, calc.compare("1.0.0-1.2", "1.0.0-1.10"));
    }

    @Test
    void compare_prereleaseComplexMixed() {
        // 1.0.0-alpha.1 < 1.0.0-alpha.2
        assertEquals(-1, calc.compare("1.0.0-alpha.1", "1.0.0-alpha.2"));
    }

    @Test
    void compare_prereleaseComplexMixedNumericVsAlpha() {
        // 1.0.0-alpha.1 < 1.0.0-alpha.beta (numeric < alpha)
        assertEquals(-1, calc.compare("1.0.0-alpha.1", "1.0.0-alpha.beta"));
    }

    @Test
    void compare_prereleaseComplexAlphaVsNumeric() {
        assertEquals(1, calc.compare("1.0.0-alpha.beta", "1.0.0-alpha.1"));
    }

    @Test
    void compare_prereleaseComplexMoreFields() {
        // 1.0.0-alpha.1 < 1.0.0-alpha.1.1 (more fields = higher)
        assertEquals(-1, calc.compare("1.0.0-alpha.1", "1.0.0-alpha.1.1"));
    }

    @Test
    void compare_prereleaseComplexFewerFields() {
        assertEquals(1, calc.compare("1.0.0-alpha.1.1", "1.0.0-alpha.1"));
    }

    @Test
    void compare_prereleaseComplexEqual() {
        assertEquals(0, calc.compare("1.0.0-alpha.1.beta", "1.0.0-alpha.1.beta"));
    }

    // ── satisfies: prerelease in ranges ─────────────────────────────────

    @Test
    void satisfies_caretWithPrerelease() {
        // ^1.0.0-alpha means >=1.0.0-alpha <2.0.0
        assertTrue(calc.satisfies("1.0.0-alpha", "^1.0.0-alpha"));
        assertTrue(calc.satisfies("1.0.0-alpha.1", "^1.0.0-alpha"));
        assertTrue(calc.satisfies("1.0.0", "^1.0.0-alpha"));
        assertTrue(calc.satisfies("1.9.9", "^1.0.0-alpha"));
        assertFalse(calc.satisfies("2.0.0", "^1.0.0-alpha"));
    }

    @Test
    void satisfies_tildeWithPrerelease() {
        // ~1.2.3-alpha means >=1.2.3-alpha <1.3.0
        assertTrue(calc.satisfies("1.2.3-alpha", "~1.2.3-alpha"));
        assertTrue(calc.satisfies("1.2.3-alpha.1", "~1.2.3-alpha"));
        assertTrue(calc.satisfies("1.2.3", "~1.2.3-alpha"));
        assertTrue(calc.satisfies("1.2.9", "~1.2.3-alpha"));
        assertFalse(calc.satisfies("1.3.0", "~1.2.3-alpha"));
    }

    @Test
    void satisfies_greaterOrEqualWithPrerelease() {
        assertTrue(calc.satisfies("1.0.0-alpha", ">=1.0.0-alpha"));
        assertTrue(calc.satisfies("1.0.0-alpha.1", ">=1.0.0-alpha"));
        assertTrue(calc.satisfies("1.0.0", ">=1.0.0-alpha"));
        // 1.0.0-beta > 1.0.0-alpha, so it satisfies >=1.0.0-alpha
        assertTrue(calc.satisfies("1.0.0-beta", ">=1.0.0-alpha"));
    }

    @Test
    void satisfies_lessOrEqualWithPrerelease() {
        assertTrue(calc.satisfies("1.0.0-alpha", "<=1.0.0"));
        assertTrue(calc.satisfies("1.0.0-alpha.1", "<=1.0.0"));
        assertFalse(calc.satisfies("1.0.1", "<=1.0.0"));
    }

    @Test
    void satisfies_compoundWithPrerelease() {
        // >=1.0.0-alpha <1.0.0 — prerelease range
        assertTrue(calc.satisfies("1.0.0-alpha", ">=1.0.0-alpha <1.0.0"));
        assertTrue(calc.satisfies("1.0.0-alpha.1", ">=1.0.0-alpha <1.0.0"));
        assertTrue(calc.satisfies("1.0.0-beta", ">=1.0.0-alpha <1.0.0"));
        assertFalse(calc.satisfies("1.0.0", ">=1.0.0-alpha <1.0.0"));
        // 1.0.0-alpha satisfies >=1.0.0-alpha AND <1.0.0-alpha.1, so it's true
        assertTrue(calc.satisfies("1.0.0-alpha", ">=1.0.0-alpha <1.0.0-alpha.1"));
        // 1.0.0-alpha.1 does NOT satisfy <1.0.0-alpha.1
        assertFalse(calc.satisfies("1.0.0-alpha.1", ">=1.0.0-alpha <1.0.0-alpha.1"));
    }

    @Test
    void satisfies_exactWithBuild() {
        // Build metadata is ignored in precedence, so exact match should work
        assertTrue(calc.satisfies("1.2.3+build.1", "1.2.3"));
        assertTrue(calc.satisfies("1.2.3", "1.2.3+build.1"));
    }

    @Test
    void satisfies_exactWithPrereleaseAndBuild() {
        assertTrue(calc.satisfies("1.2.3-alpha+build.1", "1.2.3-alpha"));
        assertTrue(calc.satisfies("1.2.3-alpha", "1.2.3-alpha+build.1"));
    }
}
