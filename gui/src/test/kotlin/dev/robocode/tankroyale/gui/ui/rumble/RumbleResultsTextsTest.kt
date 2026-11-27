package dev.robocode.tankroyale.gui.ui.rumble

import dev.robocode.tankroyale.gui.ui.components.RcToolTip
import dev.robocode.tankroyale.rumble.BatchRow
import dev.robocode.tankroyale.rumble.BatchStanding
import dev.robocode.tankroyale.rumble.ResultCounts
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import java.io.File
import java.nio.file.Files
import java.nio.file.Path
import java.util.Properties
import javax.swing.JButton
import javax.swing.JTextArea

/** Results and queue follows Contribute battles in the Rumble menu, every text it shows is in every language, and Open opens only GitHub pages. */
class RumbleResultsTextsTest {

    private val resources = listOf(Path.of("src", "main", "resources"), Path.of("gui", "src", "main", "resources")).first { Files.isDirectory(it) }
    private val sources = listOf(Path.of("src", "main", "kotlin"), Path.of("gui", "src", "main", "kotlin")).first { Files.isDirectory(it) }

    private fun texts(name: String) = Properties().apply { File(resources.toFile(), name).reader(Charsets.UTF_8).use { load(it) } }
    private fun source(path: String) = sources.resolve("dev/robocode/tankroyale/gui/ui/$path").toFile().readText()

    // Keys built from a name are listed here; the others are read from the screen's source.
    private val builtKeys = listOf("kept", "waiting", "sent", "accepted", "rejected", "set_aside").map { "rumble.results.count.$it" } +
        listOf("batch", "battles", "sent", "state").map { "rumble.results.column.$it" } +
        dev.robocode.tankroyale.rumble.SendAgainOutcome.entries.map { "rumble.results.sent_again.${it.name.lowercase()}" }

    @Test
    @Tag("RCL-153")
    @Tag("Unit")
    @Tag("Positive")
    fun testRCL153_UnitPositive_resultsFollowsContributeInTheMenuAndEveryTextIsInEveryLanguage() {
        val entries = Regex("addNewMenuItem\\(\"(item\\.rumble_[a-z_]+)\"").findAll(source("menu/Menu.kt")).map { it.groupValues[1] }.toList()
        assertEquals(entries.indexOf("item.rumble_contribute") + 1, entries.indexOf("item.rumble_results"))
        val inSource = Regex("\"(rumble\\.results\\.[a-z_.]+)\"").findAll(source("rumble/RumbleResultsDialog.kt")).map { it.groupValues[1] }.toSet()
        val wanted = (builtKeys + inSource).toSet() - setOf("rumble.results.open_issue", "rumble.results.batch_id", "rumble.results.sent_again.") // tooltips, in the hint files; a prefix of built keys
        assertTrue("rumble.results.waiting_note" in wanted && "rumble.results.state.rejected" in wanted)
        for (language in listOf("", "_da", "_es", "_ca")) {
            val messages = texts("Messages$language.properties")
            assertEquals(emptySet<String>(), wanted.filter { messages.getProperty(it).isNullOrBlank() }.toSet(), "missing in Messages$language")
            val hint = texts("Hints$language.properties").getProperty("rumble.results.open_issue")
            val idHint = texts("Hints$language.properties").getProperty("rumble.results.batch_id")
            assertTrue(idHint.orEmpty().contains("%s"), "the whole id hint in $language")
            val title = texts("UI titles$language.properties").getProperty("rumble_results_dialog")
            val entry = texts("Menu$language.properties").getProperty("item.rumble_results")
            for ((what, value) in listOf("hint" to hint, "title" to title, "menu entry" to entry)) assertFalse(value.isNullOrBlank(), "$what in $language")
            for (text in wanted.map { messages.getProperty(it).orEmpty() } + listOf(hint, idHint, title, entry).map { it.orEmpty() }) {
                assertFalse('—' in text || '–' in text, "long dash in \"$text\" ($language)")
            }
        }
    }

    @Test
    @Tag("RCL-153")
    @Tag("Unit")
    @Tag("Negative")
    fun testRCL153_UnitNegative_openNeverOpensAPageThatIsNotOnGitHubAndRejectedResultsShowTheirReasons() {
        // The real list and tiles are built, so a layout rule they do not accept fails here, not in front of a person.
        countTiles(ResultCounts(kept = 3, waiting = 1, rejected = 1)) { key, _ -> key }
        val good = "https://github.com/robocode-dev/rumble-data/issues/41"
        val rows = listOf(
            BatchRow("9998f3e6-3aa7-45db-9c69-74697e4a9a4e", 1, null, BatchStanding.WaitingForReceipt, emptyList(), "https://evil.example/issues/42"),
            BatchRow("b1", 2, "2026-11-22T10:00:00Z", BatchStanding.Answered(1, 1, 0), listOf("duplicate battle"), good),
            BatchRow("b0", 1, null, BatchStanding.Answered(0, 0, 1), emptyList(), null),
        )
        val opened = mutableListOf<String>()
        val hints = { key: String -> if (key == "rumble.results.batch_id") "whole id %s" else key }
        val table = resultsBatchTable(rows, { key, arguments -> "$key ${arguments.joinToString()}" }, hints) { opened += it }
        // A long id shows its first part, with the whole id in the program's own tooltip.
        val id = table.components.filterIsInstance<HintLabel>().first()
        assertEquals("9998f3e6", id.text)
        assertEquals("whole id 9998f3e6-3aa7-45db-9c69-74697e4a9a4e", id.toolTipText)
        assertTrue(id.createToolTip() is RcToolTip)
        val open = table.components.filterIsInstance<JButton>()
        assertEquals(listOf(false, true, false), open.map { it.isEnabled }, "Open only for an issue on github.com")
        open.forEach { it.doClick() }
        assertEquals(listOf(good), opened)
        open.forEach { assertTrue(it.createToolTip() is RcToolTip); assertEquals("rumble.results.open_issue", it.toolTipText) }
        val words = table.components.filterIsInstance<JTextArea>().map { it.text }
        assertTrue(words.any { it.startsWith("rumble.results.reasons duplicate battle") }, "the Rumble's reason is shown")
        assertTrue(words.any { it.startsWith("rumble.results.state.rejected 1, 1") })
        assertTrue(words.any { it.contains("rumble.results.state.other 1") }, "results of a batch that were not sent are said")
        // Without a sign-in the screen offers the GitHub account screen.
        assertTrue(source("rumble/RumbleResultsDialog.kt").contains("RumbleAccountDialog.open()"))
    }
}
