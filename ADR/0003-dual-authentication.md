# ADR-0003: Dual Authentication — Bearer Tokens + Form Login

**Date:** 2025-01-01 (inferred)
**Status:** Accepted

## Context

llmtoolbox serves two distinct audiences:
1. **LLMs** — programmatic API consumers that need Bearer token authentication
2. **Humans** — browser users who manage presets, API keys, and browse tools via a web UI

These audiences have different authentication needs and threat models.

## Decision

Implement **two parallel authentication mechanisms** that coexist:

### API Authentication (Bearer Tokens)
- A JAX-RS `ContainerRequestFilter` (`BearerAuthFilter`) intercepts all `/api/*` requests
- Accepts the master token configured via `llmtoolbox.auth.token`
- Also accepts user-created API keys (hashed with a pepper, stored in the `apikeys` table)
- Resolved via `ApiKeyResolver` which checks both the master token and hashed user keys
- Session cookies (`llmtools_session`) bypass Bearer auth — a logged-in browser user can call API endpoints

### Browser Authentication (Form Login)
- Quarkus Elytron Security with JDBC realm
- Form-based login with session cookies
- Admin user seeded on first boot via `AuthStartupBean` (username/password from config, bcrypt-hashed)
- Session timeout configurable (default 24h)

### Opt-Out Mechanism
- `@NoBearerAuth` annotation allows specific endpoints (OpenAPI spec serving, built-in function listing) to skip Bearer authentication entirely

## Rationale

- **Two audiences, two mechanisms** — LLMs can't fill out login forms; humans shouldn't need to manage Bearer tokens for UI access.
- **Session cookie passthrough** — avoids forcing browser users to also send Bearer tokens for API calls made from the UI.
- **Master token + user keys** — the master token is simple for initial setup; user-created keys allow revocation and scoping without changing the master token.
- **Pepper-based hashing** — user API keys are hashed with a configurable pepper before storage, so a database leak doesn't expose raw keys.
- **`@NoBearerAuth`** — cleanly separates public endpoints (OpenAPI specs are needed before authentication) from protected ones.

## Consequences

- Two auth code paths to maintain and test.
- The master token is stored in plaintext in `application.properties` — file permissions must be restrictive.
- User-created API keys are shown once at creation time (the raw key) and then stored only as a hash — if lost, they must be recreated.
- The `llmtools_session` cookie name is hardcoded in `BearerAuthFilter`.
