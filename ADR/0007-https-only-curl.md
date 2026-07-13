# ADR-0007: HTTPS-Only Curl with Optional Private-IP Blocking

**Date:** 2025-01-01 (inferred)
**Status:** Accepted

## Context

The `net_curl` tool allows LLMs to make HTTP requests via the system `curl` binary. This is a powerful capability that could be abused: SSRF attacks against internal services, exfiltration of data to attacker-controlled servers, or probing of the local network.

## Decision

The curl tool (`CurlController`) enforces multiple layers of restriction:

1. **HTTPS-only** — the URL scheme must be `https`; plain HTTP is rejected
2. **Protocol lock** — `curl --proto http,https --proto-redir http,https` prevents protocol smuggling
3. **Timeout** — `--max-time 5` hard timeout on every request
4. **Silent with errors** — `--silent --show-error` for clean output
5. **Always follow redirects** — `-L` flag
6. **Optional private-IP blocking** (`llmtoolbox.net.curl.block-private-ips`, default `false`) — when enabled, blocks:
   - `localhost`, `127.0.0.1`, `0.0.0.0`, `[::1]`
   - `169.254.0.0/16` (link-local)
   - `10.0.0.0/8`
   - `172.16.0.0/12`
   - `192.168.0.0/16`
   - `fc00::/7` and `fd00::/7` (IPv6 ULA)

7. **Header sanitization** — CR/LF characters are stripped from header names and values to prevent HTTP header injection
8. **Shell-safe construction** — all user-supplied values are passed through `ToolSupport.shellQuote()`

## Rationale

- **HTTPS-only** — ensures encryption in transit; prevents trivial sniffing on the local network.
- **Private-IP blocking is opt-in** — some deployments need to reach internal services (e.g., a local Open WebUI instance); the default `false` preserves that use case while allowing security-conscious deployments to lock down.
- **5-second timeout** — prevents the LLM from hanging on slow or unresponsive endpoints.
- **Protocol lock** — prevents `curl` from being tricked into using other protocols (ftp, file, etc.) via redirects.
- **Header sanitization** — prevents CRLF injection attacks that could smuggle additional HTTP headers or bodies.

## Consequences

- HTTP-only services are completely inaccessible; this is by design.
- The private-IP block list is hardcoded and must be updated if new private ranges are allocated.
- DNS rebinding attacks (where a hostname resolves to a private IP after the initial check) are not mitigated — the check is on the URL string, not the resolved IP.
- The 5-second timeout may be too short for some legitimate use cases but is not currently configurable.
