package dev.robocode.tankroyale.rumble.cli

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import java.nio.file.Files
import java.nio.file.Path

/**
 * The desktop program calls the shared Rumble libraries in its own process and never depends on the Rumble command
 * line or the battle worker. The real dependency graph and the sources are also checked by the desktop build's
 * `checkRumbleDependencyRules`; this test proves that the rule reports a deliberate violation.
 */
class RumbleCliDependencyRulesTest {

    private val root = Path.of(System.getProperty("repo.root") ?: error("System property 'repo.root' not set — run via :rumble-cli:test"))

    private val forbiddenProjects = listOf(":rumble-cli", ":rumble")
    private val forbiddenPackages = listOf("dev.robocode.tankroyale.rumble.cli.", "dev.robocode.tankroyale.rumble.worker.")

    private val blockComment = Regex("/\\*.*?\\*/", RegexOption.DOT_MATCHES_ALL)
    private val lineComment = Regex("(^|\\s)//.*$")

    /** The text without block comments and without line comments that start at a word boundary, so a web address stays. */
    private fun withoutComments(text: String): String =
        text.replace(blockComment) { match -> match.value.filter { it == '\n' } }
            .lines().joinToString("\n") { line -> line.replace(lineComment, "$1") }

    /** One message per declared dependency or mention of a package that the desktop program must not have. */
    private fun violations(buildFile: String, sources: Map<String, String>): List<String> {
        val declared = withoutComments(buildFile).lines().mapIndexedNotNull { index, line ->
            forbiddenProjects.firstOrNull { Regex("project\\(\\s*\"" + Regex.escape(it) + "\"\\s*\\)").containsMatchIn(line) }
                ?.let { "build.gradle.kts:${index + 1} depends on $it" }
        }
        val named = sources.flatMap { (file, text) ->
            withoutComments(text).lines().mapIndexedNotNull { index, line ->
                forbiddenPackages.firstOrNull { line.contains(it) }?.let { "$file:${index + 1} names $it" }
            }
        }
        return declared + named
    }

    // RCL-049

    @Test
    @Tag("RCL-049")
    @Tag("Unit")
    @Tag("Positive")
    fun testRCL049_UnitPositive_theDesktopProgramDependsOnAndNamesNeitherTheCommandLineNorTheWorker() {
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
    fun testRCL049_UnitNegative_everyDeliberateViolationIsReportedWithWhereItIsAndCommentsAreIgnored() {
        val build = listOf(
            "dependencies {",
            "    implementation(project(\":lib:client\"))",
            "    implementation(project(\":rumble-cli\"))",
            "    // implementation(project(\":rumble\")) is only a comment",
            "    runtimeOnly(project( \":rumble\" ))",
            "    val web = \"https://example.org\"; implementation(project(\":rumble-cli\"))",
            "    /*",
            "    implementation(project(\":rumble\"))",
            "    */",
            "}",
        ).joinToString("\n")
        val sources = mapOf(
            "A.kt" to "package x\nimport dev.robocode.tankroyale.rumble.cli.RumbleCli\nimport dev.robocode.tankroyale.rumble.SignInFlow\n",
            "B.kt" to "import dev.robocode.tankroyale.rumble.worker.MainKt\n",
            "C.kt" to "val x = dev.robocode.tankroyale.rumble.cli.RumbleCli\n/* import dev.robocode.tankroyale.rumble.worker.Ignored\n */\n// dev.robocode.tankroyale.rumble.cli.Ignored\n",
        )

        assertEquals(
            listOf(
                "build.gradle.kts:3 depends on :rumble-cli",
                "build.gradle.kts:5 depends on :rumble",
                "build.gradle.kts:6 depends on :rumble-cli",
                "A.kt:2 names dev.robocode.tankroyale.rumble.cli.",
                "B.kt:1 names dev.robocode.tankroyale.rumble.worker.",
                "C.kt:1 names dev.robocode.tankroyale.rumble.cli.",
            ),
            violations(build, sources),
        )
    }
}
