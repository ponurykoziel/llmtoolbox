package com.sheahorn.llmtoolbox.buildtools.gradle;

import com.sheahorn.llmtoolbox.buildtools.AbstractFamilyLockService;
import com.sheahorn.llmtoolbox.buildtools.BuildLockSettings;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
public class GradleLockService extends AbstractFamilyLockService {

    /** ArC needs a non-private no-args ctor: normal-scoped beans are client-proxied. */
    public GradleLockService() {
        super("gradle");
    }

    @Inject
    public GradleLockService(BuildLockSettings settings) {
        super("gradle", settings);
    }
}
