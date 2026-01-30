# Upload Sample Bot Archives to GitHub Release
# This script uploads the sample bot archives for C#, Java, and Python to an existing GitHub release

param(
    [Parameter(Mandatory=$true)]
    [string]$Token,

    [Parameter(Mandatory=$false)]
    [string]$Version = "0.35.2"
)

$ErrorActionPreference = "Stop"

$owner = "robocode-dev"
$repo = "tank-royale"
$tag = "v$Version"

# GitHub API endpoints
$apiBase = "https://api.github.com"
$uploadBase = "https://uploads.github.com"

# Headers for GitHub API
$headers = @{
    "Accept" = "application/vnd.github+json"
    "Authorization" = "Bearer $Token"
}

Write-Host "Fetching release for tag: $tag" -ForegroundColor Cyan

# Get the release by tag
$releaseUrl = "$apiBase/repos/$owner/$repo/releases/tags/$tag"
try {
    $release = Invoke-RestMethod -Uri $releaseUrl -Headers $headers -Method Get
    $releaseId = $release.id
    Write-Host "Found release: $($release.name) (ID: $releaseId)" -ForegroundColor Green
} catch {
    Write-Error "Failed to fetch release for tag $tag. Error: $_"
    exit 1
}

# List of sample bot archives to upload
$archives = @(
    @{
        Path = "sample-bots/csharp/build/sample-bots-csharp-$Version.zip"
        Name = "sample-bots-csharp-$Version.zip"
        Label = "Sample bots for C# (zip)"
        ContentType = "application/zip"
    },
    @{
        Path = "sample-bots/java/build/sample-bots-java-$Version.zip"
        Name = "sample-bots-java-$Version.zip"
        Label = "Sample bots for Java (zip)"
        ContentType = "application/zip"
    },
    @{
        Path = "sample-bots/python/build/sample-bots-python-$Version.zip"
        Name = "sample-bots-python-$Version.zip"
        Label = "Sample bots for Python (zip)"
        ContentType = "application/zip"
    }
)

# Process each archive
foreach ($archive in $archives) {
    $filePath = Join-Path $PSScriptRoot $archive.Path

    if (-not (Test-Path $filePath)) {
        Write-Warning "File not found: $filePath. Skipping..."
        continue
    }

    Write-Host "`nProcessing: $($archive.Name)" -ForegroundColor Cyan

    # Check if asset already exists
    $existingAsset = $release.assets | Where-Object { $_.name -eq $archive.Name }

    if ($existingAsset) {
        Write-Host "  Deleting existing asset (ID: $($existingAsset.id))..." -ForegroundColor Yellow
        $deleteUrl = "$apiBase/repos/$owner/$repo/releases/assets/$($existingAsset.id)"
        try {
            Invoke-RestMethod -Uri $deleteUrl -Headers $headers -Method Delete | Out-Null
            Write-Host "  Deleted successfully" -ForegroundColor Green
        } catch {
            Write-Warning "  Failed to delete existing asset: $_"
        }
    }

    # Upload the new asset
    Write-Host "  Uploading new asset..." -ForegroundColor Cyan
    $uploadUrl = "$uploadBase/repos/$owner/$repo/releases/$releaseId/assets?name=$([System.Uri]::EscapeDataString($archive.Name))&label=$([System.Uri]::EscapeDataString($archive.Label))"

    $uploadHeaders = $headers.Clone()
    $uploadHeaders["Content-Type"] = $archive.ContentType

    try {
        $fileBytes = [System.IO.File]::ReadAllBytes($filePath)
        $response = Invoke-RestMethod -Uri $uploadUrl -Headers $uploadHeaders -Method Post -Body $fileBytes
        Write-Host "  Uploaded successfully: $($response.browser_download_url)" -ForegroundColor Green
    } catch {
        Write-Error "  Failed to upload asset: $_"
        Write-Host "  Status Code: $($_.Exception.Response.StatusCode.value__)" -ForegroundColor Red
        Write-Host "  Response: $($_.Exception.Response)" -ForegroundColor Red
    }
}

Write-Host "`nAll sample bot archives processed successfully!" -ForegroundColor Green
Write-Host "Release URL: $($release.html_url)" -ForegroundColor Cyan
