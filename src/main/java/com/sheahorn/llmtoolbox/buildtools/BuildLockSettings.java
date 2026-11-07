package com.sheahorn.llmtoolbox.buildtools;

import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.config.inject.ConfigProperty;

/**
 * Build-lock configuration, read once and shared by all five families.
 *
 * Deliberately one bean instead of five copies of the same @ConfigProperty pair: the
 * families can never drift, and tests can construct it with explicit values.
 *
 * - llmtoolbox.build.lock-mode: synchronize (queue up, the default) | reject (fail fast)
 * - llmtoolbox.build.locks.enabled: false turns every family into a pass-through
 */
@ApplicationScoped
public class BuildLockSettings {

    public static final String MODE_SYNCHRONIZE = "synchronize";
    public static final String MODE_REJECT = "reject";
    public static final String MODE_DISABLED = "disabled";

    @ConfigProperty(name = "llmtoolbox.build.lock-mode", defaultValue = MODE_SYNCHRONIZE)
    String mode = MODE_SYNCHRONIZE;

    @ConfigProperty(name = "llmtoolbox.build.locks.enabled", defaultValue = "true")
    boolean enabled = true;

    public BuildLockSettings() {
    }

    public BuildLockSettings(String mode, boolean enabled) {
        this.mode = mode;
        this.enabled = enabled;
    }

    public String mode() {
        return mode == null ? MODE_SYNCHRONIZE : mode;
    }

    public boolean enabled() {
        return enabled;
    }

    public boolean reject() {
        return MODE_REJECT.equalsIgnoreCase(mode());
    }
}
