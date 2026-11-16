package dev.robocode.tankroyale.rumble

import dev.robocode.tankroyale.botproject.TreeHash
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertInstanceOf
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.IOException
import java.io.InputStream
import java.nio.file.Files
import java.nio.file.Path
import java.time.Duration
import java.time.Instant
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

class RumbleSessionRunnerTest {

    private val commit = "a".repeat(40)
    private val java = File(System.getProperty("java.home"), "bin/java").absolutePath
    private val engine = JournalEngine(7, "1.4.0", "1.4.0")
    private val client = JournalClient("client-01", "1.4.0")

    private fun files(name: String, extra: String = ""): Map<String, ByteArray> = mapOf(
        "$name.json" to "{ \"name\": \"$name\", \"version\": \"1.0\" }".toByteArray(),
        "$name.sh" to "#!/bin/sh\n$extra".toByteArray(),
        "src/$name.java" to "class $name {}\n".toByteArray(),
    )

    private fun bot(
        name: String,
        version: String = "1.0",
        content: Map<String, ByteArray> = files(name),
        hash: String = TreeHash.of(content),
        path: String = "bots/java/$name",
        members: List<String> = emptyList(),
    ) = CatalogBot(name, version, "JVM", path, hash, "owner", listOf("Author"), "2026-01-01", CatalogBot.ACTIVE, teamMembers = members)

    /** The files of a team that names its members by the folders beside it. */
    private fun teamFiles(name: String, vararg members: String): Map<String, ByteArray> = mapOf(
        "$name.json" to "{ \"name\": \"$name\", \"version\": \"1.0\", \"teamMembers\": [${members.joinToString { "\"$it\"" }}] }".toByteArray(),
    )

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

    /** A bot repository that serves the given bot folders, by their paths, as one archive, and counts how often it is asked for it. */
    private inner class Source(
        trees: Map<String, Map<String, ByteArray>>,
        private val failArchive: Boolean = false,
        private val busy: Boolean = false,
        private val onArchive: () -> Unit = {},
    ) : CatalogSource {
        var archives = 0
        private val archive = zip(trees.flatMap { (path, tree) -> tree.map { "rumble-bots-$commit/$path/${it.key}" to it.value } }.toMap())
        override fun catalogText(commit: String) = throw CatalogUnreadable("not used")
        override fun archive(commit: String): InputStream {
            archives++
            onArchive()
            if (failArchive) throw IOException("The connection was lost.")
            // The way the real source reports an error status from the bot repository.
            if (busy) throw CatalogUnreadable("The bot repository answered 503.")
            return ByteArrayInputStream(archive)
        }
    }

    private fun sourceFor(vararg names: String, failArchive: Boolean = false) = Source(names.associate { "bots/java/$it" to files(it) }, failArchive)

    /** The fake worker as a real process; each battle takes the next mode of [modes], the last one for all that follow. */
    private fun command(vararg modes: String): WorkerCommand {
        var next = 0
        return WorkerCommand {
            val mode = modes[minOf(next++, modes.size - 1)]
            listOf(java, "-cp", System.getProperty("java.class.path"), "dev.robocode.tankroyale.rumble.FakeWorkerKt", mode)
        }
    }

    private class Run(val end: SessionEnd, val events: List<SessionEvent>, val journal: RankedJournal)

    private fun request(
        entries: List<CatalogBot>,
        mode: SelectionMode = SelectionMode.ALL,
        selected: Set<String> = emptySet(),
        limit: Int? = null,
        formats: List<String> = listOf("1v1"),
    ) = SessionRequest(entries, selected, mode, formats, mapOf("1v1" to 2, "melee" to 3, "twinduel" to 4), limit, emptyMap(), seed = 1)

    private fun session(
        dir: Path,
        source: CatalogSource,
        command: WorkerCommand,
        request: () -> SessionRequest,
        control: SessionControl = SessionControl(),
        maxFailures: Int = 5,
        rounds: Map<String, Int> = mapOf("1v1" to 3),
        sleep: (Long) -> Unit = {},
        battleId: () -> String = run { var n = 0; { "battle-${++n}" } },
        limits: WorkerLimits = WorkerLimits(totalTime = Duration.ofSeconds(60)),
        onEvent: (SessionEvent, SessionControl) -> Unit = { _, _ -> },
    ): Run {
        val journal = RankedJournal.open(RumbleEnvironments.PUBLIC, dir.resolve("journal.jsonl"))
        val events = mutableListOf<SessionEvent>()
        val setup = SessionSetup(
            commit = commit, source = source, botCache = dir.resolve("cache"), host = WorkerHost(limits), command = command, journal = journal,
            engine = engine, client = client, roundsPerFormat = rounds, maxConsecutiveFailures = maxFailures, pollMillis = 1,
            sleep = { sleep(it) }, newBattleId = battleId, clock = { Instant.parse("2026-11-16T10:00:00Z") },
        )
        val end = SessionRunner(setup, control) { event -> events += event; onEvent(event, control) }.run(request)
        return Run(end, events, journal)
    }

    private fun reasons(run: Run) = run.events.filterIsInstance<SessionEvent.BattleNotKept>().map { it.reason }

    // RCL-115

    @Test
    @Tag("Positive")
    fun testRCL115_UnitPositive_aCompletedBattleBecomesExactlyOneRecordMadeFromWhatWasRun(@TempDir dir: Path) {
        val a = bot("Alpha")
        val b = bot("Beta")

        val run = session(dir, sourceFor("Alpha", "Beta"), command("ok"), { request(listOf(a, b), limit = 1) })

        assertEquals(SessionEnd.Finished, run.end)
        val record = run.journal.entries().single().record
        assertEquals("battle-1", record.battleId)
        assertEquals("1v1", record.gameType)
        assertEquals(3, record.rounds)
        assertEquals(client, record.client)
        assertEquals(engine, record.engine)
        assertEquals(commit, record.botsRepoCommit)
        assertEquals(mapOf("Alpha" to a.sourceHash, "Beta" to b.sourceHash), record.participants.associate { it.name to it.sourceHash })
        assertEquals(setOf("Alpha 1.0", "Beta 1.0"), record.participants.map { "${it.name} ${it.version}" }.toSet())
        assertTrue(run.events.any { it is SessionEvent.BattleStarted && it.battleId == "battle-1" })
        assertTrue(run.events.contains(SessionEvent.BattleKept("battle-1")))
        assertEquals(record, RankedJournal.open(RumbleEnvironments.PUBLIC, dir.resolve("journal.jsonl")).entries().single().record)
    }

    @Test
    @Tag("Negative")
    fun testRCL115_UnitNegative_aResultThatNamesOtherBotsThanTheBattleRanIsNotKept(@TempDir dir: Path) {
        // The fake worker reports every bot as version 1.0, while these bots are version 2.0.
        val entries = listOf(bot("Alpha", "2.0"), bot("Beta", "2.0"))

        val run = session(dir, sourceFor("Alpha", "Beta"), command("ok"), { request(entries, limit = 1) }, maxFailures = 2)

        assertEquals(SessionEnd.GaveUp(2), run.end)
        assertEquals(emptyList<JournalEntry>(), run.journal.entries())
        assertEquals(listOf(NotKeptReason.RESULT_MISMATCH, NotKeptReason.RESULT_MISMATCH), reasons(run))

        // A result with fewer rounds than the battle asked for is not a complete battle.
        val short = session(dir.resolve("short"), sourceFor("Alpha", "Beta"), command("fewrounds"), { request(listOf(bot("Alpha"), bot("Beta")), limit = 1) }, maxFailures = 1)
        assertEquals(listOf(NotKeptReason.RESULT_MISMATCH), reasons(short))
        assertEquals(emptyList<JournalEntry>(), short.journal.entries())

        // A battle id the journal already holds is not counted as kept; the session stops instead.
        val twice = session(dir.resolve("twice"), sourceFor("Alpha", "Beta"), command("ok"), { request(listOf(bot("Alpha"), bot("Beta")), limit = 2) }, battleId = { "same" })
        assertInstanceOf(SessionEnd.JournalFailed::class.java, twice.end)
        assertEquals(1, twice.journal.entries().size)
        assertEquals(1, twice.events.count { it is SessionEvent.BattleKept })
    }

    // RCL-116

    @Test
    @Tag("Positive")
    fun testRCL116_UnitPositive_everyKindOfBattleThatDidNotCompleteIsReportedAndKeptNothingAndTheSessionGoesOn(@TempDir dir: Path) {
        val entries = listOf(bot("Alpha"), bot("Beta"))

        val run = session(
            dir, sourceFor("Alpha", "Beta"), command("refuse", "fail", "nofinal", "badexit", "garbage", "ok"),
            { request(entries, limit = 1) }, maxFailures = 10,
        )

        assertEquals(
            listOf(NotKeptReason.WORKER_REFUSED, NotKeptReason.WORKER_FAILED, NotKeptReason.ENDED_WITHOUT_FINISH, NotKeptReason.ENDED_WITHOUT_FINISH, NotKeptReason.UNREADABLE_OUTPUT),
            reasons(run),
        )
        assertEquals(SessionEnd.Finished, run.end)
        assertEquals(1, run.journal.entries().size, "only the completed battle is kept")
        assertEquals("battle-6", run.journal.entries().single().record.battleId)
    }

    @Test
    @Tag("Positive")
    fun testRCL116_UnitPositive_aWorkerThatCannotBeStartedIsReported(@TempDir dir: Path) {
        val entries = listOf(bot("Alpha"), bot("Beta"))

        val run = session(dir, sourceFor("Alpha", "Beta"), WorkerCommand { listOf("definitely-not-a-program-rumble") }, { request(entries, limit = 1) }, maxFailures = 1)

        assertEquals(listOf(NotKeptReason.COULD_NOT_START), reasons(run))
        assertEquals(emptyList<JournalEntry>(), run.journal.entries())
    }

    @Test
    @Tag("Negative")
    fun testRCL116_UnitNegative_tooManyBattlesInARowThatFailGiveUpWithoutSpinningAndAFormatWithNoRoundsIsNotRun(@TempDir dir: Path) {
        val entries = listOf(bot("Alpha"), bot("Beta"))

        val failing = session(dir.resolve("a"), sourceFor("Alpha", "Beta"), command("fail"), { request(entries, limit = 1) }, maxFailures = 3)
        val noRounds = session(dir.resolve("b"), sourceFor("Alpha", "Beta"), command("ok"), { request(entries, limit = 1) }, maxFailures = 2, rounds = emptyMap())

        assertEquals(SessionEnd.GaveUp(3), failing.end)
        assertEquals(3, failing.events.count { it is SessionEvent.BattleStarted })
        assertEquals(emptyList<JournalEntry>(), failing.journal.entries())
        assertEquals(SessionEnd.GaveUp(2), noRounds.end)
        assertEquals(0, noRounds.events.count { it is SessionEvent.BattleStarted }, "no worker is started for a format with no rounds")
        assertEquals(emptyList<JournalEntry>(), noRounds.journal.entries())
    }

    // RCL-117

    @Test
    @Tag("Positive")
    fun testRCL117_UnitPositive_aBotThatCannotBePlacedIsNotRunAndTheSessionGoesOnWithOthers(@TempDir dir: Path) {
        val entries = listOf(bot("Bad", hash = "sha256:wrong"), bot("Gone"), bot("Alpha"), bot("Beta"), bot("Gamma"))
        val source = sourceFor("Bad", "Alpha", "Beta", "Gamma") // "Gone" is not in the archive

        val run = session(dir, source, command("ok"), { request(entries, SelectionMode.PREFER, setOf("Bad 1.0", "Gone 1.0"), limit = 3) })

        assertEquals(SessionEnd.Finished, run.end)
        val notPlaced = run.events.filterIsInstance<SessionEvent.BotNotPlaced>().associate { it.bot to it.reason }
        assertEquals(mapOf("Bad 1.0" to NotPlacedReason.HASH_MISMATCH, "Gone 1.0" to NotPlacedReason.NOT_IN_ARCHIVE), notPlaced)
        assertEquals(3, run.journal.entries().size)
        assertTrue(run.journal.entries().none { e -> e.record.participants.any { it.name == "Bad" || it.name == "Gone" } })
    }

    @Test
    @Tag("Negative")
    fun testRCL117_UnitNegative_aBotInTheCacheIsReusedAndOneThatNoLongerMatchesIsFetchedAgainAndTooFewBotsMakeTheSessionWait(@TempDir dir: Path) {
        val entries = listOf(bot("Alpha"), bot("Beta"))
        val source = sourceFor("Alpha", "Beta")

        session(dir.resolve("one"), source, command("ok"), { request(entries, limit = 1) })
        val afterFirst = source.archives
        // The same cache folder for the second and third session.
        val shared = dir.resolve("two")
        session(shared, source, command("ok"), { request(entries, limit = 1) })
        val afterSecond = source.archives
        session(shared, source, command("ok"), { request(entries, limit = 1) })
        val reused = source.archives
        val cached: Path = shared.resolve("cache").toFile().walkTopDown().single { it.name == "Alpha.sh" }.toPath()
        Files.writeString(cached, "tampered")
        val journalTwo = shared.resolve("journal.jsonl")
        Files.delete(journalTwo)
        val repaired = session(shared, source, command("ok"), { request(entries, limit = 1) })

        assertEquals(1, afterFirst)
        assertEquals(2, afterSecond, "a new cache fetches once")
        assertEquals(2, reused, "bots whose folder matches the hash are not fetched again")
        assertEquals(3, source.archives, "a folder that no longer matches its hash is fetched again")
        assertEquals(TreeHash.of(files("Alpha")), TreeHash.ofDirectory(cached.parent))
        assertEquals(SessionEnd.Finished, repaired.end)

        // A source that cannot be reached is tried again after the poll time, never left out, and counts towards giving up.
        val unreachable = sourceFor("Alpha", "Beta", failArchive = true)
        var sleeps = 0
        val unavailable = session(dir.resolve("three"), unreachable, command("ok"), { request(entries, limit = 1) }, maxFailures = 3, sleep = { sleeps++ })
        assertEquals(SessionEnd.GaveUp(3), unavailable.end)
        assertEquals(3, unreachable.archives, "the source was asked again on each try")
        assertEquals(2, sleeps, "it waited the poll time between tries")
        assertEquals(setOf("Alpha 1.0", "Beta 1.0"), unavailable.events.filterIsInstance<SessionEvent.BotNotPlaced>().map { it.bot }.toSet())
        assertTrue(unavailable.events.filterIsInstance<SessionEvent.BotNotPlaced>().all { it.reason == NotPlacedReason.SOURCE_UNAVAILABLE })
        assertTrue(unavailable.events.none { it is SessionEvent.Waiting || it is SessionEvent.BattleStarted })

        // An error status from the bot repository is the same: tried again, never left out.
        val busy = session(
            dir.resolve("busy"), Source(listOf("Alpha", "Beta").associate { "bots/java/$it" to files(it) }, busy = true), command("ok"),
            { request(entries, limit = 1) }, maxFailures = 2,
        )
        assertEquals(SessionEnd.GaveUp(2), busy.end)
        assertTrue(busy.events.filterIsInstance<SessionEvent.BotNotPlaced>().all { it.reason == NotPlacedReason.SOURCE_UNAVAILABLE })

        // A bot that can never be placed is left out, and when too few remain the session waits with the planner's reason.
        val tooFew = session(
            dir.resolve("four"), sourceFor("Alpha", "Beta"), command("ok"), { request(listOf(bot("Alpha"), bot("Beta", hash = "sha256:wrong")), limit = 1) },
            onEvent = { event, control -> if (event is SessionEvent.Waiting) control.finishAndStop() },
        )
        assertEquals(SessionEnd.Stopped, tooFew.end)
        assertEquals(listOf(SessionEvent.BotNotPlaced("Beta 1.0", NotPlacedReason.HASH_MISMATCH)), tooFew.events.filterIsInstance<SessionEvent.BotNotPlaced>().map { it.copy(detail = "") })
        assertEquals(SessionEvent.Waiting(WaitReason.NOT_ENOUGH_ENTRIES, listOf("1v1")), tooFew.events.filterIsInstance<SessionEvent.Waiting>().single())
    }

    @Test
    @Tag("Positive")
    fun testRCL117_UnitPositive_aTeamsMembersArePlacedBesideTheTeamAndBotsWithTheSameFolderNameDoNotMeet(@TempDir dir: Path) {
        val members = listOf("M1", "M2", "M3", "M4")
        val teamA = bot("Team A", content = teamFiles("TeamA", "M1", "M2"), path = "bots/java/TeamA", members = listOf("M1 1.0", "M2 1.0"))
        val teamB = bot("Team B", content = teamFiles("TeamB", "M3", "M4"), path = "bots/java/TeamB", members = listOf("M3 1.0", "M4 1.0"))
        val trees = members.associate { "bots/java/$it" to files(it) } + mapOf("bots/java/TeamA" to teamFiles("TeamA", "M1", "M2"), "bots/java/TeamB" to teamFiles("TeamB", "M3", "M4"))

        val twin = session(
            dir.resolve("twin"), Source(trees), command("ok"),
            { request(members.map { bot(it) } + teamA + teamB, limit = 1, formats = listOf("twinduel")) }, rounds = mapOf("twinduel" to 3),
        )

        assertEquals(SessionEnd.Finished, twin.end, twin.events.toString())
        assertEquals(setOf("Team A", "Team B"), twin.journal.entries().single().record.participants.map { it.name }.toSet())
        for ((team, folder, own) in listOf(Triple(teamA, "TeamA", listOf("M1", "M2")), Triple(teamB, "TeamB", listOf("M3", "M4")))) {
            val group = dir.resolve("twin/cache").resolve(team.sourceHash.removePrefix("sha256:"))
            assertEquals(TreeHash.of(teamFiles(folder, *own.toTypedArray())), TreeHash.ofDirectory(group.resolve(folder)))
            own.forEach { assertEquals(TreeHash.of(files(it)), TreeHash.ofDirectory(group.resolve(it)), "$it beside $folder") }
        }

        // Two bots from different folders that end in the same name are both fetched and placed in one battle.
        val java = bot("Walls J", content = files("Walls"), path = "bots/java/Walls")
        val python = bot("Walls P", content = files("Walls", "python"), path = "bots/python/Walls")
        val sameName = session(
            dir.resolve("same"), Source(mapOf("bots/java/Walls" to files("Walls"), "bots/python/Walls" to files("Walls", "python"))), command("ok"),
            { request(listOf(java, python), limit = 1) },
        )
        assertEquals(SessionEnd.Finished, sameName.end, sameName.events.toString())
        assertTrue(sameName.events.none { it is SessionEvent.BotNotPlaced })
        assertEquals(mapOf("Walls J" to java.sourceHash, "Walls P" to python.sourceHash), sameName.journal.entries().single().record.participants.associate { it.name to it.sourceHash })

        // A team with a member of its own folder name cannot stand beside it, so the team is left out.
        val clash = bot("Clash", content = teamFiles("M1", "M1", "M2"), path = "bots/java/M1", members = listOf("M1 1.0", "M2 1.0"))
        val clashing = session(
            dir.resolve("clash"), Source(trees), command("ok"),
            { request(members.map { bot(it) } + clash + teamB, limit = 1, formats = listOf("twinduel")) }, rounds = mapOf("twinduel" to 3),
            onEvent = { event, control -> if (event is SessionEvent.Waiting) control.finishAndStop() },
        )
        assertEquals(listOf(SessionEvent.BotNotPlaced("Clash 1.0", NotPlacedReason.FOLDER_NAME_CLASH)), clashing.events.filterIsInstance<SessionEvent.BotNotPlaced>())
        assertEquals(SessionEnd.Stopped, clashing.end)

        // A member that can never be placed leaves its team out with it.
        val badMember = session(
            dir.resolve("member"), Source(trees), command("ok"),
            { request(members.map { if (it == "M1") bot(it, hash = "sha256:wrong") else bot(it) } + teamA + teamB, limit = 1, formats = listOf("twinduel")) },
            rounds = mapOf("twinduel" to 3),
            onEvent = { event, control -> if (event is SessionEvent.Waiting) control.finishAndStop() },
        )
        assertEquals(listOf("M1 1.0" to NotPlacedReason.HASH_MISMATCH), badMember.events.filterIsInstance<SessionEvent.BotNotPlaced>().map { it.bot to it.reason })
        assertEquals(SessionEnd.Stopped, badMember.end)
        assertEquals(0, badMember.events.count { it is SessionEvent.BattleStarted }, "the team without its member is never run")
    }

    // RCL-118

    @Test
    @Tag("Integration")
    @Tag("Positive")
    fun testRCL118_IntegrationPositive_pauseLetsTheActiveBattleFinishAndFinishAndStopKeepsItsResult(@TempDir dir: Path) {
        val entries = listOf(bot("Alpha"), bot("Beta"))

        val pausing = session(
            dir.resolve("pause"), sourceFor("Alpha", "Beta"), command("ok"), { request(entries, limit = 2) },
            sleep = { },
            onEvent = { event, control ->
                if (event is SessionEvent.BattleKept && event.battleId == "battle-1") control.pause()
                if (event is SessionEvent.Paused) control.resume()
            },
        )
        val stopping = session(
            dir.resolve("stop"), sourceFor("Alpha", "Beta"), command("ok"), { request(entries, limit = 5) },
            onEvent = { event, control -> if (event is SessionEvent.BattleStarted) control.finishAndStop() },
        )

        assertEquals(SessionEnd.Finished, pausing.end)
        assertEquals(
            listOf("BattleStarted", "BattleKept", "Paused", "Resumed", "BattleStarted", "BattleKept"),
            pausing.events.map { it::class.simpleName },
        )
        assertEquals(2, pausing.journal.entries().size)
        assertEquals(SessionEnd.Stopped, stopping.end)
        assertEquals(1, stopping.journal.entries().size, "the active battle finished and was kept")
        assertEquals(1, stopping.events.count { it is SessionEvent.BattleStarted }, "no further battle was started")
    }

    @Test
    @Tag("Integration")
    @Tag("Negative")
    fun testRCL118_IntegrationNegative_cancelNowEndsTheWorkerAndEverythingItStartedAndKeepsNothing(@TempDir dir: Path) {
        val entries = listOf(bot("Alpha"), bot("Beta"))
        val pidFile = dir.resolve("child.pid")
        val control = SessionControl()
        // The fake worker in this mode starts a child and writes its process id to the file named by the battle id.
        val watcher = Thread {
            val end = System.nanoTime() + Duration.ofSeconds(50).toNanos()
            while (!Files.exists(pidFile) && System.nanoTime() < end) Thread.sleep(50)
            Thread.sleep(500)
            control.cancelNow()
        }.apply { isDaemon = true; start() }

        val run = session(dir, sourceFor("Alpha", "Beta"), command("child"), { request(entries, limit = 3) }, control = control, battleId = { pidFile.toString() })

        watcher.join(5_000)
        assertEquals(SessionEnd.Cancelled, run.end)
        assertEquals(emptyList<JournalEntry>(), run.journal.entries())
        assertEquals(listOf(NotKeptReason.CANCELLED), reasons(run))
        val childPid = Files.readString(pidFile).trim().toLong()
        val end = System.nanoTime() + Duration.ofSeconds(15).toNanos()
        while (ProcessHandle.of(childPid).map { it.isAlive }.orElse(false) && System.nanoTime() < end) Thread.sleep(100)
        assertFalse(ProcessHandle.of(childPid).map { it.isAlive }.orElse(false), "The child $childPid is still running")
        assertTrue(run.events.none { it is SessionEvent.BattleKept })
    }

    @Test
    @Tag("Integration")
    @Tag("Negative")
    fun testRCL118_IntegrationNegative_cancelNowWorksAfterTheWorkerClosedItsOutputAndDuringAPauseWithoutAResumption(@TempDir dir: Path) {
        val entries = listOf(bot("Alpha"), bot("Beta"))
        val control = SessionControl()
        // The fake worker in this mode closes its output and hangs; cancel comes well after the output is closed.
        val watcher = Thread {
            Thread.sleep(4_000)
            control.cancelNow()
        }.apply { isDaemon = true; start() }
        val started = System.nanoTime()

        val hanging = session(dir.resolve("hang"), sourceFor("Alpha", "Beta"), command("closedhang"), { request(entries, limit = 1) }, control = control)

        watcher.join(5_000)
        val seconds = Duration.ofNanos(System.nanoTime() - started).seconds
        assertEquals(SessionEnd.Cancelled, hanging.end)
        assertEquals(listOf(NotKeptReason.CANCELLED), reasons(hanging))
        assertTrue(seconds < 30, "cancel-now took $seconds s instead of ending the worker at once")

        val paused = session(
            dir.resolve("paused"), sourceFor("Alpha", "Beta"), command("ok"), { request(entries, limit = 3) },
            onEvent = { event, c ->
                if (event is SessionEvent.BattleKept) c.pause()
                if (event is SessionEvent.Paused) c.cancelNow()
            },
        )
        assertEquals(SessionEnd.Cancelled, paused.end)
        assertEquals(listOf("BattleStarted", "BattleKept", "Paused"), paused.events.map { it::class.simpleName })

        // Finish-and-stop or cancel-now asked for while the bots are fetched ends the session before a battle starts.
        for ((ask, expected) in listOf<Pair<(SessionControl) -> Unit, SessionEnd>>({ c: SessionControl -> c.finishAndStop() } to SessionEnd.Stopped, { c: SessionControl -> c.cancelNow() } to SessionEnd.Cancelled)) {
            val fetchControl = SessionControl()
            val fetching = session(
                Files.createTempDirectory(dir, "fetch"), Source(listOf("Alpha", "Beta").associate { "bots/java/$it" to files(it) }, onArchive = { ask(fetchControl) }),
                command("ok"), { request(entries, limit = 1) }, control = fetchControl,
            )
            assertEquals(expected, fetching.end)
            assertTrue(fetching.events.none { it is SessionEvent.BattleStarted })
        }
    }

    // RCL-122 and RCL-123: a session made from the Rumble's pin and advice

    private class PinData(vararg files: Pair<String, String>) : ResultDataSource {
        private val files = mapOf(*files)
        override fun fileText(path: String) = files[path]
    }

    private val enginePin = EnginePin.PATH to """
        { "schemaVersion": 1, "behaviorVersion": ${dev.robocode.tankroyale.common.rules.CURRENT_BEHAVIOR_VERSION},
          "gameTypes": { "1v1": { "rounds": 7, "battlefield": [800, 600], "participants": 2 } } }
    """.trimIndent()

    /** A session whose rounds, battle sizes, engine and advice come from [data]. */
    private fun pinnedSession(dir: Path, data: ResultDataSource, entries: List<CatalogBot>, vararg names: String): Run {
        val pin = SessionPins.prepare(data, listOf("1v1")) as SessionPin.Ready
        val advice = AdviceKeeper(data, pin.formats)
        val journal = RankedJournal.open(RumbleEnvironments.PUBLIC, dir.resolve("journal.jsonl"))
        val events = mutableListOf<SessionEvent>()
        val setup = SessionSetup(
            commit = commit, source = sourceFor(*names), botCache = dir.resolve("cache"), host = WorkerHost(WorkerLimits(totalTime = Duration.ofSeconds(60))),
            command = command("ok"), journal = journal, engine = pin.engine("1.4.0", "1.4.0"), client = client, roundsPerFormat = pin.roundsPerFormat,
            pollMillis = 1, sleep = {},
        )
        val end = SessionRunner(setup, SessionControl()) { events += it }.run {
            SessionRequest(entries, emptySet(), SelectionMode.ALL, pin.formats, pin.participantCounts, battleLimit = 1, advice = advice.current(), seed = 1)
        }
        return Run(end, events, journal)
    }

    @Test
    @Tag("Positive")
    fun testRCL122_UnitPositive_aSessionUnderAPinRunsThePinsRoundsAndKeepsThePinsBehaviorVersion(@TempDir dir: Path) {
        val run = pinnedSession(dir, PinData(enginePin), listOf(bot("Alpha"), bot("Beta")), "Alpha", "Beta")

        assertEquals(SessionEnd.Finished, run.end, run.events.toString())
        val record = run.journal.entries().single().record
        assertEquals(7, record.rounds, "the fake worker reports the rounds the job asked for")
        assertEquals(dev.robocode.tankroyale.common.rules.CURRENT_BEHAVIOR_VERSION, record.engine.behaviorVersion)
        assertEquals(2, record.participants.size)
    }

    @Test
    @Tag("Positive")
    fun testRCL123_UnitPositive_anAdvisedBattleIsRunFirstAndWithoutAdviceTheSessionRunsByCoverage(@TempDir dir: Path) {
        val entries = listOf(bot("Alpha"), bot("Beta"), bot("Gamma"), bot("Delta"))
        val advised = MatchmakingAdvice.path("1v1") to """
            { "schemaVersion": 1, "gameType": "1v1", "priorityPairs": [ { "bots": ["Gamma 1.0", "Delta 1.0"] } ] }
        """.trimIndent()

        val otherAdvice = MatchmakingAdvice.path("1v1") to """
            { "schemaVersion": 1, "gameType": "1v1", "priorityPairs": [ { "bots": ["Alpha 1.0", "Gamma 1.0"] } ] }
        """.trimIndent()

        val withAdvice = pinnedSession(dir.resolve("advice"), PinData(enginePin, advised), entries, "Alpha", "Beta", "Gamma", "Delta")
        val withOther = pinnedSession(dir.resolve("other"), PinData(enginePin, otherAdvice), entries, "Alpha", "Beta", "Gamma", "Delta")
        val without = pinnedSession(dir.resolve("none"), PinData(enginePin), entries, "Alpha", "Beta", "Gamma", "Delta")

        // The same seed with two different advices runs each advised battle, so the advice and not the seed chooses it.
        assertEquals(setOf("Gamma", "Delta"), withAdvice.journal.entries().single().record.participants.map { it.name }.toSet())
        assertEquals(setOf("Alpha", "Gamma"), withOther.journal.entries().single().record.participants.map { it.name }.toSet())
        assertEquals(SessionEnd.Finished, without.end, "without advice the session still runs, by coverage")
        assertEquals(1, without.journal.entries().size)
    }

    // RCL-119

    @Test
    @Tag("Positive")
    fun testRCL119_UnitPositive_aBattleCountEndsTheSessionAndOnlyKeptBattlesCount(@TempDir dir: Path) {
        val entries = listOf(bot("Alpha"), bot("Beta"), bot("Gamma"))

        val run = session(dir, sourceFor("Alpha", "Beta", "Gamma"), command("fail", "ok"), { request(entries, limit = 3) })

        assertEquals(SessionEnd.Finished, run.end)
        assertEquals(3, run.journal.entries().size)
        assertEquals(1, reasons(run).size, "the one failed battle did not count towards the three")
        assertEquals(4, run.events.count { it is SessionEvent.BattleStarted })
    }

    @Test
    @Tag("Negative")
    fun testRCL119_UnitNegative_aWaitIsReportedOnceForItsReasonAndARequestThatChangesWhileWaitingIsUsed(@TempDir dir: Path) {
        val alpha = bot("Alpha")
        val beta = bot("Beta")
        var looks = 0
        var sleeps = 0

        val run = session(
            dir, sourceFor("Alpha", "Beta"), command("ok"),
            { looks++; request(if (sleeps >= 3) listOf(alpha, beta) else listOf(alpha), limit = 1) },
            sleep = { sleeps++ },
        )

        assertEquals(SessionEnd.Finished, run.end)
        assertEquals(listOf(SessionEvent.Waiting(WaitReason.NOT_ENOUGH_ENTRIES, listOf("1v1"))), run.events.filterIsInstance<SessionEvent.Waiting>())
        assertEquals(3, sleeps, "it looked again after each poll instead of running anything")
        assertEquals(1, run.journal.entries().size)
        assertInstanceOf(SessionEvent.BattleKept::class.java, run.events.last())
    }
}
