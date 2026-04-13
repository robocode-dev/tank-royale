#!/bin/bash
# Helper script for GitHub Copilot Structurizr skill
# This script processes C4 DSL workspace blocks and generates SVG diagrams

set -e

PROJECT_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/../../../.." && pwd)"
ARCHITECTURE_DIR="$PROJECT_ROOT/docs-internal/architecture"
C4_VIEWS_DIR="$ARCHITECTURE_DIR/c4-views"
IMAGES_DIR="${IMAGES_DIR:-$C4_VIEWS_DIR/images}"
SKILL_DIR="$(dirname "${BASH_SOURCE[0]}")"
TOOLS_DIR="$PROJECT_ROOT/.tools"
STRUCTURIZR_CLI="$TOOLS_DIR/structurizr-cli/structurizr.sh"

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Ensure images directory exists
mkdir -p "$IMAGES_DIR"

# Function to check if command exists
command_exists() {
    command -v "$1" >/dev/null 2>&1
}

# Function to check if tools are already downloaded
tools_exist() {
    local plantuml_jar="$TOOLS_DIR/plantuml.jar"
    local structurizr_jar="$TOOLS_DIR/structurizr-dsl.jar"
    local structurizr_lib="$TOOLS_DIR/structurizr-cli/lib"

    # Check if all required files/folders exist and are non-empty
    if [ -f "$plantuml_jar" ] && [ -f "$structurizr_jar" ] && [ -d "$structurizr_lib" ] && [ "$(ls -A "$structurizr_lib" 2>/dev/null | wc -l)" -gt 0 ]; then
        return 0  # Tools exist
    else
        return 1  # Tools missing or incomplete
    fi
}

# Function to perform pre-checks
perform_prechecks() {
    echo -e "${BLUE}🔍 Performing pre-checks...${NC}"
    local issues_found=false

    echo "Detected OS: $(uname -s)"
    echo ""

    # Check Java (required for both tools)
    if command_exists java; then
        local java_version=$(java --version 2>/dev/null | head -1 || java -version 2>&1 | head -1)
        echo -e "${GREEN}✅ Java: $java_version${NC}"
    else
        echo -e "${RED}❌ Java: NOT FOUND${NC}"
        echo -e "${YELLOW}   Java is required for both Structurizr CLI and PlantUML${NC}"
        echo "   Install with: sudo apt install openjdk-17-jdk (Ubuntu/Debian)"
        echo "   Or: brew install openjdk@17 (macOS)"
        issues_found=true
    fi

    # Check if tools are already downloaded
    local download_script="$TOOLS_DIR/download-tools.sh"

    if tools_exist; then
        # Tools are complete, just verify they work
        local plantuml_jar="$TOOLS_DIR/plantuml.jar"

        echo -e "${GREEN}✅ PlantUML: Available${NC}"
        echo -e "${GREEN}✅ Structurizr CLI: Available${NC}"
    else
        # Tools are missing or incomplete, download them
        echo -e "${YELLOW}⚠️  Tools not found or incomplete${NC}"
        if [ -f "$download_script" ]; then
            echo -e "${BLUE}   Running $download_script...${NC}"
            if bash "$download_script"; then
                echo -e "${GREEN}✅ Tools downloaded successfully${NC}"
            else
                echo -e "${RED}❌ Failed to download tools${NC}"
                echo -e "${YELLOW}   Please run manually: $download_script${NC}"
                issues_found=true
            fi
        else
            echo -e "${RED}❌ Download script not found at: $download_script${NC}"
            issues_found=true
        fi
    fi

    echo ""

    if [ "$issues_found" = true ]; then
        echo -e "${RED}❌ Pre-checks failed. Please resolve the issues above.${NC}"
        exit 1
    else
        echo -e "${GREEN}✅ All pre-checks passed!${NC}"
        echo ""
    fi
}

# Function to extract diagram name from workspace content
extract_diagram_name() {
    local workspace_content="$1"
    local name=""

    # Look for workspace name
    name=$(echo "$workspace_content" | grep -oiE 'workspace\s+"[^"]+' | head -1 | sed 's/.*"\([^"]*\)".*/\1/' | tr '[:upper:]' '[:lower:]' | sed 's/[^a-z0-9]/-/g' | sed 's/-\+/-/g' | sed 's/^-\|-$//g')

    if [ -z "$name" ]; then
        # Generate name from content hash
        local hash=$(echo "$workspace_content" | md5sum | cut -c1-8)
        name="diagram-$hash"
    fi

    echo "$name"
}

# Function to process workspace
process_workspace() {
    local workspace_content="$1"

    local temp_dir="/tmp/structurizr-$$-$RANDOM"
    mkdir -p "$temp_dir"

    local diagram_name=$(extract_diagram_name "$workspace_content")
    local dsl_file="$temp_dir/$diagram_name.dsl"

    echo -e "${BLUE}[D] Processing workspace: $diagram_name${NC}"

    # Write DSL file
    echo "$workspace_content" > "$dsl_file"

    # Generate diagrams using Structurizr CLI
    echo -e "${BLUE}[>] Running Structurizr CLI export...${NC}"

    if [ -f "$STRUCTURIZR_CLI" ]; then
        bash "$STRUCTURIZR_CLI" export -w "$dsl_file" -f plantuml -o "$temp_dir" 2>&1 || true
        echo -e "${GREEN}[+] Generated PlantUML diagrams${NC}"

        # Convert PlantUML to SVG
        local svg_count=0
        local puml_count=0

        for puml_file in "$temp_dir"/structurizr-*.puml; do
            [ -f "$puml_file" ] || continue

            local base_name=$(basename "$puml_file" .puml)

            # Skip key/legend diagrams
            if [[ "$base_name" == *"-key" ]]; then
                continue
            fi

            # Determine output name
            local output_name="container"
            if [[ "$base_name" == *"Component"* ]]; then
                # Match patterns like:
                # structurizr-Booter-Components -> component-Booter
                # structurizr-TankRoyale-ServerComponent -> component-Server
                # structurizr-TankRoyale-BotAPIComponent -> component-BotAPI
                if [[ "$base_name" =~ structurizr-(TankRoyale-)?([A-Za-z]+)(Component|Components) ]]; then
                    local component_name="${BASH_REMATCH[2]}"
                    output_name="component-$component_name"
                else
                    # Fallback: remove structurizr- prefix and -Component(s) suffix
                    local component_name=$(echo "$base_name" | sed 's/structurizr-//' | sed 's/-\?Components\?$//')
                    output_name="component-$component_name"
                fi
            elif [[ "$base_name" == *"SystemContext"* ]]; then
                output_name="system-context"
            elif [[ "$base_name" == *"Deployment"* ]]; then
                output_name="deployment"
            elif [[ "$base_name" == *"Container"* ]]; then
                output_name="container"
            elif [[ "$base_name" == *"SystemLandscape"* ]]; then
                output_name="system-landscape"
            fi

            ((puml_count++))

            # Convert to SVG
            local plantuml_jar="$TOOLS_DIR/plantuml.jar"
            echo -e "${BLUE}   Converting $base_name.puml to SVG...${NC}"

            java -jar "$plantuml_jar" -tsvg "$puml_file" -o "$temp_dir" 2>&1 || true

            local svg_file="$temp_dir/$base_name.svg"
            if [ -f "$svg_file" ]; then
                local output_svg="$IMAGES_DIR/$output_name.svg"
                cp "$svg_file" "$output_svg"
                echo -e "${GREEN}[+] Created SVG: $output_name.svg${NC}"
                echo -e "${BLUE}    Reference: ![${output_name}](/docs-internal/architecture/c4-views/images/${output_name}.svg)${NC}"
                ((svg_count++))
            else
                echo -e "${RED}   [x] SVG file not created: $svg_file${NC}"
            fi
        done

        echo ""
        echo -e "${GREEN}[D] Summary:${NC}"
        echo -e "${GREEN}   [+] Processed $puml_count PlantUML diagram(s)${NC}"
        echo -e "${GREEN}   [+] Generated $svg_count SVG file(s)${NC}"

        if [ "$svg_count" -eq 0 ]; then
            echo -e "${RED}   [x] No SVG files were generated${NC}"
        fi
    else
        echo -e "${RED}[x] Failed: Structurizr CLI not found at $STRUCTURIZR_CLI${NC}"
        exit 1
    fi

    # Cleanup
    rm -rf "$temp_dir"
    echo ""
}

# Main execution
if [ -z "$1" ]; then
    echo -e "${BLUE}[*] Structurizr C4 DSL to SVG Generator${NC}"
    echo -e "${BLUE}Usage: $(basename "$0") '<workspace_dsl_content>'${NC}"
    echo ""
    echo "Requirements: Java 11+ (all tools bundled)"
    echo "Output: SVG files saved to /docs-internal/architecture/c4-views/images/"
    echo ""
    echo "Example:"
    echo '  bash generate.sh '"'"'workspace "Tank Royale" { model { user = person "Developer" } }'"'"
    exit 0
fi

# Perform checks and download tools if needed
perform_prechecks

# Process the workspace
process_workspace "$1"

echo -e "${GREEN}✅ Generation complete!${NC}"
