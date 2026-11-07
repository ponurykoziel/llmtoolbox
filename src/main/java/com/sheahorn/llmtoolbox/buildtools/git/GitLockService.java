package com.sheahorn.llmtoolbox.buildtools.git;

import com.sheahorn.llmtoolbox.buildtools.AbstractFamilyLockService;
import com.sheahorn.llmtoolbox.buildtools.BuildLockSettings;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
public class GitLockService extends AbstractFamilyLockService {

    /** ArC needs a non-private no-args ctor: normal-scoped beans are client-proxied. */
    public GitLockService() {
        super("git");
    }

    @Inject
    public GitLockService(BuildLockSettings settings) {
        super("git", settings);
    }
}
