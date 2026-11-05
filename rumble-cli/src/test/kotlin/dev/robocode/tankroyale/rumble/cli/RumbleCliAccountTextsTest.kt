package dev.robocode.tankroyale.rumble.cli

import dev.robocode.tankroyale.rumble.AccountNotice
import dev.robocode.tankroyale.rumble.AccountTexts
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import java.nio.file.Files
import java.nio.file.Path
import java.util.Properties

/** The texts of the account screen are in the desktop program's English resource files. */
class RumbleCliAccountTextsTest {

    private val root = Path.of(System.getProperty("repo.root") ?: error("System property 'repo.root' not set — run via :rumble-cli:test"))

    private fun properties(name: String): Properties =
        Properties().apply { Files.newBufferedReader(root.resolve("gui/src/main/resources").resolve(name)).use { load(it) } }

    // RCL-056

    @Test
    @Tag("RCL-056")
    @Tag("Unit")
    @Tag("Positive")
    fun testRCL056_UnitPositive_everyKeyTheAccountScreenCanAskForHasANonEmptyEnglishText() {
        val messages = properties("Messages.properties")

        assertTrue(AccountTexts.ALL_KEYS.size > 20, "the list of keys is not empty")
        val missing = AccountTexts.ALL_KEYS.filter { messages.getProperty(it).isNullOrBlank() }
        assertEquals(emptyList<String>(), missing)
        AccountNotice.entries.forEach { notice ->
            assertTrue(messages.getProperty(AccountTexts.forNotice(notice)).isNotBlank(), notice.name)
        }
    }

    @Test
    @Tag("RCL-056")
    @Tag("Unit")
    @Tag("Positive")
    fun testRCL056_UnitPositive_theEnglishMenuFilesHaveTheRumbleMenuItsAccountItemAndTheDialogTitle() {
        val menu = properties("Menu.properties")
        assertEquals("Rumble", menu.getProperty("menu.rumble"))
        assertTrue(menu.getProperty("item.rumble_account").isNotBlank())
        assertTrue(properties("UI titles.properties").getProperty("rumble_account_dialog").isNotBlank())
    }

    @Test
    @Tag("RCL-056")
    @Tag("Unit")
    @Tag("Negative")
    fun testRCL056_UnitNegative_noTextTalksAboutATokenAndPlaceholdersMatchWhatTheScreenGives() {
        val messages = properties("Messages.properties")
        val texts = AccountTexts.ALL_KEYS.associateWith { messages.getProperty(it) }

        texts.filterValues { it.contains("token", ignoreCase = true) }.let { assertEquals(emptyMap<String, String>(), it) }
        // The screen formats these with one argument each; no other text has a placeholder.
        val oneArgument = setOf(
            "rumble.account.waiting_expires", "rumble.account.signed_in", "rumble.account.registered",
            "rumble.account.details", "rumble.account.unknown_environment",
        )
        texts.forEach { (key, text) ->
            val count = Regex("%s").findAll(text).count()
            assertEquals(if (key in oneArgument) 1 else 0, count, key)
        }
    }

    /** The code of a source file without its block and line comments, so only what the program does is looked at. */
    private fun code(path: String): String =
        Files.readString(root.resolve(path)).replace(Regex("/\\*.*?\\*/", RegexOption.DOT_MATCHES_ALL), "")
            .lines().joinToString("\n") { it.replace(Regex("(^|\\s)//.*$"), "$1") }

    /** String literals that hold an English sentence: two or more words, which no resource key and no format has. */
    private fun sentences(source: String): List<String> =
        Regex("\"((?:[^\"\\\\]|\\\\.)*)\"").findAll(source).map { it.groupValues[1] }
            .filter { Regex("[A-Za-z]{2,}\\s+[A-Za-z]{2,}").containsMatchIn(it) }.toList()

    @Test
    @Tag("RCL-056")
    @Tag("Unit")
    @Tag("Negative")
    fun testRCL056_UnitNegative_noEnglishSentenceIsWrittenInTheBehaviorClassOrTheDialog() {
        val files = listOf(
            "lib/rumble/src/main/kotlin/dev/robocode/tankroyale/rumble/AccountScreen.kt",
            "lib/rumble/src/main/kotlin/dev/robocode/tankroyale/rumble/AccountTexts.kt",
            "gui/src/main/kotlin/dev/robocode/tankroyale/gui/ui/rumble/RumbleAccountDialog.kt",
        )

        files.forEach { file -> assertEquals(emptyList<String>(), sentences(code(file)), file) }
    }

    @Test
    @Tag("RCL-056")
    @Tag("Unit")
    @Tag("Negative")
    fun testRCL056_UnitNegative_theRuleFindsASentenceAndLeavesKeysAndFormatsAlone() {
        assertEquals(listOf("Something went wrong. Try again."), sentences("val a = \"Something went wrong. Try again.\""))
        assertEquals(emptyList<String>(), sentences("val a = \"rumble.account.signed_in\"; val b = \"%d:%02d\"; val c = \"<html>\${x}</html>\""))
    }
}
