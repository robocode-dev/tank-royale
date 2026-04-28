#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
REPO_ROOT="$(cd "$SCRIPT_DIR/../../.." && pwd)"
TARGET_DIR="${1:-C:/Code/bots}"

# Detect platform for Gradle wrapper
if [[ "${OSTYPE:-}" == msys* ]] || [[ "${OSTYPE:-}" == cygwin* ]] || [[ -n "${WINDIR:-}" ]]; then
    GRADLEW="$REPO_ROOT/gradlew.bat"
else
    GRADLEW="$REPO_ROOT/gradlew"
fi

# Pre-flight checks
if [[ ! -d "$REPO_ROOT/sample-bots" ]]; then
    echo "❌ ERROR: sample-bots/ directory not found at $REPO_ROOT"
    exit 1
fi
if [[ ! -f "$GRADLEW" ]]; then
    echo "❌ ERROR: Gradle wrapper not found at $GRADLEW"
    exit 1
fi

echo "📋 Platform: $(uname -o 2>/dev/null || uname -s)"
echo "📋 Target directory: $TARGET_DIR"
echo ""

# Build all sample-bot zips
echo "🔨 Building sample bots..."
cd "$REPO_ROOT"
"$GRADLEW" sample-bots:clean sample-bots:zip
echo ""

# Deploy a single language: deploy_lang <src-dir-name> <target-folder-name>
deploy_lang() {
    local LANG_SRC="$1"
    local LANG_DST="$2"
    local TARGET_LANG_DIR="$TARGET_DIR/$LANG_DST"

    local ZIP_FILE
    ZIP_FILE=$(ls "$REPO_ROOT/sample-bots/$LANG_SRC/build/sample-bots-$LANG_SRC-"*.zip 2>/dev/null | head -1)

    if [[ -z "$ZIP_FILE" ]]; then
        echo "❌ ERROR: No zip file found at sample-bots/$LANG_SRC/build/sample-bots-$LANG_SRC-*.zip"
        exit 1
    fi

    echo "📦 Deploying $LANG_SRC → $TARGET_LANG_DIR"
    rm -rf "$TARGET_LANG_DIR"
    mkdir -p "$TARGET_LANG_DIR"
    cp "$ZIP_FILE" "$TARGET_LANG_DIR/"
    unzip -q "$ZIP_FILE" -d "$TARGET_LANG_DIR"
    echo "   ✅ $(basename "$ZIP_FILE") extracted"
}

deploy_lang java       java
deploy_lang csharp     "c#"
deploy_lang python     python
deploy_lang typescript typescript

echo ""
echo "✅ All sample bots deployed to $TARGET_DIR"
