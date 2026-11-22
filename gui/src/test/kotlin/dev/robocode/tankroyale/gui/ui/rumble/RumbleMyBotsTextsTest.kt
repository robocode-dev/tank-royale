package dev.robocode.tankroyale.gui.ui.rumble

import dev.robocode.tankroyale.rumble.BotProgress
import dev.robocode.tankroyale.rumble.MyBotRow
import dev.robocode.tankroyale.rumble.MyBotStanding
import dev.robocode.tankroyale.rumble.MyBotsTexts
import dev.robocode.tankroyale.gui.ui.components.RcToolTip
import javax.swing.JButton
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import java.io.File
import java.nio.file.Files
import java.nio.file.Path
import java.util.Properties

/** My bots comes after Submit in the Rumble menu, every text it shows is in every language, and Open opens only GitHub pages. */
class RumbleMyBotsTextsTest {

    private val resources = listOf(Path.of("src", "main", "resources"), Path.of("gui", "src", "main", "resources")).first { Files.isDirectory(it) }
    private val sources = listOf(Path.of("src", "main", "kotlin"), Path.of("gui", "src", "main", "kotlin")).first { Files.isDirectory(it) }

    private fun texts(name: String) = Properties().apply { File(resources.toFile(), name).reader(Charsets.UTF_8).use { load(it) } }
    private fun source(path: String) = sources.resolve("dev/robocode/tankroyale/gui/ui/$path").toFile().readText()

    @Test
    @Tag("RCL-149")
    @Tag("Unit")
    @Tag("Positive")
    fun testRCL149_UnitPositive_myBotsFollowsSubmitInTheMenuAndEveryTextIsInEveryLanguage() {
        val entries = Regex("addNewMenuItem\\(\"(item\\.rumble_[a-z_]+)\"").findAll(source("menu/Menu.kt")).map { it.groupValues[1] }.toList()
        assertEquals(entries.indexOf("item.rumble_submit") + 1, entries.indexOf("item.rumble_my_bots"))
        // The column keys are built from their names, so they are listed by MyBotsTexts; the others are read from the screen's source.
        val inSource = Regex("\"(rumble\\.mybots\\.[a-z_.]+)\"").findAll(source("rumble/RumbleMyBotsDialog.kt")).map { it.groupValues[1] }.toSet()
        val wanted = MyBotsTexts.allKeys().toSet() + inSource
        assertTrue("rumble.mybots.button.open" in wanted && "rumble.mybots.column.state" in wanted)
        for (language in listOf("", "_da", "_es", "_ca")) {
            val messages = texts("Messages$language.properties")
            assertEquals(emptySet<String>(), wanted.filter { messages.getProperty(it).isNullOrBlank() }.toSet(), "missing in Messages$language")
            assertFalse(texts("UI titles$language.properties").getProperty("rumble_my_bots_dialog").isNullOrBlank(), "title in $language")
            assertFalse(texts("Menu$language.properties").getProperty("item.rumble_my_bots").isNullOrBlank(), "menu entry in $language")
            val hints = texts("Hints$language.properties")
            val hintKeys = listOf("rumble.my_bots.open_pull", "rumble.my_bots.open_folder", "rumble.my_bots.update")
            for (key in hintKeys) {
                assertFalse(hints.getProperty(key).isNullOrBlank(), "$key in Hints$language")
            }
            // No long dash in any text of the screen: its messages, title, menu entry and tooltips.
            val all = wanted.map { messages.getProperty(it).orEmpty() } + hintKeys.map { hints.getProperty(it).orEmpty() } +
                texts("UI titles$language.properties").getProperty("rumble_my_bots_dialog").orEmpty() +
                texts("Menu$language.properties").getProperty("item.rumble_my_bots").orEmpty()
            for (text in all) {
                assertFalse('\u2014' in text || '\u2013' in text, "long dash in \"$text\" ($language)")
            }
        }
    }

    @Test
    @Tag("RCL-149")
    @Tag("Unit")
    @Tag("Negative")
    fun testRCL149_UnitNegative_openNeverOpensAPageThatIsNotOnGitHubAndTheScreenWaitsForASignIn() {
        // The real list is built, so a layout rule it does not accept fails here, not in front of a person.
        val good = "https://github.com/robocode-dev/rumble-bots/pull/1"
        val pages = listOf(good, "http://github.com/x/y/pull/1", "https://evil.example/pull/1", "https://user@github.com/x", "file:///C:/x", null)
        val rows = pages.mapIndexed { i, page -> MyBotRow("Bot$i", "1.0.$i", null, MyBotStanding.Progress(BotProgress.InCatalog("1.0.$i")), "1.0.0", page) }
        val opened = mutableListOf<String>()
        var updates = 0
        val table = myBotsTable(rows, { key, arguments -> "$key ${arguments.joinToString()}" }, hint = { it }, onOpen = { opened += it }, onUpdate = { updates++ })
        val buttons = table.components.filterIsInstance<JButton>()
        val open = buttons.filter { it.text.startsWith("rumble.mybots.button.open") }
        assertEquals(listOf(true, false, false, false, false, false), open.map { it.isEnabled }, "Open only for a page on github.com")
        assertEquals("rumble.my_bots.open_pull", open.first().toolTipText, "the tooltip says it is the pull request")
        assertEquals("rumble.my_bots.open_folder", open.last().toolTipText)
        // The program's own yellow tooltip, as on every other screen.
        buttons.forEach { assertTrue(it.createToolTip() is RcToolTip, it.text) }
        open.forEach { it.doClick() }
        assertEquals(listOf(good), opened)
        buttons.filter { it.text.startsWith("rumble.mybots.button.update") }.first().doClick()
        assertEquals(1, updates)
        // Without a sign-in the screen offers the GitHub account screen and lists nothing.
        val dialog = source("rumble/RumbleMyBotsDialog.kt")
        assertTrue(dialog.contains("is MyBotsState.SignInNeeded -> {") && dialog.contains("RumbleAccountDialog.open()"))
    }
}
