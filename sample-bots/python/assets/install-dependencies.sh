#!/usr/bin/env bash
# install-dependencies.sh — macOS/Linux dependency installer for sample-bots/python
# Mirrors the behavior of install-dependencies.cmd on Windows.
#
# Behavior:
# - Changes to the directory where this script resides
# - If .deps_installed does not exist, installs requirements from requirements.txt
# - Only creates .deps_installed if installation succeeds
# - Exits with non-zero code and message on failure

set -euo pipefail

# Change to the directory where this script is located
cd -- "$(dirname -- "$0")"

# Helper to locate a working pip command
find_pip() {
  if command -v python3 >/dev/null 2>&1 && python3 -m pip --version >/dev/null 2>&1; then
    echo "python3 -m pip"
    return 0
  fi
  if command -v python >/dev/null 2>&1 && python -m pip --version >/dev/null 2>&1; then
    echo "python -m pip"
    return 0
  fi
  if command -v pip3 >/dev/null 2>&1; then
    echo "pip3"
    return 0
  fi
  if command -v pip >/dev/null 2>&1; then
    echo "pip"
    return 0
  fi
  return 1
}

if [ ! -f ".deps_installed" ]; then
  echo "Installing dependencies..."

  if ! PIP_CMD=$(find_pip); then
    echo "Error: pip not found. Please install Python and pip." >&2
    exit 1
  fi

  if ${PIP_CMD} install -q -r requirements.txt; then
    # Create marker file to indicate dependencies are installed
    : > .deps_installed
    echo "Dependencies installed."
  else
    echo "Error: Failed to install dependencies." >&2
    exit 1
  fi
fi
