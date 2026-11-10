package dev.robocode.tankroyale.rumble

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import java.nio.file.Path

/** How folders of downloaded bots live among the person's bot directories, and the license link in Bot Info. No Swing, no disk. */
class RumbleDownloadFoldersTest {

    private val own = listOf(BotFolderSetting("C:/Code/bots/java", true), BotFolderSetting("C:/Code/bots/python", false))

    @Test
    @Tag("RCL-089")
    @Tag("Unit")
    @Tag("Positive")
    fun testRCL089_UnitPositive_labelsComeBackForTheirFolderHoweverItIsWritten() {
        val labels = mapOf(DownloadFolders.key("C:\\Users\\me\\rumble-bots\\", ignoreCase = true) to DownloadFolders.LABEL)

        val read = DownloadFolders.parseLabels(DownloadFolders.formatLabels(labels), ignoreCase = true)

        assertEquals(labels, read)
        listOf("C:/Users/me/rumble-bots", "c:\\users\\ME\\rumble-bots", "C:/Users/me/rumble-bots/").forEach {
            assertTrue(DownloadFolders.isLabelled(it, read, ignoreCase = true), it)
        }
        assertFalse(DownloadFolders.isLabelled("/home/me/Rumble-Bots", mapOf("/home/me/rumble-bots" to DownloadFolders.LABEL), ignoreCase = false),
            "letter case counts where the file system tells it apart")
        val comma = mapOf("/home/me/bots, rumble" to DownloadFolders.LABEL, "/home/me/100%" to DownloadFolders.LABEL, "/home/me/a;b%2C" to DownloadFolders.LABEL, "/x" to DownloadFolders.LABEL)
        assertEquals(comma, DownloadFolders.parseLabels(DownloadFolders.formatLabels(comma), ignoreCase = false), "a comma, a semicolon or a percent sign in a path keeps every pair in place")
    }

    @Test
    @Tag("RCL-089")
    @Tag("Unit")
    @Tag("Negative")
    fun testRCL089_UnitNegative_brokenTextIsLeftOutAndARemovedFolderLeavesNoLabel() {
        val read = DownloadFolders.parseLabels("/a/b;rumble-downloads,,x,/c/d;,;rumble-downloads,/e/f;rumble-downloads", ignoreCase = false)
        assertEquals(mapOf("/a/b" to DownloadFolders.LABEL, "/e/f" to DownloadFolders.LABEL), read, "a half entry or an empty path is left out on its own, the rest is kept")
        assertEquals(mapOf("/c/b" to DownloadFolders.LABEL), DownloadFolders.parseLabels("/c/a,/c/b;rumble-downloads", ignoreCase = false),
            "an entry without its label does not shift the entries after it")
        assertEquals(emptyMap<String, String>(), DownloadFolders.parseLabels("", ignoreCase = false))

        val labels = mapOf("/a/b" to DownloadFolders.LABEL, "/gone" to DownloadFolders.LABEL)
        assertEquals(mapOf("/a/b" to DownloadFolders.LABEL), DownloadFolders.keepLabelsOf(listOf(BotFolderSetting("/a/b/", false)), labels, ignoreCase = false))
    }

    @Test
    @Tag("RCL-090")
    @Tag("Unit")
    @Tag("Positive")
    fun testRCL090_UnitPositive_aNewDownloadFolderIsAddedSwitchedOnAndLabelledAtTheEnd() {
        val folder = Path.of("C:/Users/me/rumble-bots")

        val remembered = DownloadFolders.remember(own, emptyMap(), folder, onlyDownloads = true, ignoreCase = true)

        assertEquals(FolderOutcome.ADDED, remembered.outcome)
        assertEquals(own + BotFolderSetting(folder.toString(), true), remembered.folders, "the person's own folders are kept as they were, in order")
        assertTrue(DownloadFolders.isLabelled(folder.toString(), remembered.labels, ignoreCase = true))
    }

    @Test
    @Tag("RCL-090")
    @Tag("Unit")
    @Tag("Negative")
    fun testRCL090_UnitNegative_aNewFolderHoldingOwnBotsIsAddedWithoutALabelAndNothingChangesWhenNothingWasPlaced() {
        val folder = Path.of("C:/Users/me/my-bots")

        val mixed = DownloadFolders.remember(own, emptyMap(), folder, onlyDownloads = false, ignoreCase = true)
        assertEquals(FolderOutcome.ADDED_AS_OWN, mixed.outcome)
        assertEquals(own + BotFolderSetting(folder.toString(), true), mixed.folders, "added, so the new bots are in New Battle")
        assertFalse(DownloadFolders.isLabelled(folder.toString(), mixed.labels, ignoreCase = true), "but the person's own bots there are never moved into the group")

        val notListed = DownloadFolders.remember(own, emptyMap(), folder, onlyDownloads = true, ignoreCase = true, placed = false)
        assertEquals(FolderOutcome.NOT_LISTED, notListed.outcome)
        assertEquals(own to emptyMap<String, String>(), notListed.folders to notListed.labels, "nothing placed, nothing added")

        val labels = mapOf(DownloadFolders.key("C:/Code/bots/java", ignoreCase = true) to DownloadFolders.LABEL)
        val there = DownloadFolders.remember(own, labels, Path.of("C:/Code/bots/java"), ignoreCase = true, placed = false)
        assertEquals(FolderOutcome.LABELLED, there.outcome, "a labelled folder is said to be in New Battle again")
        assertEquals(own to labels, there.folders to there.labels)
        val ownThere = DownloadFolders.remember(own, emptyMap(), Path.of("C:/Code/bots/java"), onlyDownloads = true, ignoreCase = true, placed = false)
        assertEquals(FolderOutcome.OWN_FOLDER, ownThere.outcome, "an unlabelled folder is not labelled when nothing was placed")
        assertEquals(emptyMap<String, String>(), ownThere.labels)
    }

    @Test
    @Tag("RCL-090")
    @Tag("Unit")
    @Tag("Negative")
    fun testRCL090_UnitNegative_aFolderAlreadyThereKeepsItsPlaceAndSwitchAndIsLabelledOnlyWhenItHoldsOnlyDownloads() {
        val folders = own + BotFolderSetting("C:/Users/me/rumble-bots", false)

        val downloads = DownloadFolders.remember(folders, emptyMap(), Path.of("c:\\users\\me\\rumble-bots"), onlyDownloads = true, ignoreCase = true)
        assertEquals(FolderOutcome.SWITCHED_OFF, downloads.outcome, "the person's switch stays off, and the screen says so")
        assertEquals(folders, downloads.folders, "not added twice")
        assertTrue(DownloadFolders.isLabelled("C:/Users/me/rumble-bots", downloads.labels, ignoreCase = true))

        val labelledBefore = DownloadFolders.remember(own, mapOf(DownloadFolders.key("C:/Code/bots/java", ignoreCase = true) to DownloadFolders.LABEL), Path.of("C:/Code/bots/java"), ignoreCase = true)
        assertEquals(FolderOutcome.LABELLED, labelledBefore.outcome, "a labelled folder stays labelled, switched on")

        val mine = DownloadFolders.remember(own, emptyMap(), Path.of("C:/Code/bots/java"), onlyDownloads = false, ignoreCase = true)
        assertEquals(FolderOutcome.OWN_FOLDER, mine.outcome, "one of the person's own bot directories")
        assertEquals(own to emptyMap<String, String>(), mine.folders to mine.labels, "their own bots are never moved into the group")

        val mineOff = DownloadFolders.remember(own, emptyMap(), Path.of("C:/Code/bots/python"), onlyDownloads = false, ignoreCase = true)
        assertEquals(FolderOutcome.OWN_FOLDER_SWITCHED_OFF, mineOff.outcome, "an own folder that is switched off is never said to be in New Battle")
        assertEquals(own to emptyMap<String, String>(), mineOff.folders to mineOff.labels)
    }

    @Test
    @Tag("RCL-090")
    @Tag("Unit")
    @Tag("Negative")
    fun testRCL090_UnitNegative_aFolderHoldsOnlyDownloadsOnlyWhenEveryBotFolderInItWasPlacedByTheScreen(@org.junit.jupiter.api.io.TempDir dir: Path) {
        fun bot(name: String, marked: Boolean) = java.nio.file.Files.createDirectories(dir.resolve(name)).also {
            if (marked) java.nio.file.Files.write(it.resolve(BotDownloads.MARKER), ByteArray(0))
        }
        assertFalse(BotDownloads.holdsOnlyDownloads(dir), "an empty folder holds no downloads")
        bot("Corners", marked = true)
        java.nio.file.Files.createDirectories(dir.resolve(".rumble-python"))
        assertTrue(BotDownloads.holdsOnlyDownloads(dir), "hidden folders such as the Python environment do not count")
        bot("MyOwnBot", marked = false)
        assertFalse(BotDownloads.holdsOnlyDownloads(dir), "one bot of the person's own makes it their folder")
        assertFalse(BotDownloads.holdsOnlyDownloads(dir.resolve("missing")))
    }

    @Test
    @Tag("RCL-090")
    @Tag("Unit")
    @Tag("Negative")
    fun testRCL090_UnitNegative_theSuggestedFolderIsWorkedOutFromTheOwnFoldersAndNeverMoves() {
        val labels = mapOf(DownloadFolders.key("C:/Users/me/rumble-bots", ignoreCase = true) to DownloadFolders.LABEL)
        val folders = listOf(BotFolderSetting("C:/Users/me/rumble-bots", true)) + own

        val ownFolders = DownloadFolders.ownFolders(folders, labels, ignoreCase = true)

        assertEquals(listOf(Path.of("C:/Code/bots/java")), ownFolders, "the download folder and switched-off folders are skipped")
        assertEquals(Path.of("C:/Code/bots/rumble-bots"), BotDownloads.defaultFolder(ownFolders, Path.of("C:/Users/me")))
    }

    @Test
    @Tag("RCL-090")
    @Tag("Unit")
    @Tag("Negative")
    fun testRCL090_UnitNegative_aFolderThatCannotBeLookedIntoOrSavedIsNotRememberedAndTheScreenStillEnds() {
        val folder = Path.of("C:/Users/me/rumble-bots")
        val settings = { own to emptyMap<String, String>() }
        var saved: RememberedFolder? = null

        assertEquals(FolderOutcome.ADDED, DownloadFolders.rememberAfterDownload(folder, { true }, settings, { saved = it }, ignoreCase = true))
        assertEquals(own + BotFolderSetting(folder.toString(), true), saved?.folders, "what was worked out is what is saved")

        assertEquals(null, DownloadFolders.rememberAfterDownload(folder, { throw java.io.UncheckedIOException(java.io.IOException("no access")) }, settings, {}, ignoreCase = true),
            "a folder that cannot be listed")
        val told = mutableListOf<Exception>()
        assertEquals(null, DownloadFolders.rememberAfterDownload(folder, { true }, settings, { throw java.io.IOException("read-only settings") }, { told += it }, ignoreCase = true),
            "settings that cannot be written")
        assertEquals(listOf("read-only settings"), told.map { it.message }, "the reason is told, so it can be logged")
        assertEquals(null, DownloadFolders.rememberAfterDownload(folder, { true }, { throw SecurityException("no access") }, {}, ignoreCase = true),
            "settings that cannot be read")
    }

    @Test
    @Tag("RCL-091")
    @Tag("Unit")
    @Tag("Positive")
    fun testRCL091_UnitPositive_aBotIsDownloadedOnlyWhenItLiesInALabelledFolder() {
        val labels = mapOf("/home/me/rumble-bots" to DownloadFolders.LABEL, "/home/me/other" to "something-else")

        assertTrue(DownloadFolders.isDownloaded("/home/me/rumble-bots/Corners", labels, ignoreCase = false))
        assertFalse(DownloadFolders.isDownloaded("/home/me/bots/Corners", labels, ignoreCase = false), "the person's own bot")
        assertFalse(DownloadFolders.isDownloaded("/home/me/other/Walls", labels, ignoreCase = false), "a folder with another label")
        assertFalse(DownloadFolders.isDownloaded("/home/me/rumble-bots/Corners/sub", labels, ignoreCase = false), "only the bot folders directly in it")
        assertFalse(DownloadFolders.isDownloaded("/home/me/rumble-bots/Corners", emptyMap(), ignoreCase = false), "no labels, no group")
    }

    @Test
    @Tag("RCL-091")
    @Tag("Unit")
    @Tag("Negative")
    fun testRCL091_UnitNegative_twoDownloadedBotsWithTheSameFolderNameAreToldApartByTheirPath() {
        val shown = DownloadFolders.shownNames(listOf("/home/me/rumble-bots/Corners", "/home/me/other/Corners", "/home/me/rumble-bots/Walls"))

        assertEquals(mapOf(
            "/home/me/rumble-bots/Corners" to "/home/me/rumble-bots/Corners",
            "/home/me/other/Corners" to "/home/me/other/Corners",
            "/home/me/rumble-bots/Walls" to "Walls",
        ), shown, "only the bots whose folder names are alike are shown by their path")
        assertEquals(emptyMap<String, String>(), DownloadFolders.shownNames(emptyList()))
    }

    @Test
    @Tag("RCL-092")
    @Tag("Unit")
    @Tag("Negative")
    fun testRCL092_UnitNegative_aJoinedBotTakesTheLicenseOnlyOfTheBotWithItsNameVersionAndAuthors() {
        val known = KnownLicenses(listOf(
            BotIdentity("Walls", "1.0", listOf("Ann")) to "MIT",
            BotIdentity("Walls", "1.0", listOf("Bob")) to "Apache-2.0",
            BotIdentity("Corners", "1.0", listOf("Ann", "Bob")) to null,
            BotIdentity("Twin", "1.0", listOf("Ann")) to "MIT",
            BotIdentity("Twin", "1.0", listOf("Ann")) to "GPL-3.0-or-later",
        ))

        assertEquals("MIT", known.of(BotIdentity("Walls", "1.0", listOf("Ann"))))
        assertEquals("Apache-2.0", known.of(BotIdentity("Walls", "1.0", listOf(" Bob "))), "the authors tell bots of the same name and version apart")
        assertEquals("", known.of(BotIdentity("Corners", "1.0", listOf("Bob", "Ann"))), "a bot that declares no license says so, whatever the order of its authors")
        assertEquals(null, known.of(BotIdentity("Walls", "2.0", listOf("Ann"))), "another version is another bot")
        assertEquals(null, known.of(BotIdentity("Twin", "1.0", listOf("Ann"))), "two such bots with different licenses leave it not known")
    }

    @Test
    @Tag("RCL-092")
    @Tag("Unit")
    @Tag("Positive")
    fun testRCL092_UnitPositive_aStandardLicenseLinksToItsSpdxPage() {
        assertEquals("https://spdx.org/licenses/MIT.html", LicenseLinks.spdxPage("MIT"))
        assertEquals("https://spdx.org/licenses/GPL-3.0-or-later.html", LicenseLinks.spdxPage(" GPL-3.0-or-later "))
        RumbleLicenses.ALL.forEach { assertEquals(it.officialPage, LicenseLinks.spdxPage(it.id), "the same page as the Download bots screen: ${it.id}") }
        assertEquals("https://spdx.org/licenses/Apache-2.0.html", LicenseLinks.spdxPage("apache-2.0"), "letter case does not matter, the page is the one on the list")
        assertEquals("https://spdx.org/licenses/GPL-3.0.html", LicenseLinks.spdxPage("GPL-3.0"), "an identifier the list keeps as deprecated still has its page")
    }

    @Test
    @Tag("RCL-092")
    @Tag("Unit")
    @Tag("Negative")
    fun testRCL092_UnitNegative_otherLicenseTextHasNoLinkAndNoOtherAddressIsOpened() {
        listOf(null, "", "   ", "Proprietary license", "Proprietary", "Custom", "LicenseRef-foo", "MIT2", "MIT OR Apache-2.0", "<script>", "../x").forEach {
            assertEquals(null, LicenseLinks.spdxPage(it), it.toString())
        }
        listOf("MIT", "Apache-2.0", "mit", "GPL-3.0", "0BSD").forEach {
            val page = LicenseLinks.spdxPage(it)!!
            assertTrue(page.startsWith(LicenseLinks.SPDX_PAGES) && page.endsWith(".html") && page.removePrefix(LicenseLinks.SPDX_PAGES).matches(Regex("[A-Za-z0-9.+-]+\\.html")),
                "only a page on the SPDX site, made from an identifier on the list: $page")
        }
    }
}
