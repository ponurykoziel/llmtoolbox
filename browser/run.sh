#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
cd "$SCRIPT_DIR"

if [ ! -d ".venv" ]; then
    echo "ERROR: .venv not found. Run ./setup.sh first."
    exit 1
fi

echo "==> Activating virtual environment..."
source .venv/bin/activate

# Default entry point; override with env var or first argument
ENTRY="${1:-${APP_ENTRY:-server.py}}"

echo "==> Launching: $ENTRY"
exec python "$ENTRY"
