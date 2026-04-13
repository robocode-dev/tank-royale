#!/usr/bin/env bash
# Idempotent setup script for Tank Royale development.
# Works both inside the dev container and on a plain Ubuntu/Debian host.
# Re-running is safe: each section is skipped when the tool is already present.
set -euo pipefail

ARCH="$(dpkg --print-architecture)"

# ---------------------------------------------------------------------------
# .NET SDK 8.0 — installed via apt (system PATH, no manual env vars needed)
# ---------------------------------------------------------------------------
if ! dotnet --version &>/dev/null 2>&1; then
    echo "==> Installing .NET SDK 8.0 ..."
    
    # Fix any broken dpkg state before proceeding
    sudo dpkg --configure -a 2>/dev/null || true
    
    # Remove any broken/corrupted dotnet installation
    if [ -d /usr/share/dotnet ] || apt list --installed 2>/dev/null | grep -q dotnet; then
        echo "    Removing existing dotnet installation..."
        sudo apt-get remove -y dotnet* aspnetcore* 2>/dev/null || true
        sudo rm -rf /usr/share/dotnet 2>/dev/null || true
        sudo rm -rf /usr/bin/dotnet 2>/dev/null || true
    fi
    
    sudo apt-get update -q 2>/dev/null || true

    # Ubuntu 24.04+ ships dotnet-sdk-8.0 natively; other distros need Microsoft's feed
    if ! apt-cache show dotnet-sdk-8.0 &>/dev/null 2>&1; then
        . /etc/os-release
        timeout 10 wget -q "https://packages.microsoft.com/config/${ID}/${VERSION_ID}/packages-microsoft-prod.deb" \
            -O /tmp/packages-microsoft-prod.deb 2>/dev/null || true
        if [ -f /tmp/packages-microsoft-prod.deb ]; then
            sudo apt-get install -y /tmp/packages-microsoft-prod.deb 2>/dev/null || true
            rm /tmp/packages-microsoft-prod.deb 2>/dev/null || true
            sudo apt-get update -q 2>/dev/null || true
        fi
    fi

    sudo apt-get install -y --no-install-recommends dotnet-sdk-8.0 2>/dev/null || true
    sudo rm -rf /var/lib/apt/lists/* 2>/dev/null || true

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
    sudo apt-get update -q 2>/dev/null || true
    sudo apt-get install -y --no-install-recommends gnupg 2>/dev/null || true
    timeout 10 wget -qO - https://packages.adoptium.net/artifactory/api/gpg/key/public 2>/dev/null \
        | gpg --dearmor 2>/dev/null \
        | sudo tee /usr/share/keyrings/adoptium.gpg >/dev/null 2>&1 || true
    echo "deb [signed-by=/usr/share/keyrings/adoptium.gpg] \
https://packages.adoptium.net/artifactory/deb bookworm main" \
        | sudo tee /etc/apt/sources.list.d/adoptium.list >/dev/null 2>&1 || true
    sudo apt-get update -q 2>/dev/null || true
    sudo apt-get install -y temurin-17-jdk temurin-11-jdk 2>/dev/null || true
    sudo rm -rf /var/lib/apt/lists/* 2>/dev/null || true

    target="/usr/lib/jvm/temurin-17-jdk-${ARCH}"
    if [ ! -d "$target" ]; then
        echo "ERROR: Expected JDK not found: $target" >&2
        exit 1
    fi
    sudo ln -sfn "$target" /usr/lib/jvm/temurin-17-jdk 2>/dev/null || true
    sudo update-alternatives --set java  "${target}/bin/java" 2>/dev/null || true
    sudo update-alternatives --set javac "${target}/bin/javac" 2>/dev/null || true

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
    PYVER=$(timeout 5 python3 -c "import sys; print(f'{sys.version_info.major}.{sys.version_info.minor}')" 2>/dev/null || true)
    sudo apt-get update -q 2>/dev/null || true
    # Install both the generic and version-specific package; apt silently skips unknown ones
    sudo apt-get install -y --no-install-recommends \
        python3 python3-pip python3-venv \
        ${PYVER:+"python${PYVER}-venv"} 2>/dev/null || true
    sudo rm -rf /var/lib/apt/lists/* 2>/dev/null || true
    sudo ln -sf /usr/bin/python3 /usr/bin/python 2>/dev/null || true
    echo "    $(python3 --version) with venv installed."
else
    echo "==> Python already installed: $(python3 --version)"
fi

# ---------------------------------------------------------------------------
# DocFX 2.78.4
# ---------------------------------------------------------------------------
if ! timeout 5 docfx --version &>/dev/null 2>&1; then
    echo "==> Installing DocFX 2.78.4 ..."
    timeout 120 dotnet tool install -g docfx --version 2.78.4 2>/dev/null \
        || timeout 120 dotnet tool update -g docfx --version 2.78.4 2>/dev/null || true
    if timeout 5 docfx --version &>/dev/null 2>&1; then
        echo "    DocFX $(docfx --version) installed."
    fi
else
    echo "==> DocFX already installed: $(docfx --version)"
fi

echo ""
echo "Setup complete."
echo "  .NET   : $(dotnet --version)"
echo "  Java   : $(java -version 2>&1 | head -1)"
echo "  Python : $(python3 --version)"
echo "  DocFX  : $(docfx --version)"
