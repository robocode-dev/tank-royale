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
rem - Uses a simple lock (.deps_lock dir) to avoid concurrent installs

rem Change to the directory where this script is located
cd /d "%~dp0"

rem Fast path: if already installed, exit silently
if exist ".deps_installed" goto :DONE

rem Acquire lock (mkdir as mutex). Wait up to 300 seconds.
set "LOCK_DIR=.deps_lock"
set "LOCK_ACQUIRED="
set /a TRY=0
:ACQUIRE_LOCK
2>nul mkdir "%LOCK_DIR%"
if not errorlevel 1 (
  set "LOCK_ACQUIRED=1"
  goto :LOCKED
)
set /a TRY+=1
if %TRY% GEQ 300 (
  echo Error: Could not acquire dependency installation lock after 300 seconds.
  exit /b 1
)
rem Sleep ~1 second and retry (avoid TIMEOUT due to stdin redirection issues)
ping 127.0.0.1 -n 2 >nul
goto :ACQUIRE_LOCK

:LOCKED
rem Double-check after acquiring lock
if exist ".deps_installed" goto :RELEASE_AND_DONE

echo Installing dependencies...

rem Find Python command (prefer the Windows py launcher)
set "PY="
where py >nul 2>nul && set "PY=py -3"
if not defined PY (
    where python3 >nul 2>nul && set "PY=python3"
)
if not defined PY (
    where python >nul 2>nul && set "PY=python"
)
if not defined PY (
    echo Error: Python not found. Please install Python 3 and ensure either 'py' or 'python' is on PATH.
    goto :RELEASE_AND_FAIL
)

rem Always use a virtual environment to avoid installing into global Python

echo Using virtual environment...

set "VENV_DIR=venv"
if not exist "%VENV_DIR%" (
    echo Creating virtual environment...
    %PY% -m venv "%VENV_DIR%"
    if errorlevel 1 (
        echo Error: Failed to create virtual environment. Make sure the Python venv module is installed.
        goto :RELEASE_AND_FAIL
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
    goto :RELEASE_AND_FAIL
)

rem Install requirements in the virtual environment
"%VENV_PY%" -m pip install -q -r requirements.txt
if errorlevel 1 (
    echo Error: Failed to install dependencies in virtual environment.
    goto :RELEASE_AND_FAIL
)

call :install_wheel_or_pypi
if errorlevel 1 goto :RELEASE_AND_FAIL

echo. > .deps_installed
echo Dependencies installed in virtual environment.
echo Note: Virtual environment created in .\venv directory

:RELEASE_AND_DONE
if defined LOCK_ACQUIRED (
  rmdir "%LOCK_DIR%" 2>nul
)
goto :DONE

:RELEASE_AND_FAIL
if defined LOCK_ACQUIRED (
  rmdir "%LOCK_DIR%" 2>nul
)
exit /b 1

:install_wheel_or_pypi
rem Install local wheel if present, otherwise install from PyPI
set "WHEEL="
for %%f in ("robocode_tank_royale-*.whl" "robocode-tank-royale-*.whl") do (
    set "WHEEL=%%~nxf"
)
if defined WHEEL (
    echo Installing local wheel: !WHEEL!
    "%VENV_PY%" -m pip install -q "!WHEEL!"
    if errorlevel 1 (
        echo Error: Failed to install local wheel !WHEEL!
        exit /b %errorlevel%
    )
) else (
    echo Local robocode_tank_royale-*.whl or robocode-tank-royale-*.whl not found. Installing robocode-tank-royale from PyPI...
    "%VENV_PY%" -m pip install -q robocode-tank-royale
    if errorlevel 1 (
        echo Error: Failed to install robocode-tank-royale from PyPI
        exit /b %errorlevel%
    )
)
exit /b 0

:DONE
endlocal