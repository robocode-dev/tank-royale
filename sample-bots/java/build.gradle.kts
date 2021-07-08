import java.nio.file.Path
import java.nio.file.Paths
import java.nio.file.Files


abstract class BaseTask : DefaultTask() {
    @Internal
    protected val cwd: Path = Paths.get(System.getProperty("user.dir"))

    @Internal
    protected val archiveDir: Path = project.buildDir.toPath().resolve("archive")

    @Internal
    protected val libsDir: Path = archiveDir.resolve("libs")

    @Internal
    protected lateinit var botApiJarFilename: String

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

val clean = tasks.register<Delete>("clean") {
    delete(project.buildDir)
}

abstract class JavaSampleBotsTask : BaseTask() {
    @TaskAction
    fun build() {
        Files.list(cwd).forEach { projectDir ->
            run {
                if (Files.isDirectory(projectDir) && isBotProjectDir(projectDir)) {
                    copyBotJar(projectDir)
                    copyBotJsonFile(projectDir)
                    createCmdFile(projectDir)
                    createShFile(projectDir)
                    createCommandFile(projectDir)
                }
            }
        }

        copyIcon()
    }

    private fun isBotProjectDir(dir: Path): Boolean {
        val filename = dir.fileName.toString()
        return !filename.startsWith(".") && filename != "build"
    }

    private fun copyBotJar(projectDir: Path) {
        val jarFilename = getBotJarPath(projectDir)
        Files.copy(jarFilename, libsDir.resolve(jarFilename.fileName))
    }

    private fun getBotJarPath(projectDir: Path): Path {
        val archiveDir: Path = projectDir.resolve("build/libs")
        for (dir in Files.list(archiveDir)) {
            if (dir.startsWith(projectDir)) {
                return archiveDir.resolve(dir)
            }
        }
        throw IllegalStateException("Could not find jar archive in dir: $projectDir")
    }

    private fun copyBotJsonFile(projectDir: Path) {
        val filename = projectDir.fileName.toString() + ".json"
        val jsonFilePath = projectDir.resolve("src/main/resources/$filename")
        Files.copy(jsonFilePath, archiveDir.resolve(filename))
    }

    private fun createCmdFile(projectDir: Path) {
        val filename = projectDir.fileName.toString() + ".cmd"
        val printWriter = object : java.io.PrintWriter(archiveDir.resolve(filename).toFile()) {
            override fun println() {
                write("\r\n") // Windows Carriage Return + New-line
            }
        }

        printWriter.use {
            val jarFilename = getBotJarPath(projectDir).fileName
            val className = "dev.robocode.tankroyale.sample.bots." + projectDir.fileName.toString()
            it.println("java -cp libs/$jarFilename;libs/robocode-tankroyale-bot-api-0.9.8.jar $className")
            it.close()
        }
    }

    private fun createShFile(projectDir: Path) {
        val filename = projectDir.fileName.toString() + ".sh"
        val printWriter = object : java.io.PrintWriter(archiveDir.resolve(filename).toFile()) {
            override fun println() {
                write("\n") // Unix New-line
            }
        }
        printWriter.use {
            it.println("#!/bin/sh")
            val jarFilename = getBotJarPath(projectDir).fileName
            val className = "dev.robocode.tankroyale.sample.bots." + projectDir.fileName.toString()
            it.println("java -cp libs/$jarFilename:libs/robocode-tankroyale-bot-api-0.9.8.jar $className")
            it.close()
        }
    }

    private fun createCommandFile(projectDir: Path) {
        val filename = projectDir.fileName.toString() + ".command"
        val printWriter = object : java.io.PrintWriter(archiveDir.resolve(filename).toFile()) {
            override fun println() {
                write("\n") // OS-X New-line
            }
        }
        printWriter.use {
            it.println("#!/bin/sh")
            val jarFilename = getBotJarPath(projectDir).fileName
            val name = projectDir.fileName.toString()
            val className = "dev.robocode.tankroyale.sample.bots.$name"
            val xdockIconAndName = "-Xdock:icon=robocode.ico -Xdock:name=$name"
            it.println("java $xdockIconAndName -cp libs/$jarFilename:libs/robocode-tankroyale-bot-api-0.9.8.jar $className")
            it.close()
        }
    }

    private fun copyIcon() {
        Files.copy(cwd.resolve("../../gfx/Tank/Tank.ico"), archiveDir.resolve("robocode.ico"))
    }
}

abstract class CreateDirsTask : BaseTask() {
    @TaskAction
    fun build() {
        createDir(project.buildDir.toPath())
        createDir(archiveDir)
        createDir(libsDir)
    }
}

abstract class PrepareBotApiJarFilenameTask : BaseTask() {
    @TaskAction
    fun build() {
        botApiJarFilename =
            Files.list(libsDir).filter { path ->
                path.fileName.toString().startsWith("robocode-tankroyale-bot-api")
            }.findFirst().get().fileName.toString()
    }
}

task<CreateDirsTask>("createDirs") {}

task<Copy>("copyBotApiJar") {
    dependsOn("createDirs")

    from(project(":bot-api:java").file("build/libs"))
    into(Paths.get(System.getProperty("user.dir")).resolve("build/archive/libs"))
    include("robocode-tankroyale-bot-api-*.jar")
    exclude("*javadoc*")
}

task<PrepareBotApiJarFilenameTask>("prepareBotApiJarFilenameTask") {
    dependsOn("copyBotApiJar")
}

task<JavaSampleBotsTask>("copyBotsToArchiveDir") {
    dependsOn("prepareBotApiJarFilenameTask")

    dependsOn(":sample-bots:java:Corners:build")
    dependsOn(":sample-bots:java:Crazy:build")
    dependsOn(":sample-bots:java:Fire:build")
    dependsOn(":sample-bots:java:MyFirstBot:build")
    dependsOn(":sample-bots:java:RamFire:build")
    dependsOn(":sample-bots:java:SpinBot:build")
    dependsOn(":sample-bots:java:Target:build")
    dependsOn(":sample-bots:java:TrackFire:build")
    dependsOn(":sample-bots:java:Walls:build")
}

task<Zip>("zipSampleBots") {
    dependsOn("copyBotsToArchiveDir")

    archiveFileName.set("robocode-tankoyale-sample-bots.zip")
    destinationDirectory.set(Paths.get(System.getProperty("user.dir")).resolve("build").toFile())

    from(Paths.get(System.getProperty("user.dir")).resolve("build/archive").toFile())
}

tasks.register("build") {
    dependsOn(clean)
    dependsOn("zipSampleBots")
}
