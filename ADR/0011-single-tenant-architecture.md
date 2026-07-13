# ADR-0011: Single-Tenant Architecture — No Multi-User Scaling

**Date:** 2025-01-01 (inferred)
**Status:** Accepted

## Context

llmtoolbox is a personal tool server — it runs on a single developer's machine or a dedicated bare-metal host, serving one human operator and their LLM(s). There is no use case for multiple organizations, teams, or concurrent heavy workloads.

## Decision

The application is designed as a **single-tenant system** with no provisions for:

- **Multi-tenancy** — no tenant isolation, no organization scoping, no per-tenant configuration
- **Horizontal scaling** — in-memory H2 database (ADR-0009), in-process global Maven lock (ADR-0008), in-memory clipboard cache
- **High concurrency** — no connection pooling beyond Quarkus defaults, no async processing, no message queues
- **Rate limiting** — no throttling, no quota management
- **Audit logging** — no structured audit trail beyond what the shell tools naturally produce

The admin user seeded on first boot is the sole human user. Additional users can be created via the API, but they share the same global state — there is no per-user data partitioning for notes, memories, presets, or clipboard.

## Rationale

- **YAGNI** — multi-tenancy, scaling, and rate limiting add significant complexity with zero benefit for a single-operator tool.
- **Simplicity as a feature** — the codebase stays small, understandable, and auditable. A single operator can read the entire source and understand every security boundary.
- **Dedicated host assumption** — the README explicitly recommends dedicated bare metal for terminal-enabled setups. This is not a SaaS product.
- **Fast iteration** — without multi-tenant concerns, features can be added quickly without worrying about isolation, billing, or tenant-aware routing.

## Consequences

- The application cannot be safely exposed as a multi-user service without significant rework.
- All data (notes, memories, presets, API keys) is globally visible to any authenticated user.
- The clipboard is a single global instance — two LLMs or two browser sessions will overwrite each other's clipboard content.
- If the operator needs multi-user isolation, they should run separate llmtoolbox instances on different ports with separate configs.
