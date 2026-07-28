# ADR-0013: LLM-Compatible Tool Naming and Categorization

**Date:** 2025-07-25
**Status:** Accepted

## Context

The ToolDispatcher exposes 194 dispatchable methods across 47 ToolBean classes to LLM agents. Each tool is identified by an `operationId` and a `summary` description. The naming and grouping of these tools directly affects how well an LLM can select, chain, and reason about them. Poor naming creates semantic ambiguity; poor grouping forces the LLM to infer structure that should be explicit.

## Decision

### 1. Operation IDs

Every tool exposed to an LLM **must** have an `operationId` and a `summary` description.

- `operationId` uses **latin characters and snake_case** only (e.g. `fs_files_read`, `host_audio_volume_set`).
- The `summary` is a single-sentence description of what the tool does, suitable for LLM function-calling APIs.

### 2. Tool Categories

Tools are divided into **groups (categories)**. A category bundles tools that share the vast majority of both their **semantic** and **functional** context.

- **Semantic context** — what domain the tool operates in (filesystem, networking, audio, build systems, etc.).
- **Functional context** — what kind of operation it performs (query, mutation, calculation, execution).

A tool belongs in a category only if both contexts align. Counterexample: `time_add_hours`, `time_shift_by_timezone`, and `time_current` share semantic similarities (all relate to time), but functionally the first two are calculations while the last is a real-world data query. They should not share a category.

A category may be defined purely by functional context when that context dominates. The `calculator_` prefix is the primary example: all operations under it are pure computations — that shared functional context is the category, even though the semantic domains vary (arithmetic, statistics, networking, color, etc.).

### 3. Gradual Context Narrowing Naming Convention

Operation IDs use **gradual context narrowing**: the prefix narrows from broadest category to most specific operation.

```
{category}_{subcategory}_{...}_{action}
```

Examples:
- `fs_files_read` — category `fs`, subcategory `files`, action `read`
- `host_audio_volume_set` — category `host`, subcategory `audio`, subcategory `volume`, action `set`
- `build_mvn_compile` — category `build`, subcategory `mvn`, action `compile`

**Grammar resemblance is explicitly rejected.** Naming tools by verb-first grammar (e.g. `read_file`, `set_volume`) degenerates the tool's identity to an independent operation, forcing the consuming LLM to infer categorization. The prefix-based convention makes category membership explicit in the identifier itself.

Alternative considered: using only one category (flat namespace). Rejected because the tool surface is too large (194 tools) for flat naming to remain navigable.

### 4. Body Parameters Only

All tool input parameters are passed **through the request body** as a JSON object. This is a fundamental semantic distinction: the function being called is identified by `operationId`; its arguments are the body payload.

- No `@QueryParam` — query parameters blur the line between identification and parameterization.
- No `@PathParam` — path parameters embed arguments in the identifier, which is semantically incorrect for tool dispatch.

### 5. Parameter Naming

Parameter names must **extend or confirm the operation's context**. A parameter name should help the LLM understand what value is expected without reading the description.

- Good: `sleep({ "seconds": 5 })` — "seconds" confirms the temporal context.
- Bad: `sleep({ "value": 5 })` — "value" is generic and provides no context.

### 6. Semantic Distance Review

All semantic distances between operation IDs — both within a category and across the entire collection — must be **revised by a human**. LLMs display very limited semantic distance management skills and should be used only to **analyze** distances, not to make final naming decisions.

- Within a category: tools should be close enough that an LLM naturally considers them together, but distinct enough that selection is unambiguous.
- Across categories: tools in different categories should have clear semantic separation. Overlap suggests a categorization error.

## Consequences

- All 194 existing tools must be audited against these rules. Some operation IDs and category assignments may need adjustment.
- The `ToolDispatcher` parameter binding has been simplified: only `DTO` and `NONE` binding strategies remain. `QUERY_PARAM` and `PATH_PARAM` strategies have been removed.
- New tools must follow these conventions from the start. A reviewer (human) must approve the `operationId`, category placement, and parameter names before a new tool is exposed to LLMs.
- The OpenAPI spec (`operationId` and `summary` fields) remains the single source of truth for tool metadata.
