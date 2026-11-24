package dev.robocode.tankroyale.rumble.cli

import dev.robocode.tankroyale.rumble.SessionBoot
import dev.robocode.tankroyale.rumble.SessionMenuItem
import dev.robocode.tankroyale.rumble.SessionStatus
import dev.robocode.tankroyale.rumble.SessionView
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
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
        addAll(listOf("session.window.battle", "session.window.no_battle", "session.window.not_kept", "session.window.results", "session.window.starting", "session.tip.starting"))
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

    private fun status(boot: SessionBoot?) = SessionStatus(
        environment = "test", pid = 1, startedAt = "2026-11-24T10:00:00Z", state = "running", battleLimit = 10, kept = 3,
        updatedAt = "2026-11-24T10:01:00Z", boot = boot,
    )

    @Test
    @Tag("RCL-161")
    @Tag("Unit")
    @Tag("Positive")
    fun testRCL161_UnitPositive_theWindowAndTheTipSayHowManyBotsHaveConnectedAndHowLongTheBattleHasWaitedInEveryLanguage() {
        val expected = mapOf(
            "en" to "Starting the bots: 2 of 4 connected, 18 s of 90 s",
            "da" to "Bots starter: 2 af 4 forbundet, 18 s af 90 s",
            "es" to "Iniciando los bots: 2 de 4 conectados, 18 s de 90 s",
            "ca" to "Iniciant els bots: 2 de 4 connectats, 18 s de 90 s",
        )
        for ((language, line) in expected) {
            val texts = SessionTexts(language)
            val status = status(SessionBoot(connected = 2, expected = 4, elapsedSeconds = 18, limitSeconds = 90))
            assertEquals(line, texts.starting(status))
            // The tip has the title, where the session stands and the count, one under the other and short enough for Windows.
            val tip = texts.tip(status).lines()
            assertEquals(listOf(texts.get("session.title"), texts.headline(status)), tip.take(2))
            assertTrue(tip[2].contains("2") && tip[2].contains("4") && !tip[2].contains("18"), tip[2])
            assertTrue(texts.tip(status).length <= 127, "$language tip is ${texts.tip(status).length} characters")
            assertFalse('—' in line || '–' in line)
        }
    }

    @Test
    @Tag("RCL-161")
    @Tag("Unit")
    @Tag("Negative")
    fun testRCL161_UnitNegative_aStatusWithoutStartingBotsShowsWhatItShowedBefore() {
        val texts = SessionTexts("da")
        val plain = status(null)
        assertNull(texts.starting(plain))
        assertEquals(listOf(texts.get("session.title"), texts.headline(plain)), texts.tip(plain).lines())
        assertTrue(texts.tip(plain).lines().none { it.startsWith("Bots starter") })
    }
}
