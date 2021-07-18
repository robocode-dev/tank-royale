import java.nio.file.Path
import java.nio.file.Paths
import java.nio.file.Files

defaultTasks("clean", "build")

val clean = tasks.register<Delete>("clean") {
    delete(project.buildDir)
}

tasks.register("build") {
    dependsOn(clean)
    dependsOn(zipSampleBots)
}

abstract class BaseTask : DefaultTask() {

    @Internal
    protected val archiveDir: Path = project.buildDir.toPath().resolve("archive")

    @Internal
    protected val libDir: Path = archiveDir.resolve("lib")

    protected fun createDir(path: Path) {
        if (!Files.exists(path)) {
            Files.createDirectory(path)
        }
    }

    protected fun deleteDir(path: Path) {
        if (Files.exists(path)) {
            Files.walk(path)
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
            Files.list(libDir).filter { path ->
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
        Files.list(project.projectDir.toPath()).forEach { projectDir ->
            run {
                if (Files.isDirectory(projectDir) && isBotProjectDir(projectDir)) {
                    copyBotJavaFiles(projectDir)
                    copyBotJsonFile(projectDir)
                    createCmdFile(projectDir)
                    createShFile(projectDir)
                }
            }
        }
    }

    private fun isBotProjectDir(dir: Path): Boolean {
        val filename = dir.fileName.toString()
        return !filename.startsWith(".") && filename != "build"
    }

    private fun copyBotJavaFiles(projectDir: Path) {
        val srcRoot = projectDir.resolve("src/main/java")
        for (file in Files.list(srcRoot)) {
            Files.copy(file, archiveDir.resolve(file.fileName))
        }
    }

    private fun copyBotJsonFile(projectDir: Path) {
        val filename = projectDir.fileName.toString() + ".json"
        val jsonFilePath = projectDir.resolve("src/main/resources/$filename")
        Files.copy(jsonFilePath, archiveDir.resolve(filename))
    }

    private fun createCmdFile(projectDir: Path) {
        val filename = projectDir.fileName.toString()
        val printWriter = object : java.io.PrintWriter(archiveDir.resolve("$filename.cmd").toFile()) {
            override fun println() {
                write("\r\n") // Windows Carriage Return + New-line
            }
        }

        printWriter.use {
            it.println("java -cp lib/${project.extra["botApiJarFilename"]} $filename.java")
            it.close()
        }
    }

    private fun createShFile(projectDir: Path) {
        val filename = projectDir.fileName.toString()
        val printWriter = object : java.io.PrintWriter(archiveDir.resolve("$filename.sh").toFile()) {
            override fun println() {
                write("\n") // Unix New-line
            }
        }
        printWriter.use {
            it.println("#!/bin/sh")
            it.println("java -cp lib/${project.extra["botApiJarFilename"]} $filename.java")
            it.close()
        }
    }
}

val copyBotFiles = task<CopyBotFiles>("copyBotFiles") {
    dependsOn(findBotApiJarFilename)
}

abstract class CopyRobocodeIcon : BaseTask() {
    @TaskAction
    fun copyIcon() {
        Files.copy(project.projectDir.toPath().resolve("../../gfx/Tank/Tank.ico"), archiveDir.resolve("robocode.ico"))
    }
}

val copyRobocodeIcon = tasks.register<CopyRobocodeIcon>("copyRobocodeIcon") {}

val zipSampleBots = task<Zip>("zipSampleBots") {
    dependsOn(copyBotFiles)
    dependsOn(copyRobocodeIcon)

    archiveFileName.set("robocode-tankoyale-sample-bots.zip")
    destinationDirectory.set(Paths.get(System.getProperty("user.dir")).resolve("build").toFile())

    from(Paths.get(System.getProperty("user.dir")).resolve("build/archive").toFile())
}
