import java.io.PrintWriter
import java.nio.file.Files.*
import java.nio.file.Path
import java.nio.file.StandardCopyOption.REPLACE_EXISTING

description = "Robocode Tank Royale sample bots for TypeScript"
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
private val assetsFolder = "assets"

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
    cd "${'$'}(dirname "${'$'}0")"
    node $botName.js
    """.trimIndent()

private fun createBatchScript(botName: String): String = """
    @echo off
    cd /d "%~dp0"
    node $botName.js
    """.trimIndent()

private fun createScriptFile(projectDir: Path, botArchivePath: Path, fileExt: String, newLine: String) {
    val botName = projectDir.botName()
    val file = botArchivePath.resolve("$botName.$fileExt").toFile()
    val printWriter = object : PrintWriter(file) {
        override fun println() {
            write(newLine)
        }
    }
    printWriter.use {
        val content = when (fileExt) {
            shellExtension -> createShellScript(botName)
            else -> createBatchScript(botName)
        }
        content.lines().forEach { line ->
            it.print(line)
            it.println()
        }
    }
    if (fileExt == shellExtension) {
        file.setExecutable(true)
    }
}

fun prepareBotFiles() {
    list(projectDir.toPath()).forEach { botDir ->
        if (isDirectory(botDir) && isBotProjectDir(botDir)) {
            val botArchivePath: Path = archiveDirPath.resolve(botDir.botName())
            mkdir(botArchivePath)
            copyBotFiles(botDir, botArchivePath)
            if (!botDir.toString().endsWith(teamSuffix)) {
                createScriptFile(botDir, botArchivePath, batchExtension, windowsLineEnding)
                createScriptFile(botDir, botArchivePath, shellExtension, unixLineEnding)
            }
        }
    }
}

val prepareArchive by tasks.registering {
    doLast {
        prepareBotFiles()
    }
}

tasks.named("build") {
    dependsOn(prepareArchive)
}
