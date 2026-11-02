package dev.robocode.tankroyale.rumble.worker

import dev.robocode.tankroyale.common.rules.CURRENT_BEHAVIOR_VERSION
import dev.robocode.tankroyale.rumble.WorkerCommand
import dev.robocode.tankroyale.rumble.WorkerHost
import dev.robocode.tankroyale.rumble.WorkerLimits
import dev.robocode.tankroyale.rumble.WorkerOutcome
import dev.robocode.tankroyale.rumble.protocol.Job
import dev.robocode.tankroyale.rumble.protocol.JobBot
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertInstanceOf
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import java.io.File
import java.nio.file.Files
import java.nio.file.Path
import java.time.Duration

/**
 * Runs the real worker through the host with sample bots, in a clean environment, with a fake token in the host's own
 * environment.
 *
 * Run via `./gradlew :rumble:integrationTest` — excluded from the default `test` task.
 */
@Tag("integration")
class WorkerHostIntegrationTest {

    private fun sampleBot(name: String): Path {
        val root = System.getProperty("sampleBots.java.dir") ?: error("System property 'sampleBots.java.dir' not set — run via :rumble:integrationTest")
        return Path.of(root).resolve(name).also { check(Files.exists(it)) { "Sample bot not found: $it" } }
    }

    private fun workerCommand() = WorkerCommand { folders ->
        listOf(
            File(System.getProperty("java.home"), "bin/java").absolutePath,
            "-Xmx512m",
            "-Duser.home=${folders.home}",
            "-Djava.io.tmpdir=${folders.temp}",
            "-cp", System.getProperty("rumble.worker.classpath") ?: error("System property 'rumble.worker.classpath' not set"),
            "dev.robocode.tankroyale.rumble.worker.MainKt",
            "worker",
        )
    }

    private fun descendants(): List<ProcessHandle> =
        ProcessHandle.current().descendants().collect(java.util.stream.Collectors.toList())

    // RBW-015

    @Test
    @Tag("RBW-015")
    @Tag("Integration")
    @Tag("Positive")
    fun testRBW015_IntegrationPositive_aRealBattleThroughTheHostGivesARankedResultInACleanEnvironment() {
        val before = descendants().filter { it.isAlive }.map { it.pid() }.toSet()
        val job = Job(
            jobId = "host-1", behaviorVersion = CURRENT_BEHAVIOR_VERSION, gameType = "1v1", numberOfRounds = 1,
            bots = listOf("Walls", "SpinBot").map { JobBot(it, "1.0.0", "sha256:unchecked", sampleBot(it).toString()) },
        )
        val dirty = System.getenv() + mapOf("GITHUB_TOKEN" to "ghu_FAKEINTEGRATIONSECRET", "DOCKER_HOST" to "unix:///var/run/docker.sock")

        val outcome = WorkerHost(WorkerLimits(totalTime = Duration.ofMinutes(3)), { dirty }).run(workerCommand(), job)

        assertInstanceOf(WorkerOutcome.Completed::class.java, outcome, outcome.toString())
        val result = (outcome as WorkerOutcome.Completed).result
        assertEquals(listOf(1, 2), result.bots.map { it.rank })
        assertEquals(setOf("Walls", "Spin Bot"), result.bots.map { it.name }.toSet())

        val end = System.nanoTime() + Duration.ofSeconds(15).toNanos()
        fun leftovers() = descendants().filter { it.pid() !in before && it.isAlive }.map { it.pid() }
        while (leftovers().isNotEmpty() && System.nanoTime() < end) Thread.sleep(100)
        assertTrue(leftovers().isEmpty(), "Left running: ${leftovers()}")
    }
}
