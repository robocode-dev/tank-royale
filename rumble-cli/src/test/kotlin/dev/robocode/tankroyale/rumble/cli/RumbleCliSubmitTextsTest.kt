package dev.robocode.tankroyale.rumble.cli

import dev.robocode.tankroyale.rumble.RumbleLinks
import dev.robocode.tankroyale.rumble.SubmitState
import dev.robocode.tankroyale.rumble.SubmitTexts
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import java.nio.file.Files
import java.nio.file.Path
import java.util.Properties
import kotlin.io.path.extension

/** The texts of the Submit or update bot wizard and the sign-up link are in the desktop program's resource files. */
class RumbleCliSubmitTextsTest {

    private val root = Path.of(System.getProperty("repo.root") ?: error("System property 'repo.root' not set — run via :rumble-cli:test"))

    private fun properties(name: String): Properties =
        Properties().apply { Files.newBufferedReader(root.resolve("gui/src/main/resources").resolve(name), Charsets.ISO_8859_1).use { load(it) } }

    /** Every problem code the project library can report, read from its sources. */
    private fun libraryCodes(): Set<String> =
        Files.walk(root.resolve("lib/bot-project/src/main/kotlin")).use { paths ->
            paths.filter { it.extension == "kt" }.collect(java.util.stream.Collectors.toList()).flatMap { file ->
                Regex("(?:Finding|problem)\\(\\s*\"([A-Z_]+)\"").findAll(Files.readString(file)).map { it.groupValues[1] }.toList()
            }.toSet()
        }

    // RCL-063

    @Test
    @Tag("RCL-063")
    @Tag("Unit")
    @Tag("Positive")
    fun testRCL063_UnitPositive_everyProblemCodeAndEveryStateAndEveryOtherKeyHasANonEmptyEnglishText() {
        val messages = properties("Messages.properties")
        val codes = libraryCodes()

        assertTrue(codes.size > 20, "the codes were found in the library's sources: $codes")
        val keys = (codes + SubmitTexts.WIZARD_CODES).map { SubmitTexts.forCode(it) } +
            SubmitTexts.ALL_STATES.map { SubmitTexts.forState(it) } + SubmitTexts.OTHER
        assertEquals(emptyList<String>(), keys.filter { messages.getProperty(it).isNullOrBlank() })
    }

    @Test
    @Tag("RCL-063")
    @Tag("Unit")
    @Tag("Positive")
    fun testRCL063_UnitPositive_theMenuItemTheDialogTitleAndAllWizardTextsAreInEveryLanguageFile() {
        val suffixes = listOf("", "_ca", "_da", "_es")
        val expected = properties("Messages.properties").stringPropertyNames().filter { it.startsWith("rumble.submit.") }.toSet()

        assertTrue(expected.size > 60)
        suffixes.forEach { suffix ->
            assertTrue(properties("Menu$suffix.properties").getProperty("item.rumble_submit").isNotBlank(), "menu $suffix")
            assertTrue(properties("UI titles$suffix.properties").getProperty("rumble_submit_dialog").isNotBlank(), "title $suffix")
            val found = properties("Messages$suffix.properties").stringPropertyNames().filter { it.startsWith("rumble.submit.") }.toSet()
            assertEquals(expected, found, "messages $suffix")
        }
    }

    @Test
    @Tag("RCL-063")
    @Tag("Unit")
    @Tag("Positive")
    fun testRCL063_UnitPositive_thePublicNoticeAndTheTwoConfirmationsAreWrittenOutAndTheConfirmationsStartUnticked() {
        val messages = properties("Messages.properties")

        assertTrue(messages.getProperty("rumble.submit.public_notice").contains("public"))
        assertTrue(messages.getProperty("rumble.submit.confirm_public").contains("public"))
        assertTrue(messages.getProperty("rumble.submit.confirm_author").contains("license"))
        val dialog = Files.readString(root.resolve("gui/src/main/kotlin/dev/robocode/tankroyale/gui/ui/rumble/RumbleSubmitDialog.kt"))
        assertTrue(dialog.contains("state.publicConfirmed") && dialog.contains("state.authorConfirmed"), "the boxes show the state, never a fixed tick")
    }

    @Test
    @Tag("RCL-063")
    @Tag("Unit")
    @Tag("Positive")
    fun testRCL063_UnitPositive_theMenuItemIsInTheRumbleMenuAndNotInAnyOtherMenu() {
        val menu = Files.readString(root.resolve("gui/src/main/kotlin/dev/robocode/tankroyale/gui/ui/menu/Menu.kt"))
        val rumbleMenu = menu.substringAfter("private fun setupRumbleMenu()").substringBefore("private fun setupHelpMenu()")

        assertEquals(1, menu.split("item.rumble_submit").size - 1)
        assertTrue(rumbleMenu.contains("item.rumble_submit"), "the item is in the Rumble menu")
    }

    @Test
    @Tag("RCL-073")
    @Tag("Unit")
    @Tag("Positive")
    fun testRCL073_UnitPositive_everyReadinessCaseEndingProgressStateContentAndLauncherHasANonEmptyTextInEveryLanguageFile() {
        val keys = SubmitTexts.ALL_READINESS.map { SubmitTexts.forReadiness(it) } + SubmitTexts.ALL_RESULTS.map { SubmitTexts.forResult(it) } +
            SubmitTexts.ALL_PROGRESS.map { SubmitTexts.forProgress(it) } + dev.robocode.tankroyale.rumble.ContentState.entries.map { SubmitTexts.forContent(it) } +
            SubmitTexts.ALL_LAUNCHERS.map { SubmitTexts.forLaunchers(it) } + SubmitTexts.ALL_STATES.map { SubmitTexts.forState(it) }

        assertTrue(keys.toSet().size > 30)
        listOf("", "_ca", "_da", "_es").forEach { suffix ->
            val messages = properties("Messages$suffix.properties")
            assertEquals(emptyList<String>(), keys.filter { messages.getProperty(it).isNullOrBlank() }, "language file $suffix")
        }
        assertTrue(SubmitState.Choose() in SubmitTexts.ALL_STATES)
    }

    @Test
    @Tag("RCL-073")
    @Tag("Unit")
    @Tag("Negative")
    fun testRCL073_UnitNegative_noTextOfTheWizardMentionsATokenAndPlaceholdersMatchWhatTheScreenGives() {
        val messages = properties("Messages.properties")
        val wizard = messages.stringPropertyNames().filter { it.startsWith("rumble.submit.") }

        assertEquals(emptyList<String>(), wizard.filter { messages.getProperty(it).contains("token", ignoreCase = true) })
        // The screen formats these with one argument each; no other text has a placeholder.
        val oneArgument = setOf(
            "rumble.submit.file", "rumble.submit.details", "rumble.submit.fix", "rumble.submit.notes",
            "rumble.submit.review_license", "rumble.submit.status_success", "rumble.submit.status_failed",
            "rumble.submit.launchers_differ", "rumble.submit.existing", "rumble.submit.progress_sent_back", "rumble.submit.progress_in_catalog",
        )
        wizard.forEach { key ->
            val count = Regex("%[sd]").findAll(messages.getProperty(key)).count()
            val expected = when (key) {
                in oneArgument -> 1
                "rumble.submit.found" -> 3
                "rumble.submit.found_team", "rumble.submit.review_name" -> 2
                "rumble.submit.review_language", "rumble.submit.review_files" -> 1
                else -> 0
            }
            assertEquals(expected, count, key)
        }
    }

    @Test
    @Tag("RCL-073")
    @Tag("Unit")
    @Tag("Negative")
    fun testRCL073_UnitNegative_theDialogOpensOnlyGitHubPagesAndGoodAndBadAreNotMixedUp() {
        val dialog = code("gui/src/main/kotlin/dev/robocode/tankroyale/gui/ui/rumble/RumbleSubmitDialog.kt")

        assertEquals(1, dialog.split("Browser.browse(").size - 1, "one place opens a page, and it checks the address first")
        assertTrue(dialog.contains("RumbleLinks.isGitHubPage(address)"))
        val good = dev.robocode.tankroyale.rumble.Tone.GOOD
        val bad = dev.robocode.tankroyale.rumble.Tone.BAD
        SubmitTexts.ALL_RESULTS.forEach { assertEquals(if (it is dev.robocode.tankroyale.rumble.SubmissionResult.Submitted) good else bad, SubmitTexts.toneOf(it)) }
        assertEquals(good, SubmitTexts.toneOf(dev.robocode.tankroyale.rumble.BotProgress.InCatalog("1")))
        assertEquals(bad, SubmitTexts.toneOf(dev.robocode.tankroyale.rumble.BotProgress.SentBack("x")))
        assertEquals(bad, SubmitTexts.toneOf(dev.robocode.tankroyale.rumble.BotProgress.Closed))
        assertEquals(good, SubmitTexts.toneOf(dev.robocode.tankroyale.rumble.SubmissionReadiness.Ready("a", "b")))
        assertEquals(bad, SubmitTexts.toneOf(dev.robocode.tankroyale.rumble.SubmissionReadiness.Failed("x")))
    }

    private fun code(path: String): String =
        Files.readString(root.resolve(path)).replace(Regex("/\\*.*?\\*/", RegexOption.DOT_MATCHES_ALL), "")
            .lines().joinToString("\n") { it.replace(Regex("(^|\\s)//.*$"), "$1") }

    private fun sentences(source: String): List<String> =
        Regex("\"((?:[^\"\\\\]|\\\\.)*)\"").findAll(source).map { it.groupValues[1] }
            .filter { Regex("[A-Za-z]{2,}\\s+[A-Za-z]{2,}").containsMatchIn(it) }.toList()

    @Test
    @Tag("RCL-063")
    @Tag("Unit")
    @Tag("Negative")
    fun testRCL063_UnitNegative_noEnglishSentenceIsWrittenInTheWizardClassesOrTheDialog() {
        val files = listOf(
            "lib/rumble/src/main/kotlin/dev/robocode/tankroyale/rumble/SubmitScreen.kt",
            "lib/rumble/src/main/kotlin/dev/robocode/tankroyale/rumble/SubmitTexts.kt",
            "lib/rumble/src/main/kotlin/dev/robocode/tankroyale/rumble/SubmitSend.kt",
            "gui/src/main/kotlin/dev/robocode/tankroyale/gui/ui/rumble/RumbleSubmitDialog.kt",
        )

        files.forEach { file -> assertEquals(emptyList<String>(), sentences(code(file)), file) }
    }

    // RCL-064

    @Test
    @Tag("RCL-064")
    @Tag("Unit")
    @Tag("Positive")
    fun testRCL064_UnitPositive_theSignUpAddressIsGitHubsOwnHttpsPageAndTheTextsExist() {
        assertEquals("https://github.com/signup", RumbleLinks.CREATE_GITHUB_ACCOUNT)
        val messages = properties("Messages.properties")
        assertTrue(messages.getProperty("rumble.account.no_account").isNotBlank())
        assertTrue(messages.getProperty("rumble.account.button.create_account").isNotBlank())
    }

    @Test
    @Tag("RCL-064")
    @Tag("Unit")
    @Tag("Negative")
    fun testRCL064_UnitNegative_theButtonIsOnlyInTheSignedOutScreenAndOnlyOpensThePage() {
        val dialog = code("gui/src/main/kotlin/dev/robocode/tankroyale/gui/ui/rumble/RumbleAccountDialog.kt")

        assertEquals(1, Regex("rumble\\.account\\.button\\.create_account").findAll(dialog).count())
        assertEquals(1, Regex("RumbleLinks\\.CREATE_GITHUB_ACCOUNT").findAll(dialog).count())
        val signedOut = dialog.substringAfter("is AccountState.SignedOut ->").substringBefore("is AccountState.Unavailable")
        assertTrue(signedOut.contains("create_account"), "the button is in the signed-out screen")
        assertTrue(!dialog.contains("github.com"), "the address comes from the library, not from the dialog")
    }
}
