#!/usr/bin/env bash
# install-dependencies.sh — macOS/Linux dependency installer for sample-bots/python
# Mirrors the behavior of install-dependencies.cmd on Windows.
#
# Behavior:
# - Changes to the directory where this script resides
# - If .deps_installed does not exist, installs requirements from requirements.txt
# - Uses a virtual environment to avoid externally-managed-environment errors
# - Also installs local robocode_tank_royale-*.whl if present
# - Only creates .deps_installed if installation succeeds
# - Exits with non-zero code and message on failure
# - Uses a simple lock (.deps_lock dir) to avoid concurrent installs

set -euo pipefail

# Change to the directory where this script is located
cd -- "$(dirname -- "$0")"

LOCK_DIR=".deps_lock"

# Fast path: if already installed, exit silently
if [ -f ".deps_installed" ]; then
  exit 0
fi

# Acquire lock using mkdir as a mutex, wait up to 300 seconds
acquire_lock() {
  local tries=0
  while ! mkdir "$LOCK_DIR" 2>/dev/null; do
    tries=$((tries+1))
    if [ $tries -ge 300 ]; then
      echo "Error: Could not acquire dependency installation lock after 300 seconds." >&2
      exit 1
    fi
    sleep 1
  done
}

release_lock() {
  rmdir "$LOCK_DIR" 2>/dev/null || rm -rf "$LOCK_DIR" 2>/dev/null || true
}

trap 'release_lock' EXIT INT TERM

acquire_lock

# Double-check after acquiring lock
if [ -f ".deps_installed" ]; then
  release_lock
  exit 0
fi

# Helper to find a working Python command
find_python() {
  if command -v python3 >/dev/null 2>&1; then
    echo "python3"
    return 0
  fi
  if command -v python >/dev/null 2>&1; then
    echo "python"
    return 0
  fi
  return 1
}

# Helper to create and use virtual environment
setup_venv() {
  local python_cmd="$1"
  local venv_dir="venv"

  # Create virtual environment if it doesn't exist
  if [ ! -d "$venv_dir" ]; then
    echo "Creating virtual environment..." >&2
    if ! "$python_cmd" -m venv "$venv_dir"; then
      echo "Error: Failed to create virtual environment. Make sure python3-venv is installed." >&2
      echo "Try: sudo apt install python3-venv python3-full" >&2
      exit 1
    fi
  fi

  local venv_python="$venv_dir/bin/python"

  # Ensure pip module is available in the virtual environment
  if ! "$venv_python" -m pip --version >/dev/null 2>&1; then
    # Try to bootstrap pip quietly; ignore failure so we can check afterwards
    "$venv_python" -m ensurepip --upgrade >/dev/null 2>&1 || true
  fi

  # Verify pip works via module invocation and return a runner command
  if "$venv_python" -m pip --version >/dev/null 2>&1; then
    echo "$venv_python -m pip"
    return 0
  else
    echo "Error: pip is not available in the virtual environment. ensurepip may be disabled. Please install python3-venv or Python with pip included." >&2
    return 1
  fi
}

install_local_wheel_or_pypi() {
  # Install local wheel if present, otherwise install from PyPI
  shopt -s nullglob
  local wheels=(robocode_tank_royale-*.whl robocode-tank-royale-*.whl)
  shopt -u nullglob
  if [ ${#wheels[@]} -gt 0 ]; then
    echo "Installing local wheel: ${wheels[0]}"
    $1 install -q "${wheels[0]}"
  else
    echo "Local robocode_tank_royale-*.whl or robocode-tank-royale-*.whl not found. Installing robocode-tank-royale from PyPI..."
    $1 install -q "robocode-tank-royale"
  fi
}

echo "Installing dependencies..."

if ! PYTHON_CMD=$(find_python); then
  echo "Error: Python not found. Please install Python." >&2
  exit 1
fi

# Try to use system pip first, fall back to virtual environment on failure
PIP_CMD=""

# First, try to find system pip
if command -v python3 >/dev/null 2>&1 && python3 -m pip --version >/dev/null 2>&1; then
  PIP_CMD="python3 -m pip"
elif command -v python >/dev/null 2>&1 && python -m pip --version >/dev/null 2>&1; then
  PIP_CMD="python -m pip"
elif command -v pip3 >/dev/null 2>&1; then
  PIP_CMD="pip3"
elif command -v pip >/dev/null 2>&1; then
  PIP_CMD="pip"
fi

# Try system pip installation first
if [ -n "$PIP_CMD" ] && ${PIP_CMD} install -q -r requirements.txt 2>/dev/null; then
  install_local_wheel_or_pypi "$PIP_CMD"
  # Create marker file to indicate dependencies are installed
  : > .deps_installed
  echo "Dependencies installed using system pip."
else
  # System pip failed (likely externally-managed-environment), use virtual environment
  echo "System pip failed (likely externally managed environment). Using virtual environment..."

  if ! VENV_PIP=$(setup_venv "$PYTHON_CMD"); then
    echo "Error: Failed to setup virtual environment." >&2
    exit 1
  fi

  if ${VENV_PIP} install -q -r requirements.txt; then
    install_local_wheel_or_pypi "$VENV_PIP"
    # Create marker file to indicate dependencies are installed
    : > .deps_installed
    echo "Dependencies installed in virtual environment."
    echo "Note: Virtual environment created in ./venv directory"
  else
    echo "Error: Failed to install dependencies in virtual environment." >&2
    exit 1
  fi
fi
