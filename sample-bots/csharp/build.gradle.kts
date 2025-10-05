import build.csproj.generateBotCsprojFile
import java.io.PrintWriter
import java.nio.file.Files.*
import java.nio.file.Path

description = "Robocode Tank Royale sample bots for C#"

version = libs.versions.tankroyale.get()

plugins {
    base // for the clean and build task
}

tasks {
    val archiveDir = layout.buildDirectory.dir("archive")
    val archiveDirPath = archiveDir.get().asFile.toPath()

    fun Path.botName() = fileName.toString()

    // Shared helpers provided by parent sample-bots/build.gradle.kts
    @Suppress("UNCHECKED_CAST")
    val isBotProjectDir = rootProject.extra["isBotProjectDir"] as (Path) -> Boolean
    @Suppress("UNCHECKED_CAST")
    val copyBotFiles = rootProject.extra["copyBotFiles"] as (Path, Path) -> Unit

    fun generateShellScript(): String = """
        #!/bin/sh
        if [ ! -d "bin" ]; then
          dotnet build
        fi
        dotnet run --no-build
        """.trimIndent()

    fun generateBatchScript(): String = """
        if not exist bin\ (
            dotnet build
        )
        dotnet run --no-build
        """.trimIndent()

    fun createScriptFile(projectDir: Path, botArchivePath: Path, fileExt: String, newLine: String) {
        val botName = projectDir.botName()
        val scriptContent = when (fileExt) {
            "sh" -> generateShellScript()
            "cmd" -> generateBatchScript()
            else -> throw IllegalArgumentException("Unsupported file extension: $fileExt")
        }

        val file = botArchivePath.resolve("$botName.$fileExt").toFile()
        file.writeText(scriptContent.replace("\n", newLine))
    }

    fun prepareBotFiles() {
        list(project.projectDir.toPath()).forEach { botDir ->
            if (isDirectory(botDir) && isBotProjectDir(botDir)) {
                val botName = botDir.botName()
                val botArchivePath: Path = archiveDirPath.resolve(botName)

                mkdir(botArchivePath)
                copyBotFiles(botDir, botArchivePath)

                if (!botDir.toString().endsWith("Team")) {
                    createScriptFile(botDir, botArchivePath, "cmd", "\r\n")
                    createScriptFile(botDir, botArchivePath, "sh", "\n")

                    generateBotCsprojFile(botArchivePath.resolve("$botName.csproj"), botName, "${project.version}")
                }
            }
        }
    }

    named("build") {
        doLast {
            prepareBotFiles()
        }
    }
}