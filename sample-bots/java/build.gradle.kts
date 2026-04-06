import java.io.PrintWriter
import java.nio.file.Files.*
import java.nio.file.Path

description = "Robocode Tank Royale sample bots for Java"

group = "dev.robocode.tankroyale"

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

    fun hasBase(botDir: Path): Boolean {
        val botName = botDir.botName()
        val jsonPath = botDir.resolve("$botName.json")
        if (!exists(jsonPath)) return false
        val content = jsonPath.toFile().readText()
        return content.contains("\"base\"")
    }

    fun createScriptFile(projectDir: Path, botArchivePath: Path, fileExt: String, newLine: String) {
        val botName = projectDir.botName()
        val file = botArchivePath.resolve("$botName.$fileExt").toFile()
        val printWriter = object : PrintWriter(file) {
            override fun println() {
                write(newLine)
            }
        }

        // Choose Java executable based on the target script type-Windows batch (.cmd): use javaw to avoid opening a
        // console window.
        // - Unix shell (.sh): use java (javaw does not exist on macOS/Linux).
        val java = when (fileExt) {
            "cmd" -> "javaw"
            else -> "java"
        }

        printWriter.use {
            if (fileExt == "sh") {
                it.println("#!/bin/sh")
            }
            it.println("$java -cp ../lib/* $botName.java")
        }
    }

    fun isTeam(botDir: Path): Boolean {
        val botName = botDir.botName()
        val jsonPath = botDir.resolve("$botName.json")
        if (!exists(jsonPath)) return false
        val content = jsonPath.toFile().readText()
        return content.contains("\"teamMembers\"")
    }

    fun prepareBotFiles() {
        list(projectDir.toPath()).forEach { botDir ->
            if (isDirectory(botDir) && isBotProjectDir(botDir)) {
                val botArchivePath: Path = archiveDirPath.resolve(botDir.botName())

                mkdir(botArchivePath)
                copyBotFiles(botDir, botArchivePath)

                if (isTeam(botDir)) {
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

    val prepareArchive by registering {
        dependsOn(copyBotApiJar)
        doLast {
            prepareBotFiles()
        }
    }

    named("build") {
        dependsOn(prepareArchive)
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
