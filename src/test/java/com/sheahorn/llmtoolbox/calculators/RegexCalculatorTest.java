package com.sheahorn.llmtoolbox.calculators;

import com.sheahorn.llmtoolbox.calculators.RegexCalculator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class RegexCalculatorTest {

    private RegexCalculator calc;

    @BeforeEach
    void setUp() {
        calc = new RegexCalculator();
    }

    // ── regexTester ────────────────────────────────────────────────────

    @Test
    void regexTester_matchWithGroups() {
        String result = calc.regexTester("(\\w+)@(\\w+)", "hello@world", null);
        assertTrue(result.contains("Match at 0-10"));
        assertTrue(result.contains("\"hello@world\""));
        assertTrue(result.contains("groups: [\"hello\", \"world\"]"));
    }

    @Test
    void regexTester_noMatch() {
        String result = calc.regexTester("\\d+", "hello world", null);
        assertEquals("No matches found.", result);
    }

    @Test
    void regexTester_multipleMatches() {
        // "abc 123 def 456": '1' at 4, '3' at 6; '4' at 12, '6' at 14
        String result = calc.regexTester("\\d+", "abc 123 def 456", null);
        assertTrue(result.contains("Match at 4-6"));
        assertTrue(result.contains("Match at 12-14"));
    }

    @Test
    void regexTester_caseInsensitive() {
        String result = calc.regexTester("hello", "HELLO world", "i");
        assertTrue(result.contains("Match at 0-4"));
    }

    @Test
    void regexTester_noGroups() {
        String result = calc.regexTester("cat", "the cat sat", null);
        assertTrue(result.contains("Match at 4-6"));
        assertFalse(result.contains("groups:"));
    }

    @Test
    void regexTester_emptyText() {
        String result = calc.regexTester(".*", "", null);
        assertTrue(result.contains("Match at 0--1")); // empty string match
    }

    // ── regexReplace ────────────────────────────────────────────────────

    @Test
    void regexReplace_replaceAll() {
        String result = calc.regexReplace("\\d", "a1b2c3", "X", null, true);
        assertEquals("aXbXcX", result);
    }

    @Test
    void regexReplace_replaceFirst() {
        String result = calc.regexReplace("\\d", "a1b2c3", "X", null, false);
        assertEquals("aXb2c3", result);
    }

    @Test
    void regexReplace_noMatch() {
        String result = calc.regexReplace("\\d+", "hello world", "X", null, true);
        assertEquals("hello world", result);
    }

    @Test
    void regexReplace_nullReplacement() {
        String result = calc.regexReplace("\\d", "a1b2c3", null, null, true);
        assertEquals("abc", result);
    }

    @Test
    void regexReplace_caseInsensitive() {
        String result = calc.regexReplace("hello", "Hello World Hello", "Hi", "i", true);
        assertEquals("Hi World Hi", result);
    }

    @Test
    void regexReplace_withGroups() {
        // swap first and last name: "John Doe" → "Doe, John"
        String result = calc.regexReplace("(\\w+) (\\w+)", "John Doe", "$2, $1", null, true);
        assertEquals("Doe, John", result);
    }

    // ── regexValidate ───────────────────────────────────────────────────

    @Test
    void regexValidate_valid() {
        assertEquals("valid", calc.regexValidate("\\d+", null));
    }

    @Test
    void regexValidate_validWithFlags() {
        assertEquals("valid", calc.regexValidate("hello", "i"));
    }

    @Test
    void regexValidate_invalidPattern() {
        String result = calc.regexValidate("[unclosed", null);
        assertTrue(result.startsWith("invalid:"));
    }

    @Test
    void regexValidate_invalidFlag_throws() {
        assertThrows(IllegalArgumentException.class,
                () -> calc.regexValidate("hello", "z"));
    }

    // ── regexSplit ──────────────────────────────────────────────────────

    @Test
    void regexSplit_simple() {
        String result = calc.regexSplit(",", "a,b,c", null, null);
        assertTrue(result.contains("[0]: \"a\""));
        assertTrue(result.contains("[1]: \"b\""));
        assertTrue(result.contains("[2]: \"c\""));
    }

    @Test
    void regexSplit_withLimit() {
        // limit=2: split into at most 2 parts
        String result = calc.regexSplit(",", "a,b,c", null, 2);
        assertTrue(result.contains("[0]: \"a\""));
        assertTrue(result.contains("[1]: \"b,c\""));
        assertFalse(result.contains("[2]:"));
    }

    @Test
    void regexSplit_noMatch() {
        String result = calc.regexSplit(",", "hello world", null, null);
        assertTrue(result.contains("[0]: \"hello world\""));
        assertFalse(result.contains("[1]:"));
    }

    @Test
    void regexSplit_emptyText() {
        String result = calc.regexSplit(",", "", null, null);
        assertTrue(result.contains("[0]: \"\""));
    }

    @Test
    void regexSplit_whitespace() {
        String result = calc.regexSplit("\\s+", "hello   world\ttest", null, null);
        assertTrue(result.contains("[0]: \"hello\""));
        assertTrue(result.contains("[1]: \"world\""));
        assertTrue(result.contains("[2]: \"test\""));
    }

    // ── regexEscape ─────────────────────────────────────────────────────

    @Test
    void regexEscape_specialChars() {
        String result = calc.regexEscape("hello.world*");
        assertEquals("\\Qhello.world*\\E", result);
    }

    @Test
    void regexEscape_normalText() {
        String result = calc.regexEscape("hello");
        assertEquals("\\Qhello\\E", result);
    }

    @Test
    void regexEscape_empty() {
        String result = calc.regexEscape("");
        assertEquals("\\Q\\E", result);
    }

    // ── multiline / dotall flags ───────────────────────────────────────

    @Test
    void regexTester_multiline() {
        // ^ matches start of each line with MULTILINE flag
        String result = calc.regexTester("^\\w+", "hello\nworld", "m");
        assertTrue(result.contains("Match at 0-4"));
        assertTrue(result.contains("Match at 6-10"));
    }

    @Test
    void regexTester_dotall() {
        // . matches newline with DOTALL flag
        String result = calc.regexTester("hello.world", "hello\nworld", "s");
        assertTrue(result.contains("Match at 0-10"));
    }

    @Test
    void regexReplace_multiline() {
        String result = calc.regexReplace("^", "line1\nline2", ">", "m", true);
        assertEquals(">line1\n>line2", result);
    }

    @Test
    void regexSplit_limitZero() {
        // limit=0: trailing empty strings are discarded (same as no limit)
        String result = calc.regexSplit(",", "a,b,", null, 0);
        assertTrue(result.contains("[0]: \"a\""));
        assertTrue(result.contains("[1]: \"b\""));
        assertFalse(result.contains("[2]:"));
    }

    // ── parseFlags errors ───────────────────────────────────────────────

    @Test
    void parseFlags_unknownFlag_throws() {
        assertThrows(IllegalArgumentException.class,
                () -> calc.regexTester("hello", "hello", "z"));
    }
}
