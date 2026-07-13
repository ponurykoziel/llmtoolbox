# ADR-0012: TLS Termination Delegated to External Reverse Proxy

**Date:** 2025-01-01 (inferred)
**Status:** Accepted

## Context

llmtoolbox exposes an HTTP server on `127.0.0.1:8080` by default. If the operator chooses to expose the application to the open network (e.g., to allow remote LLM access or integrate with cloud-hosted Open WebUI), the connection must be encrypted. However, TLS termination is an operational concern with many deployment-specific variables: certificate provisioning, renewal, cipher selection, protocol versions.

## Decision

llmtoolbox does **not** implement TLS natively. HTTPS should be handled by an **external reverse proxy** such as nginx, Caddy, or Apache httpd:

```
Browser / LLM Client
        │
        ▼
   [HTTPS:443]
   nginx / Caddy
   (TLS termination, cert management)
        │
        ▼
   [HTTP:8080]
   llmtoolbox (Quarkus, plain HTTP)
```

The application itself:
- Binds to `127.0.0.1` by default (localhost only)
- Exposes plain HTTP with no TLS configuration
- Relies on the reverse proxy for encryption, authentication headers (if needed), and request filtering

The `application.properties.sample` documents `quarkus.http.host` with the guidance: "Use 127.0.0.1 on bare metal, 0.0.0.0 in a container."

## Rationale

- **Separation of concerns** — TLS is an infrastructure concern, not an application concern. The reverse proxy ecosystem (nginx, Caddy, Traefik) is mature, well-documented, and handles certificate lifecycle better than an embedded Java server.
- **Simplicity** — no need to configure keystores, truststores, certificate formats, or TLS protocol versions in `application.properties`.
- **Flexibility** — operators can choose their preferred proxy, add rate limiting, IP filtering, or additional auth layers without modifying llmtoolbox.
- **Consistent with single-tenant model** (ADR-0011) — in a single-operator setup, the reverse proxy is typically on the same host, keeping the architecture simple.
- **Caddy as an ideal companion** — Caddy auto-provisions Let's Encrypt certificates with zero configuration, making it trivial to add HTTPS to a llmtoolbox instance.

## Consequences

- Plain HTTP between the reverse proxy and llmtoolbox — acceptable because both run on the same host (or trusted network).
- Operators who expose llmtoolbox directly to the network without a reverse proxy are responsible for their own TLS setup (or accept plain HTTP).
- The `BearerAuthFilter` reads the `Authorization` header directly — if the reverse proxy strips or modifies headers, authentication may break. Operators must configure the proxy to forward headers intact.
