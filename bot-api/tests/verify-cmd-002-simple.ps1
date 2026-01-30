# Simple script to verify TR-API-CMD-002 Fire Command Tests once

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "TR-API-CMD-002 Test Verification" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

$rootDir = "C:\Code\tank-royale"

# Test Java
Write-Host "1. Testing Java CommandsFireTest..." -ForegroundColor Yellow
cd "$rootDir\bot-api\java"
& "$rootDir\gradlew.bat" test --tests "dev.robocode.tankroyale.botapi.CommandsFireTest" --console=plain
$javaResult = $LASTEXITCODE
if ($javaResult -eq 0) {
    Write-Host "[PASS] Java tests passed" -ForegroundColor Green
} else {
    Write-Host "[FAIL] Java tests failed" -ForegroundColor Red
}
Write-Host ""

# Test .NET
Write-Host "2. Testing .NET CommandsFireTest..." -ForegroundColor Yellow
cd "$rootDir\bot-api\dotnet\test"
dotnet test --filter "FullyQualifiedName~CommandsFireTest" --verbosity normal
$dotnetResult = $LASTEXITCODE
if ($dotnetResult -eq 0) {
    Write-Host "[PASS] .NET tests passed" -ForegroundColor Green
} else {
    Write-Host "[FAIL] .NET tests failed" -ForegroundColor Red
}
Write-Host ""

# Test Python
Write-Host "3. Testing Python test_commands_fire..." -ForegroundColor Yellow
cd "$rootDir\bot-api\python"
& ".venv\Scripts\python.exe" -m pytest tests/bot_api/test_commands_fire.py -v
$pythonResult = $LASTEXITCODE
if ($pythonResult -eq 0) {
    Write-Host "[PASS] Python tests passed" -ForegroundColor Green
} else {
    Write-Host "[FAIL] Python tests failed" -ForegroundColor Red
}
Write-Host ""

# Summary
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "SUMMARY" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "Java:   $(if ($javaResult -eq 0) { '[PASS]' } else { '[FAIL]' })" -ForegroundColor $(if ($javaResult -eq 0) { 'Green' } else { 'Red' })
Write-Host ".NET:   $(if ($dotnetResult -eq 0) { '[PASS]' } else { '[FAIL]' })" -ForegroundColor $(if ($dotnetResult -eq 0) { 'Green' } else { 'Red' })
Write-Host "Python: $(if ($pythonResult -eq 0) { '[PASS]' } else { '[FAIL]' })" -ForegroundColor $(if ($pythonResult -eq 0) { 'Green' } else { 'Red' })
Write-Host ""

if ($javaResult -eq 0 -and $dotnetResult -eq 0 -and $pythonResult -eq 0) {
    Write-Host "[PASS] All tests passed!" -ForegroundColor Green
    exit 0
} else {
    Write-Host "[FAIL] Some tests failed" -ForegroundColor Red
    exit 1
}
