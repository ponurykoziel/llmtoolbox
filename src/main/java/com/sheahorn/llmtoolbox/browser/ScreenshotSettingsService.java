package com.sheahorn.llmtoolbox.browser;

import jakarta.enterprise.context.ApplicationScoped;

/**
 * Stateful, in-memory holder for the current screenshot settings.
 * Resets to defaults on restart.
 */
@ApplicationScoped
public class ScreenshotSettingsService {

    private volatile ScreenshotSettings current = new ScreenshotSettings(
            ScreenshotSettings.FORMAT_PNG, 6, 0);

    public ScreenshotSettings get() {
        return current;
    }

    public ScreenshotSettings update(String format, int compression, long maxBytes) {
        ScreenshotSettings updated = new ScreenshotSettings(format, compression, maxBytes);
        this.current = updated;
        return updated;
    }
}
