import java.io.PrintWriter
import java.nio.file.Files.*
import java.nio.file.Path
import java.nio.file.StandardCopyOption.REPLACE_EXISTING

description = "Robocode Tank Royale sample bots for Python"

version = libs.versions.tankroyale.get()

plugins {
    base // for the clean and build task
}

// Constants
private val shellExtension = "sh"
private val batchExtension = "cmd"
private val unixLineEnding = "\n"
private val windowsLineEnding = "\r\n"
private val teamSuffix = "Team"
private val depsFolder = "deps"
private val assetsFolder = "assets"
private val requirementsFile = "requirements.txt"
private val wheelPrefix = "robocode_tank_royale-"
private val wheelExtension = ".whl"
private val botApiPythonPath = "bot-api/python"
private val pythonDistPath = "$botApiPythonPath/dist"

val archiveDir = layout.buildDirectory.dir("archive")
val archiveDirPath: Path = archiveDir.get().asFile.toPath()

fun Path.botName() = fileName.toString()

// Shared helpers provided by parent sample-bots/build.gradle.kts
@Suppress("UNCHECKED_CAST")
val isBotProjectDir = rootProject.extra["isBotProjectDir"] as (Path) -> Boolean

@Suppress("UNCHECKED_CAST")
val copyBotFiles = rootProject.extra["copyBotFiles"] as (Path, Path) -> Unit

private fun createShellScript(botName: String): String = """
    #!/bin/sh
    set -e
    
    # Change to script directory
    cd -- "$(dirname -- "$0")"
    
    # Install dependencies (relative to script dir)
    ../deps/install-dependencies.sh
    
    # Try to use venv python first (correct path: ../deps/venv)
    if [ -x "../deps/venv/bin/python" ]; then
        exec "../deps/venv/bin/python" "$botName.py"
    elif command -v python3 >/dev/null 2>&1; then
        exec python3 "$botName.py"
    elif command -v python >/dev/null 2>&1; then
        exec python "$botName.py"
    else
        echo "Error: Python not found. Please install python3 or python." >&2
        exit 1
    fi
    """.trimIndent()

private fun createBatchScript(botName: String): String = """
    cd /d "%~dp0"

    call ..\deps\install-dependencies.cmd

    set "VENV_PY=..\deps\venv\Scripts\python.exe"
    if exist "%VENV_PY%" (
      "%VENV_PY%" "$botName.py"
      exit /b %errorlevel%
    )

    set "PY="
    where python3 >nul 2>nul && set "PY=python3"
    if not defined PY (
      where python >nul 2>nul && set "PY=python"
    )
    if not defined PY (
      echo Error: Python not found. Please install Python 3.
      exit /b 1
    )
    %PY% "$botName.py"
    """.trimIndent()

private fun writeScriptContent(writer: PrintWriter, scriptType: String, botName: String) {
    val script = when (scriptType) {
        shellExtension -> createShellScript(botName)
        batchExtension -> createBatchScript(botName)
        else -> throw IllegalArgumentException("Unsupported script type: $scriptType")
    }
    writer.print(script)
}

private fun createCustomPrintWriter(file: File, lineEnding: String): PrintWriter {
    return object : PrintWriter(file) {
        override fun println() {
            write(lineEnding)
        }
    }
}

private fun createBotScriptFile(botDir: Path, archivePath: Path, extension: String, lineEnding: String) {
    val botName = botDir.botName()
    val scriptFile = archivePath.resolve("$botName.$extension").toFile()

    createCustomPrintWriter(scriptFile, lineEnding).use { writer ->
        writeScriptContent(writer, extension, botName)
    }
}

private fun createBotScriptFiles(botDir: Path, archivePath: Path) {
    createBotScriptFile(botDir, archivePath, batchExtension, windowsLineEnding)
    createBotScriptFile(botDir, archivePath, shellExtension, unixLineEnding)
}

private fun isTeamBot(botDir: Path): Boolean = botDir.toString().endsWith(teamSuffix)

private fun processIndividualBot(botDir: Path) {
    val botArchivePath = archiveDirPath.resolve(botDir.botName())
    mkdir(botArchivePath)
    copyBotFiles(botDir, botArchivePath)

    if (!isTeamBot(botDir)) {
        createBotScriptFiles(botDir, botArchivePath)
    }
}

private fun copyInstallationScripts(depsDir: Path) {
    val assetsDir = project.projectDir.toPath().resolve(assetsFolder)
    val installScriptCmd = "install-dependencies.$batchExtension"
    val installScriptSh = "install-dependencies.$shellExtension"

    copy(assetsDir.resolve(installScriptCmd), depsDir.resolve(installScriptCmd), REPLACE_EXISTING)
    copy(assetsDir.resolve(installScriptSh), depsDir.resolve(installScriptSh), REPLACE_EXISTING)
}

private fun copyRequirementsFile(depsDir: Path) {
    val requirementsPath = rootProject.projectDir.toPath().resolve("$botApiPythonPath/$requirementsFile")

    if (!exists(requirementsPath)) {
        throw GradleException("$requirementsFile not found at: ${requirementsPath.toAbsolutePath()}")
    }

    copy(requirementsPath, depsDir.resolve(requirementsFile), REPLACE_EXISTING)
}

private fun findWheelFile(distDir: Path): Path {
    return list(distDir).use { paths ->
        paths.filter { path ->
            val fileName = path.fileName.toString()
            fileName.startsWith(wheelPrefix) && fileName.endsWith(wheelExtension)
        }.findFirst().orElseThrow {
            GradleException("robocode_tank_royale wheel file not found in: ${distDir.toAbsolutePath()}")
        }
    }
}

private fun copyWheelFile(depsDir: Path) {
    val distDir = rootProject.projectDir.toPath().resolve(pythonDistPath)

    if (!exists(distDir)) {
        throw GradleException("Python dist directory not found at: ${distDir.toAbsolutePath()}")
    }

    val wheelFile = findWheelFile(distDir)
    copy(wheelFile, depsDir.resolve(wheelFile.fileName.toString()), REPLACE_EXISTING)
}

tasks {
    fun prepareBotFiles() {
        list(projectDir.toPath()).forEach { botDir ->
            if (isDirectory(botDir) && isBotProjectDir(botDir)) {
                processIndividualBot(botDir)
            }
        }
    }

    fun prepareDependencies() {
        val depsDir = archiveDirPath.resolve(depsFolder)
        mkdir(depsDir)

        copyInstallationScripts(depsDir)
        copyRequirementsFile(depsDir)
        copyWheelFile(depsDir)
    }

    named("build") {
        dependsOn(":bot-api:python:build-dist")
        doLast {
            prepareBotFiles()
            prepareDependencies()
        }
    }
}