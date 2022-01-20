import org.hidetake.groovy.ssh.core.RunHandler
import org.hidetake.groovy.ssh.session.SessionHandler
import java.nio.file.Path
import java.nio.file.Files.*
import java.io.PrintWriter

version = project(":bot-api:java").version

val archiveFilename = "sample-bots-java-${project.version}.zip"


val homepageSampleBotsReleasePath: String by rootProject.extra


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

    @Internal
    protected val libDir: Path = archiveDir.resolve("lib")

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
        createDir(libDir)
    }
}

val createDirs = task<CreateDirs>("createDirs") {}

val copyBotApiJar = task<Copy>("copyBotApiJar") {
    duplicatesStrategy = DuplicatesStrategy.INCLUDE

    dependsOn(":bot-api:java:fatJar")
    dependsOn(createDirs)

    from(project(":bot-api:java").file("build/libs"))
    into(project.buildDir.resolve("archive/lib"))
    exclude("*-javadoc.jar")
    exclude("*-sources.jar")
    exclude("java-*.jar")
}

abstract class FindBotApiJarFilename : BaseTask() {
    @TaskAction
    fun build() {
        project.extra["botApiJarFilename"] =
            list(libDir).filter { path ->
                path.fileName.toString().startsWith("robocode-tankroyale-bot-api")
            }.findFirst().get().fileName.toString()
    }
}

val findBotApiJarFilename = task<FindBotApiJarFilename>("findBotApiJarFilename") {
    dependsOn(copyBotApiJar)
}

abstract class CopyBotFiles : BaseTask() {
    @TaskAction
    fun prepareBotFiles() {
        list(project.projectDir.toPath()).forEach { botDir ->
            run {
                if (isDirectory(botDir) && isBotProjectDir(botDir)) {
                    val botArchivePath: Path = archiveDir.resolve(botDir.botName())

                    createDir(botArchivePath)
                    copyBotJavaFiles(botDir, botArchivePath)
                    copyBotJsonFile(botDir, botArchivePath)
                    createScriptFile(botDir, botArchivePath, "cmd", "\r\n")
                    createScriptFile(botDir, botArchivePath, "sh", "\n")
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

    private fun copyBotJavaFiles(projectDir: Path, botArchivePath: Path) {
        val srcRoot = projectDir.resolve("src/main/java")
        for (file in list(srcRoot)) {
            copy(file, botArchivePath.resolve(botArchivePath.botName() + ".java"))
        }
    }

    private fun copyBotJsonFile(projectDir: Path, botArchivePath: Path) {
        val filename = "${projectDir.botName()}.json"
        val jsonFilePath = projectDir.resolve("src/main/resources/$filename")
        copy(jsonFilePath, botArchivePath.resolve(filename))
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
            it.println("java -cp ../lib/* $botName.java $redirect")
        }
    }
}

val copyBotFiles = task<CopyBotFiles>("copyBotFiles") {
    dependsOn(findBotApiJarFilename)
}

val zipSampleBots = task<Zip>("zipSampleBots") {
    dependsOn(copyBotFiles)

    archiveFileName.set(archiveFilename)
    destinationDirectory.set(buildDir)
    fileMode = "101101101".toInt(2) // 0555 - read & execute for everybody

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

tasks.register("uploadBots") {
    dependsOn(build)
    dependsOn(zipSampleBots)

    ssh.run (delegateClosureOf<RunHandler> {
        session(sshServer, delegateClosureOf<SessionHandler> {
            print("Uploading Java sample bots...")

            val destDir = homepageSampleBotsReleasePath + "/" + project.version
            val destFile = "$destDir/$archiveFilename"

            execute("rm -f $destFile")
            execute("mkdir -p ~/$destDir")

            put(hashMapOf("from" to "${project.projectDir}/build/$archiveFilename", "into" to destDir))

            println("done")
        })
    })
}