package com.sheahorn.llmtoolbox.security;

import com.sheahorn.llmtoolbox.domain.User;
import com.sheahorn.llmtoolbox.service.ApiKeyService;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.util.Optional;

@ApplicationScoped
public class ApiKeyResolver {

    @Inject
    ApiKeyService apiKeyService;

    public Optional<ApiKey> resolve(String providedKey) {
        if (providedKey == null || providedKey.isBlank()) {
            return Optional.empty();
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
