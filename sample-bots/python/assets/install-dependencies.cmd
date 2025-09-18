@echo off

rem install-dependencies.cmd — Windows dependency installer for sample-bots/python
rem Mirrors the behavior of install-dependencies.sh on macOS/Linux.
rem
rem Behavior:
rem - Changes to the directory where this script resides
rem - If .deps_installed does not exist, installs requirements from requirements.txt
rem - Only creates .deps_installed if installation succeeds
rem - Exits with non-zero code and message on failure

rem Change to the directory where this script is located
cd /d "%~dp0"

rem Check if dependencies are already installed
if not exist ".deps_installed" (
    echo Installing dependencies...

    set "PY="
    where python3 >nul 2>nul && set "PY=python3"
    if not defined PY (
        where python >nul 2>nul && set "PY=python"
    )
    if not defined PY (
        echo Error: Python not found. Please install Python 3.
        exit /b 1
    )

    %PY% -m pip install -q -r requirements.txt

    rem Only create marker file if pip install succeeded
    if %errorlevel% equ 0 (
        rem Create marker file to indicate dependencies are installed
        echo. > .deps_installed
        echo Dependencies installed.
    ) else (
        echo Error: Failed to install dependencies.
        exit /b %errorlevel%
    )
)