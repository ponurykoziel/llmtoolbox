package com.sheahorn.llmtoolbox.buildtools.cargo;

import com.sheahorn.llmtoolbox.buildtools.AbstractFamilyLockService;
import com.sheahorn.llmtoolbox.buildtools.BuildLockSettings;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
public class CargoLockService extends AbstractFamilyLockService {

    /** ArC needs a non-private no-args ctor: normal-scoped beans are client-proxied. */
    public CargoLockService() {
        super("cargo");
    }

    @Inject
    public CargoLockService(BuildLockSettings settings) {
        super("cargo", settings);
    }
}
