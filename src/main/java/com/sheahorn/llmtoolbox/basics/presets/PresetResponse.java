package com.sheahorn.llmtoolbox.basics.presets;

import java.util.List;

public class PresetResponse {
    public String name;
    public List<String> prefixes;
    public boolean builtin;

    public PresetResponse() {}

    public PresetResponse(String name, List<String> prefixes) {
        this.name = name;
        this.prefixes = prefixes;
        this.builtin = PresetDefaults.isDefault(name);
    }

    public static PresetResponse from(Preset preset) {
        return new PresetResponse(preset.name, preset.prefixList());
    }
}
