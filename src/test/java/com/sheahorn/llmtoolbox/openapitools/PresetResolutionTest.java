package com.sheahorn.llmtoolbox.openapitools;

import com.sheahorn.llmtoolbox.basics.presets.Preset;
import com.sheahorn.llmtoolbox.basics.presets.PresetDefaults;
import com.sheahorn.llmtoolbox.openapitools.BuiltinFunctionCache;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@QuarkusTest
class PresetResolutionTest {

    @Inject
    BuiltinFunctionCache builtinFunctionCache;

    @BeforeEach
    @Transactional
    void cleanUp() {
        Preset.findAll().stream().forEach(p -> p.delete());
    }

    @Test
    void testHardcodedPresetsExist() {
        Set<String> names = PresetDefaults.allNames();
        assertTrue(names.contains("all"));
        assertTrue(names.contains("fs"));
        assertTrue(names.contains("net"));
        assertTrue(names.contains("host"));
        assertTrue(names.contains("build"));
        assertTrue(names.contains("calculator"));
    }

    @Test
    void testResolveHardcodedPreset() {
        List<String> prefixes = PresetDefaults.resolve("fs");
        assertNotNull(prefixes);
        assertEquals(1, prefixes.size());
        assertEquals("fs_*", prefixes.get(0));
    }

    @Test
    void testResolveAllPreset() {
        List<String> prefixes = PresetDefaults.resolve("all");
        assertNotNull(prefixes);
        assertEquals(1, prefixes.size());
        assertEquals("*", prefixes.get(0));
    }

    @Test
    void testResolveUnknownPreset() {
        List<String> prefixes = PresetDefaults.resolve("nonexistent");
        assertNull(prefixes);
    }

    @Test
    void testIsHardcoded() {
        assertTrue(PresetDefaults.isHardcoded("fs"));
        assertTrue(PresetDefaults.isHardcoded("all"));
        assertFalse(PresetDefaults.isHardcoded("nonexistent"));
    }

    @Test
    @Transactional
    void testDbPresetOverridesHardcoded() {
        Preset preset = new Preset();
        preset.name = "fs";
        preset.prefixes = "custom_fs_*";
        preset.persist();

        List<String> prefixes = PresetDefaults.resolve("fs");
        assertNotNull(prefixes);
        assertEquals(1, prefixes.size());
        assertEquals("custom_fs_*", prefixes.get(0));
    }

    @Test
    @Transactional
    void testCreateAndResolveDbPreset() {
        Preset preset = new Preset();
        preset.name = "my-tools";
        preset.prefixes = "mytool_*,util_*";
        preset.persist();

        List<String> prefixes = PresetDefaults.resolve("my-tools");
        assertNotNull(prefixes);
        assertEquals(2, prefixes.size());
        assertTrue(prefixes.contains("mytool_*"));
        assertTrue(prefixes.contains("util_*"));
    }

    @Test
    void testBuiltinFunctionCacheLoaded() {
        var functions = builtinFunctionCache.all();
        assertNotNull(functions);
        assertFalse(functions.isEmpty(), "Built-in function cache should not be empty");
    }

    @Test
    void testBuiltinFunctionCacheHasExpectedFunctions() {
        var functions = builtinFunctionCache.all();
        assertTrue(functions.containsKey("fs_files_create"), "Should contain fs_files_create");
        assertTrue(functions.containsKey("fs_files_read"), "Should contain fs_files_read");
        assertTrue(functions.containsKey("presets_list_all"), "Should contain presets_list_all");
    }

    @Test
    void testBuiltinFunctionDescriptions() {
        var functions = builtinFunctionCache.all();
        functions.forEach((opId, desc) -> {
            assertNotNull(desc, "Description for " + opId + " should not be null");
        });
    }
}
