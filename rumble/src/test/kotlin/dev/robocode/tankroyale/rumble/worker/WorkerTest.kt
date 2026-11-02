package dev.robocode.tankroyale.rumble.worker

import dev.robocode.tankroyale.rumble.protocol.BotOutcome
import dev.robocode.tankroyale.rumble.protocol.ErrorCode
import dev.robocode.tankroyale.rumble.protocol.ExitCode
import dev.robocode.tankroyale.rumble.protocol.Job
import dev.robocode.tankroyale.rumble.protocol.JobBot
import dev.robocode.tankroyale.rumble.protocol.MessageCodec
import dev.robocode.tankroyale.rumble.protocol.WorkerEvent
import dev.robocode.tankroyale.runner.BattleException
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertInstanceOf
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import java.io.BufferedReader
import java.io.ByteArrayOutputStream
import java.io.PrintStream
import java.io.StringReader

class WorkerTest {

    private val job = Job(
        jobId = "job-7", behaviorVersion = 2, gameType = "1v1", numberOfRounds = 2,
        bots = listOf(JobBot("Walls", "1.0.0", "sha256:aa", "/work/Walls"), JobBot("SpinBot", "1.0.0", "sha256:bb", "/work/SpinBot")),
    )

    private fun outcome(name: String, rank: Int) = BotOutcome(name, "1.0.0", false, rank, 100 - rank, 1, 1, 1, 1, 1, 1, 1, 0, 0)

    private val result = WorkerEvent.Result(jobId = "job-7", numberOfRounds = 2, bots = listOf(outcome("Walls", 1), outcome("SpinBot", 2)))

    private class Run(val exit: Int, val raw: String) {
        val lines get() = raw.lines().filter { it.isNotEmpty() }
        val events get() = lines.map { MessageCodec.readEvent(it) }
    }

    private fun run(input: String, engine: BattleEngine): Run {
        val buffer = ByteArrayOutputStream()
        val exit = Worker.run(BufferedReader(StringReader(input)), PrintStream(buffer, true, Charsets.UTF_8), engine)
        return Run(exit, buffer.toString(Charsets.UTF_8))
    }

    private val notCalled = BattleEngine { error("The engine must not be called") }

    // RBW-003

    @Test
    @Tag("RBW-003")
    @Tag("Unit")
    @Tag("Positive")
    fun testRBW003_UnitPositive_theWorkerReportsStartedResultAndFinishedInOrderAndNothingElse() {
        var received: Job? = null
        val run = run(MessageCodec.write(job) + "\n", BattleEngine { received = it; result })

        assertEquals(ExitCode.FINISHED, run.exit)
        assertEquals(job, received)
        assertEquals(listOf(WorkerEvent.Started(jobId = "job-7"), result, WorkerEvent.Finished(jobId = "job-7")), run.events)
        assertEquals(3, run.lines.size)
        assertEquals(listOf(1, 2), (run.events[1] as WorkerEvent.Result).bots.map { it.rank })
    }

    // RBW-004

    @Test
    @Tag("RBW-004")
    @Tag("Unit")
    @Tag("Negative")
    fun testRBW004_UnitNegative_aJobThatCannotBeReadIsRefusedWithoutCallingTheEngine() {
        val newer = run(MessageCodec.write(job).replace("\"schemaVersion\":1", "\"schemaVersion\":5") + "\n", notCalled)
        assertEquals(ExitCode.REFUSED, newer.exit)
        assertEquals(ErrorCode.UNKNOWN_VERSION, (newer.events.single() as WorkerEvent.Refused).code)

        val invalid = run("{ this is not a job }\n", notCalled)
        assertEquals(ExitCode.REFUSED, invalid.exit)
        assertEquals(ErrorCode.INVALID_JOB, (invalid.events.single() as WorkerEvent.Refused).code)

        val empty = run("", notCalled)
        assertEquals(ExitCode.REFUSED, empty.exit)
        assertInstanceOf(WorkerEvent.Refused::class.java, empty.events.single())
        listOf(newer, invalid, empty).forEach { assertTrue((it.events.single() as WorkerEvent.Refused).message.isNotBlank()) }
    }

    @Test
    @Tag("RBW-004")
    @Tag("Unit")
    @Tag("Negative")
    fun testRBW004_UnitNegative_aBattleThatFailsEndsWithAStableCodeAndNoFinishedEvent() {
        val failed = run(MessageCodec.write(job) + "\n", BattleEngine { throw EngineFailed("Bot Walls did not connect") })
        assertEquals(ExitCode.FAILED, failed.exit)
        assertEquals(
            listOf(WorkerEvent.Started(jobId = "job-7"), WorkerEvent.Failed(jobId = "job-7", code = ErrorCode.BATTLE_FAILED, message = "Bot Walls did not connect")),
            failed.events,
        )

        val unexpected = run(MessageCodec.write(job) + "\n", BattleEngine { throw IllegalStateException("secret internal detail at /home/user/x") })
        assertEquals(ExitCode.FAILED, unexpected.exit)
        val event = unexpected.events.last() as WorkerEvent.Failed
        assertEquals(ErrorCode.INTERNAL_ERROR, event.code)
        assertFalse(unexpected.raw.contains("secret internal detail"))
        assertTrue(unexpected.events.none { it is WorkerEvent.Finished })
    }

    // RBW-005

    @Test
    @Tag("RBW-005")
    @Tag("Unit")
    @Tag("Positive")
    fun testRBW005_UnitPositive_theRunnerIsAskedToRequireExactlyTheJobsBehaviorVersion() {
        RunnerEngine.createRunner(7).use { assertEquals(7, it.config.requiredBehaviorVersion) }
        RunnerEngine.createRunner(2).use { assertEquals(2, it.config.requiredBehaviorVersion) }
    }

    @Test
    @Tag("RBW-005")
    @Tag("Unit")
    @Tag("Negative")
    fun testRBW005_UnitNegative_aPinMismatchIsRefusedAndAnOrdinaryFailureIsNot() {
        val mismatch = BattleException("Server behavior version 2 does not match required version 3")
        assertTrue(RunnerEngine.isPinMismatch(mismatch))
        assertFalse(RunnerEngine.isPinMismatch(BattleException("Bot Walls did not connect in time")))

        val refused = run(
            MessageCodec.write(job) + "\n",
            BattleEngine { throw EngineRefused(ErrorCode.PIN_MISMATCH, "Server behavior version 2 does not match required version 3") },
        )
        assertEquals(ExitCode.REFUSED, refused.exit)
        assertEquals(ErrorCode.PIN_MISMATCH, (refused.events.last() as WorkerEvent.Refused).code)
        assertTrue(refused.events.none { it is WorkerEvent.Result || it is WorkerEvent.Finished })
    }

    @Test
    @Tag("RBW-005")
    @Tag("Unit")
    @Tag("Positive")
    fun testRBW005_UnitPositive_everyGameTypeTheRumbleRunsHasASetupAndAnUnknownOneIsRefused() {
        listOf("classic", "melee", "1v1", "twinduel").forEach { RunnerEngine.setupFor(job.copy(gameType = it)) }

        val refused = assertThrows(EngineRefused::class.java) { RunnerEngine.setupFor(job.copy(gameType = "chess")) }
        assertEquals(ErrorCode.UNKNOWN_GAME_TYPE, refused.code)
    }
}
