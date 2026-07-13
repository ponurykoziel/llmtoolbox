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
