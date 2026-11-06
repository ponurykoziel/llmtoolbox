# llmtoolbox

A suite of tools for building custom AI-driven workflows. Tools are meant to be used by LLMs, and were strongly profiled for the target audience. 
LLMToolbox allows AI to interact with the enviroment and offers support in areas, in which AI is likely to hallucinate. 
Tool composition as been optimized to achieve efficient semantic distances between operations, backed by averaged online tests on GPT, Grok, Qwen, Gemma, Deepseek, GLM, Kimi, Minimax and Nemotron. 

Your LLM can access the filesystem, solve a quadric equation, inspect your host state, control media, traceroute and dig a crypto scam and verify their APY, count remaining bussiness days of an SSL cert, use CIELAB, drive a headless browser, call other LLMs, and more. 

Windows and MacOS are neither supported nor planned to be such. 

## Quick Start

# 1. Build

```bash
mvn clean package
```
Requires Java 17 and maven. 

# 2. Configure 
You *may* launch the application with default credentials. If doing so, you are obliged to understand the associated risk. Launching this application under improper risk management is not permitted. If you have launched this application by accident, shut it down immediately and send an apology email to your ISP.
interface: `172.0.0.1`
port: `8080`
default credentials: `admin/admin`
master API key: `change-me`
`minimal.application.properties` file is provided to support this step. Runner script utilizes this file.

# 3. Run
```bash
./run.sh
```

In addition, `run.sh` contains comments on running the application in a screen and in a docker.

# 4. Use
The app listens on `http://127.0.0.1:8080` by default. UI is available at `/`.
OpenAPI JSON specs are provided in the UI. 

## Configuration

All settings live in `application.properties`. See `application.properties.sample` for full documentation of every knob. The minimal viable set:

| Property | Description |
|---|---|
| `llmtoolbox.auth.token` | Master Bearer token for API access |
| `llmtoolbox.auth.admin.username` / `.password` | Seeded admin user for the browser UI |
| `llmtoolbox.files.allowed-root` | Workspace root — all FS operations are confined here |
| `llmtoolbox.command.timeout-seconds` | Per-command timeout (Maven may need 180+) |
| `llmtoolbox.communication.telegram.token` / `.chatid` | Telegram bot credentials (fast and cheap push notifications) |


## Authentication

Two parallel auth mechanisms:

- **Browser UI** — form login with session cookies. The admin user is seeded on first boot.
- **API** — Bearer token in the `Authorization` header. Accepts the master token (`llmtoolbox.auth.token`) or user-created API keys (managed via the browser UI under Account).

API endpoints also accept the session cookie, so a browser session works for both UI and API calls.

## Tool Categories

### Filesystem (`fs_*`)

File CRUD (create, read, overwrite, append, replace, replace-regex, copy-file, move-file, delete), directory operations (mkdir, rmdir, move-dir), listing (flat, recursive), pack-folder (concatenate a directory into a single text bundle), head/tail, df, du, lsblk, findmnt, ls-info, sha256sum. All paths are validated and confined to `llmtoolbox.files.allowed-root`. Symlink escapes are detected and blocked.

### Network (`net_*`)

ping, dig (A, AAAA, CNAME, MX, TXT, NS, SOA, SRV, CAA, and more), curl (HTTPS-only, optional private-IP blocking), traceroute, TLS certificate inspection (openssl s_client), TCP connectivity test (netcat), iperf3 client, and a combined host-info dump (hostname, ip addr/route/neigh, resolv.conf).

### Host Control (`host_*`)

- **Power** — shutdown and reboot via systemctl.
- **Audio** — volume set/up/down, mute toggle, play/pause, next/previous track (uses PulseAudio/PipeWire `pactl` and `playerctl`).
- **Monitor** — DPMS off via `xset`, wake via `xset dpms force on` + mouse nudge via `xdotool`.
- **Services** — list running and failed systemd units.
- **Logs** — journalctl (by unit, user unit, failed), dmesg.
- **System info** — free, uptime, who, ps, top, ss, ip addr/route/neigh.
- **Hardware** — lscpu, lsmem, lsusb, lspci, sensors, nvidia-smi.
- **Net info** — whois lookups.

### Build & DevOps Tools (`build_*`, `devops_*`)

**Maven** — operations scoped to a project path inside the allowed root: clean, compile, test, test-one (single test class), verify, package, package-skip-tests, dependency:tree, help:effective-pom, help:effective-settings, versions:display-dependency-updates. Supports `-o` (offline), `-P` (profile), and `-DskipTests` flags. A global lock synchronizes all Maven invocations to prevent concurrent build corruption.

**Gradle** — operations scoped to a project path inside the allowed root: clean, compile, test, test-one (single test class), check, build, build-skip-tests, dependencies, properties, buildEnvironment, dependencyUpdates. Supports `--offline`, `-P` (project property), and `-x test` (skip tests) flags. A global lock synchronizes all Gradle invocations to prevent concurrent build corruption.

**Git** (`devops_git_*`) — operations scoped to a repo path inside the allowed root: status, log (full / oneline / recent-n), diff, diff-staged, remote & config print, add, commit, reset (mixed / soft / hard / hard-to-origin), clean, branch (list local & remote, create, delete, set-upstream), checkout (branch / commit / new branch), merge (normal / squash / abort / continue / list-conflicts), fetch, push (incl. custom branch and `-u` set-upstream), pull (merge / rebase / ff-only), clone. A global lock serializes all Git invocations to prevent concurrent repo corruption.

**CMake** — operations scoped to source and build paths inside the allowed root: configure (with -D defines and build type), build (with --target, --clean-first, -j, --verbose), and read-cache (parses CMakeCache.txt for key variables). A global lock serializes all CMake invocations.

**Cargo** (`build_cargo_*`) — Rust build system, scoped to a project path inside the allowed root: build (optional --release), test, test-one (single test filter), check (fast compile check, no binary), clean, tree (dependency tree), metadata (JSON project metadata). Supports --offline. A global lock serializes all Cargo invocations.

**Docker** (`devops_docker_*`) — container and image management: version / info / stats, containers (list, inspect, top, ports, logs full & tail, start, stop, restart, rename, remove, retrieve-file, network connect & disconnect), images (list, pull, remove, prune), networks (list, inspect, create, remove), volumes (list, inspect), build, history, exec, run, compose (up, down, ps, logs, restart, build, pull). A global lock serializes all Docker invocations to prevent concurrent state corruption.

### BinPeek (`binpeek_*`)

Binary inspection tools for ELF binaries and other files, scoped to a path inside the allowed root: `ldd` (shared object dependencies), `nm -D` (dynamic symbols), `readelf -d` (dynamic section), `readelf -s` (symbol table), `objdump -T` (dynamic symbol table), `strings` (printable character sequences), `file` (file type detection).

### Basics (`time_now`, `sleep`, `memory_*`, `notes_*`, `clipboard_*`, `presets_*`)

- **Current time** — UTC timestamp with ISO, human-readable, and epoch formats.
- **Sleep** — sleeps for the given number of seconds, returns from/to timestamps.
- **Memory** — persistent key-value store (add, get, find, list, delete).
- **Notes** — titled notes with CRUD and search. Technically, similar to memories.
- **Clipboard** — in-memory clipboard (read/write/append). A scratchpad for the LLM, not the host clipboard.
- **Presets** — presets for collections of tools. Key idea of this application.

### Calculators (`calculator_*`)

A large collection of computational endpoints. Calculator tools relieve your LLM of hallucinating calculations. Ask your LLM.

Tools are grouped into categories. UI help browsing and discovering tools. 

There are numerous calculation categories, such as 
- arithmetic (add, subtract, multiply, divide, power, modulo, remainder, round)
- unary math (sqrt, cbrt, log2, log10, ln, sin, cos, tan, sign, abs, magnitude, nearest-int, ceil, floor, trunc)
- unit conversion (length, mass, temperature, pressure, energy, area, volume, speed, data) 
followed by:
- color adjustment and space conversion 
- network/subnet/CIDR
- text manipulation
- statistics
- financials
- date/time
- regex
- random value generators
- semver parsing/comparison
- base conversion
- bitwise operations
- 2D/3D vectors
- crypt (md5, sha256, sha3, base64, hmac, checksums, hex)

### Communication (`communication_*`)

A stub category which handles Telegram only. Send messages to a configured Telegram chat (max 4096 characters).

### Custom Functions

User-defined shell-command tools. Create custom functions via the browser UI under **Functions** — each gets an `operationId`, a `description`, and a `shellCommand`. Custom functions are automatically injected into the OpenAPI spec at load time, so they appear alongside built-in tools in presets and selectors. Execution returns the standard `ExecutionResponse` (stdout, stderr, exit code, duration). No parameters — just a fixed shell command.

### Terminal (`terminal_*`)

Raw shell command execution. **Disabled by default** — set `llmtoolbox.terminal.allow=true` to enable. If the JVM runs with passwordless sudo, this effectively grants root access. 
Primary use case of this tool is narrow. Useful when setting up autonomous daemon environments for LLMs. On a dedicated bare metal. You want it dedicated, trust me. It will take a few clean installs for them to learn. 

### LLM Calling (`llm_execute_request`)

llmtoolbox can act as an LLM client itself. Manage **Providers** (OpenAI-compatible or Ollama endpoints), **Models**, **Personalities** (system prompt + sampling parameters), and **Agents** (provider + model + personality + tool preset) via the browser UI under **LLM**. The `llm_execute_request` tool executes a prompt through a configured agent with tool support: the agent can call any tools from its preset, dispatched in-process, in a multi-round loop until a final answer. An optional `imagePath` field attaches a single image (by file path, read and base64-encoded server-side) to the prompt — supported in chat mode only; completions mode rejects it.

### Browser (`browser_*`)

Opt-in headless browsing via a Python sidecar (FastAPI + Playwright + persistent Chromium), managed as a child process by the Java app. **Disabled by default** — enable with `llmtoolbox.browser.enabled=true`, install dependencies with `browser/setup.sh`, and configure `browser/config.yaml` (see `config.yaml.sample`). The sidecar binds to `127.0.0.1` only and shares a bearer token with the Java proxy.

Tools: profile management (list, create, delete), sessions (open, close, list, current URL), and page interaction — navigate, get HTML, get visible text, click element, screenshots (base64, PNG, and to-file), execute JavaScript, type text. The `*_file` variants (`browser_interact_page_capture_screenshot_file`, `browser_interact_page_get_html_file`) write the payload to a file inside the allowed root and return only `{ path, bytes }`, so large screenshots/HTML don't flood the LLM context. Sessions run on isolated browser profiles with stealth mitigations.

## Host Dependencies

Some tools shell out to system commands. The table below lists what each tool category needs beyond a bare Linux install.

| Tool | Command | Package (apt) |
|---|---|---|
| `fs_sha256sum` | `sha256sum` | coreutils |
| `fs_lsblk` | `lsblk` | util-linux |
| `fs_findmnt` | `findmnt` | util-linux |
| `net_ping` | `ping` | iputils-ping |
| `net_dig` | `dig` | dnsutils |
| `net_curl` | `curl` | curl |
| `net_traceroute` | `traceroute` | traceroute |
| `net_tls_check` | `openssl` | openssl |
| `net_tcp_connect` | `nc` | netcat-openbsd |
| `net_iperf` | `iperf3` | iperf3 |
| `host_audio_*` | `pactl` | pulseaudio-utils / pipewire-pulse |
| `host_audio_*` (media keys) | `playerctl` | playerctl |
| `host_monitor_off` | `xset` | x11-xserver-utils |
| `host_monitor_on` | `xdotool` | xdotool |
| `host_hardware_lsusb` | `lsusb` | usbutils |
| `host_hardware_lspci` | `lspci` | pciutils |
| `host_hardware_sensors` | `sensors` | lm-sensors |
| `host_hardware_nvidia_smi` | `nvidia-smi` | nvidia-utils |
| `host_netinfo_whois` | `whois` | whois |
| `build_mvn_*` | `mvn` | maven |
| `build_gradle_*` | `gradle` | gradle |
| `build_cmake_*` | `cmake` | cmake |
| `build_cargo_*` | `cargo` | cargo (via rustup) |
| `devops_git_*` | `git` | git |
| `devops_docker_*` | `docker` | docker.io / docker-ce |
| `browser_*` | `python3` + Playwright + Chromium | see `browser/setup.sh` |
| `binpeek_ldd` | `ldd` | libc-bin |
| `binpeek_nm_dynamic` | `nm` | binutils |
| `binpeek_readelf_*` | `readelf` | binutils |
| `binpeek_objdump_dynamic_symbols` | `objdump` | binutils |
| `binpeek_strings` | `strings` | binutils |
| `binpeek_file` | `file` | file |

Everything else (`find`, `head`, `tail`, `df`, `du`, `ls`, `cat`, `hostname`, `free`, `uptime`, `who`, `ps`, `top`, `ss`, `ip`, `systemctl`, `journalctl`, `dmesg`, `lscpu`, `lsmem`) is part of coreutils, procps, iproute2, util-linux, or systemd — present on any typical Linux host.

## Open WebUI Integration

llmtoolbox auto-generates an OpenAPI 3.1 spec (via Quarkus SmallRye OpenAPI). You can extract subsets of it for Open WebUI's tool integration:

### By Preset (default way)

```
GET /api/openapi/preset/{name}
```

Built-in presets: `all`, `fs`, `net`, `host`, `build`, `mvn`, `maven`, `devops`, `communication`, `basics`, `current_time`, `presets`, `terminal`, `calculator`, `git`, `docker`, `binpeek`.

Seeded composite presets: `daemon` (filesystem + host info + network + clipboard + memory + notes + communication + current time), `builder` (filesystem + Maven), `host_ctl` (audio + power + monitor), `host_info` (hardware + netinfo + sysinfo + services + logs), `devops` (all git + docker tools).

### By Selector (if you must)

```
GET /api/openapi/select/{selectors}
```

Comma-separated operationIds or prefixes with `*` wildcards. Examples:
- `fs_*` — all filesystem tools
- `fs_files_read,fs_files_create,net_ping` — exact operationIds
- `host_audio_*,host_power_*` — audio and power tools

## Changelog

- **1.9.0** — Browser file-capture tools: `browser_interact_page_capture_screenshot_file` and `browser_interact_page_get_html_file` write screenshots/HTML to disk and return only `{ path, bytes }` instead of flooding the LLM context. Centralized browser screenshot settings (format, compression, byte cap) exposed as callable tools (`browser_settings_screenshot_get` / `browser_settings_screenshot_set`). Browser sidecar venv management: a 4-state status (off / init / no venv / initializing / on) plus one-click venv initialization from the dashboard with streamed setup output (SSE); the sidecar proxy now omits null fields and pins HTTP/1.1. LLM calling image attachments: `llm_execute_request` accepts an optional `imagePath` (single image, chat mode only; completions mode rejects it). System instruction generator in the LLM calling personalities UI. Toolset prefix: optional global operationId prefix (`llmtoolbox.global.function.prefix`) applied only to the OpenAPI subset, surfaced on the dashboard. Search subpage now shows function descriptions (full description in results). Preset UX: user-created presets sort above built-in defaults, create/edit pages show a live resolved-functions preview for wildcard prefixes, the edit page gains the Browse Functions picker, and the Functions list shows descriptions.
- **1.8.1** — BinPeek tools (`binpeek_*`): ldd, nm -D, readelf -d, readelf -s, objdump -T, strings, file.
- **1.8.0** — LLM calling: providers, models, personalities, and agents with the `llm_execute_request` endpoint (multi-round in-process tool loop). Browser sidecar: opt-in headless browsing (FastAPI + Playwright + Chromium) with 15 `browser_*` tools. Cargo build tools (`build_cargo_*`): build, test, test-one, check, clean, tree, metadata. Sleep tool. Filesystem copy tool (`fs_files_copy_file`). Filesystem moves split by type: `fs_files_move_file` (renamed from `fs_files_move`) and `fs_files_move_dir` (new, directories only). Git and Docker tools renamed to `devops_git_*` / `devops_docker_*` per ADR-0013. ADR-0013 (LLM tool naming) and ADR-0014 (in-process ToolBean dispatch) accepted.
- **1.7.1** — ADR compliance pass: fixed `mfop`→`llmtoolbox` config property name in calculator resources, added `implements Calculator` to all calculator resource classes, moved calculator resources to `calculators/resource/` subdirectory, clarified ADR-0004 and ADR-0013 checklist rules.
- **1.7.0** — In-process tool dispatch via `ToolBean` marker interface and CDI. `ToolDispatcher` invokes methods reflectively — no HTTP calls. `Calculator` interface extends `ToolBean` for automatic discovery. `BuiltinFunctionCache` is the single source of truth for operationId→description mapping.
- **1.6.1** — Fixed Bearer token authentication. Added Git push -u and merging support. Added sleep support.
- **1.6.0** — Custom functions v1: user-defined shell-command tools with CRUD UI, OpenAPI injection, and execution via `/api/tools/functions/custom/{operationId}`. Rewrote filesystem path guard with simpler, more robust rules that work for non-existent paths.
- **1.5.1** — Fixed a bug in filesystem path validation where accessing the allowed root itself (e.g. `fs_ls_flat` on `/workzone/angelica`) would fail with a 500 because the parent-of-root real-path check incorrectly treated the root as out-of-bounds. Improved error messages to include the actual path and allowed root. Test configuration hardened (random port, proactive auth disabled, JDBC realm properly wired).
- **1.5** — Added Gradle support (`build_gradle_*`): clean, compile, test, test-one, check, build, build-skip-tests, dependencies, properties, buildEnvironment, dependencyUpdates.
- **1.4** — Added Git support (`build_git_*`), Docker support (`build_docker_*`), and CMake support (`build_cmake_*`). Added tool call history and in-flight tracking with lock status endpoint.

## Wiring it up 

Examples in python and java are provided in `examples`.

For Open WebUI:
1. Make sure Open WebUI can reach your llmtoolbox host (VPN, proxy, or same network).
2. Go to **Admin Panel → Settings → Integrations → [+]** (Add Connection).
3. Type: **OpenAPI**
4. Name: anything you like
5. URL: `http://127.0.0.1:8080`
6. Auth: **Bearer**, paste your master token (or a user-created API key)
7. Advanced: **OpenAPI Spec** — choose **URL**, "api/openapi/preset/<preset_name>"
8. Check connection. 
9. Save and refresh (Open WebUI does not reload tools itself)

Repeat for different presets if you want separate tool groups for different models.

