#!/usr/bin/env bash
# create-venv.sh — Create and prepare a Python virtual environment for bot-api/python
# Cross-platform note: this script targets macOS/Linux. A PowerShell variant exists for Windows.
# Behavior:
# - Creates a local virtual environment in .venv if missing
# - Ensures pip is present and upgrades core tooling (pip, setuptools, wheel)
# - Installs required dependencies from requirements.txt
# - Idempotent: safe to run multiple times

set -euo pipefail

cd -- "$(dirname -- "$0")/.."

find_python() {
  if command -v python3 >/dev/null 2>&1; then
    echo "python3"
    return 0
  fi
  if command -v python >/dev/null 2>&1; then
    echo "python"
    return 0
  fi
  echo ""; return 1
}

PYTHON_CMD="$(find_python)" || {
  echo "Error: Python not found on PATH. Please install Python 3.10+" >&2
  exit 1
}

VENV_DIR=".venv"

if [ ! -d "$VENV_DIR" ]; then
  echo "Creating virtual environment in $VENV_DIR ..."
  if ! "$PYTHON_CMD" -m venv "$VENV_DIR"; then
    echo "Error: Failed to create venv. Ensure python3-venv is installed (e.g., sudo apt install python3-venv)." >&2
    exit 1
  fi
fi

VENV_PY="$VENV_DIR/bin/python"

# Make sure pip is available inside the venv
if ! "$VENV_PY" -m pip --version >/dev/null 2>&1; then
  "$VENV_PY" -m ensurepip --upgrade >/dev/null 2>&1 || true
fi

# Now upgrade core tooling quietly
"$VENV_PY" -m pip install -q --upgrade pip setuptools wheel

# Install project requirements
if [ -f "requirements.txt" ]; then
  echo "Installing Python dependencies from requirements.txt ..."
  "$VENV_PY" -m pip install -q -r requirements.txt
fi

echo "Virtual environment ready: $("$VENV_PY" --version)"
