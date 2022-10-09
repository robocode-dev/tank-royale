import build.csproj.generateBotCsprojFile
import java.io.PrintWriter
import java.nio.file.Files.*
import java.nio.file.Path
import java.nio.file.StandardCopyOption.REPLACE_EXISTING

description = "Robocode Tank Royale sample bots for C#"

version = libs.versions.tankroyale.get()

val archiveFilename = "sample-bots-csharp-${project.version}.zip"


tasks {
    val archiveDir = project.buildDir.resolve("archive").toPath()

    register("clean") {
        delete(project.buildDir)
    }

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
        // Important: It seems that we need to add the `>nul` redirection to avoid the cmd processes to halt!?
        val redirect = if (fileExt == "cmd") ">nul" else ""

        printWriter.use {
            if (fileExt == "sh") {
                it.println("#!/bin/sh")
            }
            it.println("dotnet run $redirect")
        }
    }

    fun prepareBotFiles() {
        list(project.projectDir.toPath()).forEach { botDir ->
            run {
                if (isDirectory(botDir) && isBotProjectDir(botDir)) {
                    val botName = botDir.botName()
                    val botArchivePath: Path = archiveDir.resolve(botName)

                    mkdir(botArchivePath)
                    copyBotFiles(botDir, botArchivePath)
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

    val build by registering {
        doFirst {
            prepareBotFiles()
            copyReadMeFile(project.projectDir, archiveDir)
        }
    }

    register("zip", Zip::class) {
        dependsOn(build)

        archiveFileName.set(archiveFilename)
        destinationDirectory.set(buildDir)
        fileMode = "101101101".toInt(2) // 0555 - read & execute for everybody

        from(File(buildDir, "archive"))
    }
}