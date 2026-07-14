package com.sheahorn.llmtoolbox.config;

import com.sheahorn.llmtoolbox.domain.User;
import io.quarkus.elytron.security.common.BcryptUtil;
import io.quarkus.narayana.jta.QuarkusTransaction;
import io.quarkus.runtime.Startup;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logging.Logger;

import java.util.Locale;

@Startup
@ApplicationScoped
public class AuthStartupBean {

    private static final Logger LOG = Logger.getLogger(AuthStartupBean.class);

    @ConfigProperty(name = "llmtoolbox.auth.admin.username", defaultValue = "admin")
    String adminUsername;

    @ConfigProperty(name = "llmtoolbox.auth.admin.password", defaultValue = "admin")
    String adminPassword;

    @ConfigProperty(name = "llmtoolbox.auth.seed-on-startup", defaultValue = "true")
    boolean seedOnStartup;

    @PostConstruct
    void init() {
        Locale.setDefault(Locale.US);
        QuarkusTransaction.run(() -> {
            if (!seedOnStartup) {
                LOG.debug("Seed-on-startup disabled; skipping admin user check.");
                return;
            }
            if (User.findByUsername(adminUsername) == null) {
                User admin = User.create(adminUsername, BcryptUtil.bcryptHash(adminPassword), "admin");
                admin.persist();
                LOG.infof("Seeded default admin user (%s)", adminUsername);
            }
        });
    }
}
