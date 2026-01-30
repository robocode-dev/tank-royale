# Script to verify TR-API-CMD-002 Fire Command Tests
# Task 4.4: Verify Test Coverage

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "TR-API-CMD-002 Test Verification Script" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

$ErrorActionPreference = "Continue"
$results = @{
    Java = @{ Pass = 0; Fail = 0; Time = 0 }
    DotNet = @{ Pass = 0; Fail = 0; Time = 0 }
    Python = @{ Pass = 0; Fail = 0; Time = 0 }
}

$iterations = 10
$rootDir = Split-Path (Split-Path $PSScriptRoot -Parent) -Parent

# Function to run Java tests
function Run-JavaTests {
    param([int]$iteration)

    Write-Host "[$iteration/$iterations] Running Java CommandsFireTest..." -ForegroundColor Yellow
    $start = Get-Date

    Push-Location "$rootDir\bot-api\java"
    $output = & "$rootDir\gradlew" test --tests "dev.robocode.tankroyale.botapi.CommandsFireTest" 2>&1 | Out-String
    $exitCode = $LASTEXITCODE
    Pop-Location

    $end = Get-Date
    $duration = ($end - $start).TotalSeconds

    if ($exitCode -eq 0 -and $output -match "BUILD SUCCESSFUL") {
        $results.Java.Pass++
        Write-Host "  [PASS] PASSED (${duration}s)" -ForegroundColor Green
    } else {
        $results.Java.Fail++
        Write-Host "  [FAIL] FAILED (${duration}s)" -ForegroundColor Red
        if ($iteration -eq 1) {
            Write-Host "  Error output:" -ForegroundColor Red
            Write-Host $output
        }
    }

    $results.Java.Time += $duration
}

# Function to run .NET tests
function Run-DotNetTests {
    param([int]$iteration)

    Write-Host "[$iteration/$iterations] Running .NET CommandsFireTest..." -ForegroundColor Yellow
    $start = Get-Date

    Push-Location "$rootDir\bot-api\dotnet\test"
    $output = & dotnet test --filter "FullyQualifiedName~CommandsFireTest" 2>&1 | Out-String
    $exitCode = $LASTEXITCODE
    Pop-Location

    $end = Get-Date
    $duration = ($end - $start).TotalSeconds

    if ($exitCode -eq 0) {
        $results.DotNet.Pass++
        Write-Host "  [PASS] PASSED (${duration}s)" -ForegroundColor Green
    } else {
        $results.DotNet.Fail++
        Write-Host "  [FAIL] FAILED (${duration}s)" -ForegroundColor Red
        if ($iteration -eq 1) {
            Write-Host "  Error output:" -ForegroundColor Red
            Write-Host $output
        }
    }

    $results.DotNet.Time += $duration
}

# Function to run Python tests
function Run-PythonTests {
    param([int]$iteration)

    Write-Host "[$iteration/$iterations] Running Python test_commands_fire..." -ForegroundColor Yellow
    $start = Get-Date

    Push-Location "$rootDir\bot-api\python"

    # Ensure venv is set up
    if (-not (Test-Path ".venv")) {
        Write-Host "  Setting up Python virtual environment..." -ForegroundColor Yellow
        & "$rootDir\gradlew" setupVenv 2>&1 | Out-Null
    }

    $pythonPath = ".venv\Scripts\python.exe"
    if (-not (Test-Path $pythonPath)) {
        Write-Host "  [FAIL] Python venv not found" -ForegroundColor Red
        $results.Python.Fail++
        Pop-Location
        return
    }

    $output = & $pythonPath -m pytest tests/bot_api/test_commands_fire.py -v 2>&1 | Out-String
    $exitCode = $LASTEXITCODE
    Pop-Location

    $end = Get-Date
    $duration = ($end - $start).TotalSeconds

    if ($exitCode -eq 0) {
        $results.Python.Pass++
        Write-Host "  [PASS] PASSED (${duration}s)" -ForegroundColor Green
    } else {
        $results.Python.Fail++
        Write-Host "  [FAIL] FAILED (${duration}s)" -ForegroundColor Red
        if ($iteration -eq 1) {
            Write-Host "  Error output:" -ForegroundColor Red
            Write-Host $output
        }
    }

    $results.Python.Time += $duration
}

# Run tests multiple times for stability check
Write-Host "Running $iterations iterations to check stability..." -ForegroundColor Cyan
Write-Host ""

for ($i = 1; $i -le $iterations; $i++) {
    Write-Host "=== Iteration $i ===" -ForegroundColor Cyan

    Run-JavaTests -iteration $i
    Run-DotNetTests -iteration $i
    Run-PythonTests -iteration $i

    Write-Host ""
}

# Print summary
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "SUMMARY" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

foreach ($lang in @("Java", "DotNet", "Python")) {
    $r = $results[$lang]
    $avgTime = if ($r.Pass + $r.Fail -gt 0) { [math]::Round($r.Time / ($r.Pass + $r.Fail), 2) } else { 0 }
    $passRate = if ($r.Pass + $r.Fail -gt 0) { [math]::Round(($r.Pass / ($r.Pass + $r.Fail)) * 100, 1) } else { 0 }

    Write-Host "${lang}:" -ForegroundColor White
    Write-Host "  Passed:    $($r.Pass)/$iterations ($passRate%)" -ForegroundColor $(if ($r.Pass -eq $iterations) { "Green" } else { "Yellow" })
    Write-Host "  Failed:    $($r.Fail)/$iterations" -ForegroundColor $(if ($r.Fail -eq 0) { "Green" } else { "Red" })
    Write-Host "  Avg Time:  ${avgTime}s" -ForegroundColor White
    Write-Host ""
}

# Check if all tests passed all iterations
$allPassed = ($results.Java.Fail -eq 0) -and ($results.DotNet.Fail -eq 0) -and ($results.Python.Fail -eq 0)

if ($allPassed) {
    Write-Host "[PASS] All tests passed all $iterations iterations!" -ForegroundColor Green
    Write-Host "[PASS] Tests are STABLE" -ForegroundColor Green
    Write-Host ""
    Write-Host "Next step: Update TEST-MATRIX.md to mark CMD-002 as complete" -ForegroundColor Yellow
    exit 0
} else {
    Write-Host "[FAIL] Some tests failed. Review errors above." -ForegroundColor Red
    exit 1
}
