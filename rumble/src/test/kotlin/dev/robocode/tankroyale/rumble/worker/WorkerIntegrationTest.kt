package dev.robocode.tankroyale.rumble.worker

import dev.robocode.tankroyale.common.rules.CURRENT_BEHAVIOR_VERSION
import dev.robocode.tankroyale.rumble.protocol.ErrorCode
import dev.robocode.tankroyale.rumble.protocol.ExitCode
import dev.robocode.tankroyale.rumble.protocol.Job
import dev.robocode.tankroyale.rumble.protocol.JobBot
import dev.robocode.tankroyale.rumble.protocol.MessageCodec
import dev.robocode.tankroyale.rumble.protocol.WorkerEvent
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import java.io.BufferedReader
import java.io.ByteArrayOutputStream
import java.io.PrintStream
import java.io.StringReader
import java.nio.file.Files
import java.nio.file.Path

/**
 * Runs a real battle through the worker with sample bots and the embedded server.
 *
 * Run via `./gradlew :rumble:integrationTest` — excluded from the default `test` task.
 */
@Tag("integration")
class WorkerIntegrationTest {

    private fun sampleBot(name: String): Path {
        val root = System.getProperty("sampleBots.java.dir") ?: error("System property 'sampleBots.java.dir' not set — run via :rumble:integrationTest")
        return Path.of(root).resolve(name).also { check(Files.exists(it)) { "Sample bot not found: $it" } }
    }

    // RBW-005

    @Test
    @Tag("RBW-005")
    @Tag("Integration")
    @Tag("Negative")
    fun testRBW005_IntegrationNegative_aWrongEnginePinIsRefusedByTheRealRunnerBeforeAnyBotFights() {
        val job = Job(
            jobId = "pin-1", behaviorVersion = CURRENT_BEHAVIOR_VERSION + 1, gameType = "1v1", numberOfRounds = 1,
            bots = listOf("Walls", "SpinBot").map { JobBot(it, "1.0.0", "sha256:unchecked", sampleBot(it).toString()) },
        )
        val buffer = ByteArrayOutputStream()

        val exit = Worker.run(BufferedReader(StringReader(MessageCodec.write(job) + "\n")), PrintStream(buffer, true, Charsets.UTF_8), RunnerEngine)

        val events = buffer.toString(Charsets.UTF_8).lines().filter { it.isNotEmpty() }.map { MessageCodec.readEvent(it) }
        assertEquals(ExitCode.REFUSED, exit, events.toString())
        assertEquals(ErrorCode.PIN_MISMATCH, (events.last() as WorkerEvent.Refused).code)
        assertTrue(events.none { it is WorkerEvent.Result || it is WorkerEvent.Finished })
    }

    // RBW-007

    @Test
    @Tag("RBW-007")
    @Tag("Integration")
    @Tag("Positive")
    fun testRBW007_IntegrationPositive_aRealBattleGivesARankedResultForEveryBot() {
        val job = Job(
            jobId = "real-1", behaviorVersion = CURRENT_BEHAVIOR_VERSION, gameType = "1v1", numberOfRounds = 1,
            bots = listOf("Walls", "SpinBot").map { JobBot(it, "1.0.0", "sha256:unchecked", sampleBot(it).toString()) },
        )
        val buffer = ByteArrayOutputStream()

        val exit = Worker.run(BufferedReader(StringReader(MessageCodec.write(job) + "\n")), PrintStream(buffer, true, Charsets.UTF_8), RunnerEngine)

        val events = buffer.toString(Charsets.UTF_8).lines().filter { it.isNotEmpty() }.map { MessageCodec.readEvent(it) }
        assertEquals(ExitCode.FINISHED, exit, events.toString())
        assertEquals(listOf(WorkerEvent.Started::class, WorkerEvent.Result::class, WorkerEvent.Finished::class), events.map { it::class })
        val result = events[1] as WorkerEvent.Result
        assertEquals(setOf("Walls", "Spin Bot"), result.bots.map { it.name }.toSet())
        assertEquals(listOf(1, 2), result.bots.map { it.rank })
        assertTrue(result.bots.all { it.totalScore >= 0 })
    }
}
