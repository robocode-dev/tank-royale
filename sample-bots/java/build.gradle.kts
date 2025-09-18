import java.io.PrintWriter
import java.nio.file.Files.*
import java.nio.file.Path
import java.nio.file.StandardCopyOption.REPLACE_EXISTING

description = "Robocode Tank Royale sample bots for Java"

group = "dev.robocode.tankroyale"
version = libs.versions.tankroyale.get()

base {
    archivesName = "sample-bots-java"
}

plugins {
    base // for the clean and build task
    `maven-publish`
}

tasks {
    val archiveDir = layout.buildDirectory.dir("archive")
    val archiveDirPath = archiveDir.get().asFile.toPath()
    val libDir = archiveDirPath.resolve("lib")

    val copyBotApiJar by registering(Copy::class) {
        mkdir(libDir)

        duplicatesStrategy = DuplicatesStrategy.INCLUDE

        dependsOn(":bot-api:java:jar")

        from(project(":bot-api:java").file("build/libs/robocode-tankroyale-bot-api-${version}.jar"))
        into(libDir)
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
            it.println("java -cp ../lib/* $botName.java $redirect")
        }
    }

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

    named("build") {
        doLast {
            prepareBotFiles()
        }
    }

    // Configure the maven publication to use the zip artifact
    publishing {
        publications {
            named<MavenPublication>("maven") {
                // Define the artifact
                artifact(zip) {
                    classifier = "" // No classifier for the main artifact
                    extension = "zip"
                }

                // Ensure artifactId follows base.archivesName
                artifactId = base.archivesName.get()

                // Override the POM name
                pom.name.set("Robocode Tank Royale Sample Bots for Java")
            }
        }
    }
}