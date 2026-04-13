#!/usr/bin/env pwsh
# Download and setup required tools for Structurizr C4 DSL generation
# PowerShell version for Windows
param(
    [switch]$Force = $false
)
$ErrorActionPreference = "Stop"
$SCRIPT_DIR = Split-Path -Parent $MyInvocation.MyCommand.Path
$TOOLS_DIR = $SCRIPT_DIR
# Tool versions
$STRUCTURIZR_VERSION = "2025.11.09"
$PLANTUML_VERSION = "1.2024.8"
# Colors
$GREEN = "`e[0;32m"
$YELLOW = "`e[1;33m"
$RED = "`e[0;31m"
$NC = "`e[0m"
Write-Host "${GREEN}=== Downloading Structurizr & PlantUML Tools ===${NC}"
# Check if Java is available
try {
    $java_output = & java --version 2>&1
    Write-Host "${GREEN}✓ Java available: $($java_output[0])${NC}"
} catch {
    Write-Host "${RED}Error: Java is not installed or not in PATH.${NC}"
    Write-Host "Please install Java 11+ first:"
    Write-Host "  - Windows: https://adoptium.net/"
    Write-Host "  - macOS: brew install openjdk@17"
    Write-Host "  - Linux: sudo apt install openjdk-17-jdk"
    exit 1
}
# Download PlantUML
$PLANTUML_JAR = Join-Path $TOOLS_DIR "plantuml.jar"
if ((Test-Path $PLANTUML_JAR) -and -not $Force) {
    Write-Host "${YELLOW}PlantUML already exists, skipping download${NC}"
} else {
    Write-Host "Downloading PlantUML v${PLANTUML_VERSION}..."
    $url = "https://github.com/plantuml/plantuml/releases/download/v${PLANTUML_VERSION}/plantuml-${PLANTUML_VERSION}.jar"
    try {
        [Net.ServicePointManager]::SecurityProtocol = [Net.SecurityProtocolType]::Tls12
        Invoke-WebRequest -Uri $url -OutFile $PLANTUML_JAR -UseBasicParsing
        Write-Host "${GREEN}✓ PlantUML downloaded${NC}"
    } catch {
        Write-Host "${RED}Failed to download PlantUML${NC}"
        exit 1
    }
}
# Download Structurizr CLI
$STRUCTURIZR_DIR = Join-Path $TOOLS_DIR "structurizr-cli"
$STRUCTURIZR_ZIP = Join-Path $TOOLS_DIR "structurizr-cli.zip"
if ((Test-Path $STRUCTURIZR_DIR) -and -not $Force) {
    Write-Host "${YELLOW}Structurizr CLI already exists, skipping download${NC}"
} else {
    Write-Host "Downloading Structurizr CLI v${STRUCTURIZR_VERSION}..."
    $url = "https://github.com/structurizr/cli/releases/download/v${STRUCTURIZR_VERSION}/structurizr-cli.zip"
    try {
        [Net.ServicePointManager]::SecurityProtocol = [Net.SecurityProtocolType]::Tls12
        Invoke-WebRequest -Uri $url -OutFile $STRUCTURIZR_ZIP -UseBasicParsing
        Write-Host "Extracting Structurizr CLI..."
        if (Test-Path $STRUCTURIZR_DIR) {
            Remove-Item -Recurse -Force $STRUCTURIZR_DIR
        }
        $TEMP_EXTRACT = Join-Path $TOOLS_DIR "structurizr-temp"
        Expand-Archive -Path $STRUCTURIZR_ZIP -DestinationPath $TEMP_EXTRACT -Force
        New-Item -ItemType Directory -Path $STRUCTURIZR_DIR -Force | Out-Null
        Get-ChildItem $TEMP_EXTRACT | Move-Item -Destination $STRUCTURIZR_DIR -Force
        Remove-Item -Recurse -Force $TEMP_EXTRACT
        Remove-Item -Force $STRUCTURIZR_ZIP
        Write-Host "${GREEN}✓ Structurizr CLI downloaded and extracted${NC}"
    } catch {
        Write-Host "${RED}Failed to download Structurizr CLI${NC}"
        Write-Host "Error: $_"
        exit 1
    }
}
# Create reference to structurizr-dsl.jar
$structurizr_lib = Join-Path $STRUCTURIZR_DIR "lib"
if (Test-Path $structurizr_lib) {
    $dsl_jar = Get-ChildItem -Path $structurizr_lib -Filter "structurizr-dsl-*.jar" | Select-Object -First 1
    if ($dsl_jar) {
        $link_path = Join-Path $TOOLS_DIR "structurizr-dsl.jar"
        if (Test-Path $link_path) {
            Remove-Item $link_path -Force
        }
        Copy-Item $dsl_jar.FullName -Destination $link_path
        Write-Host "${GREEN}✓ Created reference: structurizr-dsl.jar${NC}"
    }
}
Write-Host ""
Write-Host "${GREEN}=== Tools Setup Complete ===${NC}"
