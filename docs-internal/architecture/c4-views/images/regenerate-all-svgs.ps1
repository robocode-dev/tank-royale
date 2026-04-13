# Regenerate all SVG diagrams from Structurizr DSL files
# Uses the Structurizr skill located at .github/skills/structurizr/generate.ps1

param()

$ErrorActionPreference = "Stop"

# Paths
# Script is in: /docs-internal/architecture/c4-views/images/
$projectRoot = (Get-Item $PSScriptRoot).Parent.Parent.Parent.Parent.FullName
$dslDir = Join-Path $projectRoot "docs-internal\architecture\c4-views\structurizr-dsl"
$generateScript = Join-Path $projectRoot ".github\skills\structurizr\generate.ps1"

# DSL files to process
$dslFiles = @(
    'system-context.dsl',
    'container.dsl',
    'booter-components.dsl',
    'bot-api-components.dsl',
    'gui-components.dsl',
    'recorder-components.dsl',
    'server-components.dsl'
)

Write-Host "╔════════════════════════════════════════════════════════════╗" -ForegroundColor Cyan
Write-Host "║  Structurizr SVG Regeneration - Tank Royale C4 Diagrams   ║" -ForegroundColor Cyan
Write-Host "╚════════════════════════════════════════════════════════════╝" -ForegroundColor Cyan
Write-Host ""
Write-Host "Project root:    $projectRoot" -ForegroundColor Gray
Write-Host "DSL directory:   $dslDir" -ForegroundColor Gray
Write-Host "Generate script: $generateScript" -ForegroundColor Gray
Write-Host ""

# Check if pwsh is available (PowerShell Core)
$usePwsh = $false
try {
    $null = Get-Command pwsh -ErrorAction Stop
    $usePwsh = $true
    Write-Host "✓ Using PowerShell Core (pwsh)" -ForegroundColor Green
} catch {
    Write-Host "⚠ PowerShell Core not found, using Windows PowerShell" -ForegroundColor Yellow
}
Write-Host ""

$successCount = 0
$failCount = 0

foreach ($dslFile in $dslFiles) {
    $dslPath = Join-Path $dslDir $dslFile

    if (-not (Test-Path $dslPath)) {
        Write-Host "✗ File not found: $dslFile" -ForegroundColor Red
        $failCount++
        continue
    }

    Write-Host "═══════════════════════════════════════════════════════════" -ForegroundColor DarkGray
    Write-Host "Processing: $dslFile" -ForegroundColor Cyan
    Write-Host ""

    try {
        # Read DSL content
        $dslContent = Get-Content $dslPath -Raw

        # Call generate script with DSL content
        if ($usePwsh) {
            & pwsh -NoProfile -ExecutionPolicy Bypass -File $generateScript $dslContent
        } else {
            & powershell -NoProfile -ExecutionPolicy Bypass -File $generateScript $dslContent
        }

        if ($LASTEXITCODE -eq 0 -or $null -eq $LASTEXITCODE) {
            Write-Host ""
            Write-Host "✓ Successfully processed: $dslFile" -ForegroundColor Green
            $successCount++
        } else {
            Write-Host ""
            Write-Host "✗ Failed to process: $dslFile (exit code: $LASTEXITCODE)" -ForegroundColor Red
            $failCount++
        }
    } catch {
        Write-Host ""
        Write-Host "✗ Error processing $dslFile : $_" -ForegroundColor Red
        $failCount++
    }

    Write-Host ""
}

Write-Host "═══════════════════════════════════════════════════════════" -ForegroundColor DarkGray
Write-Host ""
Write-Host "╔════════════════════════════════════════════════════════════╗" -ForegroundColor Cyan
Write-Host "║  Summary                                                   ║" -ForegroundColor Cyan
Write-Host "╚════════════════════════════════════════════════════════════╝" -ForegroundColor Cyan
Write-Host ""
Write-Host "  Total DSL files:      $($dslFiles.Count)" -ForegroundColor Gray
Write-Host "  Successfully processed: $successCount" -ForegroundColor Green
Write-Host "  Failed:                $failCount" -ForegroundColor $(if ($failCount -gt 0) { "Red" } else { "Gray" })
Write-Host ""

if ($failCount -eq 0) {
    Write-Host "✓ All SVG files have been regenerated successfully!" -ForegroundColor Green
    exit 0
} else {
    Write-Host "⚠ Some SVG files failed to regenerate. Please check the output above." -ForegroundColor Yellow
    exit 1
}


