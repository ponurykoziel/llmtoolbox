# ADR-0008: Maven Build Tools with Global Lock

**Date:** 2025-01-01 (inferred)
**Status:** Accepted

## Context

llmtoolbox exposes Maven build operations (`build_mvn_*`) that allow LLMs to compile, test, package, and analyze Java projects within the allowed filesystem root. Maven is not concurrency-safe — running multiple Maven processes simultaneously in the same project directory can corrupt the local repository, produce inconsistent results, or cause build failures.

## Decision

All Maven invocations are serialized through a **global lock** (`MvnLockService`):

```java
@ApplicationScoped
public class MvnLockService {
    private final Object monitor = new Object();

    public <T> T runLocked(Callable<T> action) throws Exception {
        synchronized (monitor) { return action.call(); }
    }
}
```

Every `MvnResource` endpoint calls `lock.runLocked(() -> executor.execute(finalCommand))`.

The Maven commands are constructed with:
- `-B` (batch mode, no ANSI colors)
- `cd -- <project-path> && mvn ...` — changes to the project directory first
- Optional `-o` (offline mode) flag
- Optional `-P<profile>` flag
- Optional `-DskipTests` flag (for compile, package, verify)
- `-Dtest=<class>` for single-test execution (shell-quoted)

The project path is validated through `FsResourceSupport.resolvePath()` — it must be within the allowed root.

## Rationale

- **Global lock** — simplest correct solution. A single JVM-level `synchronized` block prevents all concurrent Maven access. More granular locking (per-project) would add complexity without significant benefit, since Maven's local repository (`~/.m2`) is also a shared resource.
- **Batch mode** — `-B` ensures consistent output parsing by LLMs (no progress bars or color codes).
- **Path validation** — reuses the same `FsResourceSupport` confinement used by filesystem tools.
- **Shell-based execution** — Maven is invoked as a shell command, consistent with the general execution model (ADR-0002).

## Consequences

- Only one Maven operation can run at a time across the entire application. If multiple LLMs or users trigger builds simultaneously, they will queue up.
- The lock is in-process only; if multiple JVM instances share the same `~/.m2` repository, they can still conflict.
- The default 30-second command timeout is likely too short for Maven builds; the README and tool summaries document this.
- Maven must be installed on the host (`mvn` on PATH).

## Update (2026-11): lock release is now guaranteed, and the lock can be switched off

The original decision stands — one global lock per build-tool family, not per project. Two
refinements, forced by a real failure (tracker 0000187B):

**1. A lock can no longer be held forever by a worker.** `Executor.execute()` used to read the
process pipes *after* `waitFor()` and only killed `sh` on timeout. A surviving grandchild kept
the pipe open, `transferTo()` blocked, `runLocked()`'s `finally` never ran, and the family stayed
locked until the app restarted (`synchronize` mode: the monitor stayed held, so later calls
blocked instead of queueing). `Executor` now drains both pipes concurrently, closes stdin, and on
timeout kills the whole process tree (`ProcessHandle.descendants()`, deepest first). Worst case is
`timeout + ~4s`, so release is unconditional.

**2. The five copies of the lock class collapsed into one implementation.** Every family
(`mvn`, `gradle`, `git`, `docker`, `cargo`) extends `AbstractFamilyLockService` and shares
`BuildLockSettings`. The `synchronized` monitor became a `ReentrantLock`, because
`isHeldByWorker()` can distinguish a stale flag from a call that is genuinely still running.

New configuration:

- `llmtoolbox.build.lock-mode` = `synchronize` (default) | `reject` — was previously undocumented
  but already implemented.
- `llmtoolbox.build.locks.enabled` = `true` (default) | `false` — one switch for all five
  families. When false, `runLocked()` is a pass-through: no mutual exclusion, no "busy",
  `build_locks_status` reports `mode: "disabled"`.

Consequences of the update:

- The concurrency argument for a global lock is now an explicit, reversible choice rather than a
  hard-coded property: an operator who accepts the `~/.m2` risk can turn locking off.
- A stuck lock has a manual escape hatch in the dashboard (`POST /ui/locks/clear`, admin only).
  It is deliberately not a tool — an LLM cannot clear locks. Self-referring operations get their
  own category later (tracker 000018EF).
- Per-path locking is still rejected as a default and remains a separate, opt-in issue (0000187A).
