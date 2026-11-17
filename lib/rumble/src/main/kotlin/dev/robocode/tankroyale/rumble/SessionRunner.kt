package dev.robocode.tankroyale.rumble

import dev.robocode.tankroyale.botproject.TreeHash
import dev.robocode.tankroyale.rumble.protocol.BotOutcome
import dev.robocode.tankroyale.rumble.protocol.Job
import dev.robocode.tankroyale.rumble.protocol.JobBot
import dev.robocode.tankroyale.rumble.protocol.WorkerEvent
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Path
import java.time.Instant
import java.util.UUID

/** What the contributor asks of a running session. Set from any thread; the session reads it between and during battles. */
class SessionControl {
    private enum class Mode { RUN, PAUSE, FINISH_AND_STOP, CANCEL_NOW }

    // Changed only by compare-and-set, so a pause from one thread never undoes a stop or a cancel from another.
    private val mode = java.util.concurrent.atomic.AtomicReference(Mode.RUN)

    /** Finish the active battle, then wait until [resume]. */
    fun pause() { mode.compareAndSet(Mode.RUN, Mode.PAUSE) }

    fun resume() { mode.compareAndSet(Mode.PAUSE, Mode.RUN) }

    /** Finish the active battle, keep its result, then end the session. */
    fun finishAndStop() { mode.updateAndGet { if (it == Mode.CANCEL_NOW) it else Mode.FINISH_AND_STOP } }

    /** End the active battle now and keep nothing of it, then end the session. */
    fun cancelNow() { mode.set(Mode.CANCEL_NOW) }

    internal val paused: Boolean get() = mode.get() == Mode.PAUSE
    internal val stopRequested: Boolean get() = mode.get() == Mode.FINISH_AND_STOP
    internal val cancelRequested: Boolean get() = mode.get() == Mode.CANCEL_NOW
}

/** Why a battle was run but is not in the journal. */
enum class NotKeptReason {
    WORKER_REFUSED, WORKER_FAILED, TIMED_OUT, LIMIT_EXCEEDED, ENDED_WITHOUT_FINISH, UNREADABLE_OUTPUT, COULD_NOT_START,

    /** The contributor cancelled the battle. */
    CANCELLED,

    /** The result does not match the bots that were run. */
    RESULT_MISMATCH,
}

/** Why a bot could not be put in place. */
enum class NotPlacedReason {
    HASH_MISMATCH, NOT_IN_ARCHIVE, UNSAFE,

    /** The source could not be reached or written this time; the bot is tried again. */
    SOURCE_UNAVAILABLE,

    /** A team and one of its members, or two of its members, have the same folder name, so they cannot stand side by side. */
    FOLDER_NAME_CLASH,
}

/** What a session reports as it goes, in plain codes. The screens and the command line turn these into text. */
sealed interface SessionEvent {
    data class BotNotPlaced(val bot: String, val reason: NotPlacedReason, val detail: String = "") : SessionEvent
    data class BattleStarted(val battleId: String, val gameType: String, val participants: List<String>) : SessionEvent
    data class BattleKept(val battleId: String) : SessionEvent
    data class BattleNotKept(val battleId: String, val reason: NotKeptReason, val detail: String = "") : SessionEvent

    /** Nothing can be run now; [reason] and [values] are the planner's. */
    data class Waiting(val reason: WaitReason, val values: List<String>) : SessionEvent
    data object Paused : SessionEvent
    data object Resumed : SessionEvent
}

/** How a session ended. */
sealed interface SessionEnd {
    /** The battle count was reached. */
    data object Finished : SessionEnd

    /** The contributor stopped it after the active battle finished. */
    data object Stopped : SessionEnd

    /** The contributor cancelled; nothing of the active battle was kept. */
    data object Cancelled : SessionEnd

    /** Too many battles in a row did not complete or could not be placed; the cause is in the events. */
    data class GaveUp(val failures: Int) : SessionEnd

    /** A completed result could not be written to the journal, so the session stopped rather than lose it. */
    data class JournalFailed(val message: String) : SessionEnd

    /** No battle could be chosen and the session was set to end then, rather than wait; [reason] and [values] are the planner's. */
    data class NothingToRun(val reason: WaitReason, val values: List<String>) : SessionEnd
}

/** Everything a session needs besides what to run. */
class SessionSetup(
    val commit: String,
    val source: CatalogSource,
    /** Where bot sources are kept between battles, each below the hash of its own source. */
    val botCache: Path,
    val host: WorkerHost,
    val command: WorkerCommand,
    val journal: RankedJournal,
    /** The behavior version the Rumble pins; the worker refuses a server that reports another. */
    val engine: JournalEngine,
    val client: JournalClient,
    /** The rounds of a battle in each game format the session may run. */
    val roundsPerFormat: Map<String, Int>,
    val maxConsecutiveFailures: Int = 5,
    val pollMillis: Long = 250,
    val sleep: (Long) -> Unit = { Thread.sleep(it) },
    val newBattleId: () -> String = { UUID.randomUUID().toString() },
    val clock: () -> Instant = Instant::now,
    /** Work done after each battle, kept or not, and while the session waits, such as [AutomaticUpload.betweenBattles]. */
    val betweenBattles: () -> Unit = {},
    /**
     * True when no battle can be chosen means the session ends with [SessionEnd.NothingToRun], for a caller whose request
     * does not change while it runs, such as the command line with its catalog read once. False waits for the request to change.
     */
    val endWhenWaiting: Boolean = false,
    /**
     * Variables for the worker of one battle, given every bot of the battle (with a team's members) and the battle's own new
     * folders, such as a copy of the Python Bot API for a battle with a Python bot.
     */
    val battleEnvironment: (List<CatalogBot>, WorkerFolders) -> Map<String, String> = { _, _ -> emptyMap() },
)

/**
 * Runs a contribution session on the calling thread: one battle at a time, each chosen by [SessionPlanner], run on the
 * worker, and kept in the journal only when it completed. Pause, finish-and-stop and cancel-now come through the
 * [SessionControl]. Nothing here writes text; it reports [SessionEvent]s.
 */
class SessionRunner(
    private val setup: SessionSetup,
    private val control: SessionControl,
    private val onEvent: (SessionEvent) -> Unit,
) {
    /** Runs until the battle count is reached, the contributor stops it, or it cannot go on. [request] is read before each battle. */
    fun run(request: () -> SessionRequest): SessionEnd {
        var history = SessionHistory()
        var failures = 0
        val excluded = mutableSetOf<String>()
        var lastWait: SessionEvent.Waiting? = null
        while (true) {
            if (control.cancelRequested) return SessionEnd.Cancelled
            if (control.stopRequested) return SessionEnd.Stopped
            if (control.paused) {
                onEvent(SessionEvent.Paused)
                while (control.paused) setup.sleep(setup.pollMillis)
                // A pause ended by cancel-now or finish-and-stop ends the session without a resumption.
                if (!control.cancelRequested && !control.stopRequested) onEvent(SessionEvent.Resumed)
                continue
            }
            val current = request().let { it.copy(entries = it.entries.filter { e -> SessionPlanner.key(e) !in excluded && e.teamMembers.none { m -> m in excluded } }) }
            when (val step = SessionPlanner.next(current, history)) {
                SessionStep.Finished -> return SessionEnd.Finished
                is SessionStep.Waiting -> {
                    val event = SessionEvent.Waiting(step.reason, step.values)
                    if (event != lastWait) onEvent(event)
                    lastWait = event
                    if (setup.endWhenWaiting) return SessionEnd.NothingToRun(step.reason, step.values)
                    betweenBattles()?.let { return it }
                    setup.sleep(setup.pollMillis)
                }
                is SessionStep.Battle -> {
                    lastWait = null
                    val battle = when (val placement = place(current, step)) {
                        is Placement.Ready -> placement
                        is Placement.LeaveOut -> {
                            // These bots can never be placed from this commit: they are left out and the next battle is chosen without them.
                            excluded += placement.keys
                            continue
                        }
                        Placement.TryAgain -> {
                            // The source could not be reached this time; it is tried again after the poll time and counts as a failure.
                            if (++failures >= setup.maxConsecutiveFailures) return SessionEnd.GaveUp(failures)
                            setup.sleep(setup.pollMillis)
                            continue
                        }
                    }
                    // Fetching can take a while; a stop or cancel asked for meanwhile ends the session before a new battle starts.
                    if (control.cancelRequested) return SessionEnd.Cancelled
                    if (control.stopRequested) return SessionEnd.Stopped
                    when (val result = runBattle(step, battle)) {
                        is Ran.Kept -> { history = history.after(step); failures = 0 }
                        is Ran.NotKept -> {
                            if (result.reason == NotKeptReason.CANCELLED) return SessionEnd.Cancelled
                            // The same battle is likely to fail again, so the next choice moves on to another where there is one.
                            history = history.afterNotCompleted(step)
                            if (++failures >= setup.maxConsecutiveFailures) return SessionEnd.GaveUp(failures)
                        }
                        is Ran.JournalFailed -> return SessionEnd.JournalFailed(result.message)
                    }
                    betweenBattles()?.let { return it }
                }
            }
        }
    }

    /** Runs the setup's work between battles, such as sending results. A journal that cannot be written ends the session. */
    private fun betweenBattles(): SessionEnd? =
        try {
            setup.betweenBattles()
            null
        } catch (e: JournalUnusable) {
            SessionEnd.JournalFailed(e.message.orEmpty())
        }

    private sealed interface Placement {
        /** Every bot of the battle is in place; [jobBots] are the participants. */
        class Ready(val jobBots: List<JobBot>, val bots: List<CatalogBot> = emptyList()) : Placement

        /** These bots can never be placed from this commit. */
        class LeaveOut(val keys: Set<String>) : Placement

        /** Some bot could not be placed this time, for example because its source could not be reached. */
        data object TryAgain : Placement
    }

    /** A bot and the folder it is run from. */
    private class Spot(val bot: CatalogBot, val folder: Path) {
        val key: String = SessionPlanner.key(bot)
    }

    private fun folderName(bot: CatalogBot) = bot.path.substringAfterLast('/')

    /**
     * Puts the bots of [step] in the cache. Each participant gets a folder below the hash of its own source, and a team's
     * members are put beside the team in that same folder, because a team names its members by the folders next to it.
     */
    private fun place(request: SessionRequest, step: SessionStep.Battle): Placement {
        val byKey = request.entries.filter { it.isActive }.associateBy { SessionPlanner.key(it) }
        val participants = mutableListOf<Spot>()
        val spots = mutableListOf<Spot>()
        val leaveOut = mutableSetOf<String>()
        for (key in step.participants) {
            val bot = byKey[key] ?: return Placement.LeaveOut(setOf(key))
            val group = setup.botCache.resolve(bot.sourceHash.removePrefix("sha256:"))
            val members = bot.teamMembers.map { byKey[it] ?: return Placement.LeaveOut(setOf(key)) }
            val here = (listOf(bot) + members).map { Spot(it, group.resolve(folderName(it))) }
            if (here.map { it.folder }.distinct().size != here.size) {
                onEvent(SessionEvent.BotNotPlaced(key, NotPlacedReason.FOLDER_NAME_CLASH))
                leaveOut += key
            }
            participants += here.first()
            spots += here
        }
        if (leaveOut.isNotEmpty()) return Placement.LeaveOut(leaveOut)

        val ready = mutableSetOf<Path>()
        var tryAgain = false
        val missing = mutableListOf<Spot>()
        for (spot in spots) {
            val matches = try {
                Files.isDirectory(spot.folder) && TreeHash.ofDirectory(spot.folder) == spot.bot.sourceHash
            } catch (e: IOException) {
                false
            } catch (e: java.io.UncheckedIOException) {
                // A file that cannot be read now, for example one held by a process that has not ended; the folder is replaced.
                false
            }
            if (matches) {
                // add, not +=: a Path is also a list of its own parts.
                ready.add(spot.folder)
            } else if (Files.exists(spot.folder) && !spot.folder.toFile().deleteRecursively()) {
                // A folder that no longer matches its hash is never run. This one could not be removed, so it is tried again later.
                onEvent(SessionEvent.BotNotPlaced(spot.key, NotPlacedReason.SOURCE_UNAVAILABLE, "a cached folder that no longer matches could not be removed"))
                tryAgain = true
            } else {
                missing += spot
            }
        }
        if (missing.isNotEmpty()) {
            val fetched = fetch(missing)
            ready.addAll(fetched.placed)
            leaveOut.addAll(fetched.leaveOut)
            tryAgain = tryAgain || fetched.tryAgain
        }
        // A team that holds a member that is left out is left out with it by the session loop.
        if (leaveOut.isNotEmpty()) return Placement.LeaveOut(leaveOut)
        if (tryAgain || spots.any { it.folder !in ready }) return Placement.TryAgain
        return Placement.Ready(participants.map { JobBot(it.bot.name, it.bot.version, it.bot.sourceHash, it.folder.toString()) }, spots.map { it.bot })
    }

    private class Fetched(val placed: Set<Path>, val leaveOut: Set<String>, val tryAgain: Boolean)

    /** Fetches [missing] from the catalog commit. Bots with the same folder name are fetched in rounds of their own so they never meet. */
    private fun fetch(missing: List<Spot>): Fetched {
        val placed = mutableSetOf<Path>()
        val leaveOut = mutableSetOf<String>()
        var tryAgain = false
        val notPlaced = { spot: Spot, reason: NotPlacedReason, detail: String ->
            onEvent(SessionEvent.BotNotPlaced(spot.key, reason, detail))
            if (reason == NotPlacedReason.SOURCE_UNAVAILABLE) tryAgain = true else leaveOut += spot.key
        }
        val rounds = mutableListOf<MutableList<Spot>>()
        for (spot in missing) {
            val round = rounds.firstOrNull { r -> r.none { folderName(it.bot) == folderName(spot.bot) } } ?: mutableListOf<Spot>().also { rounds += it }
            round += spot
        }
        for (round in rounds) {
            val stage = try {
                Files.createDirectories(setup.botCache)
                Files.createTempDirectory(setup.botCache, ".stage-")
            } catch (e: IOException) {
                round.forEach { notPlaced(it, NotPlacedReason.SOURCE_UNAVAILABLE, e.message.orEmpty()) }
                continue
            }
            try {
                val results = try {
                    BotFetcher.fetch(setup.source, setup.commit, round.map { it.bot }, stage)
                } catch (e: UnsafeFetch) {
                    round.forEach { notPlaced(it, NotPlacedReason.UNSAFE, e.message.orEmpty()) }
                    continue
                } catch (e: IOException) {
                    round.forEach { notPlaced(it, NotPlacedReason.SOURCE_UNAVAILABLE, e.message.orEmpty()) }
                    continue
                } catch (e: java.io.UncheckedIOException) {
                    round.forEach { notPlaced(it, NotPlacedReason.SOURCE_UNAVAILABLE, e.message.orEmpty()) }
                    continue
                } catch (e: CatalogUnreadable) {
                    // The bot repository answered with an error status, for example while it is busy; that passes.
                    round.forEach { notPlaced(it, NotPlacedReason.SOURCE_UNAVAILABLE, e.message.orEmpty()) }
                    continue
                }
                for ((spot, result) in round.zip(results)) {
                    when (result) {
                        is FetchResult.Placed -> try {
                            Files.createDirectories(spot.folder.parent)
                            // Fails when something is already there, so a folder is never taken as placed without the fetched source in it.
                            Files.move(result.directory, spot.folder)
                            placed.add(spot.folder)
                        } catch (e: IOException) {
                            notPlaced(spot, NotPlacedReason.SOURCE_UNAVAILABLE, e.message.orEmpty())
                        }
                        is FetchResult.HashMismatch -> notPlaced(spot, NotPlacedReason.HASH_MISMATCH, result.actual)
                        is FetchResult.NotInArchive -> notPlaced(spot, NotPlacedReason.NOT_IN_ARCHIVE, "")
                        is FetchResult.AlreadyExists -> notPlaced(spot, NotPlacedReason.SOURCE_UNAVAILABLE, "the staging folder was not empty")
                    }
                }
            } finally {
                stage.toFile().deleteRecursively()
            }
        }
        return Fetched(placed, leaveOut, tryAgain)
    }

    private sealed interface Ran {
        data object Kept : Ran
        data class NotKept(val reason: NotKeptReason) : Ran
        data class JournalFailed(val message: String) : Ran
    }

    private fun runBattle(step: SessionStep.Battle, placed: Placement.Ready): Ran {
        val rounds = setup.roundsPerFormat[step.gameType]
        val battleId = setup.newBattleId()
        if (rounds == null) {
            onEvent(SessionEvent.BattleNotKept(battleId, NotKeptReason.WORKER_REFUSED, "no rounds are set for ${step.gameType}"))
            return Ran.NotKept(NotKeptReason.WORKER_REFUSED)
        }
        val job = Job(jobId = battleId, behaviorVersion = setup.engine.behaviorVersion, gameType = step.gameType, numberOfRounds = rounds, bots = placed.jobBots)
        onEvent(SessionEvent.BattleStarted(battleId, step.gameType, step.participants))
        val outcome = setup.host.run(setup.command, job, { control.cancelRequested }) { folders -> setup.battleEnvironment(placed.bots, folders) }
        val notKept = { reason: NotKeptReason, detail: String ->
            onEvent(SessionEvent.BattleNotKept(battleId, reason, detail))
            Ran.NotKept(reason)
        }
        return when (outcome) {
            is WorkerOutcome.Completed -> keep(job, outcome.result, notKept)
            is WorkerOutcome.Refused -> notKept(NotKeptReason.WORKER_REFUSED, "${outcome.code}: ${outcome.message}")
            is WorkerOutcome.Failed -> notKept(NotKeptReason.WORKER_FAILED, "${outcome.code}: ${outcome.message}")
            is WorkerOutcome.TimedOut -> notKept(NotKeptReason.TIMED_OUT, outcome.limit.toString())
            is WorkerOutcome.LimitExceeded -> notKept(NotKeptReason.LIMIT_EXCEEDED, outcome.message)
            is WorkerOutcome.EndedWithoutFinish -> notKept(NotKeptReason.ENDED_WITHOUT_FINISH, outcome.exitCode?.toString().orEmpty())
            is WorkerOutcome.UnreadableOutput -> notKept(NotKeptReason.UNREADABLE_OUTPUT, outcome.message)
            is WorkerOutcome.CouldNotStart -> notKept(NotKeptReason.COULD_NOT_START, outcome.message)
            WorkerOutcome.Cancelled -> notKept(NotKeptReason.CANCELLED, "")
        }
    }

    /** Makes the record from the job and the result alone, and keeps it. */
    private fun keep(job: Job, result: WorkerEvent.Result, notKept: (NotKeptReason, String) -> Ran): Ran {
        val sourceHashes = job.bots.associate { "${it.name} ${it.version}" to it.sourceHash }
        val participants = result.bots.map { toParticipant(it, sourceHashes["${it.name} ${it.version}"] ?: return notKept(NotKeptReason.RESULT_MISMATCH, "${it.name} ${it.version}")) }
        if (participants.size != job.bots.size || participants.map { "${it.name} ${it.version}" }.toSet() != sourceHashes.keys) {
            return notKept(NotKeptReason.RESULT_MISMATCH, "the result names other bots than the battle ran")
        }
        if (result.numberOfRounds != job.numberOfRounds) {
            return notKept(NotKeptReason.RESULT_MISMATCH, "the result has ${result.numberOfRounds} rounds, the battle ${job.numberOfRounds}")
        }
        val record = RankedRecord(
            battleId = job.jobId, gameType = job.gameType, timestamp = setup.clock().toString(), client = setup.client, engine = setup.engine,
            botsRepoCommit = setup.commit, rounds = result.numberOfRounds, participants = participants,
        )
        return try {
            when (setup.journal.add(record)) {
                AddResult.Kept -> {
                    onEvent(SessionEvent.BattleKept(job.jobId))
                    Ran.Kept
                }
                // Only a battle id that is not unique gets here; nothing was written, so the session stops instead of counting it.
                AddResult.AlreadyKept -> Ran.JournalFailed("The journal already holds a battle with the id ${job.jobId}.")
            }
        } catch (e: JournalUnusable) {
            Ran.JournalFailed(e.message.orEmpty())
        }
    }

    private fun toParticipant(b: BotOutcome, sourceHash: String) = JournalParticipant(
        name = b.name, version = b.version, sourceHash = sourceHash, rank = b.rank, totalScore = b.totalScore, survival = b.survival,
        lastSurvivorBonus = b.lastSurvivorBonus, bulletDamage = b.bulletDamage, bulletKillBonus = b.bulletKillBonus, ramDamage = b.ramDamage,
        ramKillBonus = b.ramKillBonus, firstPlaces = b.firstPlaces, secondPlaces = b.secondPlaces, thirdPlaces = b.thirdPlaces,
    )
}
