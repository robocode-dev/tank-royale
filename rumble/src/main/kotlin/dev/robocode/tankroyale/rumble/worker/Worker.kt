package dev.robocode.tankroyale.rumble.worker

import dev.robocode.tankroyale.rumble.protocol.ErrorCode
import dev.robocode.tankroyale.rumble.protocol.ExitCode
import dev.robocode.tankroyale.rumble.protocol.Job
import dev.robocode.tankroyale.rumble.protocol.MessageCodec
import dev.robocode.tankroyale.rumble.protocol.MessageRejected
import dev.robocode.tankroyale.rumble.protocol.WorkerEvent
import java.io.BufferedReader
import java.io.PrintStream

/** The engine could not be used for this job because of what the job asked, for example a wrong engine pin. */
class EngineRefused(val code: String, message: String) : Exception(message, null, false, false)

/** The engine started the job but the battle did not complete. */
class EngineFailed(message: String, cause: Throwable? = null) : Exception(message, cause)

/** Runs one battle. The real engine is the Battle Runner; tests use a fake. */
fun interface BattleEngine {
    /** Returns the result of the battle, or throws [EngineRefused] or [EngineFailed]. */
    fun run(job: Job): WorkerEvent.Result
}

/**
 * Reads one job from [input], runs it on [engine] and writes events to [output], one per line, ending with an exit
 * code. Nothing else is ever written to [output]: a caller needs no log to know what happened.
 */
object Worker {
    fun run(input: BufferedReader, output: PrintStream, engine: BattleEngine): Int {
        fun emit(event: WorkerEvent) {
            output.println(MessageCodec.write(event))
            output.flush()
        }

        val line = input.readLine()
        if (line == null) {
            emit(WorkerEvent.Refused(jobId = null, code = ErrorCode.INVALID_JOB, message = "No job was given on standard input."))
            return ExitCode.REFUSED
        }
        val job = try {
            MessageCodec.readJob(line)
        } catch (rejected: MessageRejected) {
            emit(WorkerEvent.Refused(jobId = null, code = rejected.code, message = rejected.message.orEmpty()))
            return ExitCode.REFUSED
        }

        emit(WorkerEvent.Started(jobId = job.jobId))
        return try {
            emit(engine.run(job))
            emit(WorkerEvent.Finished(jobId = job.jobId))
            ExitCode.FINISHED
        } catch (refused: EngineRefused) {
            emit(WorkerEvent.Refused(jobId = job.jobId, code = refused.code, message = refused.message.orEmpty()))
            ExitCode.REFUSED
        } catch (failed: EngineFailed) {
            emit(WorkerEvent.Failed(jobId = job.jobId, code = ErrorCode.BATTLE_FAILED, message = failed.message.orEmpty()))
            ExitCode.FAILED
        } catch (e: Exception) {
            emit(WorkerEvent.Failed(jobId = job.jobId, code = ErrorCode.INTERNAL_ERROR, message = "The worker failed unexpectedly."))
            ExitCode.FAILED
        }
    }
}
