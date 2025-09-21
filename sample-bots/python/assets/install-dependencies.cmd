@echo off
setlocal EnableExtensions EnableDelayedExpansion

rem install-dependencies.cmd — Windows dependency installer for sample-bots/python
rem Mirrors the behavior of install-dependencies.sh on macOS/Linux.
rem
rem Behavior:
rem - Changes to the directory where this script resides
rem - If .deps_installed does not exist, installs requirements from requirements.txt
rem - Uses a virtual environment to avoid externally-managed-environment errors
rem - Also installs local robocode_tank_royale-*.whl if present
rem - Only creates .deps_installed if installation succeeds
rem - Exits with non-zero code and message on failure

rem Change to the directory where this script is located
cd /d "%~dp0"

rem If already installed, exit silently
if exist ".deps_installed" goto :DONE

echo Installing dependencies...

rem Find Python command
set "PY="
where python3 >nul 2>nul && set "PY=python3"
if not defined PY (
    where python >nul 2>nul && set "PY=python"
)
if not defined PY (
    echo Error: Python not found. Please install Python 3.
    exit /b 1
)

rem Always use a virtual environment to avoid installing into global Python

echo Using virtual environment...

set "VENV_DIR=venv"
if not exist "%VENV_DIR%" (
    echo Creating virtual environment...
    %PY% -m venv "%VENV_DIR%"
    if errorlevel 1 (
        echo Error: Failed to create virtual environment. Make sure the Python venv module is installed.
        exit /b %errorlevel%
    )
)

set "VENV_PY=%VENV_DIR%\Scripts\python.exe"

rem Ensure pip is available in the virtual environment
"%VENV_PY%" -m pip --version >nul 2>nul
if errorlevel 1 (
    "%VENV_PY%" -m ensurepip --upgrade >nul 2>nul
)
"%VENV_PY%" -m pip --version >nul 2>nul
if errorlevel 1 (
    echo Error: pip is not available in the virtual environment. ensurepip may be disabled. Please install Python with pip included.
    exit /b 1
)

rem Install requirements in the virtual environment
"%VENV_PY%" -m pip install -q -r requirements.txt
if errorlevel 1 (
    echo Error: Failed to install dependencies in virtual environment.
    exit /b %errorlevel%
)

set "PIPCMD=\"%VENV_PY%\" -m pip"
call :install_wheel_or_pypi
if errorlevel 1 exit /b %errorlevel%

echo. > .deps_installed
echo Dependencies installed in virtual environment.
echo Note: Virtual environment created in .\venv directory

goto :DONE

:install_wheel_or_pypi
rem Install local wheel if present, otherwise install from PyPI
set "WHEEL="
for %%f in ("robocode_tank_royale-*.whl") do (
    set "WHEEL=%%~nxf"
)
if defined WHEEL (
    echo Installing local wheel: !WHEEL!
    call %PIPCMD% install -q "!WHEEL!"
    if errorlevel 1 (
        echo Error: Failed to install local wheel !WHEEL!
        exit /b %errorlevel%
    )
) else (
    echo Local robocode_tank_royale-*.whl not found. Installing robocode-tank-royale from PyPI...
    call %PIPCMD% install -q robocode-tank-royale
    if errorlevel 1 (
        echo Error: Failed to install robocode-tank-royale from PyPI
        exit /b %errorlevel%
    )
)
exit /b 0

:DONE
endlocal