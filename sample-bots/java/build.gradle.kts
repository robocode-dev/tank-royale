import java.nio.file.Path
import java.nio.file.Paths
import java.nio.file.Files
import java.io.File

import java.util.Comparator


abstract class JavaSampleBotsTask : DefaultTask() {

    @TaskAction
    fun task() {
        val currentWorkingDir = Paths.get(System.getProperty("user.dir"))

        val archiveDir = currentWorkingDir.resolve(".archive")
        deleteDir(archiveDir)
        createDir(archiveDir)

        val libsDir = archiveDir.resolve("libs")
        createDir(libsDir)

        val currentWorkDir = Paths.get(System.getProperty("user.dir"))

        Files.list(currentWorkDir).forEach { projectDir -> run {
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
        if (Files.isDirectory(projectDir) && !projectDir.fileName.toString().startsWith(".")) {
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

tasks.register<JavaSampleBotsTask>("build")