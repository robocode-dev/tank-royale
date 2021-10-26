import java.nio.file.Path
import java.nio.file.Files.*
import java.io.PrintWriter

version = project(":bot-api:dotnet").version

defaultTasks("clean", "build")

val clean = tasks.register<Delete>("clean") {
    delete(project.buildDir)
}

tasks.register("build") {
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
                    val botArchivePath: Path = archiveDir.resolve(botDir.botName())

                    createDir(botArchivePath)
                    copyBotFiles(botDir, botArchivePath)
                    createScriptFile(botDir, botArchivePath, "cmd", "\r\n")
                    createScriptFile(botDir, botArchivePath, "ps1", "\r\n")
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

    private fun copyBotFiles(projectDir: Path, botArchivePath: Path) {
        for (file in list(projectDir)) {
            copy(file, botArchivePath.resolve(file.fileName))
        }
    }

    private fun copyReadMeFile(projectDir: File, archivePath: Path) {
        var filename = "ReadMe.md"
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
        printWriter.use {
            if (fileExt == "sh") {
                it.println("#!/bin/sh")
            }
            it.println("dotnet run")
            it.close()
        }
    }
}

val copyBotFiles = task<CopyBotFiles>("copyBotFiles") {
    dependsOn(createDirs)
}

val zipSampleBots = task<Zip>("zipSampleBots") {
    dependsOn(copyBotFiles)

    archiveFileName.set("sample-bots-dotnet-${project.version}.zip")
    destinationDirectory.set(buildDir)

    from(File(buildDir, "archive"))
}
