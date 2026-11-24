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

/**
 * The engine started the job but the battle did not complete. [code] is an [ErrorCode]; with
 * [ErrorCode.BOTS_DID_NOT_CONNECT], [bots] names the bots that had not connected as "name version".
 */
class EngineFailed(
    message: String,
    cause: Throwable? = null,
    val code: String = ErrorCode.BATTLE_FAILED,
    val bots: List<String> = emptyList(),
) : Exception(message, cause)

/** Runs one battle. The real engine is the Battle Runner; tests use a fake. */
fun interface BattleEngine {
    /** Returns the result of the battle, or throws [EngineRefused] or [EngineFailed]. */
    fun run(job: Job): WorkerEvent.Result

    /**
     * Runs the battle and calls [booting] as its bots start, from any thread, as often as the engine likes; the worker
     * decides what is written. An engine that has nothing to report just runs the battle.
     */
    fun run(job: Job, booting: (WorkerEvent.Booting) -> Unit): WorkerEvent.Result = run(job)
}

/**
 * Reads one job from [input], runs it on [engine] and writes events to [output], one per line, ending with an exit
 * code. Nothing else is ever written to [output]: a caller needs no log to know what happened.
 */
object Worker {
    /** How often the starting bots are reported while the number of connected bots does not change. */
    private const val BOOT_REPORT_MILLIS = 1000L

    fun run(input: BufferedReader, output: PrintStream, engine: BattleEngine): Int {
        // The engine reports the starting bots from its own threads, so a line is never written by two at once.
        fun emit(event: WorkerEvent) = synchronized(output) {
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
            // The starting bots are reported when the number of connected bots changes and about once a second otherwise.
            var last: WorkerEvent.Booting? = null
            val result = engine.run(job) { booting ->
                synchronized(output) {
                    val before = last
                    if (before == null || before.connected != booting.connected || before.expected != booting.expected ||
                        booting.elapsedMillis / BOOT_REPORT_MILLIS != before.elapsedMillis / BOOT_REPORT_MILLIS
                    ) {
                        last = booting
                        emit(booting)
                    }
                }
            }
            emit(result)
            emit(WorkerEvent.Finished(jobId = job.jobId))
            ExitCode.FINISHED
        } catch (refused: EngineRefused) {
            emit(WorkerEvent.Refused(jobId = job.jobId, code = refused.code, message = refused.message.orEmpty()))
            ExitCode.REFUSED
        } catch (failed: EngineFailed) {
            emit(WorkerEvent.Failed(jobId = job.jobId, code = failed.code, message = failed.message.orEmpty(), bots = failed.bots))
            ExitCode.FAILED
        } catch (e: Exception) {
            emit(WorkerEvent.Failed(jobId = job.jobId, code = ErrorCode.INTERNAL_ERROR, message = "The worker failed unexpectedly."))
            ExitCode.FAILED
        }
    }
}
