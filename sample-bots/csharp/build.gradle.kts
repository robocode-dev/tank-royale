import dev.robocode.tankroyale.csproj.generateBotCsprojFile
import org.hidetake.groovy.ssh.core.RunHandler
import org.hidetake.groovy.ssh.session.SessionHandler
import java.io.PrintWriter
import java.nio.file.Files.*
import java.nio.file.Path
import java.nio.file.attribute.PosixFilePermission


version = project(":bot-api:dotnet").version

val archiveFilename = "sample-bots-csharp-${project.version}.zip"

plugins {
    alias(libs.plugins.hidetake.ssh)
}

defaultTasks("clean", "build")

val clean = tasks.register<Delete>("clean") {
    delete(project.buildDir)
}

val build = tasks.register("build") {
    dependsOn(clean, zipSampleBots)
}

abstract class BaseTask : DefaultTask() {
    @Internal
    protected val archiveDir: Path = project.buildDir.toPath().resolve("archive")

    protected fun createDir(path: Path) {
        if (!exists(path)) {
            createDirectory(path)
        }
    }

    protected fun deleteDir(path: Path) {
        if (exists(path)) {
            walk(path)
                .sorted(Comparator.reverseOrder())
                .map(({ obj: Path -> obj.toFile() }))
                .forEach(({ obj: File -> obj.delete() }))
        }
    }
}

abstract class CreateDirs : BaseTask() {
    @TaskAction
    fun build() {
        createDir(project.buildDir.toPath())
        createDir(archiveDir)
    }
}

val createDirs = task<CreateDirs>("createDirs") {}

abstract class CopyBotFiles : BaseTask() {
    @TaskAction
    fun prepareBotFiles() {
        list(project.projectDir.toPath()).forEach { botDir ->
            run {
                if (isDirectory(botDir) && isBotProjectDir(botDir)) {
                    val botName = botDir.botName()

                    val botArchivePath: Path = archiveDir.resolve(botName)

                    createDir(botArchivePath)
                    copyBotFiles(botDir, botArchivePath)
                    createScriptFile(botDir, botArchivePath, "cmd", "\r\n")
                    createScriptFile(botDir, botArchivePath, "sh", "\n")

                    generateBotCsprojFile(botArchivePath.resolve("$botName.csproj"), botName, "${project.version}")
                }
            }
        }
        copyReadMeFile(project.projectDir, archiveDir)
    }

    private fun Path.botName(): String {
        return fileName.toString()
    }

    private fun isBotProjectDir(dir: Path): Boolean {
        val botName = dir.botName()
        return !botName.startsWith(".") && botName !in listOf("build", "assets")
    }

    private fun copyBotFiles(projectDir: Path, botArchivePath: Path) {
        for (file in list(projectDir)) {
            copy(file, botArchivePath.resolve(file.fileName))
        }
    }

    private fun copyReadMeFile(projectDir: File, archivePath: Path) {
        val filename = "ReadMe.md"
        copy(File(projectDir, "assets/$filename").toPath(), archivePath.resolve(filename))
    }

    private fun createScriptFile(projectDir: Path, botArchivePath: Path, fileExt: String, newLine: String) {
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
            it.close()
        }
    }
}

val copyBotFiles = task<CopyBotFiles>("copyBotFiles") {
    dependsOn(createDirs)
}

val zipSampleBots = task<Zip>("zipSampleBots") {
    dependsOn(copyBotFiles)

    archiveFileName.set(archiveFilename)
    destinationDirectory.set(buildDir)
    fileMode = "111101101".toInt(2) // 0755

    from(File(buildDir, "archive"))
}

val sshServer = remotes.create("sshServer") {
    withGroovyBuilder {
        setProperty("host", project.properties["tankroyale.ssh.host"])
        setProperty("port", (project.properties["tankroyale.ssh.port"] as String).toInt())
        setProperty("user", project.properties["tankroyale.ssh.user"])
        setProperty("password", project.properties["tankroyale.ssh.pass"])
    }
}

val uploadSampleBots = tasks.register("uploadSampleBots") {
    dependsOn(build)
    dependsOn(zipSampleBots)

    ssh.run (delegateClosureOf<RunHandler> {
        session(sshServer, delegateClosureOf<SessionHandler> {
            print("Uploading sample bots...")

            val destDir = "public_html/tankroyale/sample-bots/${project.version}"
            val destFile = "$destDir/$archiveFilename"

            execute("rm -f $destFile")
            execute("mkdir -p ~/$destDir")

            put(hashMapOf("from" to "${project.projectDir}/build/$archiveFilename", "into" to destDir))

            println("done")
        })
    })
}