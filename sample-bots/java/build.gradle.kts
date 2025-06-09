import java.io.PrintWriter
import java.nio.file.Files.*
import java.nio.file.Path
import java.nio.file.StandardCopyOption.REPLACE_EXISTING

description = "Robocode Tank Royale sample bots for Java"

group = "dev.robocode.tankroyale"
version = libs.versions.tankroyale.get()

val archiveFilename = "sample-bots-java-${version}.zip"

plugins {
    base // for the clean and build task
    `maven-publish`
    signing
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

    fun copyReadMeFile(projectDir: File, archivePath: Path) {
        val filename = "ReadMe.md"
        copy(File(projectDir, "assets/$filename").toPath(), archivePath.resolve(filename), REPLACE_EXISTING)
    }

    val build = named("build") {
        dependsOn(copyBotApiJar)

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

    publishing {
        publications {
            create<MavenPublication>("sample-bots") {
                // Define the artifact
                artifact(zip) {
                    classifier = "" // No classifier for main artifact
                    extension = "zip"
                }

                // Set publication coordinates
                groupId = group.toString()
                artifactId = "sample-bots-java"
                version = project.version.toString()

                pom {
                    name.set("Robocode Tank Royale Sample Bots for Java")
                    description.set(project.description)
                    url.set("https://github.com/robocode-dev/tank-royale")

                    licenses {
                        license {
                            name.set("The Apache License, Version 2.0")
                            url.set("https://www.apache.org/licenses/LICENSE-2.0.txt")
                        }
                    }
                    developers {
                        developer {
                            id = "fnl"
                            name = "Flemming Nørnberg Larsen"
                            url = "https://github.com/flemming-n-larsen"
                            organization = "robocode.dev"
                            organizationUrl = "https://robocode-dev.github.io/tank-royale/"
                        }
                    }
                    scm {
                        connection.set("scm:git:git://github.com/robocode-dev/tank-royale.git")
                        developerConnection.set("scm:git:ssh://github.com:robocode-dev/tank-royale.git")
                        url.set("https://github.com/robocode-dev/tank-royale/tree/master")
                    }
                }
            }
        }

        repositories {
            maven {
                name = "MavenCentral"
                url = uri("https://s01.oss.sonatype.org/service/local/staging/deploy/maven2/")

                credentials {
                    username = project.findProperty("ossrhUsername") as String? ?: System.getenv("OSSRH_USERNAME")
                    password = project.findProperty("ossrhPassword") as String? ?: System.getenv("OSSRH_PASSWORD")
                }
            }
        }
    }
}

signing {
    sign(publishing.publications["sample-bots"])
}