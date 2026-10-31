package dev.robocode.tankroyale.botproject

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import java.nio.file.Files
import java.nio.file.Path
import kotlin.streams.toList

class BotProjectDependencyRulesTest {

    private val forbiddenImports = listOf(
        "javax.swing",
        "java.awt",
        "java.net.http",
        "dev.robocode.tankroyale.gui",
        "dev.robocode.tankroyale.runner",
        "dev.robocode.tankroyale.rumble",
    )

    /** Returns one message per source line that imports something this library must not use. */
    private fun violations(sources: Map<String, String>): List<String> =
        sources.flatMap { (file, text) ->
            text.lines().mapIndexedNotNull { index, line ->
                val imported = line.trim().takeIf { it.startsWith("import ") }?.removePrefix("import ")
                forbiddenImports.firstOrNull { imported != null && imported.startsWith(it) }
                    ?.let { "$file:${index + 1} imports $it" }
            }
        }

    @Test
    @Tag("RBC-019")
    @Tag("Unit")
    @Tag("Positive")
    fun testRBC019_UnitPositive_theLibrarySourcesImportNoDesktopRunnerRumbleOrNetworkCode() {
        val sourceRoot = Path.of("src", "main")
        val sources = Files.walk(sourceRoot).use { paths ->
            paths.filter { it.toString().endsWith(".kt") || it.toString().endsWith(".java") }
                .toList()
                .associate { it.toString() to Files.readString(it) }
        }

        assertTrue(sources.isNotEmpty(), "No library sources were found under $sourceRoot")
        assertEquals(emptyList<String>(), violations(sources))
    }

    @Test
    @Tag("RBC-019")
    @Tag("Unit")
    @Tag("Negative")
    fun testRBC019_UnitNegative_aDeliberateViolationIsReported() {
        val offending = mapOf(
            "A.kt" to "package x\nimport javax.swing.JPanel\nclass A",
            "B.kt" to "import java.net.http.HttpClient\nimport dev.robocode.tankroyale.rumble.RumbleEnvironments",
            "C.kt" to "import dev.robocode.tankroyale.gui.MainFrame",
            "D.kt" to "// import java.awt.Color is only a comment here\nclass D",
        )

        assertEquals(
            listOf(
                "A.kt:2 imports javax.swing",
                "B.kt:1 imports java.net.http",
                "B.kt:2 imports dev.robocode.tankroyale.rumble",
                "C.kt:1 imports dev.robocode.tankroyale.gui",
            ),
            violations(offending),
        )
    }
}
