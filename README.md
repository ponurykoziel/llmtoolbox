# llmtoolbox

A suite of tools for building custom AI-driven workflows. Tools are meant to be used by LLMs, and were strongly profiled for the target audience. 
LLMToolbox allows AI to interact with the enviroment and offers support in areas, in which AI is likely to hallucinate. 
Tool composition as been optimized to achieve efficient semantic distances between operations, backed by averaged online tests on GPT, Grok, Qwen, Gemma, Deepseek, GLM, Kimi, Minimax and Nemotron. 

Your LLM can access the filesystem, solve a quadric equation, inspect your host state, control media, traceroute and dig a crypto scam and verify their APY, count remaining bussiness days of an SSL cert, use CIELAB, and more. 

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

File CRUD (create, read, overwrite, append, replace, replace-regex, move, delete), directory operations (mkdir, rmdir), listing (flat, recursive), pack-folder (concatenate a directory into a single text bundle), head/tail, df, du, lsblk, findmnt, ls-info, sha256sum. All paths are validated and confined to `llmtoolbox.files.allowed-root`. Symlink escapes are detected and blocked.

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

### Build Tools (`build_*`)

**Maven** — operations scoped to a project path inside the allowed root: clean, compile, test, test-one (single test class), verify, package, package-skip-tests, dependency:tree, help:effective-pom, help:effective-settings, versions:display-dependency-updates. Supports `-o` (offline), `-P` (profile), and `-DskipTests` flags. A global lock synchronizes all Maven invocations to prevent concurrent build corruption.

**Git** — operations scoped to a repo path inside the allowed root: status, log, diff, diff-staged, add, commit, push, pull, branch, clone, stash, tag. A global lock serializes all Git invocations to prevent concurrent repo corruption.

**Docker** — container and image management: ps, images, logs, run, stop, start, rm, rmi, pull, build, exec, inspect, compose up/down. A global lock serializes all Docker invocations to prevent concurrent state corruption.

### Basics (`time_now`, `memory_*`, `notes_*`, `clipboard_*`, `presets_*`)

- **Current time** — UTC timestamp with ISO, human-readable, and epoch formats.
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

### Terminal (`terminal_*`)

Raw shell command execution. **Disabled by default** — set `llmtoolbox.terminal.allow=true` to enable. If the JVM runs with passwordless sudo, this effectively grants root access. 
Primary use case of this tool is narrow. Useful when setting up autonomous daemon environments for LLMs. On a dedicated bare metal. You want it dedicated, trust me. It will take a few clean installs for them to learn. 

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
| `build_git_*` | `git` | git |
| `build_docker_*` | `docker` | docker.io / docker-ce |

Everything else (`find`, `head`, `tail`, `df`, `du`, `ls`, `cat`, `hostname`, `free`, `uptime`, `who`, `ps`, `top`, `ss`, `ip`, `systemctl`, `journalctl`, `dmesg`, `lscpu`, `lsmem`) is part of coreutils, procps, iproute2, util-linux, or systemd — present on any typical Linux host.

## Open WebUI Integration

llmtoolbox auto-generates an OpenAPI 3.1 spec (via Quarkus SmallRye OpenAPI). You can extract subsets of it for Open WebUI's tool integration:

### By Preset (default way)

```
GET /api/openapi/preset/{name}
```

Built-in presets: `all`, `fs`, `net`, `host`, `build`, `communication`, `basics`, `current_time`, `presets`, `terminal`, `calculator`.

Seeded composite presets: `daemon` (filesystem + host info + network + clipboard + memory + notes + communication + current time), `builder` (filesystem + Maven), `host_ctl` (audio + power + monitor), `host_info` (hardware + netinfo + sysinfo + services + logs).

### By Selector (if you must)

```
GET /api/openapi/select/{selectors}
```

Comma-separated operationIds or prefixes with `*` wildcards. Examples:
- `fs_*` — all filesystem tools
- `fs_files_read,fs_files_create,net_ping` — exact operationIds
- `host_audio_*,host_power_*` — audio and power tools

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

