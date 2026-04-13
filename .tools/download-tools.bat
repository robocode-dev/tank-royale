@echo off
REM Download and setup Structurizr tools for Windows
setlocal enabledelayedexpansion

set "TOOLS_DIR=%~dp0"
set "STRUCTURIZR_VERSION=2025.11.09"
set "PLANTUML_VERSION=1.2024.8"

echo === Downloading Structurizr ^& PlantUML Tools ===

REM Check Java
java --version >nul 2>&1
if errorlevel 1 (
    echo Error: Java not found. Please install Java 11+
    exit /b 1
)
echo ✓ Java found

REM Download PlantUML
set "PLANTUML_JAR=%TOOLS_DIR%plantuml.jar"
if exist "%PLANTUML_JAR%" (
    echo PlantUML already exists, skipping
) else (
    echo Downloading PlantUML v%PLANTUML_VERSION%...
    set "URL=https://github.com/plantuml/plantuml/releases/download/v%PLANTUML_VERSION%/plantuml-%PLANTUML_VERSION%.jar"
    powershell -Command "[Net.ServicePointManager]::SecurityProtocol = [Net.SecurityProtocolType]::Tls12; Invoke-WebRequest -Uri '!URL!' -OutFile '!PLANTUML_JAR!' -UseBasicParsing" || (
        echo Failed to download PlantUML
        exit /b 1
    )
    echo ✓ PlantUML downloaded
)

REM Download Structurizr CLI
set "STRUCTURIZR_DIR=%TOOLS_DIR%structurizr-cli"
set "STRUCTURIZR_ZIP=%TOOLS_DIR%structurizr-cli-temp.zip"

if exist "%STRUCTURIZR_DIR%" (
    echo Structurizr CLI already exists, skipping
) else (
    echo Downloading Structurizr CLI v%STRUCTURIZR_VERSION%...
    set "URL=https://github.com/structurizr/cli/releases/download/v%STRUCTURIZR_VERSION%/structurizr-cli.zip"
    powershell -Command "[Net.ServicePointManager]::SecurityProtocol = [Net.SecurityProtocolType]::Tls12; Invoke-WebRequest -Uri '!URL!' -OutFile '!STRUCTURIZR_ZIP!' -UseBasicParsing" || (
        echo Failed to download
        exit /b 1
    )

    echo Extracting...
    set "TEMP_EXTRACT=%TOOLS_DIR%structurizr-temp"
    powershell -Command "Expand-Archive -Path '!STRUCTURIZR_ZIP!' -DestinationPath '!TEMP_EXTRACT!' -Force" || (
        echo Failed to extract
        exit /b 1
    )

    REM Move extracted content to structurizr-cli
    mkdir "%STRUCTURIZR_DIR%" 2>nul
    powershell -Command "Get-ChildItem '!TEMP_EXTRACT!' | Move-Item -Destination '!STRUCTURIZR_DIR!' -Force"

    REM Clean up temp directory
    rmdir /s /q "!TEMP_EXTRACT!" 2>nul
    del /q "!STRUCTURIZR_ZIP!" 2>nul

    echo ✓ Structurizr CLI downloaded
)

REM Create link to structurizr-dsl.jar
set "STRUCTURIZR_LIB=%STRUCTURIZR_DIR%\lib"
if exist "%STRUCTURIZR_LIB%" (
    for /f "delims=" %%f in ('dir /b "%STRUCTURIZR_LIB%\structurizr-dsl-*.jar" 2^>nul') do (
        set "DSL_JAR=%%f"
    )
    if defined DSL_JAR (
        set "LINK_PATH=%TOOLS_DIR%structurizr-dsl.jar"
        if exist "!LINK_PATH!" del /q "!LINK_PATH!" 2>nul
        copy "%STRUCTURIZR_LIB%\!DSL_JAR!" "!LINK_PATH!" >nul
        echo ✓ Created reference for structurizr-dsl.jar
    )
)

echo.
echo === Tools Setup Complete ===
echo.
echo Available tools:
echo   - PlantUML: %PLANTUML_JAR%
echo   - Structurizr CLI: %STRUCTURIZR_DIR%
echo.
echo Test: java -jar %PLANTUML_JAR% -version



