# Session Handoff — 2026-07-26

**Project:** LLMToolbox (`00000087` under toolchain)  
**Branch:** `main` (commit `9186744`, clean)  
**Version:** 1.7.1

---

## Completed this session

### WireMock integration tests (`00000C41` — DONE)
- Added `quarkus-junit5-mockito` and `wiremock-standalone` 3.9.1 test dependencies
- Created `WireMockTestResource.java` — QuarkusTestResourceLifecycleManager, dynamic port
- Created `LlmExecutionServiceTest.java` — 18 test scenarios, all passing:
  - Chat mode: simple response, single/multi tool calls, multi-round loop, max rounds exceeded, API errors, malformed JSON, empty choices
  - Completions mode: success, Ollama rejection, API error
  - Agent resolution: agent/provider/model/personality not found
  - URL verification: OpenAI `/v1/chat/completions`, OpenWebUI `/api/chat/completions`, Ollama `/api/chat` with stream:false + options
- Uses `@InjectMock` on `ToolDispatcher`, `@QuarkusTestResource(WireMockTestResource.class)`, `@Transactional` on each test
- Separate H2 DB (`llmtoolbox-wiremock`) with `drop-and-create` to avoid schema conflicts with `LlmCrudTest`

### ADR Compliance (`00000C2B` — DONE, previous session)
- Fixed `mfop.calculator.rounding-digits` → `llmtoolbox.calculator.rounding-digits` in `UnaryOperatorsResource` and `BinaryOperatorsResource`
- Added `implements Calculator` to `BaseResource`, `UnaryOperatorsResource`, `BinaryOperatorsResource`
- Moved all three to `calculators/resource/` subdirectory with proper package and explicit imports
- Added `ADR/COMPLIANCE-CHECKLIST.md` — full 14-ADR audit, 93/93 checks pass
- Clarified ADR-0004: `df`/`du` extend `FsResourceSupport` for path validation (ADR was wrong, not the code)
- Clarified ADR-0013: administrative CRUD endpoints like `PresetResource` are exempt from no-`@PathParam` rule

### Other (previous session)
- Version bump: `1.7.0` → `1.7.1` in `pom.xml`, `README.md` changelog
- `run.sh` now version-agnostic: `ls target/llmtoolbox-*-runner.jar` (matching Gauge's `runner.sh` pattern)
- Compiled successfully: `mvn -B clean compile` — 331 source files, BUILD SUCCESS

---

## Ready for next session

### Merge headless_browser into llmtoolbox (`00000C44` — TODO)
Full plan in the issue. Merge `toolchain/headless_browser` (FastAPI + Playwright + Chromium) as a managed Python sidecar, following the angallery pattern.

**Source:** `/workzone/angelica/toolchain/headless_browser/` — 15 REST endpoints, stealth patches, profile/session management

**Reference:** `/workzone/angelica/toolchain/angallery/` — Python-in-Java sidecar pattern (ProcessBuilder, shell wrappers, venv)

**Work items:**
1. Copy Python sidecar to `browser/` subdirectory
2. `BrowserSidecar.java` — lifecycle management (start/stop/health check)
3. Java resource classes (15 endpoints) proxying to Python via HttpClient, all implementing `ToolBean`
4. Configuration: `llmtoolbox.browser.enabled` (default false), port, python path
5. Security: 127.0.0.1 binding, token auth, consider admin-only gating
6. WireMock integration tests

**Files to create:** `browser/` (6 files), `src/main/java/.../browser/` (4 files), `src/test/java/.../browser/` (1 file)

**Files to modify:** `application.properties`, `.gitignore`

---

## Key files

| File | Why |
|------|-----|
| `ADR/COMPLIANCE-CHECKLIST.md` | 14-ADR audit checklist (93/93 pass) |
| `calculators/resource/BaseResource.java` | New location, `implements Calculator` |
| `calculators/resource/UnaryOperatorsResource.java` | New location, `implements Calculator`, fixed config property |
| `calculators/resource/BinaryOperatorsResource.java` | New location, `implements Calculator`, fixed config property |
| `llm/LlmExecutionService.java` | Service under test — chat tool-loop + completions, 3 API types |
| `llm/ToolDispatcher.java` | In-process dispatch via CDI, mocked in WireMock tests |
| `src/test/java/.../llm/WireMockTestResource.java` | QuarkusTestResourceLifecycleManager, dynamic WireMock port |
| `src/test/java/.../llm/LlmExecutionServiceTest.java` | 18 WireMock integration tests |
| `run.sh` | Glob-based jar discovery, no more version bumps |
