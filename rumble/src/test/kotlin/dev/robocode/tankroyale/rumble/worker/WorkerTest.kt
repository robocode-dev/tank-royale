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

    // RBW-024

    private fun booting(connected: Int, elapsed: Long, expected: Int = 4) =
        WorkerEvent.Booting(jobId = "job-7", connected = connected, expected = expected, elapsedMillis = elapsed, limitMillis = 90_000)

    @Test
    @Tag("RBW-024")
    @Tag("Unit")
    @Tag("Positive")
    fun testRBW024_UnitPositive_theWorkerReportsTheStartingBotsWhenTheCountChangesAndAboutOnceASecondBetweenStartedAndResult() {
        val reports = listOf(
            booting(0, 0), booting(0, 500), booting(0, 1000), booting(0, 1500), booting(1, 1700), booting(1, 2100),
            booting(1, 2600), booting(2, 2700), booting(4, 3000),
        )
        val run = run(MessageCodec.write(job) + "\n", object : BattleEngine {
            override fun run(job: Job): WorkerEvent.Result = result
            override fun run(job: Job, booting: (WorkerEvent.Booting) -> Unit): WorkerEvent.Result {
                reports.forEach(booting)
                return result
            }
        })

        assertEquals(ExitCode.FINISHED, run.exit)
        val events = run.events
        assertEquals(WorkerEvent.Started(jobId = "job-7"), events.first())
        assertEquals(result, events[events.size - 2])
        assertEquals(WorkerEvent.Finished(jobId = "job-7"), events.last())
        // A report is written when the count changes or the second turns, so 0 at 500 and 1 at 2600 are left out.
        assertEquals(
            listOf(booting(0, 0), booting(0, 1000), booting(1, 1700), booting(1, 2100), booting(2, 2700), booting(4, 3000)),
            events.filterIsInstance<WorkerEvent.Booting>(),
        )
    }

    @Test
    @Tag("RBW-024")
    @Tag("Unit")
    @Tag("Negative")
    fun testRBW024_UnitNegative_aBootEventCarriesOnlyCountsAndTimesAndNoWorkerWritesOneWithoutAnEngineThatReports() {
        val line = MessageCodec.write(booting(1, 1234))
        // The names of its fields are all of what it can say: a bot's name, folder or output has no place in it.
        assertEquals(
            setOf("type", "schemaVersion", "jobId", "connected", "expected", "elapsedMillis", "limitMillis"),
            kotlinx.serialization.json.Json.parseToJsonElement(line).let { (it as kotlinx.serialization.json.JsonObject).keys },
        )
        // The Battle Runner's progress becomes the event with its counts and times, and an engine that reports nothing adds none.
        val progress = dev.robocode.tankroyale.runner.BootProgress(
            expected = mapOf(dev.robocode.tankroyale.runner.BotIdentity("Walls", "1.0.0", "A") to 2, dev.robocode.tankroyale.runner.BotIdentity("SpinBot", "1.0.0", "A") to 1),
            connected = mapOf(dev.robocode.tankroyale.runner.BotIdentity("Walls", "1.0.0", "A") to 1),
            pending = emptyMap(), elapsedMs = 2500, timeoutMs = 90_000,
        )
        assertEquals(WorkerEvent.Booting(jobId = "job-7", connected = 1, expected = 3, elapsedMillis = 2500, limitMillis = 90_000), RunnerEngine.bootEvent(job, progress))
        val quiet = run(MessageCodec.write(job) + "\n", BattleEngine { result })
        assertTrue(quiet.events.none { it is WorkerEvent.Booting })
    }
}
