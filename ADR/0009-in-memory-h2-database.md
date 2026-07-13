# ADR-0009: In-Memory H2 Database with Volatile Storage

**Date:** 2025-01-01 (inferred)
**Status:** Accepted

## Context

llmtoolbox needs to persist users, API keys, presets, notes, and memory entries. The application is designed as a lightweight tool server running on bare-metal or in containers — not as a production data store. Simplicity of deployment and zero operational overhead are primary goals.

## Decision

Use **H2 in-memory database** with the JDBC URL `jdbc:h2:mem:llmtoolbox;DB_CLOSE_DELAY=-1`:

- `DB_CLOSE_DELAY=-1` keeps the database alive as long as the JVM runs, even if all connections are closed
- No filesystem storage — all data is lost on restart
- Hibernate DDL generation set to `update` by default (adds columns/tables without dropping data)
- Entities use `UUID.randomUUID().toString()` for primary keys (36-char strings)
- Panache `PanacheEntityBase` for Active Record pattern

### Entity Schema

| Entity | Table | Key Fields |
|--------|-------|------------|
| `User` | `users` | id (UUID), username (unique), password (bcrypt), role, active |
| `ApiKey` | `apikeys` | id (UUID), userId, name, keyHash |
| `Preset` | `presets` | id (auto), name, prefixes |
| `Note` | `notes` | id (auto), title, content |
| `MemoryEntry` | `memories` | id (auto), content, timestamp |

## Rationale

- **Zero configuration** — no database server to install, no connection strings to configure beyond the defaults.
- **Volatility is acceptable** — the primary data (presets, notes, memories) is ephemeral by nature; the admin user is re-seeded on every boot.
- **H2 + Panache** — Quarkus provides first-class integration; minimal boilerplate.
- **UUID primary keys** — avoid sequential ID guessing for users and API keys; auto-increment IDs are fine for notes, memories, and presets.
- **`update` DDL** — allows schema evolution without manual migration scripts during development.

## Consequences

- **All data lost on restart** — users, API keys, presets, notes, and memories must be recreated after every restart. The admin user is automatically re-seeded.
- **No horizontal scaling** — the database is in-process; multiple JVM instances have independent state.
- **No backup/restore** — data is ephemeral by design.
- **H2 compatibility** — some Hibernate features may behave differently than on PostgreSQL/MySQL; testing against H2 is sufficient for this use case.
- The `update` DDL strategy does not remove columns or tables — schema cleanup requires manual intervention or a `drop-and-create` cycle.

## Replaceability

H2 is trivially replaceable with any database supported by Hibernate (PostgreSQL, MySQL, MariaDB, H2 file-based, etc.) — it's a single Quarkus datasource configuration change. However, there is **little practical gain** in doing so for a single-tenant application (ADR-0011):

- **No concurrent write pressure** — a single operator and their LLM(s) generate negligible database load. H2 in-memory handles this easily.
- **No replication or failover need** — the application runs on a single host; if the host goes down, the application is unavailable regardless of the database.
- **No complex query patterns** — all queries are simple CRUD with Panache; there are no joins, aggregations, or analytical workloads that would benefit from a more powerful engine.
- **Volatility is a feature, not a bug** — ephemeral storage means a restart always returns to a clean, known state. For a tool that may be experimented with by LLMs, this is desirable.

If an operator needs persistence across restarts, switching to H2 file-based mode (`jdbc:h2:file:./data/llmtoolbox`) or PostgreSQL is a one-line config change — no code modifications required.
