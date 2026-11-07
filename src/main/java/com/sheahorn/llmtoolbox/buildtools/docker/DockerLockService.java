package com.sheahorn.llmtoolbox.buildtools.docker;

import com.sheahorn.llmtoolbox.buildtools.AbstractFamilyLockService;
import com.sheahorn.llmtoolbox.buildtools.BuildLockSettings;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
public class DockerLockService extends AbstractFamilyLockService {

    /** ArC needs a non-private no-args ctor: normal-scoped beans are client-proxied. */
    public DockerLockService() {
        super("docker");
    }

    @Inject
    public DockerLockService(BuildLockSettings settings) {
        super("docker", settings);
    }
}
