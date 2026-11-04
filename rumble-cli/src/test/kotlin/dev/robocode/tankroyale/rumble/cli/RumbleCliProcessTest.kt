package dev.robocode.tankroyale.rumble.cli

import dev.robocode.tankroyale.common.rules.CURRENT_BEHAVIOR_VERSION
import dev.robocode.tankroyale.rumble.WorkerCommands
import dev.robocode.tankroyale.rumble.WorkerHost
import dev.robocode.tankroyale.rumble.WorkerLimits
import dev.robocode.tankroyale.rumble.WorkerOutcome
import dev.robocode.tankroyale.rumble.protocol.Job
import dev.robocode.tankroyale.rumble.protocol.JobBot
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.boolean
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertInstanceOf
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File
import java.nio.file.Files
import java.nio.file.Path
import java.security.MessageDigest
import java.time.Duration
import java.util.concurrent.TimeUnit
import java.util.jar.JarFile

/**
 * Runs the command line and the battle worker as real programs, from their jars.
 *
 * Run via `./gradlew :rumble-cli:integrationTest` — excluded from the default `test` task.
 */
@Tag("integration")
class RumbleCliProcessTest {

    private val java = File(System.getProperty("java.home"), "bin/java").absolutePath
    private val cliJar = Path.of(System.getProperty("rumble.cli.jar") ?: error("System property 'rumble.cli.jar' not set — run via :rumble-cli:integrationTest"))
    private val workerJar = Path.of(System.getProperty("rumble.worker.jar") ?: error("System property 'rumble.worker.jar' not set — run via :rumble-cli:integrationTest"))

    private class Finished(val exit: Int, val out: String, val err: String)

    /** Runs the command line jar with nothing else on the classpath and standard input closed at once. */
    private fun runCli(vararg args: String): Finished {
        val process = ProcessBuilder(listOf(java, "-jar", cliJar.toString(), *args)).start()
        process.outputStream.close()
        val out = process.inputStream.bufferedReader().readText()
        val err = process.errorStream.bufferedReader().readText()
        assertTrue(process.waitFor(60, TimeUnit.SECONDS), "The command did not finish by itself; it may be waiting for input")
        return Finished(process.exitValue(), out, err)
    }

    private fun sha256(bytes: ByteArray) = MessageDigest.getInstance("SHA-256").digest(bytes).joinToString("") { "%02x".format(it) }

    private fun entries(jar: Path): List<String> = JarFile(jar.toFile()).use { it.entries().asSequence().map { e -> e.name }.toList() }

    // RCI-003 and RCI-010

    @Test
    @Tag("RCI-010")
    @Tag("Integration")
    @Tag("Positive")
    fun testRCI010_IntegrationPositive_theJarRunsWithNothingElseOnTheClasspathAndAnswersInJson() {
        val version = runCli("version", "--json")

        assertEquals(0, version.exit, version.err)
        val answer = Json.parseToJsonElement(version.out.trim()).jsonObject
        assertEquals("rumble", answer["program"]!!.jsonPrimitive.content)
        assertTrue(answer["worker"]!!.jsonObject["embedded"]!!.jsonPrimitive.boolean)
    }

    @Test
    @Tag("RCI-010")
    @Tag("Integration")
    @Tag("Negative")
    fun testRCI010_IntegrationNegative_anUnknownCommandGivesExitCodeTwo() {
        assertEquals(2, runCli("nonsense").exit)
        assertEquals(2, runCli("status", "--environment", "nowhere").exit)
    }

    @Test
    @Tag("RCI-003")
    @Tag("Integration")
    @Tag("Positive")
    fun testRCI003_IntegrationPositive_noCommandWaitsForInputWhenStandardInputIsClosed(@TempDir dir: Path) {
        listOf(listOf("version"), listOf("environments"), listOf("status"), listOf("status", "--environment", "test", "--json"), listOf("import-legacy", dir.resolve("none.json").toString())).forEach { command ->
            val result = runCli(*command.toTypedArray(), "--state-folder", dir.resolve("state").toString())
            assertTrue(result.exit == 0 || result.exit == 2, "$command ended with ${result.exit}")
        }
    }

    // RCI-011

    @Test
    @Tag("RCI-011")
    @Tag("Integration")
    @Tag("Positive")
    fun testRCI011_IntegrationPositive_theWorkerJarIsRunnableAndHoldsNoControllerCode() {
        val names = entries(workerJar)
        val forbidden = listOf(
            "dev/robocode/tankroyale/botproject/", "dev/robocode/tankroyale/gui/", "dev/robocode/tankroyale/rumble/Registration",
            "dev/robocode/tankroyale/rumble/GitHub", "dev/robocode/tankroyale/rumble/SignIn", "dev/robocode/tankroyale/rumble/SavedSignIn",
            "dev/robocode/tankroyale/rumble/BotSubmission", "dev/robocode/tankroyale/rumble/WorkerHost", "dev/robocode/tankroyale/rumble/cli/",
            "org/kohsuke/github/", "org/eclipse/jgit/",
        )

        assertTrue(names.contains("dev/robocode/tankroyale/rumble/worker/MainKt.class"))
        assertTrue(names.contains("robocode-tankroyale-server.jar") && names.contains("robocode-tankroyale-booter.jar"), "The runner's embedded server and booter must come along")
        assertEquals(emptyList<String>(), names.filter { name -> forbidden.any { name.startsWith(it) } })
        JarFile(workerJar.toFile()).use { assertEquals("dev.robocode.tankroyale.rumble.worker.MainKt", it.manifest.mainAttributes.getValue("Main-Class")) }
    }

    @Test
    @Tag("RCI-011")
    @Tag("Integration")
    @Tag("Negative")
    fun testRCI011_IntegrationNegative_theCommandLineCarriesTheWorkerAsOneResourceAndNoneOfItsClasses() {
        val names = entries(cliJar)

        assertTrue(names.contains("robocode-tankroyale-rumble-worker.jar"))
        assertEquals(emptyList<String>(), names.filter { it.startsWith("dev/robocode/tankroyale/rumble/worker/") || it.startsWith("dev/robocode/tankroyale/runner/") })
        val carried = JarFile(cliJar.toFile()).use { jar -> jar.getInputStream(jar.getEntry("robocode-tankroyale-rumble-worker.jar")).readBytes() }
        assertEquals(sha256(Files.readAllBytes(workerJar)), sha256(carried), "The carried worker must be byte for byte the built one")
    }

    @Test
    @Tag("RCI-011")
    @Tag("Integration")
    @Tag("Positive")
    fun testRCI011_IntegrationPositive_theCarriedWorkerIsExtractedAndRunsARealBattleThroughTheHost(@TempDir dir: Path) {
        val extracted = EmbeddedWorker.extract(dir)
        val bots = System.getProperty("sampleBots.java.dir") ?: error("System property 'sampleBots.java.dir' not set")
        val job = Job(
            jobId = "cli-1", behaviorVersion = CURRENT_BEHAVIOR_VERSION, gameType = "1v1", numberOfRounds = 1,
            bots = listOf("Walls", "SpinBot").map { JobBot(it, "1.0.0", "sha256:unchecked", Path.of(bots).resolve(it).toString()) },
        )

        val outcome = WorkerHost(WorkerLimits(totalTime = Duration.ofMinutes(3))).run(WorkerCommands.javaJar(extracted, 512), job)

        assertInstanceOf(WorkerOutcome.Completed::class.java, outcome, outcome.toString())
        assertEquals(listOf(1, 2), (outcome as WorkerOutcome.Completed).result.bots.map { it.rank })
        assertEquals(extracted, EmbeddedWorker.extract(dir), "A second extraction reuses the file")
        assertFalse(Files.list(dir.resolve("worker")).use { s -> s.anyMatch { it.fileName.toString().endsWith(".part") } })
    }

    // RCI-031

    @Test
    @Tag("RCI-031")
    @Tag("Integration")
    @Tag("Positive")
    fun testRCI031_IntegrationPositive_aCommandThatNeedsASignInEndsWithExitThreeAndOneDocument(@TempDir dir: Path) {
        val answer = runCli("register", "-e", "test", "--json", "--state-folder", dir.toString())

        assertEquals(3, answer.exit, answer.out + answer.err)
        assertEquals("no-sign-in", Json.parseToJsonElement(answer.out.trim()).jsonObject["outcome"]!!.jsonPrimitive.content)
        assertEquals("", answer.err)
    }

    @Test
    @Tag("RCI-031")
    @Tag("Integration")
    @Tag("Negative")
    fun testRCI031_IntegrationNegative_noWarningFromJavaReachesStandardErrorInNormalUse() {
        listOf(listOf("--help"), listOf("version"), listOf("environments", "--json")).forEach { command ->
            val answer = runCli(*command.toTypedArray())
            assertEquals(0, answer.exit, command.toString())
            assertEquals("", answer.err, command.toString())
        }
        val bad = runCli("status", "-e", "nowhere")
        assertEquals(2, bad.exit)
        assertFalse(bad.err.contains("WARNING"), bad.err)
        assertEquals(1, bad.err.trim().lines().size, bad.err)
    }
}
