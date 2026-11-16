package dev.robocode.tankroyale.rumble.cli

import dev.robocode.tankroyale.rumble.protocol.BotOutcome
import dev.robocode.tankroyale.rumble.protocol.ErrorCode
import dev.robocode.tankroyale.rumble.protocol.MessageCodec
import dev.robocode.tankroyale.rumble.protocol.WorkerEvent
import kotlin.system.exitProcess

/**
 * A tiny battle worker for the command line's session tests, started as a real process. The first argument selects
 * what it does: `ok` completes the battle, `slow` completes it after two seconds, `fail` fails it, and `hang` starts it
 * and never ends.
 */
fun main(args: Array<String>) {
    val mode = args.firstOrNull() ?: "ok"
    val job = MessageCodec.readJob(readLine().orEmpty())
    fun emit(event: WorkerEvent) {
        println(MessageCodec.write(event))
        System.out.flush()
    }
    emit(WorkerEvent.Started(jobId = job.jobId))
    when (mode) {
        "fail" -> {
            emit(WorkerEvent.Failed(jobId = job.jobId, code = ErrorCode.BATTLE_FAILED, message = "no bots"))
            exitProcess(1)
        }
        "hang" -> Thread.sleep(120_000)
        else -> {
            if (mode == "slow") Thread.sleep(2_000)
            val bots = job.bots.mapIndexed { i, b -> BotOutcome(b.name, b.version, false, i + 1, 100 - i, 1, 1, 1, 1, 1, 1, if (i == 0) job.numberOfRounds else 0, if (i == 0) 0 else job.numberOfRounds, 0) }
            emit(WorkerEvent.Result(jobId = job.jobId, numberOfRounds = job.numberOfRounds, bots = bots))
            emit(WorkerEvent.Finished(jobId = job.jobId))
        }
    }
}
