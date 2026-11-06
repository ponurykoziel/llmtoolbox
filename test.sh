#!/usr/bin/env bash
# Run the test suite. Output is teed to test.log (gitignored).
#
# Boots the whole application with src/test/resources/application.properties
# layered over the main one. No build required first; Maven compiles as needed.
#
# Note: some tool tests shell out (mvn, git, docker) and the browser sidecar
# tests use WireMock, so this is not a fast suite.
set -euo pipefail

mvn -B clean test | tee test.log
