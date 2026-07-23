package com.sheahorn.llmtoolbox.config;

import io.quarkus.narayana.jta.QuarkusTransaction;
import io.quarkus.runtime.Startup;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.jboss.logging.Logger;

@Startup
@ApplicationScoped
public class CustomFunctionStartupBean {

    private static final Logger LOG = Logger.getLogger(CustomFunctionStartupBean.class);

    @PersistenceContext
    EntityManager em;

    @PostConstruct
    void init() {
        QuarkusTransaction.run(() -> {
            em.createNativeQuery("""
                CREATE TABLE IF NOT EXISTS custom_functions (
                    id VARCHAR(36) NOT NULL PRIMARY KEY,
                    operationId VARCHAR(256) NOT NULL UNIQUE,
                    description VARCHAR(1024),
                    shellCommand VARCHAR(4096) NOT NULL
                )
            """).executeUpdate();
            LOG.info("Ensured custom_functions table exists");
        });
    }
}
