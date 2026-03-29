#!/bin/sh
# install-dependencies.sh - macOS/Linux dependency installer for sample-bots/typescript
# Mirrors the behavior of install-dependencies.cmd on Windows.
#
# Behavior:
# - Changes to the archive root (parent of the deps folder where this script lives)
# - If deps/.deps_installed does not exist, installs npm dependencies
# - npm install uses package.json at the archive root; node_modules/ is created there
#   so that Node.js module resolution can find it from any bot subdirectory
# - Only creates deps/.deps_installed if installation succeeds
# - Exits with non-zero code and message on failure
# - Uses a simple lock (deps/.deps_lock dir) to avoid concurrent installs

set -eu

# Change to the archive root (parent of the directory containing this script)
cd -- "$(dirname -- "$0")/.."

LOCK_DIR="deps/.deps_lock"

# Fast path: if already installed, exit silently
if [ -f "deps/.deps_installed" ]; then
  exit 0
fi

# Acquire lock using mkdir as a mutex, wait up to 300 seconds
acquire_lock() {
  local tries=0
  while ! mkdir "$LOCK_DIR" 2>/dev/null; do
    tries=$((tries + 1))
    if [ "$tries" -ge 300 ]; then
      echo "Error: Could not acquire dependency installation lock after 300 seconds." >&2
      exit 1
    fi
    sleep 1
  done
}

release_lock() {
  rmdir "$LOCK_DIR" 2>/dev/null || true
}

trap 'release_lock' EXIT INT TERM

acquire_lock

# Double-check after acquiring lock
if [ -f "deps/.deps_installed" ]; then
  exit 0
fi

echo "Installing dependencies..."

if ! command -v node >/dev/null 2>&1; then
  echo "Error: Node.js not found. Please install Node.js 18 or newer from https://nodejs.org/" >&2
  exit 1
fi

# Run npm install from the archive root (package.json is here, node_modules/ will be created here)
npm install --prefer-offline

touch "deps/.deps_installed"
echo "Dependencies installed."
