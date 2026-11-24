package dev.robocode.tankroyale.rumble.worker

import dev.robocode.tankroyale.rumble.protocol.ErrorCode
import dev.robocode.tankroyale.rumble.protocol.Job
import dev.robocode.tankroyale.rumble.protocol.BotOutcome
import dev.robocode.tankroyale.rumble.protocol.WorkerEvent
import dev.robocode.tankroyale.runner.BattleException
import dev.robocode.tankroyale.runner.BattleRunner
import dev.robocode.tankroyale.runner.BattleSetup
import dev.robocode.tankroyale.runner.BootProgress
import dev.robocode.tankroyale.runner.BotEntry
import java.time.Duration
import java.util.function.Consumer

/** Runs a job on the Battle Runner with an embedded server, requiring the job's engine pin. */
object RunnerEngine : BattleEngine {

    /**
     * How long a battle waits for all its bots to connect. The Rumble's bots start from source, and a Java bot is
     * compiled at every start, which takes 10 to 30 seconds when four start together on a slow computer; the runner's
     * 30 seconds let a battle fail on one bot that was only slow. It stays far below the host's limit on a whole job.
     */
    internal val BOT_CONNECT_TIMEOUT: Duration = Duration.ofSeconds(90)

    /** A runner that refuses to run a battle on a server whose behavior version is not [behaviorVersion]. */
    internal fun createRunner(behaviorVersion: Int, booting: Consumer<BootProgress>? = null): BattleRunner =
        BattleRunner.create {
            embeddedServer().requireBehaviorVersion(behaviorVersion).botConnectTimeout(BOT_CONNECT_TIMEOUT)
            booting?.let { onBootProgress(it) }
        }

    internal fun setupFor(job: Job): BattleSetup {
        val rounds = job.numberOfRounds
        return when (job.gameType) {
            "classic" -> BattleSetup.classic { numberOfRounds = rounds }
            "melee" -> BattleSetup.melee { numberOfRounds = rounds }
            "1v1" -> BattleSetup.oneVsOne { numberOfRounds = rounds }
            "twinduel" -> BattleSetup.twinDuel { numberOfRounds = rounds }
            else -> throw EngineRefused(ErrorCode.UNKNOWN_GAME_TYPE, "The game type '${job.gameType}' is not one the worker can run.")
        }
    }

    /** The progress of a battle's start as the worker reports it: counts and times only. */
    internal fun bootEvent(job: Job, progress: BootProgress) = WorkerEvent.Booting(
        jobId = job.jobId, connected = progress.totalConnected, expected = progress.totalExpected,
        elapsedMillis = progress.elapsedMs, limitMillis = progress.timeoutMs,
    )

    /** True when the runner's refusal is about the engine pin, which is the job's fault and not a failed battle. */
    internal fun isPinMismatch(e: BattleException): Boolean =
        e.message.orEmpty().contains("behavior version", ignoreCase = true)

    override fun run(job: Job): WorkerEvent.Result = run(job) { }

    override fun run(job: Job, booting: (WorkerEvent.Booting) -> Unit): WorkerEvent.Result {
        val setup = setupFor(job)
        val bots = job.bots.map { BotEntry.of(it.folder) }
        try {
            val progress = Consumer<BootProgress> { booting(bootEvent(job, it)) }
            createRunner(job.behaviorVersion, progress).use { runner ->
                val results = runner.runBattle(setup, bots)
                return WorkerEvent.Result(
                    jobId = job.jobId,
                    numberOfRounds = results.numberOfRounds,
                    bots = results.results.map {
                        BotOutcome(
                            it.name, it.version, it.isTeam, it.rank, it.totalScore, it.survival, it.lastSurvivorBonus,
                            it.bulletDamage, it.bulletKillBonus, it.ramDamage, it.ramKillBonus,
                            it.firstPlaces, it.secondPlaces, it.thirdPlaces,
                        )
                    },
                )
            }
        } catch (e: BattleException) {
            if (isPinMismatch(e)) throw EngineRefused(ErrorCode.PIN_MISMATCH, e.message.orEmpty())
            throw EngineFailed(e.message.orEmpty(), e)
        }
    }
}
