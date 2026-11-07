package com.sheahorn.llmtoolbox.buildtools.mvn;

import com.sheahorn.llmtoolbox.buildtools.AbstractFamilyLockService;
import com.sheahorn.llmtoolbox.buildtools.BuildLockSettings;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
public class MvnLockService extends AbstractFamilyLockService {

    /** ArC needs a non-private no-args ctor: normal-scoped beans are client-proxied. */
    public MvnLockService() {
        super("mvn");
    }

    @Inject
    public MvnLockService(BuildLockSettings settings) {
        super("mvn", settings);
    }
}
