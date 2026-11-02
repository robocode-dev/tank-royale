package dev.robocode.tankroyale.rumble.worker

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File
import java.nio.file.Files
import java.nio.file.Path
import java.util.jar.JarFile
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

/**
 * Looks at what the worker's process really loads, instead of trusting the build file. It guards against mistakes, such
 * as a dependency that pulls the host library in; it cannot catch a copy made to hide.
 */
class WorkerClasspathTest {

    /** Package prefixes, as folder names, that the worker must never carry. */
    private val forbidden = listOf(
        "dev/robocode/tankroyale/botproject/",
        "dev/robocode/tankroyale/gui/",
        "dev/robocode/tankroyale/rumble/Registration",
        "dev/robocode/tankroyale/rumble/GitHub",
        "dev/robocode/tankroyale/rumble/SignIn",
        "dev/robocode/tankroyale/rumble/SavedSignIn",
        "dev/robocode/tankroyale/rumble/BotSubmission",
        "dev/robocode/tankroyale/rumble/WorkerHost",
        "org/kohsuke/github/",
        "org/eclipse/jgit/",
    )

    /** The entries of [classpath] that hold something forbidden, as "entry: item". */
    private fun findForbidden(classpath: String): List<String> {
        val found = mutableListOf<String>()
        for (entry in classpath.split(File.pathSeparator).filter { it.isNotBlank() }) {
            val file = File(entry)
            val names = when {
                file.isDirectory -> file.walkTopDown().filter { it.isFile }.map { it.relativeTo(file).path.replace('\\', '/') }.toList()
                file.isFile && file.extension == "jar" -> JarFile(file).use { jar -> jar.entries().asSequence().map { it.name }.toList() }
                else -> emptyList()
            }
            names.firstOrNull { name -> forbidden.any { name.startsWith(it) } }?.let { found += "$entry: $it" }
        }
        return found
    }

    // RBW-020

    @Test
    @Tag("RBW-020")
    @Tag("Unit")
    @Tag("Positive")
    fun testRBW020_UnitPositive_theWorkersRuntimeClasspathHoldsNoHostBotProjectDesktopOrGitHubCode() {
        val classpath = System.getProperty("rumble.worker.classpath") ?: error("System property 'rumble.worker.classpath' not set")

        assertTrue(classpath.split(File.pathSeparator).size > 3, "The classpath must hold the worker, the runner and their libraries: $classpath")
        assertTrue(findForbidden(classpath).isEmpty(), "The worker carries forbidden code: ${findForbidden(classpath)}")
    }

    @Test
    @Tag("RBW-020")
    @Tag("Unit")
    @Tag("Negative")
    fun testRBW020_UnitNegative_aClasspathWithForbiddenCodeInAFolderOrAJarIsReportedWithItsEntry(@TempDir dir: Path) {
        val folder = Files.createDirectories(dir.resolve("classes/dev/robocode/tankroyale/rumble"))
        Files.writeString(folder.resolve("GitHubApi.class"), "x")
        val jar = dir.resolve("lib.jar")
        ZipOutputStream(Files.newOutputStream(jar)).use { zip ->
            zip.putNextEntry(ZipEntry("dev/robocode/tankroyale/botproject/BotStaging.class"))
            zip.write(1)
            zip.closeEntry()
        }
        val clean = Files.createDirectories(dir.resolve("clean/dev/robocode/tankroyale/rumble/worker"))
        Files.writeString(clean.resolve("Worker.class"), "x")

        val found = findForbidden(listOf(dir.resolve("classes"), jar, dir.resolve("clean")).joinToString(File.pathSeparator))

        assertEquals(2, found.size, found.toString())
        assertTrue(found.any { it.contains("classes") && it.contains("GitHubApi.class") })
        assertTrue(found.any { it.contains("lib.jar") && it.contains("BotStaging.class") })
    }
}
