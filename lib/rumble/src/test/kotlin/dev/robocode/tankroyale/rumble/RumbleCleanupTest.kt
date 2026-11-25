package dev.robocode.tankroyale.rumble

import dev.robocode.tankroyale.botproject.TreeHash
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.InputStream
import java.nio.file.Files
import java.nio.file.Path
import java.time.Instant

/** Faults a code review of the Rumble library found: each case is one that went wrong before. */
class RumbleCleanupTest {

    @TempDir
    lateinit var dir: Path

    @Test
    @Tag("Unit")
    fun aBotThatRanFromItsFolderIsNotTakenForAChangedBot() {
        val bot = Files.createDirectories(dir.resolve("Sharp"))
        Files.writeString(bot.resolve("Sharp.json"), "{}")
        Files.writeString(bot.resolve("Sharp.cs"), "class Sharp {}")
        val source = BotSourceHash.of(bot)
        assertEquals(TreeHash.ofDirectory(bot), source)

        // What a run leaves behind, such as a build's output, does not change the source's hash.
        Files.createDirectories(bot.resolve("bin/Release")).also { Files.writeString(it.resolve("Sharp.dll"), "binary") }
        Files.createDirectories(bot.resolve("obj")).also { Files.writeString(it.resolve("project.assets.json"), "{}") }
        assertEquals(source, BotSourceHash.of(bot))

        // A change to the source does.
        Files.writeString(bot.resolve("Sharp.cs"), "class Sharp { }")
        assertNotEquals(source, BotSourceHash.of(bot))
    }

    @Test
    @Tag("Unit")
    fun twoActiveBotsWithTheSameFolderNameAreNeitherPlacedNorFetched() {
        fun bot(path: String) = CatalogBot("Walls", "1.0", "JVM", path, "sha256:aa", "owner", listOf("A"), "2026-01-01", CatalogBot.ACTIVE)
        val java = bot("bots/java/Walls")
        val python = bot("bots/python/Walls").copy(platform = "Python")
        val catalog = Catalog(1, "2026-11-25T10:00:00Z", "a".repeat(40), listOf(java, python))
        val neverFetched = object : CatalogSource {
            override fun catalogText(commit: String): String = error("not read")
            override fun archive(commit: String): InputStream = error("nothing is fetched for bots that share a folder")
        }

        val results = BotDownloads.download(neverFetched, catalog, listOf(java, python), emptySet(), dir.resolve("downloads"), { _, _ -> error("not moved") }, {})

        assertEquals(2, results.size)
        assertTrue(results.all { it is DownloadResult.NotPlaced }, results.toString())
        assertTrue(Files.list(dir.resolve("downloads")).use { it.noneMatch { f -> f.fileName.toString() == "Walls" } })
    }

    @Test
    @Tag("Unit")
    fun theWorkersProcessesAreKeptOnceEach() {
        val me = ProcessHandle.current()
        val set = DescendantSet()
        repeat(1_000) { set.addAll(listOf(me)) }
        assertEquals(listOf(me.pid()), set.map { it.pid() })
    }

    @Test
    @Tag("Unit")
    fun aPausedSessionStillDoesItsWorkBetweenBattles() {
        val control = SessionControl().apply { pause() }
        var calls = 0
        val setup = SessionSetup(
            commit = "a".repeat(40),
            source = object : CatalogSource {
                override fun catalogText(commit: String): String = error("not read")
                override fun archive(commit: String): InputStream = error("not read")
            },
            botCache = dir.resolve("cache"), host = WorkerHost(), command = WorkerCommand { listOf("none") },
            journal = RankedJournal.open(RumbleEnvironments.TEST, dir.resolve("journal.jsonl")),
            engine = JournalEngine(2, "1.4.0", "1.4.0"), client = JournalClient("client-1", "1.4.0"),
            roundsPerFormat = mapOf("1v1" to 1), pollMillis = 1,
            // Three rounds of the pause, then the person stops the session.
            sleep = { if (calls >= 3) control.finishAndStop() },
            clock = { Instant.parse("2026-11-25T10:00:00Z") },
            betweenBattles = { calls++ },
        )

        val end = SessionRunner(setup, control) { }.run { error("no battle is chosen while paused") }

        assertEquals(SessionEnd.Stopped, end)
        assertTrue(calls >= 3, "results are sent and receipts read while paused: $calls")
    }
}
