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
            "rumble.account.refused", "rumble.account.registration_failed", "rumble.account.unknown_environment",
        )
        texts.forEach { (key, text) ->
            val count = Regex("%s").findAll(text).count()
            assertEquals(if (key in oneArgument) 1 else 0, count, key)
        }
    }
}
