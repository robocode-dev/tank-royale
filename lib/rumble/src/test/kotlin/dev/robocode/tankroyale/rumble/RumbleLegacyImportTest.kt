package dev.robocode.tankroyale.rumble

import org.junit.jupiter.api.Assertions.assertArrayEquals
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertInstanceOf
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.attribute.FileTime
import java.security.MessageDigest
import java.time.Clock
import java.time.Instant
import java.time.ZoneOffset

class RumbleLegacyImportTest {

    private val publicBots = "https://github.com/robocode-dev/rumble-bots"
    private val publicData = "https://github.com/robocode-dev/rumble-data"
    private val testBots = "https://github.com/robocode-dev/rumble-bots-test"
    private val testData = "https://github.com/robocode-dev/rumble-data-test"
    private val secretInJournal = "ghr_FAKEJOURNALSECRET0123456789"

    private class Old(val config: Path, val work: Path)

    /** An old client's folder: its configuration file beside a work folder with a journal, evidence, a cache and a stray file. */
    private fun oldClient(
        dir: Path, bots: String = publicBots, data: String = publicData, clientId: String? = "alice-1a2b3c4d",
        version: Int = 1, workDirectory: String? = ".rumble-client", withFiles: Boolean = true, name: String = "old",
    ): Old {
        val root = Files.createDirectories(dir.resolve(name))
        val config = root.resolve("rumble-client.json")
        val fields = mutableListOf(
            "\"schemaVersion\": $version", "\"botsRepo\": \"$bots\"", "\"dataRepo\": \"$data\"",
            "\"myBots\": [\"Walls\", \"Orbit\"]", "\"gameTypes\": [\"1v1\", \"melee\"]", "\"battlesPerSession\": 25", "\"mode\": \"ranked\"",
        )
        if (clientId != null) fields += "\"clientId\": \"$clientId\""
        if (workDirectory != null) fields += "\"workDirectory\": \"$workDirectory\""
        Files.writeString(config, "{ ${fields.joinToString(", ")} }")
        val work = root.resolve(workDirectory ?: ".rumble-client")
        if (withFiles) {
            Files.createDirectories(work.resolve("journal"))
            Files.writeString(work.resolve("journal/records.jsonl"), "{\"battleId\":\"b1\",\"note\":\"$secretInJournal\"}\n{\"battleId\":\"b2\"}\n")
            Files.writeString(work.resolve("journal/acknowledgements.jsonl"), "{\"battleId\":\"b1\",\"receipt\":7}\n")
            Files.writeString(work.resolve("journal/quarantine.jsonl"), "")
            Files.writeString(work.resolve("journal/submissions.jsonl"), "{\"issueNumber\":7,\"battleIds\":[\"b1\"]}\n")
            Files.createDirectories(work.resolve("evidence/1v1/b1"))
            Files.write(work.resolve("evidence/1v1/b1/replay.json.gz"), ByteArray(5000) { (it * 31).toByte() })
            Files.createDirectories(work.resolve("cache/bots/Walls"))
            Files.writeString(work.resolve("cache/bots/Walls/Walls.json"), "{}")
            Files.writeString(work.resolve("notes.txt"), "stray")
        } else {
            Files.createDirectories(work)
        }
        return Old(config, work)
    }

    private fun importer(base: Path, hook: (Path, Path) -> Unit = { _, _ -> }) =
        LegacyClientImport(base, Clock.fixed(Instant.parse("2026-11-02T12:00:00Z"), ZoneOffset.UTC), "9.9.9", hook)

    private fun sha(path: Path) = MessageDigest.getInstance("SHA-256").digest(Files.readAllBytes(path)).joinToString("") { "%02x".format(it) }

    /** Everything below [dir]: relative path to hash and modification time, or "dir". */
    private fun snapshot(dir: Path): Map<String, String> =
        if (!Files.exists(dir)) emptyMap()
        else dir.toFile().walkTopDown().filter { it != dir.toFile() }.associate { f ->
            f.relativeTo(dir.toFile()).path.replace('\\', '/') to
                (if (f.isDirectory) "dir" else sha(f.toPath()) + "@" + Files.getLastModifiedTime(f.toPath()).toMillis())
        }

    // RLI-001

    @Test
    @Tag("RLI-001")
    @Tag("Unit")
    @Tag("Positive")
    fun testRLI001_UnitPositive_theConfigurationIsReadAndARelativeWorkFolderIsTakenFromTheConfigurationsFolder(@TempDir dir: Path) {
        val old = oldClient(dir)

        val plan = (importer(dir.resolve("state")).preview(old.config) as LegacyPreview.Plan).plan

        val c = plan.configuration
        assertEquals("alice-1a2b3c4d", c.clientId)
        assertEquals(listOf("Walls", "Orbit"), c.myBots)
        assertEquals(listOf("1v1", "melee"), c.gameTypes)
        assertEquals(25, c.battlesPerSession)
        assertEquals("ranked", c.mode)
        assertEquals(old.work.toAbsolutePath().normalize(), plan.workFolder)

        val placeholder = oldClient(dir, clientId = "replace-with-registered-client-id", name = "placeholder")
        assertNull((importer(dir.resolve("state2")).preview(placeholder.config) as LegacyPreview.Plan).plan.configuration.clientId)
        val empty = oldClient(dir, clientId = "", name = "empty")
        assertNull((importer(dir.resolve("state3")).preview(empty.config) as LegacyPreview.Plan).plan.configuration.clientId)
    }

    @Test
    @Tag("RLI-001")
    @Tag("Unit")
    @Tag("Negative")
    fun testRLI001_UnitNegative_anUnknownVersionABrokenFileAMissingFieldAndAMissingFolderAreRefusedBeforeAnythingIsCopied(@TempDir dir: Path) {
        val state = dir.resolve("state")
        val newer = oldClient(dir, version = 2, name = "newer")
        val refusedVersion = importer(state).import(newer.config) as LegacyImportResult.Refused
        assertTrue(refusedVersion.message.contains("version 2") && refusedVersion.message.contains("Update the program"), refusedVersion.message)

        val broken = oldClient(dir, name = "broken")
        Files.writeString(broken.config, "{ not json")
        assertInstanceOf(LegacyImportResult.Refused::class.java, importer(state).import(broken.config))

        val missing = oldClient(dir, name = "missing")
        Files.writeString(missing.config, """{ "schemaVersion": 1, "dataRepo": "$publicData", "workDirectory": ".rumble-client" }""")
        assertInstanceOf(LegacyImportResult.Refused::class.java, importer(state).import(missing.config))

        val noWork = oldClient(dir, workDirectory = null, name = "nowork")
        assertInstanceOf(LegacyImportResult.Refused::class.java, importer(state).import(noWork.config))

        val noFolder = oldClient(dir, name = "nofolder")
        noFolder.work.toFile().deleteRecursively()
        assertInstanceOf(LegacyPreview.Refused::class.java, importer(state).preview(noFolder.config))
        assertFalse(Files.exists(state), "Nothing may be created before the refusals")

        val badId = oldClient(dir, clientId = "bad id with spaces!", name = "badid")
        assertInstanceOf(LegacyImportResult.Refused::class.java, importer(state).import(badId.config))
        assertFalse(Files.exists(state))
    }

    // RLI-002

    @Test
    @Tag("RLI-002")
    @Tag("Unit")
    @Tag("Positive")
    fun testRLI002_UnitPositive_twoPublicAddressesGiveThePublicEnvironmentAndTwoTestAddressesTheTestEnvironmentInAnyForm(@TempDir dir: Path) {
        val publicOld = oldClient(dir, bots = "git@github.com:robocode-dev/rumble-bots.git", data = "robocode-dev/rumble-data", name = "p")
        val testOld = oldClient(dir, bots = testBots, data = "https://github.com/robocode-dev/rumble-data-test.git", name = "t")

        assertEquals("public", (importer(dir.resolve("s1")).preview(publicOld.config) as LegacyPreview.Plan).plan.environment.name)
        assertEquals("test", (importer(dir.resolve("s2")).preview(testOld.config) as LegacyPreview.Plan).plan.environment.name)
    }

    @Test
    @Tag("RLI-002")
    @Tag("Unit")
    @Tag("Negative")
    fun testRLI002_UnitNegative_aMixAnUnknownAddressAndSwappedAddressesAreRefusedWithTheProblemNamed(@TempDir dir: Path) {
        val state = dir.resolve("state")
        val mixed = oldClient(dir, bots = publicBots, data = testData, name = "mixed")
        val mixedResult = importer(state).import(mixed.config) as LegacyImportResult.Refused
        assertTrue(mixedResult.message.contains("Nothing was copied"), mixedResult.message)

        val unknown = oldClient(dir, bots = "https://github.com/someone/else", data = publicData, name = "unknown")
        val unknownResult = importer(state).import(unknown.config) as LegacyImportResult.Refused
        assertTrue(unknownResult.message.contains("bot repository"), unknownResult.message)

        val swapped = oldClient(dir, bots = publicData, data = publicBots, name = "swapped")
        assertInstanceOf(LegacyImportResult.Refused::class.java, importer(state).import(swapped.config))

        val lookalike = oldClient(dir, bots = "https://github.com/robocode-dev/rumble-bots-tést", data = publicData, name = "lookalike")
        assertInstanceOf(LegacyImportResult.Refused::class.java, importer(state).import(lookalike.config))
        assertFalse(Files.exists(state))
    }

    // RLI-003

    @Test
    @Tag("RLI-003")
    @Tag("Unit")
    @Tag("Positive")
    fun testRLI003_UnitPositive_thePreviewListsWhatWouldBeCopiedAndWhatWouldNotAndWritesNothing(@TempDir dir: Path) {
        val old = oldClient(dir)
        val state = dir.resolve("state")
        val before = snapshot(dir)

        val plan = (importer(state).preview(old.config) as LegacyPreview.Plan).plan

        assertEquals(
            listOf(
                "evidence/1v1/b1/replay.json.gz", "journal/acknowledgements.jsonl", "journal/quarantine.jsonl",
                "journal/records.jsonl", "journal/submissions.jsonl",
            ),
            plan.files.map { it.path }.sorted(),
        )
        assertEquals(5000L, plan.files.first { it.path.endsWith("replay.json.gz") }.size)
        assertEquals(setOf("cache", "notes.txt"), plan.notCopied.map { it.path }.toSet())
        assertTrue(plan.notCopied.first { it.path == "cache" }.reason.contains("downloaded again"))
        assertTrue(plan.target.toString().contains("legacy-client"))
        assertEquals(before, snapshot(dir), "The preview must write nothing")
    }

    @Test
    @Tag("RLI-003")
    @Tag("Unit")
    @Tag("Negative")
    fun testRLI003_UnitNegative_aFolderWithNothingToCopyGivesNothingToImport(@TempDir dir: Path) {
        val old = oldClient(dir, withFiles = false)
        Files.writeString(old.work.resolve("stray.txt"), "x")

        assertInstanceOf(LegacyPreview.NothingToImport::class.java, importer(dir.resolve("state")).preview(old.config))
        assertInstanceOf(LegacyImportResult.NothingToImport::class.java, importer(dir.resolve("state")).import(old.config))
    }

    // RLI-004

    @Test
    @Tag("RLI-004")
    @Tag("Unit")
    @Tag("Positive")
    fun testRLI004_UnitPositive_everyFileIsCopiedByteForByteBelowTheEnvironmentsImportedStateFolder(@TempDir dir: Path) {
        val old = oldClient(dir)
        val state = dir.resolve("state")

        val result = importer(state).import(old.config) as LegacyImportResult.Imported

        assertEquals(5, result.copied)
        val target = LegacyClientImport.targetOf(RumbleEnvironments.PUBLIC, state)
        assertEquals(target, result.target)
        listOf("evidence/1v1/b1/replay.json.gz", "journal/records.jsonl", "journal/acknowledgements.jsonl", "journal/quarantine.jsonl", "journal/submissions.jsonl").forEach { path ->
            assertArrayEquals(Files.readAllBytes(old.work.resolve(path)), Files.readAllBytes(target.resolve("files").resolve(path)), path)
        }
        assertTrue(snapshot(target).keys.none { it.endsWith(".part") })
        assertFalse(Files.exists(target.resolve("files/cache")), "The bot cache is not copied")
    }

    @Test
    @Tag("RLI-004")
    @Tag("Unit")
    @Tag("Negative")
    fun testRLI004_UnitNegative_aCopyThatDoesNotMatchTheSourceIsRemovedAndNeverGivenAFinalName(@TempDir dir: Path) {
        val old = oldClient(dir)
        val state = dir.resolve("state")
        val corrupt: (Path, Path) -> Unit = { source, temp -> if (source.fileName.toString() == "records.jsonl") Files.writeString(temp, "tampered") }

        val result = importer(state, corrupt).import(old.config)

        assertInstanceOf(LegacyImportResult.Incomplete::class.java, result)
        val target = LegacyClientImport.targetOf(RumbleEnvironments.PUBLIC, state)
        assertFalse(Files.exists(target.resolve("files/journal/records.jsonl")))
        assertTrue(snapshot(target).keys.none { it.endsWith(".part") })
        assertFalse(Files.exists(target.resolve("import-record.json")))
    }

    // RLI-005

    @Test
    @Tag("RLI-005")
    @Tag("Unit")
    @Tag("Positive")
    fun testRLI005_UnitPositive_theOldFolderAndConfigurationAreUnchangedAfterEveryKindOfRun(@TempDir dir: Path) {
        val old = oldClient(dir)
        Files.setLastModifiedTime(old.work.resolve("journal/records.jsonl"), FileTime.fromMillis(1_700_000_000_000))
        val before = snapshot(old.config.parent)
        val state = dir.resolve("state")

        importer(state).import(old.config)
        importer(state).import(old.config)
        var calls = 0
        importer(dir.resolve("state2")) { _, _ -> if (++calls == 2) throw IOException("interrupted") }.import(old.config)
        importer(dir.resolve("state3")) { source, temp -> Files.writeString(temp, "x") }.import(old.config)
        importer(dir.resolve("state.txt").also { Files.writeString(it, "a file, not a folder") }).import(old.config)

        assertEquals(before, snapshot(old.config.parent), "Not one file of the old client may change")
        assertEquals(1_700_000_000_000, Files.getLastModifiedTime(old.work.resolve("journal/records.jsonl")).toMillis())
    }

    @Test
    @Tag("RLI-005")
    @Tag("Unit")
    @Tag("Negative")
    fun testRLI005_UnitNegative_aStateFolderInsideTheOldFolderIsRefused(@TempDir dir: Path) {
        val old = oldClient(dir)
        val before = snapshot(old.config.parent)

        val inside = importer(old.work.resolve("state")).import(old.config)

        assertInstanceOf(LegacyImportResult.Refused::class.java, inside)
        assertEquals(before, snapshot(old.config.parent))
    }

    // RLI-006

    @Test
    @Tag("RLI-006")
    @Tag("Unit")
    @Tag("Positive")
    fun testRLI006_UnitPositive_theRecordHoldsEveryFileItsHashTheTimeTheVersionAndTheEnvironment(@TempDir dir: Path) {
        val old = oldClient(dir)
        val state = dir.resolve("state")

        importer(state).import(old.config)

        val record = Files.readString(LegacyClientImport.targetOf(RumbleEnvironments.PUBLIC, state).resolve("import-record.json"))
        listOf("\"complete\": true", "\"environment\": \"public\"", "\"programVersion\": \"9.9.9\"", "2026-11-02T12:00:00Z", "journal/records.jsonl",
            "sha256:" + sha(old.work.resolve("journal/records.jsonl"))).forEach { assertTrue(record.contains(it), it) }
    }

    @Test
    @Tag("RLI-006")
    @Tag("Unit")
    @Tag("Negative")
    fun testRLI006_UnitNegative_anEarlierRecordIsNotLeftStandingWhileALaterImportIsIncomplete(@TempDir dir: Path) {
        val old = oldClient(dir)
        val state = dir.resolve("state")
        val record = LegacyClientImport.targetOf(RumbleEnvironments.PUBLIC, state).resolve("import-record.json")
        importer(state).import(old.config)
        assertTrue(Files.exists(record))
        Files.writeString(old.work.resolve("journal/records.jsonl"), "{\"battleId\":\"b3\"}\n", java.nio.file.StandardOpenOption.APPEND)

        val grow: (Path, Path) -> Unit = { source, _ -> if (source.fileName.toString() == "records.jsonl") Files.writeString(source, "more\n", java.nio.file.StandardOpenOption.APPEND) }
        val result = importer(state, grow).import(old.config)

        assertInstanceOf(LegacyImportResult.Incomplete::class.java, result)
        assertFalse(Files.exists(record), "A record may only exist for an import that completed")
    }

    // RLI-007

    @Test
    @Tag("RLI-007")
    @Tag("Unit")
    @Tag("Positive")
    fun testRLI007_UnitPositive_aSecondRunCompletesAnInterruptedImportAndRepeatsNothing(@TempDir dir: Path) {
        val old = oldClient(dir)
        val state = dir.resolve("state")
        var calls = 0
        val crash: (Path, Path) -> Unit = { _, _ -> if (++calls == 3) throw IOException("power cut") }

        val interrupted = importer(state, crash).import(old.config)

        assertInstanceOf(LegacyImportResult.Failed::class.java, interrupted)
        val target = LegacyClientImport.targetOf(RumbleEnvironments.PUBLIC, state)
        assertFalse(Files.exists(target.resolve("import-record.json")))
        assertTrue(snapshot(target).keys.any { it.endsWith(".part") }, "The crash must leave a temporary file for this test to mean anything")

        val resumed = importer(state).import(old.config) as LegacyImportResult.Imported
        assertEquals(2, resumed.alreadyCopied)
        assertEquals(3, resumed.copied)
        assertTrue(snapshot(target).keys.none { it.endsWith(".part") })
        assertTrue(Files.exists(target.resolve("import-record.json")))

        val third = importer(state).import(old.config) as LegacyImportResult.Imported
        assertEquals(0, third.copied)
        assertEquals(5, third.alreadyCopied)
    }

    @Test
    @Tag("RLI-007")
    @Tag("Unit")
    @Tag("Negative")
    fun testRLI007_UnitNegative_aFileThatChangedInTheOldFolderIsCopiedAgainAndTheOldCopyIsReplaced(@TempDir dir: Path) {
        val old = oldClient(dir)
        val state = dir.resolve("state")
        importer(state).import(old.config)
        Files.writeString(old.work.resolve("journal/records.jsonl"), "{\"battleId\":\"b3\"}\n", java.nio.file.StandardOpenOption.APPEND)

        val again = importer(state).import(old.config) as LegacyImportResult.Imported

        assertEquals(1, again.copied)
        val copy = LegacyClientImport.targetOf(RumbleEnvironments.PUBLIC, state).resolve("files/journal/records.jsonl")
        assertArrayEquals(Files.readAllBytes(old.work.resolve("journal/records.jsonl")), Files.readAllBytes(copy))
    }

    // RLI-008

    @Test
    @Tag("RLI-008")
    @Tag("Unit")
    @Tag("Negative")
    fun testRLI008_UnitNegative_aFileThatGrowsWhileItIsReadIsReportedAndCopiedByTheNextRun(@TempDir dir: Path) {
        val old = oldClient(dir)
        val state = dir.resolve("state")
        val grow: (Path, Path) -> Unit = { source, _ -> if (source.fileName.toString() == "records.jsonl") Files.writeString(source, "{\"battleId\":\"b9\"}\n", java.nio.file.StandardOpenOption.APPEND) }

        val result = importer(state, grow).import(old.config) as LegacyImportResult.Incomplete

        assertEquals(listOf("journal/records.jsonl"), result.changedDuringImport)
        val target = LegacyClientImport.targetOf(RumbleEnvironments.PUBLIC, state)
        assertFalse(Files.exists(target.resolve("files/journal/records.jsonl")))
        assertFalse(Files.exists(target.resolve("import-record.json")))

        val next = importer(state).import(old.config)
        assertInstanceOf(LegacyImportResult.Imported::class.java, next)
        assertArrayEquals(Files.readAllBytes(old.work.resolve("journal/records.jsonl")), Files.readAllBytes(target.resolve("files/journal/records.jsonl")))
    }

    // RLI-009

    @Test
    @Tag("RLI-009")
    @Tag("Unit")
    @Tag("Positive")
    fun testRLI009_UnitPositive_thePreferencesAreSavedInTheRightEnvironmentAndCanBeReadBack(@TempDir dir: Path) {
        val old = oldClient(dir, bots = testBots, data = testData, clientId = "alice-test-9z8y7x")
        val state = dir.resolve("state")

        importer(state).import(old.config)

        val loaded = ImportedPreferences.load(RumbleEnvironments.TEST, state)!!
        assertEquals("alice-test-9z8y7x", loaded.clientId)
        assertEquals(listOf("Walls", "Orbit"), loaded.myBots)
        assertEquals(listOf("1v1", "melee"), loaded.gameTypes)
        assertEquals(25, loaded.battlesPerSession)
        assertEquals("ranked", loaded.mode)
    }

    @Test
    @Tag("RLI-009")
    @Tag("Unit")
    @Tag("Negative")
    fun testRLI009_UnitNegative_nothingIsEverSavedInTheOtherEnvironmentsStateFolder(@TempDir dir: Path) {
        val old = oldClient(dir, bots = testBots, data = testData)
        val state = dir.resolve("state")

        importer(state).import(old.config)

        assertNull(ImportedPreferences.load(RumbleEnvironments.PUBLIC, state))
        assertFalse(Files.exists(RumbleEnvironments.PUBLIC.stateRoot(state)), "The public state folder must not even be created")
        assertTrue(Files.exists(LegacyClientImport.targetOf(RumbleEnvironments.TEST, state)))
    }

    // RLI-010

    @Test
    @Tag("RLI-010")
    @Tag("Unit")
    @Tag("Positive")
    fun testRLI010_UnitPositive_everyEndingIsATypedResultWithAPlainMessage(@TempDir dir: Path) {
        val old = oldClient(dir)
        val results = listOf(
            importer(dir.resolve("a")).import(old.config),
            importer(dir.resolve("b")).import(oldClient(dir, withFiles = false, name = "none").config),
            importer(dir.resolve("c")).import(oldClient(dir, version = 3, name = "refuse").config),
            importer(dir.resolve("d")) { source, _ -> if (source.fileName.toString() == "records.jsonl") Files.writeString(source, "x", java.nio.file.StandardOpenOption.APPEND) }.import(old.config),
            importer(dir.resolve("e.txt").also { Files.writeString(it, "a file") }).import(old.config),
        )

        assertEquals(
            listOf("Imported", "NothingToImport", "Refused", "Incomplete", "Failed"),
            results.map { it::class.simpleName },
        )
    }

    @Test
    @Tag("RLI-010")
    @Tag("Unit")
    @Tag("Negative")
    fun testRLI010_UnitNegative_noResultOrMessageShowsTheContentOfACopiedFile(@TempDir dir: Path) {
        val old = oldClient(dir)
        val texts = mutableListOf<String>()
        texts += importer(dir.resolve("a")).import(old.config).toString()
        texts += importer(dir.resolve("b")).preview(old.config).toString().replace(old.work.toString(), "")
        texts += importer(dir.resolve("c.txt").also { Files.writeString(it, "a file") }).import(old.config).toString()
        texts += importer(dir.resolve("d")) { _, _ -> throw IOException("boom $secretInJournal") }.import(old.config).toString()

        texts.forEach { assertFalse(it.contains(secretInJournal), it) }
    }
}
