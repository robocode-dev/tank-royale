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
private val depsFolder = "deps"
private val assetsFolder = "assets"
private val tarballExtension = ".tgz"
private val botApiTypescriptPath = "bot-api/typescript"

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
    cd -- "${'$'}(dirname -- "${'$'}0")"
    ../deps/install-dependencies.sh
    exec "../node_modules/.bin/tsx" "$botName.ts"
    """.trimIndent()

private fun createBatchScript(botName: String): String = """
    @echo off
    cd /d "%~dp0"
    call ..\deps\install-dependencies.cmd
    ..\node_modules\.bin\tsx $botName.ts
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

    @Suppress("ResultOfMethodCallIgnored")
    scriptFile.setExecutable(true, false)
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

fun prepareBotFiles() {
    list(projectDir.toPath()).forEach { botDir ->
        if (isDirectory(botDir) && isBotProjectDir(botDir)) {
            processIndividualBot(botDir)
        }
    }
}

private fun copyInstallationScripts(depsDir: Path) {
    val assetsDir = project.projectDir.toPath().resolve(assetsFolder)
    val installScriptCmd = "install-dependencies.$batchExtension"
    val installScriptSh = "install-dependencies.$shellExtension"

    copy(assetsDir.resolve(installScriptCmd), depsDir.resolve(installScriptCmd), REPLACE_EXISTING)
    copy(assetsDir.resolve(installScriptSh), depsDir.resolve(installScriptSh), REPLACE_EXISTING)
}

private fun findTarball(): Path {
    val botApiDir = rootProject.projectDir.toPath().resolve(botApiTypescriptPath)
    return list(botApiDir).use { paths ->
        paths.filter { it.fileName.toString().endsWith(tarballExtension) }
            .findFirst()
            .orElseThrow { GradleException("No $tarballExtension tarball found in $botApiDir. Run :bot-api:typescript:npmPack first.") }
    }
}

private fun generatePackageJson(tarballName: String) {
    // package.json lives at the archive root so node_modules/ is created there,
    // making it visible to Node.js resolution from any bot subdirectory
    val content = """
        {
          "private": true,
          "type": "module",
          "dependencies": {
            "@robocode.dev/tank-royale-bot-api": "file:./$depsFolder/$tarballName",
            "tsx": "^4.19.2",
            "ws": "^8.18.1"
          }
        }
    """.trimIndent()
    archiveDirPath.resolve("package.json").toFile().writeText(content)
}

private fun prepareDependencies() {
    val depsDir = archiveDirPath.resolve(depsFolder)
    createDirectories(depsDir)

    copyInstallationScripts(depsDir)

    val tarball = findTarball()
    val tarballName = tarball.fileName.toString()
    copy(tarball, depsDir.resolve(tarballName), REPLACE_EXISTING)

    generatePackageJson(tarballName)
}

val prepareArchive by tasks.registering {
    doLast {
        prepareBotFiles()
    }
}

val prepareDeps by tasks.registering {
    dependsOn(":bot-api:typescript:npmPack")
    doLast {
        prepareDependencies()
    }
}

tasks.named("build") {
    dependsOn(prepareArchive, prepareDeps)
}
