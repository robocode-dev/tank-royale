package dev.robocode.tankroyale.rumble.cli

import dev.robocode.tankroyale.rumble.SessionMenuItem
import dev.robocode.tankroyale.rumble.SessionView
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import java.util.Locale
import java.util.Properties

/** Every text of a session's tray icon and window is in every language, and a language never stands in for another. */
class RumbleCliSessionViewTest {

    private fun texts(suffix: String) = Properties().apply {
        RumbleCliSessionViewTest::class.java.getResourceAsStream("/dev/robocode/tankroyale/rumble/cli/SessionView$suffix.properties")!!
            .reader(Charsets.UTF_8).use { load(it) }
    }

    private val keys = buildList {
        add("session.title")
        SessionView.STATES.forEach { add("session.headline.limit.$it"); add("session.headline.$it") }
        SessionMenuItem.entries.forEach { add("session.menu.${it.name.lowercase()}") }
        addAll(listOf("session.window.battle", "session.window.no_battle", "session.window.not_kept", "session.window.results"))
        addAll(listOf("session.notice.title", "session.notice.text"))
    }

    @Test
    @Tag("RCL-157")
    @Tag("Unit")
    @Tag("Positive")
    fun testRCL157_UnitPositive_everyTextOfTheTrayAndTheWindowIsInEveryLanguage() {
        for (suffix in listOf("", "_da", "_es", "_ca")) {
            val read = texts(suffix)
            assertEquals(emptyList<String>(), keys.filter { read.getProperty(it).isNullOrBlank() }, "missing in SessionView$suffix")
            for (key in keys) {
                val text = read.getProperty(key)
                assertFalse('—' in text || '–' in text, "long dash in $key$suffix")
            }
            // The headline with a limit takes the kept battles and the limit; without one only the kept battles.
            assertEquals(2, Regex("%d").findAll(read.getProperty("session.headline.limit.running")).count())
            assertEquals(1, Regex("%d").findAll(read.getProperty("session.headline.running")).count())
        }
    }

    @Test
    @Tag("RCL-157")
    @Tag("Unit")
    @Tag("Negative")
    fun testRCL157_UnitNegative_theComputersOwnLanguageNeverStandsInForTheChosenOne() {
        val before = Locale.getDefault()
        try {
            Locale.setDefault(Locale.forLanguageTag("da"))
            assertEquals("Show status", SessionTexts("en").menu(SessionMenuItem.SHOW_STATUS))
            assertNotEquals(SessionTexts("en").menu(SessionMenuItem.STOP_NOW), SessionTexts("da").menu(SessionMenuItem.STOP_NOW))
        } finally {
            Locale.setDefault(before)
        }
    }
}
