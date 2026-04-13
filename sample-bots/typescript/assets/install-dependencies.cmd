@echo off
setlocal EnableExtensions EnableDelayedExpansion

rem install-dependencies.cmd - Windows dependency installer for sample-bots/typescript
rem Mirrors the behavior of install-dependencies.sh on macOS/Linux.
rem
rem Behavior:
rem - Changes to the archive root (parent of the deps folder where this script lives)
rem - If deps\.deps_installed does not exist, installs npm dependencies
rem - npm install uses package.json at the archive root; node_modules/ is created there
rem   so that Node.js module resolution can find it from any bot subdirectory
rem - Only creates deps\.deps_installed if installation succeeds
rem - Exits with non-zero code and message on failure
rem - Uses a simple lock (deps\.deps_lock dir) to avoid concurrent installs

rem Change to the archive root (parent of the directory containing this script)
cd /d "%~dp0.."

rem Fast path: if already installed, exit silently
if exist "deps\.deps_installed" goto :DONE

rem Acquire lock (mkdir as mutex). Wait up to 300 seconds.
set "LOCK_DIR=deps\.deps_lock"
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
if exist "deps\.deps_installed" goto :RELEASE_AND_DONE

echo Installing dependencies...

rem Check that Node.js is available
where node >nul 2>nul
if errorlevel 1 (
    echo Error: Node.js not found. Please install Node.js 18 or newer from https://nodejs.org/
    goto :RELEASE_AND_FAIL
)

rem Run npm install from the archive root (package.json is here, node_modules/ will be created here)
call npm install --prefer-offline
if errorlevel 1 (
    echo Error: npm install failed.
    goto :RELEASE_AND_FAIL
)

echo. > "deps\.deps_installed"
echo Dependencies installed.

:RELEASE_AND_DONE
if defined LOCK_ACQUIRED rmdir "%LOCK_DIR%" 2>nul
goto :DONE

:RELEASE_AND_FAIL
if defined LOCK_ACQUIRED rmdir "%LOCK_DIR%" 2>nul
exit /b 1

:DONE
endlocal
