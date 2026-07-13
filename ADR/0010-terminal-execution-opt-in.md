# ADR-0010: Terminal Execution — Disabled by Default, Explicit Opt-In

**Date:** 2025-01-01 (inferred)
**Status:** Accepted

## Context

The `terminal_execute` endpoint allows arbitrary shell command execution. This is the most powerful and dangerous tool in llmtoolbox — if the JVM runs with passwordless sudo, it effectively grants root access to the LLM. The README explicitly warns: "You want it dedicated, trust me. It will take a few clean installs for them to learn."

## Decision

The terminal tool is **disabled by default** via `llmtoolbox.terminal.allow=false`:

```java
@ConfigProperty(name = "llmtoolbox.terminal.allow", defaultValue = "false")
boolean terminalAllowed;
```

When disabled, the endpoint returns a `400 Bad Request` with a clear error message: "terminal is not enabled; set llmtoolbox.terminal.allow=true".

When enabled, the endpoint accepts a `TerminalRequestDto` with a `cmd` field and passes it directly to `Executor.execute(cmd)` — the same shell-out path used by all other tools.

## Rationale

- **Safety by default** — a fresh install cannot execute arbitrary commands until the operator explicitly enables it.
- **Clear error message** — tells the operator exactly which property to change.
- **Same execution path** — when enabled, terminal commands go through the same `Executor` with timeout protection and structured `ExecutionResponse`.
- **Documented risk** — the README dedicates a paragraph to warning about dedicated bare-metal use and the learning curve.

## Consequences

- The terminal tool is an all-or-nothing toggle — there's no command allowlist or sandboxing beyond the OS user's permissions.
- The README's primary use case is "setting up autonomous daemon environments for LLMs" — a narrow but powerful use case.
- Operators who enable this must understand that any Bearer token holder can run arbitrary commands.
