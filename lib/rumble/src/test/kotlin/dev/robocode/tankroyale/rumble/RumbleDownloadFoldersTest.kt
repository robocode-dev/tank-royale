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
        val labels = mapOf(DownloadFolders.key("C:\\Users\\me\\rumble-bots\\", windows = true) to DownloadFolders.LABEL)

        val read = DownloadFolders.parseLabels(DownloadFolders.formatLabels(labels), windows = true)

        assertEquals(labels, read)
        listOf("C:/Users/me/rumble-bots", "c:\\users\\ME\\rumble-bots", "C:/Users/me/rumble-bots/").forEach {
            assertTrue(DownloadFolders.isLabelled(it, read, windows = true), it)
        }
        assertFalse(DownloadFolders.isLabelled("/home/me/Rumble-Bots", mapOf("/home/me/rumble-bots" to DownloadFolders.LABEL), windows = false),
            "letter case counts where the file system tells it apart")
        val comma = mapOf("/home/me/bots, rumble" to DownloadFolders.LABEL, "/home/me/100%" to DownloadFolders.LABEL, "/x" to DownloadFolders.LABEL)
        assertEquals(comma, DownloadFolders.parseLabels(DownloadFolders.formatLabels(comma), windows = false), "a comma or a percent sign in a path keeps every pair in place")
    }

    @Test
    @Tag("RCL-089")
    @Tag("Unit")
    @Tag("Negative")
    fun testRCL089_UnitNegative_brokenTextIsLeftOutAndARemovedFolderLeavesNoLabel() {
        val read = DownloadFolders.parseLabels("/a/b,rumble-downloads,,x,/c/d", windows = false)
        assertEquals(mapOf("/a/b" to DownloadFolders.LABEL), read, "a half pair and an empty path are left out, the rest is kept")
        assertEquals(emptyMap<String, String>(), DownloadFolders.parseLabels("", windows = false))

        val labels = mapOf("/a/b" to DownloadFolders.LABEL, "/gone" to DownloadFolders.LABEL)
        assertEquals(mapOf("/a/b" to DownloadFolders.LABEL), DownloadFolders.keepLabelsOf(listOf(BotFolderSetting("/a/b/", false)), labels, windows = false))
    }

    @Test
    @Tag("RCL-090")
    @Tag("Unit")
    @Tag("Positive")
    fun testRCL090_UnitPositive_aNewDownloadFolderIsAddedSwitchedOnAndLabelledAtTheEnd() {
        val folder = Path.of("C:/Users/me/rumble-bots")

        val remembered = DownloadFolders.remember(own, emptyMap(), folder, windows = true)

        assertTrue(remembered.added)
        assertEquals(FolderOutcome.ADDED, remembered.outcome)
        assertEquals(own + BotFolderSetting(folder.toString(), true), remembered.folders, "the person's own folders are kept as they were, in order")
        assertTrue(DownloadFolders.isLabelled(folder.toString(), remembered.labels, windows = true))
    }

    @Test
    @Tag("RCL-090")
    @Tag("Unit")
    @Tag("Negative")
    fun testRCL090_UnitNegative_aFolderAlreadyThereKeepsItsPlaceAndSwitchAndIsLabelledOnlyWhenItHoldsOnlyDownloads() {
        val folders = own + BotFolderSetting("C:/Users/me/rumble-bots", false)

        val downloads = DownloadFolders.remember(folders, emptyMap(), Path.of("c:\\users\\me\\rumble-bots"), onlyDownloads = true, windows = true)
        assertEquals(FolderOutcome.SWITCHED_OFF, downloads.outcome, "the person's switch stays off, and the screen says so")
        assertEquals(folders, downloads.folders, "not added twice")
        assertTrue(DownloadFolders.isLabelled("C:/Users/me/rumble-bots", downloads.labels, windows = true))

        val labelledBefore = DownloadFolders.remember(own, mapOf(DownloadFolders.key("C:/Code/bots/java", true) to DownloadFolders.LABEL), Path.of("C:/Code/bots/java"), windows = true)
        assertEquals(FolderOutcome.LABELLED, labelledBefore.outcome, "a labelled folder stays labelled, switched on")

        val mine = DownloadFolders.remember(own, emptyMap(), Path.of("C:/Code/bots/java"), onlyDownloads = false, windows = true)
        assertEquals(FolderOutcome.OWN_FOLDER, mine.outcome, "one of the person's own bot directories")
        assertEquals(own to emptyMap<String, String>(), mine.folders to mine.labels, "their own bots are never moved into the group")
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
        val labels = mapOf(DownloadFolders.key("C:/Users/me/rumble-bots", windows = true) to DownloadFolders.LABEL)
        val folders = listOf(BotFolderSetting("C:/Users/me/rumble-bots", true)) + own

        val ownFolders = DownloadFolders.ownFolders(folders, labels, windows = true)

        assertEquals(listOf(Path.of("C:/Code/bots/java")), ownFolders, "the download folder and switched-off folders are skipped")
        assertEquals(Path.of("C:/Code/bots/rumble-bots"), BotDownloads.defaultFolder(ownFolders, Path.of("C:/Users/me")))
    }

    @Test
    @Tag("RCL-091")
    @Tag("Unit")
    @Tag("Positive")
    fun testRCL091_UnitPositive_aBotIsDownloadedOnlyWhenItLiesInALabelledFolder() {
        val labels = mapOf("/home/me/rumble-bots" to DownloadFolders.LABEL, "/home/me/other" to "something-else")

        assertTrue(DownloadFolders.isDownloaded("/home/me/rumble-bots/Corners", labels, windows = false))
        assertFalse(DownloadFolders.isDownloaded("/home/me/bots/Corners", labels, windows = false), "the person's own bot")
        assertFalse(DownloadFolders.isDownloaded("/home/me/other/Walls", labels, windows = false), "a folder with another label")
        assertFalse(DownloadFolders.isDownloaded("/home/me/rumble-bots/Corners/sub", labels, windows = false), "only the bot folders directly in it")
        assertFalse(DownloadFolders.isDownloaded("/home/me/rumble-bots/Corners", emptyMap(), windows = false), "no labels, no group")
    }

    @Test
    @Tag("RCL-092")
    @Tag("Unit")
    @Tag("Positive")
    fun testRCL092_UnitPositive_aStandardLicenseLinksToItsSpdxPage() {
        assertEquals("https://spdx.org/licenses/MIT.html", LicenseLinks.spdxPage("MIT"))
        assertEquals("https://spdx.org/licenses/GPL-3.0-or-later.html", LicenseLinks.spdxPage(" GPL-3.0-or-later "))
        RumbleLicenses.ALL.forEach { assertEquals(it.officialPage, LicenseLinks.spdxPage(it.id), "the same page as the Download bots screen: ${it.id}") }
        assertTrue(LicenseLinks.isSpdxPage(LicenseLinks.spdxPage("Apache-2.0")!!))
    }

    @Test
    @Tag("RCL-092")
    @Tag("Unit")
    @Tag("Negative")
    fun testRCL092_UnitNegative_otherLicenseTextHasNoLinkAndNoOtherAddressIsOpened() {
        listOf(null, "", "   ", "Proprietary license", "MIT OR Apache-2.0", "<script>", "../x").forEach {
            assertEquals(null, LicenseLinks.spdxPage(it), it.toString())
        }
        listOf("https://example.com/MIT.html", "https://spdx.org/licenses/../x.html", "https://spdx.org/licenses/MIT", "http://spdx.org/licenses/MIT.html")
            .forEach { assertFalse(LicenseLinks.isSpdxPage(it), it) }
    }
}
