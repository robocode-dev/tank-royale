import org.hidetake.groovy.ssh.core.RunHandler
import org.hidetake.groovy.ssh.session.SessionHandler
import java.io.PrintWriter
import java.nio.file.Files.*
import java.nio.file.Path
import java.nio.file.StandardCopyOption.REPLACE_EXISTING

apply(from = "../../groovy.gradle")

version = libs.versions.tankroyale.get()

val archiveFilename = "sample-bots-java-${project.version}.zip"


val sampleBotsReleasePath: String by rootProject.extra


@Suppress("DSL_SCOPE_VIOLATION")
plugins {
    alias(libs.plugins.hidetake.ssh)
}

tasks {
    val archiveDir = project.buildDir.resolve("archive").toPath()
    val libDir = archiveDir.resolve("lib")

    register("clean") {
        delete(project.buildDir)
    }

    val copyBotApiJar by registering(Copy::class) {
        mkdir(libDir)

        duplicatesStrategy = DuplicatesStrategy.INCLUDE

        dependsOn(":bot-api:java:jar")

        from(project(":bot-api:java").file("build/libs"))
        into(libDir)

        exclude("*-javadoc.jar")
        exclude("*-sources.jar")
        exclude("java-*.jar")
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
        list(project.projectDir.toPath()).forEach { botDir ->
            run {
                if (isDirectory(botDir) && isBotProjectDir(botDir)) {
                    val botArchivePath: Path = archiveDir.resolve(botDir.botName())

                    mkdir(botArchivePath)
                    copyBotFiles(botDir, botArchivePath)
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

    val build by registering {
        dependsOn(copyBotApiJar)

        doLast {
            prepareBotFiles()
            copyReadMeFile(project.projectDir, archiveDir)
        }
    }

    val zip by registering(Zip::class) {
        dependsOn(build)

        archiveFileName.set(archiveFilename)
        destinationDirectory.set(buildDir)
        fileMode = "101101101".toInt(2) // 0555 - read & execute for everybody

        from(File(buildDir, "archive"))
    }

    register("upload") {
        dependsOn(zip)

        doLast {
            ssh.run(delegateClosureOf<RunHandler> {
                session(remotes["sshServer"], delegateClosureOf<SessionHandler> {
                    print("Uploading Java sample bots...")

                    val destDir = sampleBotsReleasePath + "/" + project.version
                    val destFile = "$destDir/$archiveFilename"

                    execute("rm -f $destFile")
                    execute("mkdir -p ~/$destDir")

                    put(hashMapOf("from" to "${project.projectDir}/build/$archiveFilename", "into" to destDir))

                    println("done")
                })
            })
        }
    }
}