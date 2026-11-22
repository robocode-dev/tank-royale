package dev.robocode.tankroyale.gui.ui.rumble

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import java.io.File
import java.nio.file.Files
import java.nio.file.Path
import java.util.Properties

/** The Rumble menu lists its entries in the order of the mockups, and every entry has its text in every language. */
class RumbleMenuOrderTest {

    private val resources = listOf(Path.of("src", "main", "resources"), Path.of("gui", "src", "main", "resources")).first { Files.isDirectory(it) }
    private val sources = listOf(Path.of("src", "main", "kotlin"), Path.of("gui", "src", "main", "kotlin")).first { Files.isDirectory(it) }

    // The mockups' order of the whole menu; My bots and Results and queue are not built yet.
    private val mockup = listOf(
        "item.rumble_get_started", "item.rumble_submit", "item.rumble_my_bots", "item.rumble_download", "item.rumble_contribute",
        "item.rumble_results", "item.rumble_rankings", "item.rumble_account", "item.rumble_help",
    )

    private fun entries(): List<String> {
        val menu = sources.resolve("dev/robocode/tankroyale/gui/ui/menu/Menu.kt").toFile().readText()
        return Regex("addNewMenuItem\\(\"(item\\.rumble_[a-z_]+)\"").findAll(menu).map { it.groupValues[1] }.toList()
    }

    private fun texts(name: String) = Properties().apply { File(resources.toFile(), name).reader(Charsets.UTF_8).use { load(it) } }

    @Test
    @Tag("RCL-145")
    @Tag("Unit")
    @Tag("Positive")
    fun testRCL145_UnitPositive_theBuiltEntriesFollowTheMockupsOrderAndHaveTheirTextInEveryLanguage() {
        val built = entries()
        assertEquals(mockup.filter { it in built }, built)
        assertEquals(listOf("item.rumble_rankings", "item.rumble_account", "item.rumble_help"), built.takeLast(3))
        for (language in listOf("", "_da", "_es", "_ca")) {
            val names = texts("Menu$language.properties")
            for (key in built) assertFalse(names.getProperty(key).isNullOrBlank(), "$key in Menu$language")
        }
    }

    @Test
    @Tag("RCL-145")
    @Tag("Unit")
    @Tag("Negative")
    fun testRCL145_UnitNegative_noEntryIsOutsideTheMockupsOrListedTwice() {
        val built = entries()
        assertEquals(emptyList<String>(), built.filter { it !in mockup })
        assertEquals(built.size, built.toSet().size)
    }
}
