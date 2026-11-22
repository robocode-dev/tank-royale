package dev.robocode.tankroyale.gui.ui.rumble

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import java.io.File
import java.nio.file.Files
import java.nio.file.Path
import java.util.Properties

/** The Get started screen is the first entry of the Rumble menu, and every text it shows is in every language. */
class RumbleGetStartedTextsTest {

    private val resources = listOf(Path.of("src", "main", "resources"), Path.of("gui", "src", "main", "resources")).first { Files.isDirectory(it) }
    private val sources = listOf(Path.of("src", "main", "kotlin"), Path.of("gui", "src", "main", "kotlin")).first { Files.isDirectory(it) }

    private fun texts(name: String) = Properties().apply { File(resources.toFile(), name).reader(Charsets.UTF_8).use { load(it) } }
    private fun source(path: String) = sources.resolve("dev/robocode/tankroyale/gui/ui/$path").toFile().readText()

    // The card keys are built from the card's name, so they are listed here; the others are read from the screen's source.
    private val cardKeys = listOf("make", "submit", "contribute").flatMap { card -> listOf("title", "text", "button").map { "rumble.getstarted.$card.$it" } }
    private val sourceKeys = Regex("\"(rumble\\.getstarted\\.[a-z_.]+)\"").findAll(source("rumble/RumbleGetStartedDialog.kt")).map { it.groupValues[1] }.toSet()

    @Test
    @Tag("RCL-141")
    @Tag("Unit")
    @Tag("Positive")
    fun testRCL141_UnitPositive_everyTextOfTheScreenItsTitleAndItsMenuEntryIsInEveryLanguage() {
        assertTrue("rumble.getstarted.beta" in sourceKeys && "rumble.getstarted.intro" in sourceKeys, "the screen's own keys are found")
        for (language in listOf("", "_da", "_es", "_ca")) {
            val messages = texts("Messages$language.properties")
            assertEquals(emptySet<String>(), (cardKeys + sourceKeys).toSet() - messages.stringPropertyNames(), "missing in Messages$language")
            val title = texts("UI titles$language.properties").getProperty("rumble_get_started_dialog")
            val entry = texts("Menu$language.properties").getProperty("item.rumble_get_started")
            assertFalse(title.isNullOrBlank(), "title in $language")
            assertFalse(entry.isNullOrBlank(), "menu entry in $language")
            // No long dashes in texts a person reads.
            for (text in (cardKeys + sourceKeys).map { messages.getProperty(it).orEmpty() } + title.orEmpty() + entry.orEmpty()) {
                assertFalse('—' in text || '–' in text, "long dash in \"$text\" ($language)")
            }
        }
    }

    @Test
    @Tag("RCL-141")
    @Tag("Unit")
    @Tag("Negative")
    fun testRCL141_UnitNegative_noOtherRumbleEntryComesBeforeGetStarted() {
        val menu = source("menu/Menu.kt")
        val entries = Regex("addNewMenuItem\\(\"(item\\.rumble_[a-z_]+)\"").findAll(menu).map { it.groupValues[1] }.toList()
        assertEquals("item.rumble_get_started", entries.first())
        assertEquals(1, entries.count { it == "item.rumble_get_started" })
    }
}
