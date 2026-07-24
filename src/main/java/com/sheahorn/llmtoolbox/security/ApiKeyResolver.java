package com.sheahorn.llmtoolbox.security;

import com.sheahorn.llmtoolbox.domain.User;
import com.sheahorn.llmtoolbox.service.ApiKeyService;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.util.Optional;

@ApplicationScoped
public class ApiKeyResolver {

    private final String accessKey;

    @Inject
    ApiKeyService apiKeyService;

    public ApiKeyResolver(
        @ConfigProperty(name = "llmtoolbox.auth.token") String accessKey
    ) {
        this.accessKey = accessKey;
    }

    public Optional<ApiKey> resolve(String providedKey) {
        if (providedKey == null || providedKey.isBlank()) {
            return Optional.empty();
        }
        // Static config master key
        if (providedKey.equals(accessKey)) {
            return Optional.of(ApiKey.MASTER);
        }
        // User-created API keys (hashed)
        String keyHash = apiKeyService.hashKey(providedKey);
        com.sheahorn.llmtoolbox.domain.ApiKey entity = apiKeyService.findByKeyHash(keyHash);
        if (entity != null) {
            User user = User.findById(entity.userId);
            if (user != null) {
                return Optional.of(new ApiKey(user.id, user.username, user.role));
            }
        }
        return Optional.empty();
    }
}
