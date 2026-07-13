# ADR-0004: Filesystem Confinement with Allowed Root and Symlink Defense

**Date:** 2025-01-01 (inferred)
**Status:** Accepted

## Context

llmtoolbox exposes filesystem operations to LLMs — create, read, overwrite, delete, move, mkdir, rmdir, list, pack, head, tail, and more. An LLM with unrestricted filesystem access could read sensitive files, modify system configuration, or exfiltrate data. The application must confine all filesystem operations to a designated workspace.

## Decision

All filesystem operations are confined to a configurable **allowed root** (`llmtoolbox.files.allowed-root`). The confinement is implemented in `FsResourceSupport.resolvePath(String)`:

1. **Relative paths** are resolved against the allowed root
2. **Absolute paths** are normalized and checked with `path.startsWith(root)`
3. **Symlink escape detection** — the parent directory's real path (via `Path.toRealPath()`) is checked against the allowed root's real path. This defeats symlink-based escapes (e.g., `/opt/app/link -> /etc`, then `/opt/app/link/passwd`)
4. Symlinks are explicitly rejected for file operations (`Files.isSymbolicLink` check)

Additional safeguards:
- **Max read bytes** (`llmtoolbox.files.max-read-bytes`, default 1MB) — prevents reading huge files
- **Pack-folder limits** — max total bytes, max per-file bytes, max file count
- All file operations use `java.nio.file.Files` directly (not shell commands), avoiding shell injection

## Rationale

- **Defense in depth** — prefix check + real-path check + symlink rejection provides three layers of protection.
- **Configurable root** — different deployments can set different workspaces.
- **NIO over shell** — using Java NIO for file operations eliminates shell injection risks entirely for this category.
- **Size limits** — prevents accidental (or malicious) resource exhaustion from reading or packing large files.

## Consequences

- The allowed root must exist and be readable/writable by the JVM process.
- `Path.toRealPath()` follows symlinks, so the allowed root itself should not be a symlink (or if it is, the real path is used for the containment check).
- Tools that need to operate outside the allowed root (e.g., `df`, `du`, `lsblk`, `findmnt`) are implemented as separate shell-out endpoints that don't go through `FsResourceSupport`.
- Symlinks are completely unsupported — even legitimate use cases within the allowed root are blocked.
