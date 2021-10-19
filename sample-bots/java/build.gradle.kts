import java.nio.file.Path
import java.nio.file.Paths
import java.nio.file.Files.*
import java.io.PrintWriter

version = project(":bot-api:java").version

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
    include("java-*.jar")
    exclude("*javadoc*")
    rename("^.*(\\d\\.\\d\\.\\d)\\.jar", "robocode-tankroyale-bot-api-$1.jar")
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
                    createCmdFile(botDir, botArchivePath)
                    createShFile(botDir, botArchivePath)
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
        var filename = "ReadMe.md"
        copy(File(projectDir, "assets/$filename").toPath(), archivePath.resolve(filename))
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
            it.println("java -cp ../lib/* $botName.java")
            it.close()
        }
    }

    private fun createShFile(projectDir: Path, botArchivePath: Path) {
        val botName = projectDir.botName()
        val file = botArchivePath.resolve("$botName.sh").toFile()
        val printWriter = object : PrintWriter(file) {
            override fun println() {
                write("\n") // Unix New-line
            }
        }
        printWriter.use {
            it.println("#!/bin/sh")
            it.println("java -cp ../lib/* $botName.java")
            it.close()
        }
    }
}

val copyBotFiles = task<CopyBotFiles>("copyBotFiles") {
    dependsOn(findBotApiJarFilename)
}

val zipSampleBots = task<Zip>("zipSampleBots") {
    dependsOn(copyBotFiles)

    archiveFileName.set("sample-bots-java-${project.version}.zip")
    destinationDirectory.set(buildDir)

    from(File(buildDir, "archive"))
}
