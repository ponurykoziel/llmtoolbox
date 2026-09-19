# ADR-0014: In-Process Tool Dispatch via ToolBean Marker Interface and CDI

**Date:** 2025-07-25
**Status:** Accepted

## Context

The `ToolDispatcher` is the central component that routes LLM tool calls to the appropriate handler method. In its original form, it operated as an HTTP reverse-proxy: it parsed the OpenAPI spec to build an endpoint index (`operationId → {method, path}`), then made HTTP requests to `http://localhost:{port}{path}` for every tool invocation. This had several problems:

- **Auth wall** — the HTTP calls hit the same `BearerAuthFilter` that protects external API access, requiring the dispatcher to carry auth headers
- **Localhost assumption** — the dispatcher assumed the application was reachable at `localhost:{port}`, which breaks in containerized or proxied deployments
- **Unnecessary serialization** — request/response went through JSON serialization, HTTP transport, and JSON deserialization for a call within the same JVM
- **Fragile endpoint index** — the index was built by parsing the OpenAPI spec at runtime, coupling dispatch to the spec format

## Decision

### ToolBean Marker Interface

A **marker interface** `ToolBean` (`com.sheahorn.llmtoolbox.llm.ToolBean`) identifies CDI beans that expose dispatchable tool methods:

```java
public interface ToolBean {
}
```

Every resource class with `@Operation`-annotated methods that should be callable by LLMs implements `ToolBean`. This includes:

- All non-calculator resource classes across `basics`, `custom`, `fstools`, `hosttools`, `nettools`, `commtools`, `terminal`, `buildtools`, and `resource` packages
- All calculator resource classes (via `Calculator extends ToolBean` — see ADR-0006)

### CDI-Based Startup Scan

At startup (`@Observes StartupEvent`), `ToolDispatcher` scans for all `ToolBean` instances via CDI:

```java
for (Object bean : Arc.container().select(ToolBean.class)) {
    // unwrap CDI proxy, reflect on @Operation methods, build dispatch index
}
```

For each bean, it reflects on the declared methods, finds those annotated with `@Operation`, and builds a `Map<String, ToolMethod>` where:

- **Key** — `operationId` from `@Operation`
- **Value** — `ToolMethod` containing the bean instance, the `Method` object, and parameter binding metadata

### In-Process Dispatch

The `dispatch(String operationId, String argumentsJson)` method:

1. Looks up the `ToolMethod` by `operationId`
2. Parses the JSON arguments into a `JsonNode`
3. Binds arguments to method parameters using one of two strategies:
   - **DTO** — deserialize the JSON body into the method's parameter type (the common case)
   - **NONE** — no-arg methods
4. Invokes the method reflectively: `method.invoke(bean, args)`
5. Serializes the return value to JSON (handles `jakarta.ws.rs.core.Response` specially by extracting the entity)

### What Was Removed

- `HttpClient` and all HTTP connection logic
- `port` configuration property (`quarkus.http.port`)
- `EndpointInfo` record and `buildEndpointIndex()` method
- `loadOpenApi()` — the OpenAPI spec is no longer parsed for dispatch purposes
- Auth header plumbing
- `@QueryParam` and `@PathParam` binding strategies (all tools now use body DTOs — see ADR-0013)

### What Was Preserved

- `resolveTools(String presetName)` — unchanged; continues to read `BuiltinFunctionCache` for operationId→description mapping
- `buildToolDef(String operationId, String description)` — unchanged; builds the tool definition structure for LLM APIs
- `BuiltinFunctionCache` — unchanged; continues to load operationId→description from the OpenAPI spec at startup

## Rationale

- **No HTTP, no auth wall** — tools dispatch in-process. The `BearerAuthFilter` still protects external HTTP calls, but internal dispatch bypasses it entirely.
- **No localhost assumption** — dispatch works identically regardless of network configuration, containerization, or proxy setup.
- **No serialization overhead** — Java objects pass directly from caller to callee without JSON round-tripping (arguments are still parsed from JSON since they arrive from the LLM as JSON strings).
- **CDI is the natural discovery mechanism** — Quarkus already manages all resource classes as CDI beans. The `ToolBean` marker interface is a zero-cost filter on top of existing CDI infrastructure.
- **Marker interface over annotation** — a marker interface is more intentional than a class-level annotation: implementing `ToolBean` is an explicit design decision visible in the class signature, not a buried annotation.
- **Reflection is acceptable at startup** — the method scan happens once at boot. Dispatch-time reflection (`method.invoke`) is the same cost as the JAX-RS layer would incur for HTTP-bound calls.
- **`resolveTools()` separation** — tool metadata (operationId, description) continues to come from the OpenAPI spec via `BuiltinFunctionCache`. The dispatch index only concerns invocation. This keeps the single source of truth for tool descriptions in the OpenAPI spec while the dispatch mechanism is independent.

## Consequences

- The `ToolDispatcher` no longer depends on HTTP configuration, the OpenAPI spec format, or network reachability.
- Adding a new tool requires: (1) `implements ToolBean` on the resource class, (2) `@Operation(operationId = "...", summary = "...")` on the method. The CDI scan picks it up automatically.
- The `QUERY_PARAM` and `PATH_PARAM` binding strategies were removed after all tools were migrated to body DTOs (see ADR-0013). If a future tool genuinely needs path or query parameters, these strategies can be restored — but the preference is body DTOs.
- `BuiltinFunctionCache` and `ToolDispatcher` both read the OpenAPI spec at startup, but for different purposes: `BuiltinFunctionCache` for operationId→description mapping, `ToolDispatcher` for nothing (it uses CDI). The OpenAPI spec remains the single source of truth for tool metadata exposed to LLMs.
- The `ToolBean` interface is in `com.sheahorn.llmtoolbox.llm` alongside `ToolDispatcher`, keeping the dispatch mechanism co-located.
