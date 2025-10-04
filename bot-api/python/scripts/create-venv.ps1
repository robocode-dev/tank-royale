<#
.SYNOPSIS
  Create and prepare a Python virtual environment for bot-api/python (Windows/PowerShell)
.DESCRIPTION
  - Creates .venv if missing
  - Ensures pip is available and upgrades pip/setuptools/wheel
  - Installs dependencies from requirements.txt
  - Idempotent
#>

$ErrorActionPreference = 'Stop'
Set-StrictMode -Version Latest

# Move to project directory (script/..)
$scriptDir = Split-Path -Parent $MyInvocation.MyCommand.Path
Set-Location (Join-Path $scriptDir '..')

function Find-Python {
    $candidates = @('python3', 'python')
    foreach ($c in $candidates) {
        try {
            $v = & $c --version 2>$null
            if ($LASTEXITCODE -eq 0) { return $c }
        } catch {}
    }
    return $null
}

$py = Find-Python
if (-not $py) {
    throw 'Python not found on PATH. Please install Python 3.10+'
}

$venvDir = '.venv'
if (-not (Test-Path $venvDir)) {
    Write-Host "Creating virtual environment in $venvDir ..."
    & $py -m venv $venvDir
}

# Determine venv python path
$venvPy = Join-Path $venvDir 'Scripts/python.exe'
if (-not (Test-Path $venvPy)) {
    throw "Virtual environment python not found at $venvPy"
}

# Ensure pip is present
try {
    & $venvPy -m pip --version *> $null
} catch {
    & $venvPy -m ensurepip --upgrade *> $null
}

# Upgrade core tooling quietly
& $venvPy -m pip install --upgrade pip setuptools wheel | Out-Null

# Install project requirements
if (Test-Path 'requirements.txt') {
    Write-Host 'Installing Python dependencies from requirements.txt ...'
    & $venvPy -m pip install -r requirements.txt | Out-Null
}

Write-Host ("Virtual environment ready: " + (& $venvPy --version))
