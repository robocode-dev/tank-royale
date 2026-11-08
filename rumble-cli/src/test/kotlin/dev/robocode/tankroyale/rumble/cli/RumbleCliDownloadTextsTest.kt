package dev.robocode.tankroyale.rumble.cli

import dev.robocode.tankroyale.rumble.DownloadFailure
import dev.robocode.tankroyale.rumble.DownloadResult
import dev.robocode.tankroyale.rumble.DownloadTexts
import dev.robocode.tankroyale.rumble.DownloadedState
import dev.robocode.tankroyale.rumble.Tone
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import java.nio.file.Files
import java.nio.file.Path
import java.util.Properties

/** The texts of the Download bots screen and its place in the menu, read from the desktop program's own files. */
class RumbleCliDownloadTextsTest {

    private val root = Path.of(System.getProperty("repo.root") ?: error("System property 'repo.root' not set, run via :rumble-cli:test"))
    private val languages = listOf("", "_ca", "_da", "_es")

    private fun properties(name: String): Properties =
        Properties().apply { Files.newBufferedReader(root.resolve("gui/src/main/resources").resolve(name), Charsets.ISO_8859_1).use { load(it) } }

    private fun code(path: String): String =
        Files.readString(root.resolve(path)).replace(Regex("/\\*.*?\\*/", RegexOption.DOT_MATCHES_ALL), "")
            .lines().joinToString("\n") { it.replace(Regex("(^|\\s)//.*$"), "$1") }

    private val dialogFile = "gui/src/main/kotlin/dev/robocode/tankroyale/gui/ui/rumble/RumbleDownloadDialog.kt"
    private val licensePanelFile = "gui/src/main/kotlin/dev/robocode/tankroyale/gui/ui/rumble/RumbleLicensePanel.kt"

    private val keys: List<String> =
        DownloadedState.entries.map { DownloadTexts.forState(it) } + DownloadFailure.entries.map { DownloadTexts.forFailure(it) } +
            DownloadTexts.ALL_RESULTS.map { DownloadTexts.forResult(it) } + DownloadTexts.ALL_LICENSE_KEYS + DownloadTexts.OTHER

    @Test
    @Tag("RCL-080")
    @Tag("Unit")
    @Tag("Positive")
    fun testRCL080_UnitPositive_everyStateResultFailureAndLicenseHasATextInEveryLanguageFileWithTheSameKeys() {
        val expected = properties("Messages.properties").stringPropertyNames().filter { it.startsWith("rumble.download.") }.toSet()

        assertEquals(expected, keys.toSet(), "the list of keys and the base file agree")
        languages.forEach { suffix ->
            val messages = properties("Messages$suffix.properties")
            assertEquals(emptyList<String>(), keys.filter { messages.getProperty(it).isNullOrBlank() }, "language file $suffix")
            assertEquals(expected, messages.stringPropertyNames().filter { it.startsWith("rumble.download.") }.toSet(), "language file $suffix")
            assertTrue(properties("Menu$suffix.properties").getProperty("item.rumble_download").isNotBlank(), "menu $suffix")
            assertTrue(properties("UI titles$suffix.properties").getProperty("rumble_download_dialog").isNotBlank(), "title $suffix")
            assertTrue(properties("UI titles$suffix.properties").getProperty("rumble_licenses_dialog").isNotBlank(), "licenses title $suffix")
        }
    }

    @Test
    @Tag("RCL-080")
    @Tag("Unit")
    @Tag("Positive")
    fun testRCL080_UnitPositive_everyLicenseIsExplainedForTheAuthorAndForTheUserAndTheGuideSaysItIsNotLegalAdvice() {
        val messages = properties("Messages.properties")

        assertEquals(listOf("author", "may", "must", "note"), DownloadTexts.LICENSE_PARTS)
        assertEquals(16, DownloadTexts.ALL_LICENSE_KEYS.toSet().size, "four licenses with four parts each")
        assertTrue(messages.getProperty("rumble.download.licenses_intro").contains("not legal advice"))
        assertTrue(messages.getProperty("rumble.download.credit").contains("choosealicense.com"))
        assertTrue(messages.getProperty("rumble.download.warning").contains("other people's code"))
    }

    @Test
    @Tag("RCL-080")
    @Tag("Unit")
    @Tag("Negative")
    fun testRCL080_UnitNegative_noEnglishSentenceIsWrittenInTheScreenClassesOrTheDialog() {
        val sentence = Regex("[A-Za-z]{2,}\\s+[A-Za-z]{2,}")
        listOf(
            "lib/rumble/src/main/kotlin/dev/robocode/tankroyale/rumble/DownloadScreen.kt",
            "lib/rumble/src/main/kotlin/dev/robocode/tankroyale/rumble/DownloadTexts.kt",
            "lib/rumble/src/main/kotlin/dev/robocode/tankroyale/rumble/BotDownloads.kt",
            "lib/rumble/src/main/kotlin/dev/robocode/tankroyale/rumble/RumbleLicenses.kt",
            dialogFile,
            licensePanelFile,
        ).forEach { file ->
            val strings = Regex("\"((?:[^\"\\\\]|\\\\.)*)\"").findAll(code(file)).map { it.groupValues[1] }
            assertEquals(emptyList<String>(), strings.filter { sentence.containsMatchIn(it) }.toList(), file)
        }
    }

    @Test
    @Tag("RCL-080")
    @Tag("Unit")
    @Tag("Negative")
    fun testRCL080_UnitNegative_theDialogOpensOnlyTheFixedLicensePagesAndTheWarningIsOnTheScreenThatOffersTheDownload() {
        val dialog = code(dialogFile)

        assertEquals(1, dialog.split("Browser.browse(").size - 1, "one place opens a page")
        assertTrue(dialog.contains("if (RumbleLicenses.isFixedPage(address)) Browser.browse(address)"), "and it checks the address first")
        assertTrue(!code(licensePanelFile).contains("Browser."), "the panel of the licenses opens nothing by itself")
        val choosing = dialog.substringAfter("private fun renderChoosing").substringBefore("private fun row(")
        assertTrue(choosing.contains("rumble.download.warning"))
        assertTrue(choosing.indexOf("rumble.download.warning") < choosing.indexOf("rumble.download.button.download"), "the warning comes before the button")
        assertTrue(dialog.substringAfter("private fun renderEnded").substringBefore("private fun resultText").contains("showLicenses()"), "the licenses can be reached from the result screen too")
        assertTrue(choosing.contains("showLicenses()"), "and from the screen that offers the download")
    }

    @Test
    @Tag("RCL-080")
    @Tag("Unit")
    @Tag("Negative")
    fun testRCL080_UnitNegative_goodAndBadAreNotMixedUpAndNoTextHoldsAToken() {
        val messages = properties("Messages.properties")
        DownloadTexts.ALL_RESULTS.forEach {
            val expected = when (it) {
                is DownloadResult.Placed -> Tone.GOOD
                is DownloadResult.HashDiffers, is DownloadResult.NotInArchive, is DownloadResult.NotPlaced -> Tone.BAD
                else -> Tone.NEUTRAL
            }
            assertEquals(expected, DownloadTexts.toneOf(it), it.toString())
        }
        assertEquals(emptyList<String>(), keys.filter { messages.getProperty(it).contains("token", ignoreCase = true) })
    }

    @Test
    @Tag("RCL-081")
    @Tag("Unit")
    @Tag("Positive")
    fun testRCL081_UnitPositive_theItemIsInTheRumbleMenuBehindTheSwitchAndTheTitleShowsTheEnvironment() {
        val menu = code("gui/src/main/kotlin/dev/robocode/tankroyale/gui/ui/menu/Menu.kt")
        val rumbleMenu = menu.substringAfter("private fun setupRumbleMenu()").substringBefore("private fun setupHelpMenu()")

        assertEquals(1, menu.split("item.rumble_download").size - 1)
        assertTrue(rumbleMenu.contains("item.rumble_download"))
        assertTrue(rumbleMenu.indexOf("RumbleMenuSwitch.menuShown") < rumbleMenu.indexOf("item.rumble_download"), "the switch decides first")
        val dialog = code(dialogFile)
        assertTrue(dialog.contains("environment.displayName") && dialog.contains("title = text(\"rumble.download.title\", title, environmentName)"))
    }

    @Test
    @Tag("RCL-081")
    @Tag("Unit")
    @Tag("Negative")
    fun testRCL081_UnitNegative_theRumbleMenuHoldsOnlyTheThreeBuiltItems() {
        val menu = code("gui/src/main/kotlin/dev/robocode/tankroyale/gui/ui/menu/Menu.kt")
        val rumbleMenu = menu.substringAfter("private fun setupRumbleMenu()").substringBefore("private fun setupHelpMenu()")

        assertEquals(listOf("item.rumble_account", "item.rumble_submit", "item.rumble_download"), Regex("item\\.rumble_\\w+").findAll(rumbleMenu).map { it.value }.toList())
        assertTrue(!code(dialogFile).contains("SignIn"), "the screen asks for no sign-in")
    }
}
