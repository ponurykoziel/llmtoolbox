package com.sheahorn.llmtoolbox.calculators;

import com.sheahorn.llmtoolbox.calculators.TextCalculator;
import com.sheahorn.llmtoolbox.calculators.TextNormalizationOptions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class TextCalculatorTest {

    private TextCalculator calc;
    private TextNormalizationOptions defaultOpts;
    private TextNormalizationOptions diffOpts; // no collapse_whitespace for line-based diff

    @BeforeEach
    void setUp() {
        calc = new TextCalculator(8);
        defaultOpts = new TextNormalizationOptions();
        defaultOpts.case_sensitive = false;
        defaultOpts.trim = true;
        defaultOpts.normalize_unicode = true;
        defaultOpts.remove_punctuation = false;
        defaultOpts.collapse_whitespace = true;

        diffOpts = new TextNormalizationOptions();
        diffOpts.case_sensitive = false;
        diffOpts.trim = true;
        diffOpts.normalize_unicode = true;
        diffOpts.remove_punctuation = false;
        diffOpts.collapse_whitespace = false; // keep newlines for diff
    }

    // ── normalize ───────────────────────────────────────────────────────

    @Test
    void normalize_trim() {
        TextNormalizationOptions opts = new TextNormalizationOptions();
        opts.trim = true;
        opts.case_sensitive = true;
        opts.normalize_unicode = false;
        opts.collapse_whitespace = false;
        assertEquals("hello", calc.normalize("  hello  ", opts));
    }

    @Test
    void normalize_lowercase() {
        TextNormalizationOptions opts = new TextNormalizationOptions();
        opts.case_sensitive = false;
        opts.trim = false;
        opts.normalize_unicode = false;
        opts.collapse_whitespace = false;
        assertEquals("hello world", calc.normalize("Hello World", opts));
    }

    @Test
    void normalize_caseSensitive() {
        TextNormalizationOptions opts = new TextNormalizationOptions();
        opts.case_sensitive = true;
        opts.trim = false;
        opts.normalize_unicode = false;
        opts.collapse_whitespace = false;
        assertEquals("Hello World", calc.normalize("Hello World", opts));
    }

    @Test
    void normalize_collapseWhitespace() {
        TextNormalizationOptions opts = new TextNormalizationOptions();
        opts.collapse_whitespace = true;
        opts.trim = true;
        opts.case_sensitive = true;
        opts.normalize_unicode = false;
        assertEquals("a b c", calc.normalize("a   b    c", opts));
    }

    @Test
    void normalize_removePunctuation() {
        TextNormalizationOptions opts = new TextNormalizationOptions();
        opts.remove_punctuation = true;
        opts.trim = false;
        opts.case_sensitive = true;
        opts.normalize_unicode = false;
        opts.collapse_whitespace = false;
        assertEquals("Hello World", calc.normalize("Hello, World!", opts));
    }

    @Test
    void normalize_nullInput() {
        assertEquals("", calc.normalize(null, defaultOpts));
    }

    @Test
    void normalize_nullOpts() {
        String result = calc.normalize("  Hello  World  ", null);
        assertEquals("hello world", result);
    }

    @Test
    void normalize_unicode() {
        TextNormalizationOptions opts = new TextNormalizationOptions();
        opts.normalize_unicode = true;
        opts.case_sensitive = true;
        opts.trim = false;
        opts.collapse_whitespace = false;
        String result = calc.normalize("\uFF21\uFF22\uFF23", opts);
        assertEquals("ABC", result);
    }

    // ── levenshtein ─────────────────────────────────────────────────────

    @Test
    void levenshtein_identical() {
        assertEquals(0.0, calc.levenshtein("hello", "hello", defaultOpts));
    }

    @Test
    void levenshtein_completelyDifferent() {
        assertEquals(5.0, calc.levenshtein("abcde", "vwxyz", defaultOpts));
    }

    @Test
    void levenshtein_oneSubstitution() {
        assertEquals(1.0, calc.levenshtein("kitten", "sitten", defaultOpts));
    }

    @Test
    void levenshtein_oneInsertion() {
        assertEquals(1.0, calc.levenshtein("cat", "cats", defaultOpts));
    }

    @Test
    void levenshtein_oneDeletion() {
        assertEquals(1.0, calc.levenshtein("cats", "cat", defaultOpts));
    }

    @Test
    void levenshtein_emptyStrings() {
        assertEquals(0.0, calc.levenshtein("", "", defaultOpts));
    }

    @Test
    void levenshtein_oneEmpty() {
        assertEquals(5.0, calc.levenshtein("hello", "", defaultOpts));
    }

    @Test
    void levenshtein_caseInsensitive() {
        assertEquals(0.0, calc.levenshtein("Hello", "hello", defaultOpts));
    }

    @Test
    void levenshtein_whitespaceNormalized() {
        assertEquals(0.0, calc.levenshtein("hello   world", "hello world", defaultOpts));
    }

    // ── normalizedLevenshtein ────────────────────────────────────────────

    @Test
    void normalizedLevenshtein_identical() {
        assertEquals(1.0, calc.normalizedLevenshtein("hello", "hello", defaultOpts));
    }

    @Test
    void normalizedLevenshtein_completelyDifferent() {
        assertEquals(0.0, calc.normalizedLevenshtein("abcde", "vwxyz", defaultOpts));
    }

    @Test
    void normalizedLevenshtein_halfSimilar() {
        assertEquals(0.57142857, calc.normalizedLevenshtein("kitten", "sitting", defaultOpts), 1e-8);
    }

    @Test
    void normalizedLevenshtein_bothEmpty() {
        assertEquals(1.0, calc.normalizedLevenshtein("", "", defaultOpts));
    }

    @Test
    void normalizedLevenshtein_oneEmpty() {
        assertEquals(0.0, calc.normalizedLevenshtein("hello", "", defaultOpts));
    }

    // ── jaccard ──────────────────────────────────────────────────────────

    @Test
    void jaccard_identical() {
        assertEquals(1.0, calc.jaccard("hello", "hello", defaultOpts));
    }

    @Test
    void jaccard_disjoint() {
        assertEquals(0.0, calc.jaccard("abc", "xyz", defaultOpts));
    }

    @Test
    void jaccard_partial() {
        assertEquals(0.5, calc.jaccard("abc", "bcd", defaultOpts));
    }

    @Test
    void jaccard_bothEmpty() {
        assertEquals(1.0, calc.jaccard("", "", defaultOpts));
    }

    @Test
    void jaccard_caseInsensitive() {
        assertEquals(1.0, calc.jaccard("ABC", "abc", defaultOpts));
    }

    // ── ngramOverlap ─────────────────────────────────────────────────────

    @Test
    void ngramOverlap_identical_bigram() {
        assertEquals(1.0, calc.ngramOverlap("hello", "hello", 2, defaultOpts));
    }

    @Test
    void ngramOverlap_disjoint_bigram() {
        assertEquals(0.0, calc.ngramOverlap("abc", "xyz", 2, defaultOpts));
    }

    @Test
    void ngramOverlap_partial_trigram() {
        assertEquals(0.2, calc.ngramOverlap("hello", "hallo", 3, defaultOpts));
    }

    @Test
    void ngramOverlap_n1() {
        assertEquals(0.5, calc.ngramOverlap("abc", "bcd", 1, defaultOpts));
    }

    @Test
    void ngramOverlap_bothEmpty() {
        assertEquals(1.0, calc.ngramOverlap("", "", 2, defaultOpts));
    }

    @Test
    void ngramOverlap_nTooSmall_throws() {
        assertThrows(IllegalArgumentException.class,
                () -> calc.ngramOverlap("a", "b", 0, defaultOpts));
    }

    @Test
    void ngramOverlap_shortString() {
        assertEquals(1.0, calc.ngramOverlap("a", "a", 2, defaultOpts));
    }

    // ── fuzzyMatch ──────────────────────────────────────────────────────

    @Test
    void fuzzyMatch_withinThreshold() {
        assertEquals(1.0, calc.fuzzyMatch("hello", "hallo", 2, defaultOpts));
    }

    @Test
    void fuzzyMatch_outsideThreshold() {
        assertEquals(0.0, calc.fuzzyMatch("hello", "hallo", 0, defaultOpts));
    }

    @Test
    void fuzzyMatch_identical() {
        assertEquals(1.0, calc.fuzzyMatch("hello", "hello", 0, defaultOpts));
    }

    @Test
    void fuzzyMatch_lengthDifferenceExceedsThreshold() {
        assertEquals(0.0, calc.fuzzyMatch("hi", "hello", 2, defaultOpts));
    }

    @Test
    void fuzzyMatch_emptyStrings() {
        assertEquals(1.0, calc.fuzzyMatch("", "", 0, defaultOpts));
    }

    @Test
    void fuzzyMatch_oneEmpty() {
        assertEquals(0.0, calc.fuzzyMatch("hello", "", 1, defaultOpts));
    }

    // ── countOccurrences ─────────────────────────────────────────────────

    @Test
    void countOccurrences_multiple() {
        assertEquals(1, calc.countOccurrences("banana", "ana", defaultOpts));
    }

    @Test
    void countOccurrences_none() {
        assertEquals(0, calc.countOccurrences("hello", "xyz", defaultOpts));
    }

    @Test
    void countOccurrences_nonOverlapping() {
        assertEquals(1, calc.countOccurrences("aaaa", "aaa", defaultOpts));
    }

    @Test
    void countOccurrences_caseInsensitive() {
        assertEquals(2, calc.countOccurrences("Hello hello", "hello", defaultOpts));
    }

    @Test
    void countOccurrences_emptySubstring_throws() {
        assertThrows(IllegalArgumentException.class,
                () -> calc.countOccurrences("hello", "", defaultOpts));
    }

    @Test
    void countOccurrences_wholeString() {
        assertEquals(1, calc.countOccurrences("hello", "hello", defaultOpts));
    }

    @Test
    void countOccurrences_nonOverlappingMultiple() {
        assertEquals(3, calc.countOccurrences("ababab", "ab", defaultOpts));
    }

    // ── diff ─────────────────────────────────────────────────────────────

    @Test
    void diff_identical() {
        String result = calc.diff("line1\nline2\nline3", "line1\nline2\nline3", diffOpts);
        String[] lines = result.split("\n");
        assertEquals(3, lines.length);
        assertTrue(lines[0].startsWith("  "));
        assertTrue(lines[1].startsWith("  "));
        assertTrue(lines[2].startsWith("  "));
    }

    @Test
    void diff_addedLine() {
        String result = calc.diff("line1\nline2", "line1\nline2\nline3", diffOpts);
        String[] lines = result.split("\n");
        assertEquals(3, lines.length);
        assertTrue(result.contains("+ line3"));
    }

    @Test
    void diff_removedLine() {
        String result = calc.diff("line1\nline2\nline3", "line1\nline3", diffOpts);
        assertTrue(result.contains("- line2"));
    }

    @Test
    void diff_changedLine() {
        String result = calc.diff("line1\nold line\nline3", "line1\nnew line\nline3", diffOpts);
        assertTrue(result.contains("- old line"));
        assertTrue(result.contains("+ new line"));
    }

    @Test
    void diff_empty() {
        String result = calc.diff("", "", diffOpts);
        assertEquals("  ", result);
    }

    @Test
    void diff_firstEmpty() {
        String result = calc.diff("", "line1\nline2", diffOpts);
        assertTrue(result.contains("+ line1"));
        assertTrue(result.contains("+ line2"));
    }

    @Test
    void diff_secondEmpty() {
        String result = calc.diff("line1\nline2", "", diffOpts);
        assertTrue(result.contains("- line1"));
        assertTrue(result.contains("- line2"));
    }

    @Test
    void diff_caseInsensitive() {
        String result = calc.diff("Hello\nWorld", "hello\nworld", diffOpts);
        String[] lines = result.split("\n");
        assertEquals(2, lines.length);
        assertTrue(lines[0].startsWith("  "));
        assertTrue(lines[1].startsWith("  "));
    }

    // ── diffRaw ──────────────────────────────────────────────────────────

    @Test
    void diffRaw_identical() {
        String result = calc.diffRaw("line1\nline2\nline3", "line1\nline2\nline3");
        String[] lines = result.split("\n");
        assertEquals(3, lines.length);
        assertTrue(lines[0].startsWith("  "));
        assertTrue(lines[1].startsWith("  "));
        assertTrue(lines[2].startsWith("  "));
    }

    @Test
    void diffRaw_addedLine() {
        String result = calc.diffRaw("line1\nline2", "line1\nline2\nline3");
        assertTrue(result.contains("+ line3"));
    }

    @Test
    void diffRaw_removedLine() {
        String result = calc.diffRaw("line1\nline2\nline3", "line1\nline3");
        assertTrue(result.contains("- line2"));
    }

    @Test
    void diffRaw_changedLine() {
        String result = calc.diffRaw("line1\nold line\nline3", "line1\nnew line\nline3");
        assertTrue(result.contains("- old line"));
        assertTrue(result.contains("+ new line"));
    }

    @Test
    void diffRaw_preservesCase() {
        // diffRaw does NOT normalize, so case differences should show as changes
        String result = calc.diffRaw("Hello\nWorld", "hello\nworld");
        assertTrue(result.contains("- Hello"));
        assertTrue(result.contains("+ hello"));
    }

    @Test
    void diffRaw_preservesWhitespace() {
        String result = calc.diffRaw("line1  \nline2", "line1\nline2");
        assertTrue(result.contains("- line1  "));
        assertTrue(result.contains("+ line1"));
    }

    @Test
    void diffRaw_nullInputs() {
        String result = calc.diffRaw(null, null);
        assertEquals("  ", result);
    }

    @Test
    void diffRaw_firstNull() {
        String result = calc.diffRaw(null, "line1\nline2");
        assertTrue(result.contains("+ line1"));
        assertTrue(result.contains("+ line2"));
    }

    @Test
    void diffRaw_secondNull() {
        String result = calc.diffRaw("line1\nline2", null);
        assertTrue(result.contains("- line1"));
        assertTrue(result.contains("- line2"));
    }

    // ── rounding precision ──────────────────────────────────────────────

    @Test
    void rounding_8digits() {
        assertEquals(0.66666667, calc.jaccard("ab", "abc", defaultOpts), 1e-8);
    }
}
