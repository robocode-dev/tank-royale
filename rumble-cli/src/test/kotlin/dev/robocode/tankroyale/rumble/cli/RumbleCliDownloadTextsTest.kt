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

    private val newBattleFile = "gui/src/main/kotlin/dev/robocode/tankroyale/gui/ui/newbattle/BotSelectionPanel.kt"
    private val botInfoFile = "gui/src/main/kotlin/dev/robocode/tankroyale/gui/ui/newbattle/BotInfoPanel.kt"

    @Test
    @Tag("RCL-091")
    @Tag("Unit")
    @Tag("Positive")
    fun testRCL091_UnitPositive_theGroupSitsBelowTheOwnBotsClosesOnOpeningAndItsBotsBootLikeAnyOther() {
        val panel = code(newBattleFile)
        val directories = panel.substringAfter("private fun createBotsDirectoryPanel").substringBefore("private fun createRunningBotsPanel")
        assertTrue(directories.indexOf("botsDirectoryScrollPane") < directories.indexOf("downloadedHeader") &&
            directories.indexOf("downloadedHeader") < directories.indexOf("downloadedScrollPane"), "own bots, then the header row, then the group")
        assertTrue(panel.contains("private val botsDirectoryScrollPane = JScrollPane(botsDirectoryList)"), "the header is not inside the sideways-scrolled list of own bots")
        assertTrue(panel.contains("DownloadFolders.isDownloaded(botEntry.dir, labels)"), "the library decides which bots belong to the group")
        assertTrue(panel.contains("botsDirectoryList.selectedValuesList + downloadedList.selectedValuesList"), "booting covers the group's bots")
        assertTrue(panel.contains("isEnabled = !botsDirectoryList.isSelectionEmpty || !downloadedList.isSelectionEmpty"), "Boot is offered for a selection in either list")
        assertTrue(panel.contains("if (!open) downloadedList.clearSelection()"), "a closed group keeps no selection, so Boot never boots a hidden bot")
        assertTrue(panel.contains("if (!botsDirectoryList.isSelectionEmpty) downloadedList.clearSelection()") && panel.contains("if (!downloadedList.isSelectionEmpty) botsDirectoryList.clearSelection()"),
            "one selection at a time across the two lists")
        assertTrue(panel.contains("runFromBotDirectoryAtIndex(downloadedListModel, it)"), "a double-click boots a downloaded bot the same way as an own one")
        assertTrue(panel.contains("downloadedHeader.isVisible = downloadedListModel.size > 0"), "no header row without downloaded bots")
        assertTrue(panel.contains("downloadedNames = DownloadFolders.shownNames(downloadedListModel.list().map { it.host })") &&
            panel.contains("text = downloadedNames[host] ?: java.io.File(host).name"), "a downloaded bot is shown by its own folder name, or its path when two are alike")
        val dialog = code("gui/src/main/kotlin/dev/robocode/tankroyale/gui/ui/newbattle/NewBattleDialog.kt")
        assertTrue(dialog.contains("if (visible && !isVisible) BotSelectionPanel.opened()"), "closed each time New Battle opens")
        val opened = panel.substringAfter("fun opened() {").substringBefore("}")
        assertTrue(opened.contains("showDownloaded(false)") && opened.contains("refreshDirectories.shown()"), "closed, and read again when the settings changed while New Battle was closed")
    }

    @Test
    @Tag("RCL-090")
    @Tag("Unit")
    @Tag("Negative")
    fun testRCL090_UnitNegative_theScreenAddsOnlyAfterPlacingAndTheSettingsDialogNeverWritesBackAnOldList() {
        val download = code(dialogFile)
        assertTrue(download.contains("rememberFolder = ::rememberFolder") && download.contains("when (state.folderOutcome)"),
            "the folder is remembered by the download, away from the screen's thread, and the result only shows what that did")
        assertTrue(download.contains("runCatching { BotDownloads.holdsOnlyDownloads(folder) }") && download.contains("save = { ConfigSettings.saveBotFolders(it.folders, it.labels) }"),
            "an own folder is never labelled, and the folders and labels are saved in one write")
        assertTrue(download.contains("DownloadFolders.rememberAfterDownload("), "a folder that cannot be looked into or saved leaves the screen showing what the download did")
        assertTrue(download.contains("null -> text(\"rumble.download.add_folder_hint\")"), "and asks the person to add the folder by hand")
        val panel = code(newBattleFile)
        assertTrue(panel.contains("ConfigSettings.onSaved.on(this) { refreshDirectories.changed() }") && panel.contains("RefreshWhenShown({ isShowing })"),
            "New Battle reads the bot directories after a download only when it is open, so the download screen never waits for it")
        listOf("ADDED", "LABELLED", "SWITCHED_OFF", "OWN_FOLDER", "OWN_FOLDER_SWITCHED_OFF").forEach { assertTrue(download.contains("FolderOutcome.$it ->"), "a message for $it") }
        assertTrue(download.contains("DownloadFolders.ownFolders(ConfigSettings.botFolders, ConfigSettings.botDirectoryLabels)"), "the suggestion skips download folders")
        val settings = code("gui/src/main/kotlin/dev/robocode/tankroyale/gui/ui/config/BotRootDirectoriesConfigDialog.kt")
        assertTrue(settings.contains("if (visible && !isVisible) BotDirectoryConfigPanel.reload()"), "the dialog reads the bot directories again each time it opens")
        assertTrue(settings.contains("if (!saving && BotRootDirectoriesConfigDialog.isVisible) reload()"), "and when a download saves them while it is open")
        assertTrue(settings.contains("DownloadFolders.key(it.label) == key"), "a folder added by hand that is there already, however its path is written, is not added twice")
        assertTrue(settings.contains("DownloadFolders.keepLabelsOf(folders, ConfigSettings.botDirectoryLabels)"), "a removed folder leaves no label behind")
        assertTrue(settings.contains("DownloadFolders.isLabelled(path, labels)"), "the rows are painted from the labels read with the list, not from the disk")
        val config = code("gui/src/main/kotlin/dev/robocode/tankroyale/gui/settings/ConfigSettings.kt")
        assertTrue(config.contains("private const val BOT_DIRECTORY_LABELS = \"bot-directory-labels\"") && config.contains("private const val BOT_DIRECTORIES = \"bot-directories\""),
            "the labels have a setting of their own beside the unchanged one")
    }

    @Test
    @Tag("RCL-092")
    @Tag("Unit")
    @Tag("Negative")
    fun testRCL092_UnitNegative_botInfoLinksOnlyTheSpdxPageAndAJoinedBotTakesItsLicenseFromItsFolder() {
        val info = code(botInfoFile)
        assertTrue(info.contains("licensePage = LicenseLinks.spdxPage(botInfo?.license)") && info.contains("licensePage?.let { Browser.browse(it) }"),
            "the only page opened is the one the library made from an identifier on the list")
        assertTrue(info.contains("showLicenseButton.isEnabled = licensePage != null"), "a visible button opens it, and only when there is a page")
        assertTrue(info.contains("Browser.browse(it)") && !info.contains("Desktop.getDesktop().browse(java.net.URI"), "through the program's own browser helper, which tells the person when it cannot")
        assertTrue(info.contains("Strings.get(\"bot_info.show_license\")"), "the button's text is a resource")
        assertTrue(info.contains("Strings.get(\"bot_info.no_license\")") && info.contains("Strings.get(\"bot_info.license_unknown\")"))
        val panel = code(newBattleFile)
        assertTrue(panel.contains("BotSelectionEvents.onJoinedBotSelected(withKnownLicense(it))") && panel.contains("BotSelectionEvents.onBotSelected(withKnownLicense(it))"),
            "a joined bot shows the same license in Joined Bots and in Selected Bots")
        assertTrue(panel.contains("license = license ?: \"\""), "a bot in the bot directories with no license says so; null is kept for not known")
        assertTrue(panel.contains("knownLicenses.of(BotIdentity(botInfo.name, botInfo.version, botInfo.authors))"), "the license of the bot with the same name, version and authors")
        assertTrue(panel.contains("val entries = BootProcess.info()") && panel.contains("knownLicenses = KnownLicenses(entries.map"), "taken from every bot, whatever the filter shows")
        val strings = listOf("bot_info.license", "bot_info.no_license", "bot_info.license_unknown", "rumble_downloads_label", "rumble_downloads_group", "bot_root_dirs.labelled", "bot_info.show_license")
        languages.forEach { suffix ->
            val file = Properties().apply { Files.newBufferedReader(root.resolve("gui/src/main/resources/Strings$suffix.properties"), Charsets.UTF_8).use { load(it) } }
            assertEquals(emptyList<String>(), strings.filter { file.getProperty(it).isNullOrBlank() }, "Strings$suffix")
        }
    }
}
