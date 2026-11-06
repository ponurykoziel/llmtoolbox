package com.sheahorn.llmtoolbox.config;

import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logging.Logger;

import java.util.Optional;
import java.util.regex.Pattern;

/**
 * Optional global prefix applied to tool operationIds in the OpenAPI subset
 * served to Open WebUI.
 *
 * <p>Configured via {@code llmtoolbox.global.function.prefix}. When empty
 * (the default) no prefix is applied. When set, the value is restricted to
 * {@code [A-Za-z0-9_]+} and is joined to the operationId with a single
 * underscore: {@code <prefix>_<operationId>}.</p>
 *
 * <p>Only the operationId in the OpenAPI subset is affected. REST paths are
 * the HTTP contract and are never changed, and the internal tool catalog
 * (functions list, in-process dispatch) keeps raw operationIds.</p>
 */
@ApplicationScoped
public class ToolsetPrefix {

    private static final Logger LOG = Logger.getLogger(ToolsetPrefix.class);
    private static final Pattern VALID = Pattern.compile("[A-Za-z0-9_]+");

    @ConfigProperty(name = "llmtoolbox.global.function.prefix")
    Optional<String> configuredPrefix;

    /** Cached effective prefix: "" = disabled, otherwise the validated prefix. */
    private volatile String effective;

    public boolean enabled() {
        return !effective().isEmpty();
    }

    /** The effective prefix (empty string when disabled). */
    public String value() {
        return effective();
    }

    /** Prepends the prefix (if any) to the given operationId. */
    public String apply(String operationId) {
        String prefix = effective();
        if (prefix.isEmpty() || operationId == null) {
            return operationId;
        }
        return prefix + "_" + operationId;
    }

    private String effective() {
        String e = effective;
        if (e == null) {
            synchronized (this) {
                e = effective;
                if (e == null) {
                    String raw = (configuredPrefix == null || configuredPrefix.isEmpty())
                            ? ""
                            : configuredPrefix.get().trim();
                    if (!raw.isEmpty() && !VALID.matcher(raw).matches()) {
                        LOG.errorf(
                                "Invalid llmtoolbox.global.function.prefix '%s' — must match [A-Za-z0-9_]+. Prefix disabled.",
                                raw);
                        raw = "";
                    }
                    effective = raw;
                    e = raw;
                }
            }
        }
        return e;
    }
}
