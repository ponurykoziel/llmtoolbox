# ADR Compliance Checklist

Instructions for an LLM session: verify the llmtoolbox project against every ADR below.
For each check, answer PASS, FAIL, or N/A. Cite specific files and line-level evidence.
If a check fails, explain what's wrong and what would fix it.

---

## ADR-0001: Use Quarkus with Uber-Jar Packaging

- [ ] `pom.xml` uses Quarkus 3.x with `quarkus.package.jar.type=uber-jar`
- [ ] Java 17 is the compile target (`maven.compiler.release=17`)
- [ ] Dependencies include: `quarkus-rest-jackson`, `quarkus-hibernate-orm-panache`, `quarkus-jdbc-h2`, `quarkus-smallrye-openapi`, `quarkus-elytron-security-jdbc`, Thymeleaf 3.1.x
- [ ] `run.sh` invokes the uber-jar via `java -jar target/llmtoolbox-*-runner.jar`
- [ ] No features from Java 18+ are used (grep for `java.lang.foreign`, virtual threads, pattern matching for switch, etc.)

## ADR-0002: Shell-Out Execution Model with ProcessBuilder

- [ ] `Executor.java` exists and wraps `ProcessBuilder("sh", "-c", command)`
- [ ] Timeout is configurable via `llmtoolbox.command.timeout-seconds` (default 30)
- [ ] `ExecutionResponse` has fields: `command`, `exitCode`, `stdout`, `stderr`, `timedOut`, `durationMs`
- [ ] `ToolSupport.java` provides `shellQuote(String)`, `validateHost(String)`, `validatePort(Integer)`, `sanitizeSafeChars(String)`
- [ ] Filesystem tools (`fstools/files/FileResource.java`) use `java.nio.file.Files` directly, not shell commands
- [ ] All tool resource classes inject `Executor` and call `executor.execute(command)` — no direct `ProcessBuilder` usage outside `Executor`

## ADR-0003: Dual Authentication — Bearer Tokens + Form Login

- [ ] `BearerAuthFilter.java` exists as a `ContainerRequestFilter` intercepting `/api/*`
- [ ] Master token is configurable via `llmtoolbox.auth.token`
- [ ] `ApiKeyResolver.java` checks both master token and hashed user API keys
- [ ] Session cookies (`llmtools_session`) bypass Bearer auth for logged-in browser users
- [ ] Quarkus Elytron Security with JDBC realm is configured for form login
- [ ] `AuthStartupBean.java` seeds admin user on first boot (bcrypt-hashed password)
- [ ] `@NoBearerAuth` annotation exists and is used on public endpoints (OpenAPI spec serving, built-in function listing)
- [ ] User API keys are hashed with a pepper before storage; raw key shown once at creation

## ADR-0004: Filesystem Confinement with Allowed Root and Symlink Defense

- [ ] `FsResourceSupport.resolvePath(String)` exists and enforces allowed root confinement
- [ ] `llmtoolbox.files.allowed-root` is configurable
- [ ] Relative paths are resolved against the allowed root
- [ ] Absolute paths are checked with `path.startsWith(root)`
- [ ] Parent directory real-path check (`Path.toRealPath()`) defeats symlink escapes
- [ ] Symlinks are explicitly rejected (`Files.isSymbolicLink` check) for file operations
- [ ] `llmtoolbox.files.max-read-bytes` is enforced (default 1MB)
- [ ] Pack-folder has max total bytes, max per-file bytes, max file count limits
- [ ] Tools that accept filesystem paths (`df`, `du`) extend `FsResourceSupport` for path validation. Tools without path parameters (`lsblk`, `findmnt`) do not need it.

## ADR-0005: OpenAPI Subset Generation for LLM Tool Integration

- [ ] `OpenApiSubsetResource.java` exists with endpoints for preset-based and selector-based subsetting
- [ ] Full OpenAPI spec is cached on first access (loaded from `/META-INF/openapi.yaml`, `.yml`, or `.json`)
- [ ] Subset engine performs transitive closure on `#/components/schemas` references
- [ ] `PresetDefaults.java` provides hardcoded category presets (`all`, `fs`, `net`, `host`, `build`, `communication`, `basics`, `current_time`, `presets`, `terminal`, `calculator`)
- [ ] DB-stored presets are seeded on first boot (`daemon`, `builder`, `host_ctl`, `host_info`)
- [ ] `PresetResource.java` provides CRUD for user-created presets
- [ ] Resolution order: DB first, then hardcoded fallback
- [ ] `@NoBearerAuth` is on `OpenApiSubsetResource` (publicly accessible)
- [ ] `BuiltinFunctionCache.java` loads operationId→description from the OpenAPI spec at startup

## ADR-0006: Calculator Architecture — Marker Interface + Rounding Strategy

- [ ] `Calculator` marker interface exists in `calculators/common/Calculator.java`
- [ ] All calculator resource classes implement `Calculator` (directly or via `Calculator extends ToolBean`)
- [ ] `DoubleValueCalculator` provides shared `round(double)` with configurable precision
- [ ] `llmtoolbox.calculator.rounding-digits` is configurable (default 8)
- [ ] Rounding handles NaN, Infinity, and overflow edge cases
- [ ] Each calculator domain has a separate calculator class (pure logic) and resource class (HTTP)

## ADR-0007: HTTPS-Only Curl with Optional Private-IP Blocking

- [ ] `CurlController.java` enforces HTTPS-only (rejects plain HTTP)
- [ ] `curl --proto http,https --proto-redir http,https` is in the command
- [ ] `--max-time 5` hard timeout
- [ ] `--silent --show-error` for clean output
- [ ] `-L` (follow redirects) is always set
- [ ] `llmtoolbox.net.curl.block-private-ips` is configurable (default `false`)
- [ ] When enabled, blocks: localhost, 127.0.0.1, 0.0.0.0, [::1], 169.254.0.0/16, 10.0.0.0/8, 172.16.0.0/12, 192.168.0.0/16, fc00::/7, fd00::/7
- [ ] CR/LF characters are stripped from header names and values
- [ ] All user-supplied values are passed through `ToolSupport.shellQuote()`

## ADR-0008: Maven Build Tools with Global Lock

- [ ] `MvnLockService.java` exists with a `synchronized` global lock
- [ ] Every `MvnResource` endpoint calls `lock.runLocked(...)`
- [ ] Maven commands use `-B` (batch mode, no ANSI colors)
- [ ] Commands are constructed as `cd -- <project-path> && mvn ...`
- [ ] Project path is validated through `FsResourceSupport.resolvePath()`
- [ ] Optional flags: `-o` (offline), `-P<profile>`, `-DskipTests`, `-Dtest=<class>`
- [ ] Same lock pattern exists for `GitResource` (`GitLockService`), `GradleResource` (`GradleLockService`), `DockerResource` (`DockerLockService`), `CmakeResource` (`CmakeLockService`)

## ADR-0009: In-Memory H2 Database with Volatile Storage

- [ ] JDBC URL is `jdbc:h2:mem:llmtoolbox;DB_CLOSE_DELAY=-1`
- [ ] Hibernate DDL generation is `update` (not `drop-and-create` in production config)
- [ ] Entities use appropriate ID strategies (UUID for User/ApiKey, auto-increment for Preset/Note/MemoryEntry)
- [ ] Panache `PanacheEntityBase` for Active Record pattern
- [ ] `AuthStartupBean` re-seeds admin user on every boot (idempotent)

## ADR-0010: Terminal Execution — Disabled by Default, Explicit Opt-In

- [ ] `llmtoolbox.terminal.allow` defaults to `false`
- [ ] `TerminalResource.java` checks `terminalAllowed` and returns 400 with clear error message when disabled
- [ ] When enabled, passes `cmd` to `Executor.execute(cmd)` — same path as all other tools
- [ ] No command allowlist or sandboxing beyond OS user permissions

## ADR-0011: Single-Tenant Architecture — No Multi-User Scaling

- [ ] No tenant isolation, organization scoping, or per-tenant configuration
- [ ] No horizontal scaling mechanisms (in-memory H2, in-process locks, in-memory clipboard)
- [ ] No rate limiting, throttling, or quota management
- [ ] No structured audit logging
- [ ] All data (notes, memories, presets, API keys) is globally visible to any authenticated user
- [ ] Stateful 3rd-party tools (git, docker, mvn, gradle, cmake) use global synchronized locks

## ADR-0012: TLS Termination Delegated to External Reverse Proxy

- [ ] No TLS configuration in `application.properties` (no keystore, truststore, or SSL settings)
- [ ] Default bind is `127.0.0.1` (localhost only)
- [ ] `application.properties.sample` documents `quarkus.http.host` with guidance for bare metal vs container
- [ ] `BearerAuthFilter` reads `Authorization` header directly — no proxy header rewriting dependency

## ADR-0013: LLM-Compatible Tool Naming and Categorization

- [ ] Every tool has an `operationId` and a `summary` in its `@Operation` annotation
- [ ] All `operationId`s use latin characters and snake_case only
- [ ] Operation IDs follow gradual context narrowing: `{category}_{subcategory}_{...}_{action}`
- [ ] No verb-first grammar (e.g., `read_file`, `set_volume`)
- [ ] All tool input parameters are body DTOs — no `@QueryParam`, no `@PathParam` on tool endpoints (exception: administrative CRUD endpoints like `PresetResource` are exempt; they may use `@PathParam` for resource identification)
- [ ] `ToolDispatcher` only has `DTO` and `NONE` binding strategies (no `QUERY_PARAM` or `PATH_PARAM`)
- [ ] Parameter names extend or confirm the operation's context (no generic names like `value`)
- [ ] `calculator_` prefix groups all pure-computation tools (functional category)

## ADR-0014: In-Process Tool Dispatch via ToolBean Marker Interface and CDI

- [ ] `ToolBean` marker interface exists in `com.sheahorn.llmtoolbox.llm.ToolBean`
- [ ] Every resource class with `@Operation`-annotated tool methods implements `ToolBean`
- [ ] `Calculator` interface extends `ToolBean` (so all calculators are automatically discoverable)
- [ ] `ToolDispatcher` scans for `ToolBean` instances via CDI at startup (`Arc.container().select(ToolBean.class)`)
- [ ] `ToolDispatcher.dispatch()` invokes methods reflectively in-process (no HTTP calls)
- [ ] No `HttpClient`, `port` config, `EndpointInfo`, or `buildEndpointIndex()` in `ToolDispatcher`
- [ ] `resolveTools()` and `buildToolDef()` are preserved and unchanged
- [ ] `BuiltinFunctionCache` remains the single source of truth for operationId→description mapping
- [ ] CDI proxy unwrapping handles synthetic/`$$` classes
