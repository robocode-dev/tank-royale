#!/bin/bash
# Download and setup required tools for Structurizr C4 DSL generation
# This script ensures consistent tool versions across all environments

set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
TOOLS_DIR="${SCRIPT_DIR}"

# Tool versions
STRUCTURIZR_VERSION="2025.11.09"
PLANTUML_VERSION="1.2024.8"

# Colors for output
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
NC='\033[0m' # No Color

echo -e "${GREEN}=== Downloading Structurizr & PlantUML Tools ===${NC}"

# Check if Java is available
if ! command -v java &> /dev/null; then
    echo -e "${RED}Error: Java is not installed. Please install Java 11+ first.${NC}"
    echo "  Ubuntu/Debian: sudo apt install openjdk-17-jdk"
    echo "  macOS: brew install openjdk@17"
    exit 1
fi

echo -e "${GREEN}✓ Java: $(java -version 2>&1 | head -1)${NC}"

# Download PlantUML
PLANTUML_JAR="${TOOLS_DIR}/plantuml.jar"
if [ -f "$PLANTUML_JAR" ]; then
    echo -e "${YELLOW}PlantUML already exists, skipping download${NC}"
else
    echo "Downloading PlantUML v${PLANTUML_VERSION}..."
    curl -L -o "$PLANTUML_JAR" "https://github.com/plantuml/plantuml/releases/download/v${PLANTUML_VERSION}/plantuml-${PLANTUML_VERSION}.jar" || {
        echo -e "${RED}Failed to download PlantUML${NC}"
        exit 1
    }
    echo -e "${GREEN}✓ PlantUML downloaded${NC}"
fi

# Download Structurizr CLI
STRUCTURIZR_DIR="${TOOLS_DIR}/structurizr-cli"
STRUCTURIZR_ZIP="${TOOLS_DIR}/structurizr-cli.zip"

if [ -d "$STRUCTURIZR_DIR" ]; then
    echo -e "${YELLOW}Structurizr CLI already exists, skipping download${NC}"
else
    echo "Downloading Structurizr CLI v${STRUCTURIZR_VERSION}..."
    curl -L -o "$STRUCTURIZR_ZIP" "https://github.com/structurizr/cli/releases/download/v${STRUCTURIZR_VERSION}/structurizr-cli.zip" || {
        echo -e "${RED}Failed to download Structurizr CLI${NC}"
        exit 1
    }

    echo "Extracting Structurizr CLI..."
    unzip -q "$STRUCTURIZR_ZIP" -d "$STRUCTURIZR_DIR"
    rm "$STRUCTURIZR_ZIP"

    # Make shell scripts executable
    chmod +x "$STRUCTURIZR_DIR"/*.sh 2>/dev/null || true

    echo -e "${GREEN}✓ Structurizr CLI downloaded and extracted${NC}"
fi

# Create symlink for easier access to structurizr-dsl.jar
STRUCTURIZR_DSL_JAR=$(find "$STRUCTURIZR_DIR/lib" -name "structurizr-dsl-*.jar" | head -n 1)
if [ -n "$STRUCTURIZR_DSL_JAR" ]; then
    # Create symlink: .tools/structurizr-dsl.jar -> structurizr-cli/lib/structurizr-dsl-X.X.X.jar
    ln -sf "structurizr-cli/lib/$(basename "$STRUCTURIZR_DSL_JAR")" "${TOOLS_DIR}/structurizr-dsl.jar"
    echo -e "${GREEN}✓ Created symlink: structurizr-dsl.jar -> $(basename "$STRUCTURIZR_DSL_JAR")${NC}"
fi

echo ""
echo -e "${GREEN}=== Tools Setup Complete ===${NC}"
echo ""
echo "Available tools:"
echo "  - PlantUML: ${PLANTUML_JAR}"
echo "  - Structurizr CLI: ${STRUCTURIZR_DIR}"
echo ""
echo "Test the setup:"
echo "  java -jar ${PLANTUML_JAR} -version"
echo "  ${STRUCTURIZR_DIR}/structurizr.sh --version"
