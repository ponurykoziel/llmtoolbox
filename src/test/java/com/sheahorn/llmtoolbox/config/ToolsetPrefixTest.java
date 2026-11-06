package com.sheahorn.llmtoolbox.config;

import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class ToolsetPrefixTest {

    private ToolsetPrefix prefix(String configured) {
        ToolsetPrefix tp = new ToolsetPrefix();
        tp.configuredPrefix = Optional.ofNullable(configured);
        return tp;
    }

    @Test
    void emptyPrefixIsDisabled() {
        ToolsetPrefix tp = prefix("");
        assertFalse(tp.enabled());
        assertEquals("fs_files_read", tp.apply("fs_files_read"));
    }

    @Test
    void nullPrefixIsDisabled() {
        ToolsetPrefix tp = prefix(null);
        assertFalse(tp.enabled());
        assertEquals("fs_files_read", tp.apply("fs_files_read"));
    }

    @Test
    void whitespaceOnlyPrefixIsDisabled() {
        ToolsetPrefix tp = prefix("   ");
        assertFalse(tp.enabled());
        assertEquals("fs_files_read", tp.apply("fs_files_read"));
    }

    @Test
    void appliesPrefixWithUnderscore() {
        ToolsetPrefix tp = prefix("acme");
        assertTrue(tp.enabled());
        assertEquals("acme_fs_files_read", tp.apply("fs_files_read"));
    }

    @Test
    void allowsAlphanumericAndUnderscore() {
        ToolsetPrefix tp = prefix("Acme_2024");
        assertTrue(tp.enabled());
        assertEquals("Acme_2024_fs_files_read", tp.apply("fs_files_read"));
    }

    @Test
    void invalidCharactersDisablePrefix() {
        ToolsetPrefix tp = prefix("acme-tools");
        assertFalse(tp.enabled());
        assertEquals("fs_files_read", tp.apply("fs_files_read"));
    }

    @Test
    void invalidSpaceDisablesPrefix() {
        ToolsetPrefix tp = prefix("acme tools");
        assertFalse(tp.enabled());
        assertFalse(tp.enabled());
    }

    @Test
    void trimsSurroundingWhitespace() {
        ToolsetPrefix tp = prefix("  acme  ");
        assertTrue(tp.enabled());
        assertEquals("acme_fs_files_read", tp.apply("fs_files_read"));
    }
}
