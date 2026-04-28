#!/usr/bin/env pwsh
param(
    [string]$TargetDir = "C:\Code\bots"
)

$ErrorActionPreference = "Stop"

$SCRIPT_DIR = Split-Path -Parent $MyInvocation.MyCommand.Path
$REPO_ROOT = Resolve-Path (Join-Path $SCRIPT_DIR "..\..\..") -ErrorAction Stop

# Detect Gradle wrapper
$GRADLEW = Join-Path $REPO_ROOT "gradlew.bat"
if (-not (Test-Path $GRADLEW)) {
    $GRADLEW = Join-Path $REPO_ROOT "gradlew"
}

# Pre-flight checks
$SampleBotsDir = Join-Path $REPO_ROOT "sample-bots"
if (-not (Test-Path $SampleBotsDir)) {
    Write-Host "❌ ERROR: sample-bots/ directory not found at $REPO_ROOT"
    exit 1
}
if (-not (Test-Path $GRADLEW)) {
    Write-Host "❌ ERROR: Gradle wrapper not found at $GRADLEW"
    exit 1
}

Write-Host "📋 Platform: Windows (PowerShell)"
Write-Host "📋 Target directory: $TargetDir"
Write-Host ""

# Build all sample-bot zips
Write-Host "🔨 Building sample bots..."
Push-Location $REPO_ROOT
try {
    & $GRADLEW sample-bots:clean sample-bots:zip
    if ($LASTEXITCODE -ne 0) { exit $LASTEXITCODE }
}
finally {
    Pop-Location
}
Write-Host ""

function Deploy-Lang {
    param([string]$LangSrc, [string]$LangDst)

    $TargetLangDir = Join-Path $TargetDir $LangDst
    $BuildDir = Join-Path $REPO_ROOT "sample-bots\$LangSrc\build"
    $ZipFile = Get-ChildItem -Path $BuildDir -Filter "sample-bots-$LangSrc-*.zip" -ErrorAction SilentlyContinue | Select-Object -First 1

    if (-not $ZipFile) {
        Write-Host "❌ ERROR: No zip file found at sample-bots\$LangSrc\build\sample-bots-$LangSrc-*.zip"
        exit 1
    }

    Write-Host "📦 Deploying $LangSrc → $TargetLangDir"
    if (Test-Path $TargetLangDir) { Remove-Item -Recurse -Force $TargetLangDir }
    New-Item -ItemType Directory -Path $TargetLangDir -Force | Out-Null
    Copy-Item $ZipFile.FullName -Destination $TargetLangDir
    Expand-Archive -Path $ZipFile.FullName -DestinationPath $TargetLangDir -Force
    Write-Host "   ✅ $($ZipFile.Name) extracted"
}

Deploy-Lang java       java
Deploy-Lang csharp     "c#"
Deploy-Lang python     python
Deploy-Lang typescript typescript

Write-Host ""
Write-Host "✅ All sample bots deployed to $TargetDir"
