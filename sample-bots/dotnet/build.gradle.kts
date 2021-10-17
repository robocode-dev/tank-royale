import java.nio.file.Path
import java.nio.file.Paths
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
        list(project.projectDir.toPath()).forEach { projectDir ->
            run {
                if (isDirectory(projectDir) && isBotProjectDir(projectDir)) {
                    val botArchivePath: Path = archiveDir.resolve(projectDir.botName())

                    createDir(botArchivePath)
                    copyBotFiles(projectDir, botArchivePath)
                    createCmdFile(projectDir, botArchivePath)
                }
            }
        }
    }

    private fun Path.botName(): String {
        return fileName.toString()
    }

    private fun isBotProjectDir(dir: Path): Boolean {
        val botName = dir.botName()
        return !botName.startsWith(".") && botName != "build"
    }

    private fun copyBotFiles(projectDir: Path, botArchivePath: Path) {
        for (file in list(projectDir)) {
            copy(file, botArchivePath.resolve(file.fileName))
        }
    }

    private fun createCmdFile(projectDir: Path, botArchivePath: Path) {
        val botName = projectDir.botName()
        val file = botArchivePath.resolve("$botName.cmd").toFile()
        val printWriter = object : PrintWriter(file) {
            override fun println() {
                write("\r\n") // Windows Carriage Return + New-line
            }
        }
        printWriter.use {
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

    archiveFileName.set("robocode-tankoyale-sample-bots-dotnet-${project.version}.zip")
    destinationDirectory.set(buildDir)

    from(File(buildDir, "archive"))
}
