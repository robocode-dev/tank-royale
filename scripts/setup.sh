#!/usr/bin/env bash
# Idempotent setup script for Tank Royale development.
# Works both inside the dev container and on a plain Ubuntu/Debian host.
# Re-running is safe: each section is skipped when the tool is already present.
set -euo pipefail

ARCH="$(dpkg --print-architecture)"

# ---------------------------------------------------------------------------
# .NET SDK 8.0 — installed via apt (system PATH, no manual env vars needed)
# ---------------------------------------------------------------------------
if ! command -v dotnet &>/dev/null; then
    echo "==> Installing .NET SDK 8.0 ..."
    sudo apt-get update -q

    # Ubuntu 24.04+ ships dotnet-sdk-8.0 natively; other distros need Microsoft's feed
    if ! apt-cache show dotnet-sdk-8.0 &>/dev/null 2>&1; then
        . /etc/os-release
        wget -q "https://packages.microsoft.com/config/${ID}/${VERSION_ID}/packages-microsoft-prod.deb" \
            -O /tmp/packages-microsoft-prod.deb
        sudo dpkg -i /tmp/packages-microsoft-prod.deb
        rm /tmp/packages-microsoft-prod.deb
        sudo apt-get update -q
    fi

    sudo apt-get install -y --no-install-recommends dotnet-sdk-8.0
    sudo rm -rf /var/lib/apt/lists/*

    # dotnet global tools (e.g. docfx) install to ~/.dotnet/tools — persist to PATH
    for f in "$HOME/.bashrc" "$HOME/.profile"; do
        if [ -f "$f" ] && ! grep -q '.dotnet/tools' "$f"; then
            printf '\nexport PATH="$PATH:$HOME/.dotnet/tools"\n' >> "$f"
        fi
    done
    export PATH="$PATH:$HOME/.dotnet/tools"
    echo "    .NET $(dotnet --version) installed."
else
    echo "==> .NET already installed: $(dotnet --version)"
fi

# ---------------------------------------------------------------------------
# Java 17 (default) + Java 11 (toolchain) via Eclipse Temurin / Adoptium
# ---------------------------------------------------------------------------
if ! command -v java &>/dev/null; then
    echo "==> Installing Java 17 and 11 (Eclipse Temurin) ..."
    sudo apt-get update -q
    sudo apt-get install -y --no-install-recommends gnupg
    wget -qO - https://packages.adoptium.net/artifactory/api/gpg/key/public \
        | gpg --dearmor \
        | sudo tee /usr/share/keyrings/adoptium.gpg >/dev/null
    echo "deb [signed-by=/usr/share/keyrings/adoptium.gpg] \
https://packages.adoptium.net/artifactory/deb bookworm main" \
        | sudo tee /etc/apt/sources.list.d/adoptium.list >/dev/null
    sudo apt-get update -q
    sudo apt-get install -y temurin-17-jdk temurin-11-jdk
    sudo rm -rf /var/lib/apt/lists/*

    target="/usr/lib/jvm/temurin-17-jdk-${ARCH}"
    if [ ! -d "$target" ]; then
        echo "ERROR: Expected JDK not found: $target" >&2
        exit 1
    fi
    sudo ln -sfn "$target" /usr/lib/jvm/temurin-17-jdk
    sudo update-alternatives --set java  "${target}/bin/java"
    sudo update-alternatives --set javac "${target}/bin/javac"

    for f in "$HOME/.bashrc" "$HOME/.profile"; do
        if [ -f "$f" ] && ! grep -q 'JAVA_HOME' "$f"; then
            printf '\nexport JAVA_HOME="/usr/lib/jvm/temurin-17-jdk"\n' >> "$f"
        fi
    done
    export JAVA_HOME="/usr/lib/jvm/temurin-17-jdk"
    echo "    $(java -version 2>&1 | head -1) installed."
else
    echo "==> Java already installed: $(java -version 2>&1 | head -1)"
fi

# ---------------------------------------------------------------------------
# Python 3 + venv  (ensurepip is what Gradle's setupVenv task requires)
# ---------------------------------------------------------------------------
if ! python3 -c "import ensurepip" &>/dev/null 2>&1; then
    echo "==> Installing python3-venv ..."
    PYVER=$(python3 -c "import sys; print(f'{sys.version_info.major}.{sys.version_info.minor}')" 2>/dev/null || true)
    sudo apt-get update -q
    # Install both the generic and version-specific package; apt silently skips unknown ones
    sudo apt-get install -y --no-install-recommends \
        python3 python3-pip python3-venv \
        ${PYVER:+"python${PYVER}-venv"}
    sudo rm -rf /var/lib/apt/lists/*
    sudo ln -sf /usr/bin/python3 /usr/bin/python 2>/dev/null || true
    echo "    $(python3 --version) with venv installed."
else
    echo "==> Python already installed: $(python3 --version)"
fi

# ---------------------------------------------------------------------------
# DocFX 2.78.4
# ---------------------------------------------------------------------------
if ! command -v docfx &>/dev/null; then
    echo "==> Installing DocFX 2.78.4 ..."
    dotnet tool install -g docfx --version 2.78.4
    echo "    DocFX $(docfx --version) installed."
else
    echo "==> DocFX already installed: $(docfx --version)"
fi

echo ""
echo "Setup complete."
echo "  .NET   : $(dotnet --version)"
echo "  Java   : $(java -version 2>&1 | head -1)"
echo "  Python : $(python3 --version)"
echo "  DocFX  : $(docfx --version)"
