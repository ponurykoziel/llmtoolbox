#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
cd "$SCRIPT_DIR"

echo "==> Creating virtual environment..."
python3 -m venv .venv

echo "==> Activating and installing dependencies..."
source .venv/bin/activate
pip install -r requirements.txt

echo "==> Installing Chromium browser for Playwright..."
playwright install chromium

echo "==> Done. Virtual environment ready."
echo "    Activate with: source .venv/bin/activate"
