import java.nio.file.Path
import java.nio.file.Paths
import java.nio.file.Files
import java.io.File

import java.util.Comparator

abstract class JavaSampleBotsTask : DefaultTask() {

    @TaskAction
    fun build() {
        val cwd = Paths.get(System.getProperty("user.dir"))

        val buildDir = cwd.resolve("build")
        deleteDir(buildDir)
        createDir(buildDir)

        val archiveDir = buildDir.resolve(".archive")
        createDir(archiveDir)

        val libsDir = archiveDir.resolve("libs")
        createDir(libsDir)

        Files.list(cwd).forEach { projectDir -> run {
            copyBotJarArchive(projectDir, libsDir)
        }}

    }

    private fun createDir(path: Path) {
        if (!Files.exists(path)) {
            Files.createDirectory(path)
        }
    }

    private fun deleteDir(path: Path) {
        if (Files.exists(path)) {
            Files.walk(path)
                .sorted(Comparator.reverseOrder())
                .map(({ obj: Path -> obj.toFile() }))
                .forEach(({ obj: File -> obj.delete() }))
        }
    }

    private fun copyBotJarArchive(projectDir: Path, destDir: Path) {
        val filename = projectDir.fileName.toString()

        if (Files.isDirectory(projectDir) && !filename.startsWith(".") && filename != "build") {
            val jarFilename = getJarBotArchiveName(projectDir)
            if (jarFilename != null) {
                Files.copy(jarFilename, destDir.resolve(jarFilename.fileName))
            }
        }
    }

    private fun getJarBotArchiveName(projectDir: Path): Path? {
        val buildDir: Path = Paths.get(System.getProperty("user.dir")).resolve(projectDir).resolve("build/libs")
        for (dir in Files.list(buildDir)) {
            if (dir.startsWith(projectDir)) {
                return buildDir.resolve(dir)
            }
        }
        System.err.println("Could not find jar archive in dir: $projectDir")
        return null
    }
}

task<JavaSampleBotsTask>("copyBotsToArchiveDir")

task<Copy>("copyBotApiJar") {
    dependsOn("copyBotsToArchiveDir")

    from(project(":bot-api:java").file("build/libs/robocode-tankroyale-bot-api-0.9.8.jar"))
    into(Paths.get(System.getProperty("user.dir")).resolve("build/.archive/libs"))
}

tasks.register("build") {
    dependsOn("copyBotApiJar")
}
