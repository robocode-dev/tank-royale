package dev.robocode.tankroyale.rumble

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import java.nio.file.Files
import java.nio.file.Path
import kotlin.streams.toList

class RumbleDependencyRulesTest {

    private val forbiddenImports = listOf(
        "javax.swing",
        "java.awt",
        "dev.robocode.tankroyale.gui",
        "dev.robocode.tankroyale.runner",
    )

    /** Returns one message per source line that imports something the shared library must not use. */
    private fun violations(sources: Map<String, String>): List<String> =
        sources.flatMap { (file, text) ->
            text.lines().mapIndexedNotNull { index, line ->
                val imported = line.trim().removePrefix("import ").takeIf { line.trim().startsWith("import ") }
                forbiddenImports.firstOrNull { imported != null && imported.startsWith(it) }
                    ?.let { "$file:${index + 1} imports $it" }
            }
        }

    @Test
    @Tag("RCL-020")
    @Tag("Unit")
    @Tag("Positive")
    fun testRCL020_UnitPositive_theLibrarySourcesImportNoDesktopOrRunnerCode() {
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
    @Tag("RCL-020")
    @Tag("Unit")
    @Tag("Negative")
    fun testRCL020_UnitNegative_aDeliberateViolationIsReported() {
        val offending = mapOf(
            "A.kt" to "package x\nimport javax.swing.JPanel\nclass A",
            "B.kt" to "import java.awt.Color\nimport dev.robocode.tankroyale.gui.MainFrame",
            "C.kt" to "import dev.robocode.tankroyale.runner.BattleRunner",
            "D.kt" to "// import javax.swing.JPanel is only a comment here\nclass D",
        )

        assertEquals(
            listOf(
                "A.kt:2 imports javax.swing",
                "B.kt:1 imports java.awt",
                "B.kt:2 imports dev.robocode.tankroyale.gui",
                "C.kt:1 imports dev.robocode.tankroyale.runner",
            ),
            violations(offending),
        )
    }
}
