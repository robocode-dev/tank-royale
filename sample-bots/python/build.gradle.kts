import java.io.PrintWriter
import java.nio.file.Files.*
import java.nio.file.Path
import java.nio.file.StandardCopyOption.REPLACE_EXISTING

description = "Robocode Tank Royale sample bots for Python"

version = libs.versions.tankroyale.get()

plugins {
    base // for the clean and build task
}

val archiveDir = layout.buildDirectory.dir("archive")
val archiveDirPath = archiveDir.get().asFile.toPath()

fun Path.botName() = fileName.toString()

// Shared helpers provided by parent sample-bots/build.gradle.kts
@Suppress("UNCHECKED_CAST")
val isBotProjectDir = rootProject.extra["isBotProjectDir"] as (Path) -> Boolean
@Suppress("UNCHECKED_CAST")
val copyBotFiles = rootProject.extra["copyBotFiles"] as (Path, Path) -> Unit

private fun writeShellScript(writer: PrintWriter, botName: String) {
    val shellScript = """
        #!/bin/sh
        ../deps/install-dependencies.sh

        cd -- "${'$'}(dirname -- "${'$'}0")"
        if command -v python3 >/dev/null 2>&1; then
          PY=python3
        elif command -v python >/dev/null 2>&1; then
          PY=python
        else
          echo "Error: Python not found. Please install python3 or python." >&2
          exit 1
        fi

        exec "${'$'}PY" $botName.py
        """.trimIndent()

    writer.print(shellScript)
}

private fun writeBatchScript(writer: PrintWriter, botName: String) {
    // Important: We need to add the `>nul` redirection to avoid cmd processes halting
    val batchScript = """
        call ..\deps\install-dependencies.cmd

        cd /d "%~dp0"
        set "PY="
        where python3 >nul 2>nul && set "PY=python3"
        if not defined PY (
          where python >nul 2>nul && set "PY=python"
        )
        if not defined PY (
          echo Error: Python not found. Please install Python 3.
          exit /b 1
        )
        %PY% $botName.py >nul
        """.trimIndent()

    writer.print(batchScript)
}

private fun createScriptFile(projectDir: Path, botArchivePath: Path, fileExt: String, newLine: String) {
    val botName = projectDir.botName()
    val file = botArchivePath.resolve("$botName.$fileExt").toFile()
    val printWriter = object : PrintWriter(file) {
        override fun println() {
            write(newLine)
        }
    }

    printWriter.use { writer ->
        when (fileExt) {
            "sh" -> writeShellScript(writer, botName)
            "cmd" -> writeBatchScript(writer, botName)
        }
    }
}

tasks {
    fun prepareBotFiles() {
        list(projectDir.toPath()).forEach { botDir ->
            if (isDirectory(botDir) && isBotProjectDir(botDir)) {
                val botArchivePath: Path = archiveDirPath.resolve(botDir.botName())

                mkdir(botArchivePath)
                copyBotFiles(botDir, botArchivePath)

                if (!botDir.toString().endsWith("Team")) {
                    createScriptFile(botDir, botArchivePath, "cmd", "\r\n")
                    createScriptFile(botDir, botArchivePath, "sh", "\n")
                }
            }
        }
    }

    fun prepareDepsDir() {
        // Create a deps folder and copy dependency installers + requirements.txt
        val depsDir = archiveDirPath.resolve("deps")
        mkdir(depsDir)

        // Copy install-dependencies scripts from assets
        val assetsDir = project.projectDir.toPath().resolve("assets")
        copy(assetsDir.resolve("install-dependencies.cmd"), depsDir.resolve("install-dependencies.cmd"), REPLACE_EXISTING)
        copy(assetsDir.resolve("install-dependencies.sh"), depsDir.resolve("install-dependencies.sh"), REPLACE_EXISTING)

        // Copy requirements.txt from bot-api/python into deps
        val requirements = rootProject.projectDir.toPath().resolve("bot-api/python/requirements.txt")
        if (exists(requirements)) {
            copy(requirements, depsDir.resolve("requirements.txt"), REPLACE_EXISTING)
        } else {
            throw GradleException("requirements.txt not found at: ${requirements.toAbsolutePath()}")
        }
    }

    named("build") {
        doLast {
            prepareBotFiles()
            prepareDepsDir()
        }
    }
}