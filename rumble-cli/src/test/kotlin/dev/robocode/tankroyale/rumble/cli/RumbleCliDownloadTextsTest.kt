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
    private val platformDialogFile = "gui/src/main/kotlin/dev/robocode/tankroyale/gui/ui/rumble/RumblePlatformDialog.kt"

    private val keys: List<String> =
        DownloadedState.entries.map { DownloadTexts.forState(it) } + DownloadFailure.entries.map { DownloadTexts.forFailure(it) } +
            DownloadTexts.ALL_RESULTS.map { DownloadTexts.forResult(it) } + DownloadTexts.ALL_LICENSE_KEYS + DownloadTexts.OTHER +
            DownloadTexts.ALL_PLATFORM_KEYS

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
            assertTrue(properties("UI titles$suffix.properties").getProperty("rumble_platform_dialog").isNotBlank(), "platform title $suffix")
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
            platformDialogFile,
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
        assertTrue(dialog.contains("if (RumbleLicenses.isFixedPage(address) || PlatformDownloads.isFixedPage(address)) Browser.browse(address)"), "and it checks the address first")
        assertTrue(!code(platformDialogFile).contains("Browser."), "the platform dialog opens nothing by itself")
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
    @Tag("Positive")
    fun testRCL080_UnitPositive_aRowShowsTheVersionInTheFolderOnlyWhenItDiffersFromTheCatalog() {
        val rowText = code(dialogFile).substringAfter("private fun rowText").substringBefore("private fun renderEnded")

        assertTrue(rowText.contains("row.versionHere?.takeIf { it != row.version }") && rowText.contains("rumble.download.row_here"))
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
                is DownloadResult.HashDiffers, is DownloadResult.NotInArchive, is DownloadResult.NotPlaced, is DownloadResult.KeptAside -> Tone.BAD
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

    @Test
    @Tag("RCL-087")
    @Tag("Unit")
    @Tag("Positive")
    fun testRCL087_UnitPositive_everyReasonOfferAndResultOfThePlatformChecksHasATextInEveryLanguageFile() {
        val expected = dev.robocode.tankroyale.rumble.PlatformReason.entries.size - 1
        assertEquals(expected * 2 + dev.robocode.tankroyale.rumble.BotPlatform.entries.size + dev.robocode.tankroyale.rumble.PythonSetupStep.entries.size + 13,
            DownloadTexts.ALL_PLATFORM_KEYS.toSet().size, "every reason but ready has a short text and an explanation")
        languages.forEach { suffix ->
            val messages = properties("Messages$suffix.properties")
            assertEquals(emptyList<String>(), DownloadTexts.ALL_PLATFORM_KEYS.filter { messages.getProperty(it).isNullOrBlank() }, "language file $suffix")
        }
        assertTrue(properties("Messages.properties").getProperty("rumble.download.platform.venv_package").contains("python3-venv"))
        listOf("missing", "npm_missing").forEach {
            assertTrue(properties("Messages.properties").getProperty("rumble.download.platform.explain.$it").contains("%4\$s"), "a missing tool is named with the version needed: $it")
        }
    }

    @Test
    @Tag("RCL-084")
    @Tag("Unit")
    @Tag("Negative")
    fun testRCL084_UnitNegative_tickingAPlatformThatCannotRunOpensTheDialogInsteadOfTickingIt() {
        val dialog = code(dialogFile)
        val box = dialog.substringAfter("private fun platformBox").substringBefore("/** One bot:")
        assertTrue(box.contains("if (isSelected && check != null)") && box.indexOf("isSelected = false") < box.indexOf("RumblePlatformDialog("),
            "the box is unticked before the dialog opens, so closing the dialog leaves it unticked")
        assertTrue(box.contains("showAnyway = { controller.setPlatform(platform, true) }") && box.contains("setUp = { controller.setUpPython() }"))
        val platform = code(platformDialogFile)
        assertTrue(platform.contains("PlatformReason.NO_ENVIRONMENT && commands.isNotEmpty()"), "setting up is offered only when the environment is what is missing")
        assertTrue(platform.contains("open(PlatformDownloads.page(check.platform))"), "otherwise the official download page")
        assertTrue(platform.indexOf("rumble.download.platform.commands") < platform.indexOf("rumble.download.button.set_up"), "the commands are shown before the button that runs them")
        assertTrue(platform.contains("rumble.download.button.show_anyway") && platform.contains("PlatformReason.NO_VENV"))
    }
}
