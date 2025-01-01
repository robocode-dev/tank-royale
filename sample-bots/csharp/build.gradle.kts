import build.csproj.generateBotCsprojFile
import java.io.PrintWriter
import java.nio.file.Files.*
import java.nio.file.Path
import java.nio.file.StandardCopyOption.REPLACE_EXISTING

description = "Robocode Tank Royale sample bots for C#"

version = libs.versions.tankroyale.get()

val archiveFilename = "sample-bots-csharp-${version}.zip"

plugins {
    base // for the clean and build task
}

tasks {
    val archiveDir = layout.buildDirectory.dir("archive")
    val archiveDirPath = archiveDir.get().asFile.toPath()

    fun Path.botName() = fileName.toString()

    fun isBotProjectDir(dir: Path): Boolean {
        val botName = dir.botName()
        return !botName.startsWith(".") && botName !in listOf("build", "assets")
    }

    fun copyBotFiles(projectDir: Path, botArchivePath: Path) {
        for (file in list(projectDir)) {
            copy(file, botArchivePath.resolve(file.fileName), REPLACE_EXISTING)
        }
    }

    fun createScriptFile(projectDir: Path, botArchivePath: Path, fileExt: String, newLine: String) {
        val botName = projectDir.botName()
        val file = botArchivePath.resolve("$botName.$fileExt").toFile()
        val printWriter = object : PrintWriter(file) {
            override fun println() {
                write(newLine)
            }
        }
        printWriter.use {
            when (fileExt) {
                "sh" -> {
                    it.println("""#!/bin/sh
if [ -d "bin" ]; then
  dotnet build
fi
dotnet run --no-build
"""
                    )
                }

                "cmd" -> {
                    it.println("""
if not exist bin\ (
  dotnet build >nul
)
dotnet run --no-build >nul
"""
                    )
                }
            }
        }
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

    fun copyReadMeFile(projectDir: File, archivePath: Path) {
        val filename = "ReadMe.md"
        copy(File(projectDir, "assets/$filename").toPath(), archivePath.resolve(filename), REPLACE_EXISTING)
    }

    val build = named("build") {
        doLast {
            prepareBotFiles()
            copyReadMeFile(projectDir, archiveDirPath)
        }
    }

    val zip by registering(Zip::class) {
        dependsOn(build)

        archiveFileName.set(archiveFilename)
        destinationDirectory.set(layout.buildDirectory)
        filePermissions {
            user {
                read = true
                execute = true
            }
            other {
                execute = true
            }
        }

        from(archiveDir)
    }
}