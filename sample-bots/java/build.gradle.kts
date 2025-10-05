import java.io.PrintWriter
import java.nio.file.Files.*
import java.nio.file.Path

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

    fun Path.botName() = fileName.toString()

    // Shared helpers provided by parent sample-bots/build.gradle.kts
    @Suppress("UNCHECKED_CAST")
    val isBotProjectDir = rootProject.extra["isBotProjectDir"] as (Path) -> Boolean
    @Suppress("UNCHECKED_CAST")
    val copyBotFiles = rootProject.extra["copyBotFiles"] as (Path, Path) -> Unit

    fun isWindows() = System.getProperty("os.name").lowercase().contains("windows")

    fun createScriptFile(projectDir: Path, botArchivePath: Path, fileExt: String, newLine: String) {
        val botName = projectDir.botName()
        val file = botArchivePath.resolve("$botName.$fileExt").toFile()
        val printWriter = object : PrintWriter(file) {
            override fun println() {
                write(newLine)
            }
        }

        val java = if (isWindows()) "javaw" else "java"

        printWriter.use {
            if (fileExt == "sh") {
                it.println("#!/bin/sh")
            }
            it.println("$java -cp ../lib/* $botName.java")
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

    val copyBotApiJar by registering(Copy::class) {
        mkdir(libDir)

        duplicatesStrategy = DuplicatesStrategy.INCLUDE

        dependsOn(":bot-api:java:jar")

        from(project(":bot-api:java").file("build/libs/robocode-tankroyale-bot-api-${version}.jar"))
        into(libDir)
    }

    named("build") {
        dependsOn(copyBotApiJar)

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