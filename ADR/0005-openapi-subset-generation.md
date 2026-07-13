# ADR-0005: OpenAPI Subset Generation for LLM Tool Integration

**Date:** 2025-01-01 (inferred)
**Status:** Accepted

## Context

llmtoolbox exposes dozens of REST endpoints across multiple tool categories. LLM platforms like Open WebUI need a focused set of tools — sending all 100+ endpoints to a model wastes context window and confuses the model. The application must provide a way to generate focused OpenAPI specs containing only the tools relevant to a particular use case.

## Decision

Implement an **OpenAPI subset engine** (`OpenApiSubsetResource`) that:

1. **Caches the full OpenAPI spec** on first access (loaded from `/META-INF/openapi.yaml`, `.yml`, or `.json`)
2. **Supports two selection mechanisms:**
   - **By preset** (`GET /api/openapi/preset/{name}`) — named collections of operationId prefixes
   - **By selector** (`GET /api/openapi/select/{selectors}`) — comma-separated operationIds with `*` wildcard support
3. **Builds a valid subset** by:
   - Filtering paths to only those with matching operationIds
   - Collecting referenced JSON schemas from `#/components/schemas`
   - Performing **transitive closure** on schema references (if schema A references schema B, both are included)
   - Preserving the OpenAPI 3.1.0 structure with `info`, `paths`, and `components/schemas`

### Preset System

Presets are resolved via a **two-tier system** (`PresetDefaults`):

- **Hardcoded category presets** — `all`, `fs`, `net`, `host`, `build`, `communication`, `basics`, `current_time`, `presets`, `terminal`, `calculator`
- **DB-stored composite presets** — seeded on first boot: `daemon`, `builder`, `host_ctl`, `host_info`
- **User-created presets** — stored in the `presets` table via `PresetResource` CRUD
- Resolution order: DB first, then hardcoded fallback

## Rationale

- **Context window efficiency** — LLMs get only the tools they need, reducing token usage and improving reliability.
- **Presets as first-class concept** — the README calls presets the "key idea of this application"; they are the primary integration mechanism.
- **Transitive schema closure** — ensures the generated spec is self-contained and valid; Open WebUI and other consumers don't need to fetch schemas separately.
- **Two selection modes** — presets for curated collections, selectors for ad-hoc combinations.
- **DB + hardcoded** — hardcoded presets provide a stable baseline; DB presets allow customization without code changes.

## Why OpenAPI Over MCP

The application deliberately uses **OpenAPI 3.1** as its tool description format rather than the Model Context Protocol (MCP). The reasons:

- **Type metadata** — OpenAPI carries full JSON Schema type definitions for every request and response field. LLMs can see that `volume` is an `integer` with min 0 and max 100, that `host` must match `[a-zA-Z0-9._:-]+`, or that `path` is required and must be a string. MCP's tool descriptions are less structured — they rely on free-text descriptions and loose JSON Schema, losing the rich type information that Quarkus SmallRye OpenAPI extracts from JAX-RS annotations and DTO classes automatically.
- **Trivially convertible to MCP** — an OpenAPI spec can be mechanically transformed into MCP tool definitions (iterate paths, extract operationId/summary/parameters/requestBody). The reverse is lossy — MCP lacks the schema richness to reconstruct a full OpenAPI spec. By generating OpenAPI natively, llmtoolbox supports both ecosystems without maintaining two description formats.
- **Ecosystem compatibility** — Open WebUI, the primary integration target, natively consumes OpenAPI specs. Many other LLM platforms (LangChain, Flowise, Dify) also have OpenAPI tool loaders. MCP is newer and less universally supported.
- **Auto-generation** — Quarkus SmallRye OpenAPI generates the full spec from existing JAX-RS annotations at build time. No separate tool description maintenance is needed. Adding a new endpoint automatically appears in the spec with correct types, summaries, and parameter documentation.

## Consequences

- The full OpenAPI spec must be parseable at runtime; if Quarkus SmallRye OpenAPI changes its output format, the subset engine may break.
- Transitive closure is implemented manually with a `while(changed)` loop — could be slow for very large specs, but the current schema count is manageable.
- The `@NoBearerAuth` annotation on `OpenApiSubsetResource` means OpenAPI specs are publicly accessible — by design, since Open WebUI needs to fetch them without authentication.
