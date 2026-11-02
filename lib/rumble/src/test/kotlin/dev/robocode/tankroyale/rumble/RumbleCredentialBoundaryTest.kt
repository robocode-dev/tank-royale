package dev.robocode.tankroyale.rumble

import dev.robocode.tankroyale.rumble.protocol.Job
import dev.robocode.tankroyale.rumble.protocol.JobBot
import dev.robocode.tankroyale.rumble.protocol.MessageCodec
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertInstanceOf
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File
import java.nio.file.Files
import java.nio.file.Path
import java.time.Duration

/**
 * Checks, with real processes, that what the host hands a worker leaves a bot inside it no ordinary way to the saved
 * sign-in. These tests prove the ordinary ways are closed. They do not prove more: a process running as the same
 * operating system user can still open a file by its full path, and only a container or a separate account stops that.
 */
class RumbleCredentialBoundaryTest {

    private val java = File(System.getProperty("java.home"), "bin/java").absolutePath
    private val fakeToken = "ghr_FAKESAVEDREFRESHTOKEN0123456789"

    private fun job() = Job(
        jobId = "job-1", behaviorVersion = 2, gameType = "1v1", numberOfRounds = 1,
        bots = listOf(JobBot("Walls", "1.0.0", "sha256:aa", "/work/Walls"), JobBot("SpinBot", "1.0.0", "sha256:bb", "/work/SpinBot")),
    )

    /** Starts the fake worker with a command that carries no Java home or temporary folder setting of its own. */
    private fun plainCommand(mode: String) = WorkerCommand {
        listOf(java, "-cp", System.getProperty("java.class.path"), "dev.robocode.tankroyale.rumble.FakeWorkerKt", mode)
    }

    private fun fields(message: String): Map<String, String> =
        message.split("\u0001").filter { it.contains('=') }.associate { it.substringBefore('=') to it.substringAfter('=') }

    // RBW-016

    @Test
    @Tag("RBW-016")
    @Tag("Unit")
    @Tag("Positive")
    fun testRBW016_UnitPositive_aWorkerAndAJavaProcessItStartsSeeTheWorkersOwnEmptyFolders() {
        val host = mapOf("PATH" to (System.getenv("PATH") ?: ""), "HOME" to "C:/fake-participant-home")

        val outcome = WorkerHost(WorkerLimits(totalTime = Duration.ofSeconds(60)), { host }).run(plainCommand("envchild"), job()) as WorkerOutcome.Failed

        val report = outcome.message.split("\u0002")
        val childHome = report[1].removePrefix("user.home=")
        assertTrue(childHome.contains("rumble-worker"), "A Java process the worker started saw the home folder $childHome")
        assertFalse(childHome.equals(System.getProperty("user.home"), ignoreCase = true), childHome)
    }

    @Test
    @Tag("RBW-016")
    @Tag("Unit")
    @Tag("Negative")
    fun testRBW016_UnitNegative_theWorkerItselfSeesItsOwnFoldersEvenWithoutTheJavaSettingsOnItsCommand() {
        val outcome = WorkerHost(WorkerLimits(totalTime = Duration.ofSeconds(60)), { mapOf("PATH" to (System.getenv("PATH") ?: "")) })
            .run(plainCommand("env"), job()) as WorkerOutcome.Failed

        val home = outcome.message.split("\u0002")[1].removePrefix("user.home=")
        assertTrue(home.contains("rumble-worker"), home)
        assertFalse(home.equals(System.getProperty("user.home"), ignoreCase = true), home)
    }

    // RBW-017

    @Test
    @Tag("RBW-017")
    @Tag("Unit")
    @Tag("Positive")
    fun testRBW017_UnitPositive_everyOrdinaryWayOfFindingTheSavedSignInLandsInTheWorkersOwnFolders(@TempDir dir: Path) {
        val standIn = dir.resolve("host-data")
        val savedFor = SignInIdentity("alice", dir.resolve("host-home").toString(), "Windows 11")
        listOf("Robocode Tank Royale", "robocode-tank-royale").forEach { app ->
            assertEquals(SaveResult.Saved, SignInStore(RumbleEnvironments.TEST, standIn.resolve(app), savedFor).save(fakeToken, null))
        }
        assertInstanceOf(
            LoadResult.Found::class.java,
            SignInStore(RumbleEnvironments.TEST, standIn.resolve("Robocode Tank Royale"), savedFor).load(),
            "the stand-in saved sign-in must be real, or this check proves nothing",
        )
        val host = mapOf(
            "PATH" to (System.getenv("PATH") ?: ""),
            "HOME" to dir.resolve("host-home").toString(), "USERPROFILE" to dir.resolve("host-home").toString(),
            "LOCALAPPDATA" to standIn.toString(), "APPDATA" to standIn.toString(), "XDG_CONFIG_HOME" to standIn.toString(),
        )

        val outcome = WorkerHost(WorkerLimits(totalTime = Duration.ofSeconds(60)), { host }).run(plainCommand("discover"), job()) as WorkerOutcome.Failed

        val seen = fields(outcome.message)
        listOf("user.home", "tmpdir", "dataDir", "env.HOME", "env.USERPROFILE").forEach { key ->
            assertTrue(seen.getValue(key).contains("rumble-worker"), "$key was ${seen[key]}")
        }
        listOf("env.LOCALAPPDATA", "env.APPDATA", "env.XDG_CONFIG_HOME").forEach { assertEquals("null", seen[it], it) }
        assertEquals("Missing", seen["load"], "The worker must not find a saved sign-in")
        assertFalse(outcome.message.contains(fakeToken))
        assertFalse(outcome.message.contains(standIn.toString()) || outcome.message.contains(dir.resolve("host-home").toString()))
    }

    // RBW-018

    @Test
    @Tag("RBW-018")
    @Tag("Unit")
    @Tag("Negative")
    fun testRBW018_UnitNegative_aCopyOfTheSavedSignInCannotBeOpenedFromAnotherHomeFolder(@TempDir dir: Path) {
        val made = SignInIdentity("alice", dir.resolve("host-home").toString(), "Windows 11")
        val workers = SignInIdentity("alice", dir.resolve("worker-home").toString(), "Windows 11")
        assertEquals(SaveResult.Saved, SignInStore(RumbleEnvironments.TEST, dir, made).save(fakeToken, null))

        val fromWorker = SignInStore(RumbleEnvironments.TEST, dir, workers).load()

        assertInstanceOf(LoadResult.Unusable::class.java, fromWorker)
        assertFalse(fromWorker.toString().contains(fakeToken))
        assertInstanceOf(LoadResult.Found::class.java, SignInStore(RumbleEnvironments.TEST, dir, made).load())
    }

    // RBW-019

    @Test
    @Tag("RBW-019")
    @Tag("Unit")
    @Tag("Positive")
    fun testRBW019_UnitPositive_theWorkersCommandLineHoldsNoSecretAndItsStandardInputIsExactlyTheJob() {
        val secrets = mapOf(
            "GITHUB_TOKEN" to "ghu_FAKEHOSTSECRET123", "ROBOCODE_RUMBLE_TOKEN" to "ghr_FAKEREFRESH456",
            "AWS_SECRET_ACCESS_KEY" to "fake-aws-secret", "PATH" to (System.getenv("PATH") ?: ""),
        )
        val job = job()

        val outcome = WorkerHost(WorkerLimits(totalTime = Duration.ofSeconds(60)), { secrets }).run(plainCommand("selfreport"), job) as WorkerOutcome.Failed

        val report = outcome.message.split("\u0001")
        val commandLine = report[0].removePrefix("commandLine=")
        val stdin = report[1].removePrefix("stdin=")
        assertTrue(commandLine.contains("FakeWorkerKt"), "the command line must be readable, or this check proves nothing: $commandLine")
        secrets.filterKeys { it != "PATH" }.values.forEach { secret ->
            assertFalse(commandLine.contains(secret), "The command line holds a secret")
            assertFalse(stdin.contains(secret), "Standard input holds a secret")
        }
        assertEquals(MessageCodec.write(job) + "\n", stdin)
        assertNotNull(MessageCodec.readJob(stdin.trim()))
    }
}
