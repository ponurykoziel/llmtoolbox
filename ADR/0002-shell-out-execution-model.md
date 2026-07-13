# ADR-0002: Shell-Out Execution Model with ProcessBuilder

**Date:** 2025-01-01 (inferred)
**Status:** Accepted

## Context

llmtoolbox tools need to interact with the host system: run system commands (`ping`, `dig`, `curl`, `mvn`, `systemctl`, `pactl`, etc.), manipulate the filesystem, and execute arbitrary shell pipelines. The application must provide a uniform execution interface with timeout protection and structured output.

## Decision

All external command execution goes through a single **`Executor`** class (`com.sheahorn.llmtoolbox.execution.Executor`) that:

- Wraps `ProcessBuilder("sh", "-c", command)` — every command is executed via a shell
- Applies a configurable timeout (`llmtoolbox.command.timeout-seconds`, default 30s)
- Force-destroys the process on timeout (`process.destroyForcibly()`)
- Returns a uniform **`ExecutionResponse`** DTO with fields: `command`, `exitCode`, `stdout`, `stderr`, `timedOut`, `durationMs`
- Catches all exceptions and surfaces them in `stderr` with `exitCode=null`

A companion utility class **`ToolSupport`** provides:
- `shellQuote(String)` — single-quote escaping for safe command construction
- `validateHost(String)` — hostname character and length validation
- `validatePort(Integer)` — port range validation (1-65535)
- `sanitizeSafeChars(String)` — strips everything except `[A-Za-z0-9._@:-]`

Filesystem tools (in `fstools/files/FileResource`) use a different pattern: they call `java.nio.file.Files` directly rather than shelling out, but still return `ExecutionResponse` for API consistency.

## Rationale

- **Uniform response format** — every tool returns the same structure, making it easy for LLMs to parse results.
- **Timeout protection** — prevents runaway processes from hanging the JVM; critical for Maven builds and network tools.
- **Shell wrapping** — `sh -c` allows pipelines, redirects, and compound commands without the application needing to parse them.
- **Single executor** — centralizes timeout configuration, error handling, and duration measurement.
- **Direct NIO for filesystem** — avoids shell injection risks for file operations; path validation is done in Java.

## Consequences

- All commands inherit the JVM's environment and user permissions. If the JVM runs as root or with passwordless sudo, tools have unrestricted host access.
- Shell injection is a risk for tools that construct commands from user input. `ToolSupport.shellQuote` mitigates this but must be used consistently.
- The 30s default timeout is too short for Maven builds; the README documents this and the `build_mvn_*` tools note it in their summaries.
- `ProcessBuilder("sh", "-c", ...)` means the application is Linux-only.
