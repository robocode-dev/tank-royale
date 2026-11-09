package dev.robocode.tankroyale.rumble

import dev.robocode.tankroyale.botproject.TreeHash
import dev.robocode.tankroyale.botproject.ValidationRules
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertInstanceOf
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Assumptions.assumeTrue
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.io.InputStream
import java.nio.file.Files
import java.nio.file.Path
import java.util.concurrent.Executor
import java.util.stream.Collectors
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

/** The Download bots screen, with a fake catalog and archive and real folders. Nothing here contacts GitHub. */
class RumbleDownloadTest {

    private val commit = "a".repeat(40)

    private fun files(name: String, version: String = "1.0.0", license: String = "MIT"): Map<String, ByteArray> = mapOf(
        "$name.json" to "{ \"name\": \"$name\", \"version\": \"$version\", \"license\": \"$license\" }".toByteArray(),
        "$name.sh" to "#!/bin/sh\n".toByteArray(),
        "src/$name.java" to "class $name {}\n".toByteArray(),
    )

    private fun bot(name: String, version: String = "1.0.0", platform: String = "JVM", status: String = CatalogBot.ACTIVE, content: Map<String, ByteArray> = files(name, version), hash: String = TreeHash.of(content)) =
        CatalogBot(name, version, platform, "bots/java/$name", hash, "someone", listOf("A. Author"), "2026-10-01", status)

    private fun catalog(vararg bots: CatalogBot) = Catalog(1, "now", commit, bots.toList())

    private fun zip(entries: Map<String, ByteArray>): ByteArray {
        val bytes = ByteArrayOutputStream()
        ZipOutputStream(bytes).use { zip ->
            entries.forEach { (name, content) ->
                zip.putNextEntry(ZipEntry(name))
                zip.write(content)
                zip.closeEntry()
            }
        }
        return bytes.toByteArray()
    }

    private inner class Source(val text: String? = null, val failArchive: Boolean = false, trees: Map<String, Map<String, ByteArray>> = emptyMap(), val extra: Map<String, ByteArray> = emptyMap()) : CatalogSource {
        val archives = mutableListOf<String>()
        val archive = zip(extra + trees.flatMap { (name, tree) -> tree.map { "rumble-bots-$commit/bots/java/$name/${it.key}" to it.value } }.toMap())
        override fun catalogText(commit: String) = text ?: throw CatalogUnreadable("The catalog cannot be read.")
        override fun archive(commit: String): InputStream {
            archives += commit
            if (failArchive) throw IOException("The connection was lost.")
            return ByteArrayInputStream(archive)
        }
    }

    private class Head : GitHubApi {
        override fun get(path: String, token: String?) = ApiResponse(200, "{\"sha\":\"" + "a".repeat(40) + "\"}")
        override fun postJson(path: String, token: String, json: String) = throw UnsupportedOperationException()
        override fun patchJson(path: String, token: String, json: String) = throw UnsupportedOperationException()
    }

    private fun write(folder: Path, content: Map<String, ByteArray>, marked: Boolean = true): Path {
        content.forEach { (path, bytes) ->
            val file = folder.resolve(path)
            Files.createDirectories(file.parent)
            Files.write(file, bytes)
        }
        if (marked) {
            Files.createDirectories(folder)
            Files.write(folder.resolve(BotDownloads.MARKER), ByteArray(0))
        }
        return folder
    }

    private fun tree(root: Path): Map<String, String> =
        Files.walk(root).use { s -> s.filter { Files.isRegularFile(it) }.collect(Collectors.toList()) }.associate { root.relativize(it).joinToString("/") to String(Files.readAllBytes(it)) }

    private fun states(rows: List<DownloadRow>) = rows.associate { it.name to it.state }

    // RCL-075

    @Test
    @Tag("RCL-075")
    @Tag("Unit")
    @Tag("Positive")
    fun testRCL075_UnitPositive_onlyActiveBotsAreListedAndThePlatformChoiceKeepsTheChosenOnes(@TempDir dir: Path) {
        val rows = BotDownloads.rows(
            catalog(
                bot("Walls"), bot("Orbit", platform = "Python"), bot("Fire", platform = "C#"),
                bot("Old", status = "superseded"), bot("Gone", status = "retired"), bot("Bad", status = "disqualified"),
            ),
            dir.resolve("none"),
        )
        assertEquals(listOf("Fire", "Orbit", "Walls"), rows.map { it.name })
        assertEquals(setOf(DownloadedState.NEW), rows.map { it.state }.toSet())

        val choosing = DownloadState.Choosing(dir, rows, setOf("JVM", "C#", "Python"))
        assertEquals(listOf("C#", "JVM", "Python"), choosing.allPlatforms)
        assertEquals(listOf("Fire", "Walls"), choosing.copy(platforms = setOf("JVM", "C#")).shown.map { it.name })
        assertEquals(listOf("Walls"), choosing.copy(platforms = setOf("JVM")).toDownload.map { it.name }, "a bot that is not shown is not downloaded")
    }

    @Test
    @Tag("RCL-075")
    @Tag("Unit")
    @Tag("Negative")
    fun testRCL075_UnitNegative_noBotsConfigurationFileIsFetchedBeforeTheDownloadAndABotInTheFolderShowsItsOwnLicense(@TempDir dir: Path) {
        val walls = bot("Walls", content = files("Walls", license = "Apache-2.0"))
        write(dir.resolve("Walls"), files("Walls", license = "Apache-2.0"))
        var fileReads = 0
        val source = object : CatalogSource {
            override fun catalogText(commit: String) = "{\"schemaVersion\":1,\"generatedAt\":\"x\",\"commit\":\"$commit\",\"bots\":[" +
                "{\"name\":\"Walls\",\"version\":\"1.0.0\",\"platform\":\"JVM\",\"path\":\"bots/java/Walls\",\"sourceHash\":\"${walls.sourceHash}\",\"owner\":\"a\",\"authors\":[\"A\"],\"addedAt\":\"x\",\"status\":\"active\"}," +
                "{\"name\":\"Orbit\",\"version\":\"1.0.0\",\"platform\":\"JVM\",\"path\":\"bots/java/Orbit\",\"sourceHash\":\"sha256:0\",\"owner\":\"a\",\"authors\":[\"A\"],\"addedAt\":\"x\",\"status\":\"active\"}]}"
            override fun archive(commit: String): InputStream = error("nothing is fetched before Download is pressed")
            override fun fileText(commit: String, path: String): String? {
                fileReads++
                return null
            }
        }

        val listed = LibraryDownloadPort(RumbleEnvironments.TEST, Head(), source).read(dir) as DownloadRead.Listed

        assertEquals(0, fileReads)
        assertEquals("Apache-2.0", listed.rows.first { it.name == "Walls" }.license)
        assertEquals(null, listed.rows.first { it.name == "Orbit" }.license, "a bot that is not in the folder has no known license")
    }

    @Test
    @Tag("RCL-075")
    @Tag("Unit")
    @Tag("Positive")
    fun testRCL075_UnitPositive_everyLicenseTheRumbleAllowsHasItsFixedPages() {
        assertEquals(ValidationRules.current.allowedLicenses.toSet(), RumbleLicenses.ALL.map { it.id }.toSet(), "a newly allowed license needs its explanation and links")
        val mit = RumbleLicenses.ALL.first { it.id == "MIT" }
        assertEquals("https://spdx.org/licenses/MIT.html", mit.officialPage)
        assertEquals("https://choosealicense.com/licenses/mit/", mit.explanationPage("en"))
        assertEquals("https://choosealicense.com/licenses/mit/", mit.explanationPage("da"), "no Danish page exists, so English")
        assertEquals("https://choosealicense.com/es/licenses/mit/", mit.explanationPage("es"))
        RumbleLicenses.ALL.forEach { license ->
            assertTrue(RumbleLicenses.isFixedPage(license.officialPage))
            listOf("en", "da", "ca", "es").forEach { assertTrue(RumbleLicenses.isFixedPage(license.explanationPage(it))) }
        }
    }

    @Test
    @Tag("RCL-075")
    @Tag("Unit")
    @Tag("Negative")
    fun testRCL075_UnitNegative_noOtherAddressCountsAsALicensePage() {
        listOf("https://spdx.org/licenses/Evil.html", "http://spdx.org/licenses/MIT.html", "https://evil.example/https://spdx.org/licenses/MIT.html", "https://github.com/x", "file:///C:/x", "").forEach {
            assertFalse(RumbleLicenses.isFixedPage(it), it)
        }
    }

    // RCL-076

    @Test
    @Tag("RCL-076")
    @Tag("Unit")
    @Tag("Positive")
    fun testRCL076_UnitPositive_eachBotShowsItsStateFromTheMarkerAndTheCatalogsChecksum(@TempDir dir: Path) {
        val catalog = catalog(
            bot("Same"), bot("Older", version = "2.0.0"), bot("Older", version = "1.0.0", status = "superseded"), bot("Edited"),
            bot("Mine"), bot("Retired", status = "retired"), bot("Fresh"),
        )
        write(dir.resolve("Same"), files("Same"))
        write(dir.resolve("Older"), files("Older", "1.0.0"))
        write(dir.resolve("Edited"), files("Edited") + ("src/Edited.java" to "class Mine {}\n".toByteArray()))
        write(dir.resolve("Mine"), files("Mine"), marked = false)
        write(dir.resolve("Retired"), files("Retired"))

        val before = tree(dir)
        val rows = BotDownloads.rows(catalog, dir)

        assertEquals(
            mapOf(
                "Same" to DownloadedState.UP_TO_DATE, "Older" to DownloadedState.UPDATE, "Edited" to DownloadedState.CHANGED_HERE,
                "Mine" to DownloadedState.KEPT_APART, "Retired" to DownloadedState.NO_LONGER_ACTIVE, "Fresh" to DownloadedState.NEW,
            ),
            states(rows),
        )
        assertEquals("Retired", rows.last().name, "what is no longer active comes last")
        assertEquals(null, rows.last().bot, "and cannot be downloaded")
        assertEquals(before, tree(dir), "looking changes nothing")
    }

    @Test
    @Tag("RCL-076")
    @Tag("Unit")
    @Tag("Positive")
    fun testRCL076_UnitPositive_whatRunningABotLeavesBehindIsNotAChangeButAnEditIs(@TempDir dir: Path) {
        val catalog = catalog(bot("Ran"), bot("Edited"))
        write(dir.resolve("Ran"), files("Ran") + mapOf("bin/Debug/x.txt" to "y".toByteArray(), "obj/z.txt" to "y".toByteArray(), "src/Ran.class" to "y".toByteArray(), "__pycache__/a" to "y".toByteArray()))
        write(dir.resolve("Edited"), files("Edited") + ("notes.txt" to "mine".toByteArray()))

        val rows = states(BotDownloads.rows(catalog, dir))

        assertEquals(DownloadedState.UP_TO_DATE, rows["Ran"])
        assertEquals(DownloadedState.CHANGED_HERE, rows["Edited"])
    }

    @Test
    @Tag("RCL-076")
    @Tag("Unit")
    @Tag("Positive")
    fun testRCL076_UnitPositive_aBotInTheFolderShowsTheVersionItsOwnConfigurationDeclares(@TempDir dir: Path) {
        val catalog = catalog(
            bot("Older", version = "1.1.0"), bot("Older", version = "1.0.0", status = "superseded"),
            bot("Edited"), bot("Placed"), bot("Fresh"), bot("Own"),
        )
        write(dir.resolve("Older"), files("Older", "1.0.0"))
        write(dir.resolve("Edited"), files("Edited", "1.0.1"))
        write(dir.resolve("Placed"), files("Placed"))
        write(dir.resolve("Own"), files("Own", "9.9.9"), marked = false)

        val rows = BotDownloads.rows(catalog, dir).associateBy { it.name }

        assertEquals("1.1.0" to "1.0.0", rows.getValue("Older").version to rows.getValue("Older").versionHere, "an update shows both")
        assertEquals("1.0.0" to "1.0.1", rows.getValue("Edited").version to rows.getValue("Edited").versionHere, "the person's own number is shown")
        assertEquals(DownloadedState.CHANGED_HERE, rows.getValue("Edited").state)
        assertEquals("1.0.0", rows.getValue("Placed").versionHere)
        assertEquals(null, rows.getValue("Fresh").versionHere, "nothing is in the folder yet")
        assertEquals(null, rows.getValue("Own").versionHere, "a folder the screen did not place is not read")
    }

    @Test
    @Tag("RCL-076")
    @Tag("Unit")
    @Tag("Negative")
    fun testRCL076_UnitNegative_aMarkedFolderWithAnUnreadableConfigurationOrAVersionTheCatalogDoesNotKnowIsTreatedAsChanged(@TempDir dir: Path) {
        val catalog = catalog(bot("Broken"), bot("Unknown", version = "2.0.0"))
        write(dir.resolve("Broken"), mapOf("Broken.json" to "not json".toByteArray()))
        write(dir.resolve("Unknown"), files("Unknown", "9.9.9"))

        val rows = states(BotDownloads.rows(catalog, dir))

        assertEquals(DownloadedState.CHANGED_HERE, rows["Broken"])
        assertEquals(DownloadedState.CHANGED_HERE, rows["Unknown"], "when in doubt it is never replaced on its own")
    }

    @Test
    @Tag("RCL-076")
    @Tag("Unit")
    @Tag("Positive")
    fun testRCL076_UnitPositive_theDefaultFolderIsBesideTheFirstBotFolderOrInTheHomeFolder(@TempDir dir: Path) {
        assertEquals(dir.resolve("rumble-bots"), BotDownloads.defaultFolder(listOf(dir.resolve("my-bots"), dir.resolve("other")), dir.resolve("home")))
        assertEquals(dir.resolve("home").resolve("rumble-bots"), BotDownloads.defaultFolder(emptyList(), dir.resolve("home")))
        assertFalse(Files.exists(dir.resolve("rumble-bots")), "nothing is created")
    }

    // RCL-077

    @Test
    @Tag("RCL-077")
    @Tag("Unit")
    @Tag("Positive")
    fun testRCL077_UnitPositive_matchingBotsArePlacedWithAnEmptyMarkerAndExactlyTheirOwnFilesAndEachResultIsTold(@TempDir dir: Path) {
        val walls = files("Walls", license = "BSD-3-Clause") + ("LICENSE" to "the author's own".toByteArray())
        val catalog = catalog(bot("Walls", content = walls), bot("Tampered", content = files("Tampered"), hash = "sha256:other"), bot("Missing"))
        val source = Source(trees = mapOf("Walls" to walls, "Tampered" to files("Tampered")))
        val target = dir.resolve("rumble-bots")

        val results = BotDownloads.download(source, catalog, catalog.bots, emptySet(), target)

        assertEquals(listOf(commit), source.archives, "the sources come from the catalog's own commit")
        val placed = results[0] as DownloadResult.Placed
        assertEquals("BSD-3-Clause", placed.license, "read from the bot's own file")
        assertFalse(placed.updated)
        assertInstanceOf(DownloadResult.HashDiffers::class.java, results[1])
        assertInstanceOf(DownloadResult.NotInArchive::class.java, results[2])
        assertEquals(
            walls.map { it.key to String(it.value) }.toMap() + (BotDownloads.MARKER to ""),
            tree(target.resolve("Walls")),
            "exactly the source, with the author's license file, plus the empty marker",
        )
        assertEquals(listOf("Walls"), Files.list(target).use { s -> s.map { it.fileName.toString() }.collect(Collectors.toList()) }, "nothing of a failing bot and no working folder is left")
    }

    @Test
    @Tag("RCL-077")
    @Tag("Unit")
    @Tag("Positive")
    fun testRCL077_UnitPositive_anUnchangedEarlierDownloadIsReplacedAndAChangedOneOnlyWhenAsked(@TempDir dir: Path) {
        val newer = files("Older", "2.0.0")
        val edited = files("Edited", "2.0.0")
        val catalog = catalog(
            bot("Older", "2.0.0", content = newer), bot("Older", "1.0.0", status = "superseded"),
            bot("Edited", "2.0.0", content = edited), bot("Edited", "1.0.0", status = "superseded"),
        )
        val source = Source(trees = mapOf("Older" to newer, "Edited" to edited))
        write(dir.resolve("Older"), files("Older", "1.0.0") + ("bin/left.txt" to "y".toByteArray()))
        val changed = files("Edited", "1.0.0") + ("src/Edited.java" to "class Mine {}\n".toByteArray())
        write(dir.resolve("Edited"), changed)
        val mineBefore = tree(dir.resolve("Edited"))

        val first = BotDownloads.download(source, catalog, catalog.activeBots, emptySet(), dir)
        assertTrue((first[0] as DownloadResult.Placed).updated)
        assertEquals(newer.map { it.key to String(it.value) }.toMap() + (BotDownloads.MARKER to ""), tree(dir.resolve("Older")))
        assertInstanceOf(DownloadResult.KeptChanged::class.java, first[1])
        assertEquals(mineBefore, tree(dir.resolve("Edited")), "not replaced unless the person asks")

        val second = BotDownloads.download(source, catalog, catalog.activeBots, setOf("Edited"), dir)
        assertInstanceOf(DownloadResult.UpToDate::class.java, second[0], "an up to date bot is left alone")
        assertTrue((second[1] as DownloadResult.Placed).updated, "asked for by name")
        assertEquals(edited.map { it.key to String(it.value) }.toMap() + (BotDownloads.MARKER to ""), tree(dir.resolve("Edited")))
    }

    @Test
    @Tag("RCL-077")
    @Tag("Unit")
    @Tag("Negative")
    fun testRCL077_UnitNegative_aFolderWithoutTheMarkerIsNeverTouchedEvenWhenTheNamesMatchOrItIsAskedFor(@TempDir dir: Path) {
        val mine = write(dir.resolve("Walls"), mapOf("Walls.json" to "my own bot".toByteArray()), marked = false)
        val before = tree(mine)
        val catalog = catalog(bot("Walls"))
        val source = Source(trees = mapOf("Walls" to files("Walls")))

        val results = BotDownloads.download(source, catalog, catalog.bots, setOf("Walls"), dir)

        assertInstanceOf(DownloadResult.KeptOwn::class.java, results.single())
        assertEquals(before, tree(mine))
        assertEquals(emptyList<String>(), source.archives, "nothing is fetched for it")
    }

    @Test
    @Tag("RCL-077")
    @Tag("Unit")
    @Tag("Negative")
    fun testRCL077_UnitNegative_theScreenOnlyPlacesFilesAndStartsNothing() {
        val root = generateSequence(Path.of("").toAbsolutePath()) { it.parent }.first { Files.exists(it.resolve("settings.gradle.kts")) }
        val code = listOf("BotDownloads.kt", "DownloadScreen.kt").joinToString("\n") {
            Files.readString(root.resolve("lib/rumble/src/main/kotlin/dev/robocode/tankroyale/rumble").resolve(it))
        }
        listOf("ProcessBuilder", "Runtime.getRuntime", "ConfigSettings", "botDirectories").forEach { assertFalse(code.contains(it), it) }
    }

    @Test
    @Tag("RCL-077")
    @Tag("Unit")
    @Tag("Positive")
    fun testRCL077_UnitPositive_theLauncherScriptCanBeRunAfterTheDownloadWhereFilesHaveThatRight(@TempDir dir: Path) {
        assumeTrue("posix" in dir.fileSystem.supportedFileAttributeViews())
        val catalog = catalog(bot("Walls"))

        BotDownloads.download(Source(trees = mapOf("Walls" to files("Walls"))), catalog, catalog.bots, emptySet(), dir)

        assertTrue(Files.isExecutable(dir.resolve("Walls/Walls.sh")))
    }

    // RCL-078

    @Test
    @Tag("RCL-078")
    @Tag("Unit")
    @Tag("Negative")
    fun testRCL078_UnitNegative_aCatalogThatCannotBeReadOrIsNewerHasItsOwnFailure(@TempDir dir: Path) {
        val unreadable = LibraryDownloadPort(RumbleEnvironments.TEST, Head(), Source(text = null)).read(dir)
        assertEquals(DownloadFailure.CATALOG_UNREADABLE, (unreadable as DownloadRead.Failed).failure)

        val newer = LibraryDownloadPort(RumbleEnvironments.TEST, Head(), Source(text = "{\"schemaVersion\":99,\"generatedAt\":\"x\",\"commit\":\"$commit\",\"bots\":[]}")).read(dir)
        assertEquals(DownloadFailure.CATALOG_UNSUPPORTED, (newer as DownloadRead.Failed).failure)

        val broken = LibraryDownloadPort(RumbleEnvironments.TEST, Head(), Source(text = "not a catalog")).read(dir)
        assertEquals(DownloadFailure.CATALOG_UNREADABLE, (broken as DownloadRead.Failed).failure)
    }

    @Test
    @Tag("RCL-078")
    @Tag("Unit")
    @Tag("Negative")
    fun testRCL078_UnitNegative_aFolderThatCannotBeWrittenIsToldBeforeAnythingIsFetched(@TempDir dir: Path) {
        val file = Files.writeString(dir.resolve("a-file"), "x")
        val catalog = catalog(bot("Walls"))
        val source = Source(trees = mapOf("Walls" to files("Walls")))

        val ended = LibraryDownloadPort(RumbleEnvironments.TEST, Head(), source).download(catalog, catalog.bots, emptySet(), file.resolve("below"))

        assertEquals(DownloadFailure.FOLDER_NOT_WRITABLE, (ended as DownloadEnded.Failed).failure)
        assertEquals(emptyList<String>(), source.archives)
    }

    @Test
    @Tag("RCL-078")
    @Tag("Unit")
    @Tag("Negative")
    fun testRCL078_UnitNegative_aNetworkFailureKeepsWhatWasPlacedAndCanBeContinued(@TempDir dir: Path) {
        val placed = write(dir.resolve("Walls"), files("Walls"))
        val before = tree(placed)
        val catalog = catalog(bot("Walls", version = "1.0.0"), bot("Orbit"))

        val failed = LibraryDownloadPort(RumbleEnvironments.TEST, Head(), Source(failArchive = true)).download(catalog, catalog.bots, emptySet(), dir)

        assertEquals(DownloadFailure.NETWORK, (failed as DownloadEnded.Failed).failure)
        assertEquals(before, tree(placed))
        assertEquals(listOf("Walls"), Files.list(dir).use { s -> s.map { it.fileName.toString() }.collect(Collectors.toList()) }, "no working folder is left")

        val again = LibraryDownloadPort(RumbleEnvironments.TEST, Head(), Source(trees = mapOf("Orbit" to files("Orbit")))).download(catalog, catalog.bots, emptySet(), dir) as DownloadEnded.Done
        assertInstanceOf(DownloadResult.UpToDate::class.java, again.results[0], "what was downloaded is not fetched again")
        assertInstanceOf(DownloadResult.Placed::class.java, again.results[1])
    }

    @Test
    @Tag("RCL-078")
    @Tag("Unit")
    @Tag("Negative")
    fun testRCL078_UnitNegative_anArchiveThatTriesToEscapeWritesNothingAndAnOrdinaryOneStillWorks(@TempDir dir: Path) {
        val target = dir.resolve("rumble-bots")
        val catalog = catalog(bot("Walls"))
        val evil = Source(trees = mapOf("Walls" to files("Walls")), extra = mapOf("rumble-bots-$commit/bots/java/Walls/../../../../../escaped.txt" to "x".toByteArray()))

        val failed = LibraryDownloadPort(RumbleEnvironments.TEST, Head(), evil).download(catalog, catalog.bots, emptySet(), target)

        assertEquals(DownloadFailure.UNSAFE_ARCHIVE, (failed as DownloadEnded.Failed).failure)
        assertEquals(emptyList<String>(), Files.walk(dir).use { s -> s.filter { Files.isRegularFile(it) }.map { it.toString() }.collect(Collectors.toList()) }, "nothing anywhere")

        val ended = LibraryDownloadPort(RumbleEnvironments.TEST, Head(), Source(trees = mapOf("Walls" to files("Walls")))).download(catalog, catalog.bots, emptySet(), target)
        assertInstanceOf(DownloadResult.Placed::class.java, (ended as DownloadEnded.Done).results.single())
    }

    // RCL-079

    private class FakePort(var read: DownloadRead, var ended: DownloadEnded = DownloadEnded.Done(emptyList())) : DownloadPort {
        val reads = mutableListOf<Path>()
        val downloads = mutableListOf<List<CatalogBot>>()
        val replaced = mutableListOf<Set<String>>()
        val threads = mutableListOf<Thread>()
        override fun read(folder: Path): DownloadRead {
            reads.add(folder)
            threads += Thread.currentThread()
            return read
        }
        override fun download(catalog: Catalog, bots: List<CatalogBot>, replaceChanged: Set<String>, folder: Path): DownloadEnded {
            downloads += bots
            replaced += replaceChanged
            threads += Thread.currentThread()
            return ended
        }
    }

    /** Holds back what is given to it until the test lets it go. */
    private class Held : Executor {
        val queue = ArrayDeque<Runnable>()
        override fun execute(command: Runnable) {
            queue.addLast(command)
        }
        fun runAll() {
            while (queue.isNotEmpty()) queue.removeFirst().run()
        }
    }

    private val direct = Executor { it.run() }

    private fun listed(vararg bots: CatalogBot, folder: Path) = catalog(*bots).let { DownloadRead.Listed(it, BotDownloads.rows(it, folder)) }

    @Test
    @Tag("RCL-079")
    @Tag("Unit")
    @Tag("Positive")
    fun testRCL079_UnitPositive_nothingIsFetchedBeforeDownloadAndAllWorkRunsAwayFromTheScreenAndInOrder(@TempDir dir: Path) {
        val port = FakePort(listed(bot("Walls"), bot("Orbit"), folder = dir), DownloadEnded.Done(listOf(DownloadResult.UpToDate(bot("Walls")))))
        val background = Held()
        val seen = mutableListOf<String>()
        val controller = DownloadController(port, dir, background, direct) { seen += it::class.simpleName!! }

        controller.start()
        assertEquals(emptyList<Path>(), port.reads, "the screen is free until the background runs")
        background.runAll()
        assertInstanceOf(DownloadState.Choosing::class.java, controller.state)
        assertEquals(emptyList<List<CatalogBot>>(), port.downloads, "nothing is fetched yet")

        controller.download()
        assertInstanceOf(DownloadState.Downloading::class.java, controller.state)
        background.runAll()

        assertEquals(listOf("Reading", "Choosing", "Downloading", "Ended"), seen)
        assertEquals(1, port.downloads.size)
    }

    @Test
    @Tag("RCL-079")
    @Tag("Unit")
    @Tag("Negative")
    fun testRCL079_UnitNegative_aResultAfterClosingIsThrownAwayAndASecondPressStartsNothing(@TempDir dir: Path) {
        val port = FakePort(listed(bot("Walls"), folder = dir))
        val background = Held()
        val controller = DownloadController(port, dir, background, direct) {}

        controller.start()
        controller.close()
        background.runAll()
        assertInstanceOf(DownloadState.Reading::class.java, controller.state, "the late result changed nothing")

        controller.start()
        background.runAll()
        controller.download()
        controller.download()
        background.runAll()
        assertEquals(1, port.downloads.size, "a second press starts nothing new")

        controller.start()
        background.runAll()
        controller.download()
        controller.close()
        background.runAll()
        assertInstanceOf(DownloadState.Downloading::class.java, controller.state, "the ending of a download that was left is thrown away")
    }

    @Test
    @Tag("RCL-079")
    @Tag("Unit")
    @Tag("Negative")
    fun testRCL079_UnitNegative_aChoiceThatLeavesNothingToDownloadOffersNothingAndChoicesReachThePort(@TempDir dir: Path) {
        write(dir.resolve("Edited"), files("Edited") + ("notes.txt" to "x".toByteArray()))
        val port = FakePort(listed(bot("Walls"), bot("Edited"), folder = dir))
        val controller = DownloadController(port, dir, direct, direct) {}
        controller.start()

        controller.setAll(false)
        assertEquals(emptyList<CatalogBot>(), (controller.state as DownloadState.Choosing).toDownload)
        controller.download()
        assertEquals(emptyList<List<CatalogBot>>(), port.downloads, "nothing is chosen, so nothing happens")

        controller.setChosen("Walls", true)
        controller.setReplaceChanged("Edited", true)
        controller.download()
        assertEquals(listOf("Edited", "Walls"), port.downloads.single().map { it.name })
        assertEquals(setOf("Edited"), port.replaced.single())
    }

    @Test
    @Tag("RCL-079")
    @Tag("Unit")
    @Tag("Negative")
    fun testRCL079_UnitNegative_aCallThatThrowsIsAPlainFailureWithAWayOn(@TempDir dir: Path) {
        val port = object : DownloadPort {
            var fail = true
            override fun read(folder: Path): DownloadRead {
                if (fail) throw IOException("kaboom")
                return DownloadRead.Listed(catalog(), emptyList())
            }
            override fun download(catalog: Catalog, bots: List<CatalogBot>, replaceChanged: Set<String>, folder: Path): DownloadEnded = error("not used")
        }
        val controller = DownloadController(port, dir, direct, direct) {}

        controller.start()
        assertEquals(DownloadFailure.OTHER, (controller.state as DownloadState.Failed).failure)

        port.fail = false
        controller.start()
        assertInstanceOf(DownloadState.Choosing::class.java, controller.state, "trying again works")
    }
}
