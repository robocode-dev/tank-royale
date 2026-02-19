# Wrapper script to update sample bot archives in GitHub release
# Reads the GitHub token from user's gradle.properties and calls the upload script

$ErrorActionPreference = "Stop"

Write-Host "Updating sample bot archives for GitHub release 0.36.0" -ForegroundColor Cyan
Write-Host "=" * 60 -ForegroundColor Cyan

# Read token from gradle.properties
$gradlePropsPath = "$env:USERPROFILE\.gradle\gradle.properties"

if (-not (Test-Path $gradlePropsPath)) {
    Write-Error "User gradle.properties file not found at: $gradlePropsPath"
    Write-Host "Please create the file and add: tankRoyaleGitHubToken=your_token_here" -ForegroundColor Yellow
    exit 1
}

Write-Host "Reading GitHub token from gradle.properties..." -ForegroundColor Cyan

$content = Get-Content $gradlePropsPath -Raw
if ($content -match "tankRoyaleGitHubToken\s*=\s*(.+)") {
    $token = $matches[1].Trim()
    Write-Host "Token found!" -ForegroundColor Green
} else {
    Write-Error "tankRoyaleGitHubToken not found in $gradlePropsPath"
    Write-Host "Please add: tankRoyaleGitHubToken=your_token_here" -ForegroundColor Yellow
    exit 1
}

# Build the sample bots first to ensure we have fresh archives
Write-Host "`nBuilding sample bot archives..." -ForegroundColor Cyan
$gradlewPath = Join-Path $PSScriptRoot "..\..\gradlew.bat"
& $gradlewPath sample-bots:zip

if ($LASTEXITCODE -ne 0) {
    Write-Error "Failed to build sample bot archives"
    exit $LASTEXITCODE
}

Write-Host "`nSample bot archives built successfully!" -ForegroundColor Green

# Call the upload script
$uploadScript = Join-Path $PSScriptRoot "upload-sample-bots.ps1"
& $uploadScript -Token $token -Version "0.36.0"

if ($LASTEXITCODE -ne 0) {
    Write-Error "Failed to upload sample bot archives"
    exit $LASTEXITCODE
}

Write-Host "`n" + ("=" * 60) -ForegroundColor Cyan
Write-Host "Sample bot archives updated successfully!" -ForegroundColor Green
Write-Host "Please verify the release at: https://github.com/robocode-dev/tank-royale/releases/tag/v0.36.0" -ForegroundColor Cyan
