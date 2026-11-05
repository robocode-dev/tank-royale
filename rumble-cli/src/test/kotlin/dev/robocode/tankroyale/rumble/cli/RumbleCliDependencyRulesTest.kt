package dev.robocode.tankroyale.rumble.cli

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import java.nio.file.Files
import java.nio.file.Path

/**
 * The desktop program calls the shared Rumble libraries in its own process and never depends on the Rumble command
 * line or the battle worker. The real dependency graph is checked by the desktop build's `checkRumbleDependencyRules`.
 */
class RumbleCliDependencyRulesTest {

    private val root = Path.of(System.getProperty("repo.root") ?: error("System property 'repo.root' not set — run via :rumble-cli:test"))

    private val forbiddenProjects = listOf(":rumble-cli", ":rumble")
    private val forbiddenImports = listOf("dev.robocode.tankroyale.rumble.cli.", "dev.robocode.tankroyale.rumble.worker.")

    /** One message per declared dependency or import that the desktop program must not have. */
    private fun violations(buildFile: String, sources: Map<String, String>): List<String> {
        val declared = buildFile.lines().map { it.replace(Regex("//.*$"), "") }.mapIndexedNotNull { index, line ->
            forbiddenProjects.firstOrNull { Regex("""project\(\s*"${Regex.escape(it)}"\s*\)""").containsMatchIn(line) }
                ?.let { "build.gradle.kts:${index + 1} depends on $it" }
        }
        val imported = sources.flatMap { (file, text) ->
            text.lines().mapIndexedNotNull { index, line ->
                val name = line.trim().takeIf { it.startsWith("import ") }?.removePrefix("import ")?.trim()
                forbiddenImports.firstOrNull { name != null && name.startsWith(it) }?.let { "$file:${index + 1} imports $it" }
            }
        }
        return declared + imported
    }

    // RCL-049

    @Test
    @Tag("RCL-049")
    @Tag("Unit")
    @Tag("Positive")
    fun testRCL049_UnitPositive_theDesktopProgramDependsOnAndImportsNeitherTheCommandLineNorTheWorker() {
        val buildFile = Files.readString(root.resolve("gui/build.gradle.kts"))
        val sources = Files.walk(root.resolve("gui/src")).use { paths ->
            paths.filter { it.toString().endsWith(".kt") || it.toString().endsWith(".java") }
                .collect(java.util.stream.Collectors.toList())
                .associate { root.relativize(it).toString() to Files.readString(it) }
        }

        assertTrue(sources.isNotEmpty(), "No desktop sources were found")
        assertEquals(emptyList<String>(), violations(buildFile, sources))
    }

    @Test
    @Tag("RCL-049")
    @Tag("Unit")
    @Tag("Negative")
    fun testRCL049_UnitNegative_everyDeliberateViolationIsReportedWithWhereItIs() {
        val build = """
            dependencies {
                implementation(project(":lib:client"))
                implementation(project(":rumble-cli"))
                // implementation(project(":rumble")) is only a comment
                runtimeOnly(project( ":rumble" ))
            }
        """.trimIndent()
        val sources = mapOf(
            "A.kt" to "package x\nimport dev.robocode.tankroyale.rumble.cli.RumbleCli\nimport dev.robocode.tankroyale.rumble.SignInFlow\n",
            "B.kt" to "import dev.robocode.tankroyale.rumble.worker.MainKt\n",
        )

        assertEquals(
            listOf(
                "build.gradle.kts:3 depends on :rumble-cli",
                "build.gradle.kts:5 depends on :rumble",
                "A.kt:2 imports dev.robocode.tankroyale.rumble.cli.",
                "B.kt:1 imports dev.robocode.tankroyale.rumble.worker.",
            ),
            violations(build, sources),
        )
    }
}
