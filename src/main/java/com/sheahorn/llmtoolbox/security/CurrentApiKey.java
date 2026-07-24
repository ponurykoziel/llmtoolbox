package com.sheahorn.llmtoolbox.security;

import jakarta.enterprise.context.RequestScoped;

/**
 * Request-scoped holder for the resolved API key, set by BearerAuthFilter.
 */
@RequestScoped
public class CurrentApiKey {

    private ApiKey apiKey;

    public ApiKey get() {
        return apiKey;
    }

    public void set(ApiKey apiKey) {
        this.apiKey = apiKey;
    }
}
