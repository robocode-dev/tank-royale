package dev.robocode.tankroyale.gui.ui.menu

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import java.io.File
import java.nio.file.Files
import java.nio.file.Path
import java.util.Properties

/** Every menu entry the menu bar adds has its text in every language. */
@Tag("Unit")
class MenuTextsTest {

    private val resources = listOf(Path.of("src", "main", "resources"), Path.of("gui", "src", "main", "resources")).first { Files.isDirectory(it) }
    private val sources = listOf(Path.of("src", "main", "kotlin"), Path.of("gui", "src", "main", "kotlin")).first { Files.isDirectory(it) }

    private fun texts(name: String) = Properties().apply { File(resources.toFile(), name).reader(Charsets.UTF_8).use { load(it) } }

    @Test
    fun everyMenuEntryHasItsTextInEveryLanguage() {
        val menu = sources.resolve("dev/robocode/tankroyale/gui/ui/menu/Menu.kt").toFile().readText()
        val keys = Regex("(?:addNewMenuItem|MenuTitles\\.get)\\(\"([a-z_.]+)\"").findAll(menu).map { it.groupValues[1] }.toSet()
        assertTrue(keys.containsAll(listOf("menu.feedback", "item.report_problem", "item.suggest_feature")), "the Help menu has Feedback with its two entries")
        for (language in listOf("", "_da", "_es", "_ca")) {
            val names = texts("Menu$language.properties")
            assertEquals(emptySet<String>(), keys.filter { names.getProperty(it).isNullOrBlank() }.toSet(), "missing in Menu$language")
        }
    }
}
