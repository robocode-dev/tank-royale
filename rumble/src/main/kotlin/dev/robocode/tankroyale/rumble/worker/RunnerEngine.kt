package dev.robocode.tankroyale.rumble.worker

import dev.robocode.tankroyale.rumble.protocol.ErrorCode
import dev.robocode.tankroyale.rumble.protocol.Job
import dev.robocode.tankroyale.rumble.protocol.BotOutcome
import dev.robocode.tankroyale.rumble.protocol.WorkerEvent
import dev.robocode.tankroyale.runner.BattleException
import dev.robocode.tankroyale.runner.BattleRunner
import dev.robocode.tankroyale.runner.BattleSetup
import dev.robocode.tankroyale.runner.BotEntry

/** Runs a job on the Battle Runner with an embedded server, requiring the job's engine pin. */
object RunnerEngine : BattleEngine {

    /** A runner that refuses to run a battle on a server whose behavior version is not [behaviorVersion]. */
    internal fun createRunner(behaviorVersion: Int): BattleRunner =
        BattleRunner.create { embeddedServer().requireBehaviorVersion(behaviorVersion) }

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

    /** True when the runner's refusal is about the engine pin, which is the job's fault and not a failed battle. */
    internal fun isPinMismatch(e: BattleException): Boolean =
        e.message.orEmpty().contains("behavior version", ignoreCase = true)

    override fun run(job: Job): WorkerEvent.Result {
        val setup = setupFor(job)
        val bots = job.bots.map { BotEntry.of(it.folder) }
        try {
            createRunner(job.behaviorVersion).use { runner ->
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
