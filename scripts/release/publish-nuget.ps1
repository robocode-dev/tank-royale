# Publish .NET NuGet package script
# This script reads the version and API key from gradle.properties and prepares the NuGet publish command
# NOTE: This script does NOT actually push to NuGet - it only displays the command that would be executed

param(
    [switch]$Execute = $false
)

# Get the root directory (two levels up from scripts/release)
$rootDir = Split-Path -Parent (Split-Path -Parent $PSScriptRoot)
$projectGradleProps = Join-Path $rootDir "gradle.properties"
$userGradleProps = Join-Path $env:USERPROFILE ".gradle\gradle.properties"

# Check if project gradle.properties exists
if (-not (Test-Path $projectGradleProps)) {
    Write-Error "gradle.properties not found at: $projectGradleProps"
    exit 1
}

# Read version from project gradle.properties
$version = $null
$apiKey = $null

Get-Content $projectGradleProps | ForEach-Object {
    $line = $_.Trim()
    if ($line -match "^version=(.+)$") {
        $version = $matches[1]
    }
    if ($line -match "^nuget-api-key=(.+)$") {
        $apiKey = $matches[1]
    }
}

# Override with user-level gradle.properties if it exists
if (Test-Path $userGradleProps) {
    Write-Host "Checking user-level gradle.properties at: $userGradleProps" -ForegroundColor Gray
    Get-Content $userGradleProps | ForEach-Object {
        $line = $_.Trim()
        if ($line -match "^nuget-api-key=(.+)$") {
            $apiKey = $matches[1]
            Write-Host "Using nuget-api-key from user-level gradle.properties" -ForegroundColor Gray
        }
    }
}
else {
    Write-Host "No user-level gradle.properties found at: $userGradleProps" -ForegroundColor Gray
}

# Validate that we found both values
if (-not $version) {
    Write-Error "Could not find 'version' in gradle.properties"
    exit 1
}

if (-not $apiKey) {
    Write-Error "Could not find 'nuget-api-key' in gradle.properties"
    exit 1
}

if ($apiKey -eq "dummy") {
    Write-Warning "API key is set to 'dummy'. Please update nuget-api-key in gradle.properties or ~/.gradle/gradle.properties"
}

# Construct the paths
$releaseDir = Join-Path $rootDir "bot-api\dotnet\api\bin\Release"
$packageFile = "robocode.tankroyale.botapi.$version.nupkg"
$packagePath = Join-Path $releaseDir $packageFile

# Check if the package exists
if (-not (Test-Path $packagePath)) {
    Write-Error "Package not found: $packagePath"
    Write-Host "Please build the .NET package first."
    exit 1
}

# Display the information
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "NuGet Package Publish Command" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""
Write-Host "Version:     " -NoNewline -ForegroundColor Yellow
Write-Host $version
Write-Host "Package:     " -NoNewline -ForegroundColor Yellow
Write-Host $packageFile
Write-Host "Path:        " -NoNewline -ForegroundColor Yellow
Write-Host $packagePath
Write-Host "API Key:     " -NoNewline -ForegroundColor Yellow
Write-Host "$(if ($apiKey -eq 'dummy') { 'dummy (NOT REAL!)' } else { '***' + $apiKey.Substring([Math]::Max(0, $apiKey.Length - 4)) })"
Write-Host ""
Write-Host "Command that would be executed:" -ForegroundColor Green
Write-Host ""
Write-Host "  cd `"$releaseDir`"" -ForegroundColor White
Write-Host "  dotnet nuget push $packageFile --api-key `"$apiKey`" --source https://api.nuget.org/v3/index.json" -ForegroundColor White
Write-Host ""

if ($Execute) {
    Write-Host "========================================" -ForegroundColor Red
    Write-Host "EXECUTING NUGET PUSH (ARE YOU SURE?)" -ForegroundColor Red
    Write-Host "========================================" -ForegroundColor Red
    Write-Host ""
    $confirmation = Read-Host "Type 'YES' to confirm"

    if ($confirmation -eq "YES") {
        Push-Location $releaseDir
        try {
            dotnet nuget push $packageFile --api-key "$apiKey" --source https://api.nuget.org/v3/index.json
            Write-Host ""
            Write-Host "Package published successfully!" -ForegroundColor Green
        }
        catch {
            Write-Error "Failed to publish package: $_"
            exit 1
        }
        finally {
            Pop-Location
        }
    }
    else {
        Write-Host "Publish cancelled." -ForegroundColor Yellow
    }
}
else {
    Write-Host "========================================" -ForegroundColor Yellow
    Write-Host "DRY RUN MODE - Nothing was published" -ForegroundColor Yellow
    Write-Host "========================================" -ForegroundColor Yellow
    Write-Host ""
    Write-Host "To actually execute the push, run with -Execute flag:" -ForegroundColor Cyan
    Write-Host "  .\scripts\release\publish-nuget.ps1 -Execute" -ForegroundColor White
}


