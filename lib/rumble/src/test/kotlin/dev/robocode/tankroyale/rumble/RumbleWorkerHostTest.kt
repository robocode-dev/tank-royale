package dev.robocode.tankroyale.rumble

import dev.robocode.tankroyale.rumble.protocol.ErrorCode
import dev.robocode.tankroyale.rumble.protocol.Job
import dev.robocode.tankroyale.rumble.protocol.JobBot
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertInstanceOf
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File
import java.nio.file.Files
import java.nio.file.Path
import java.time.Duration

class RumbleWorkerHostTest {

    private val java = File(System.getProperty("java.home"), "bin/java").absolutePath

    private fun job(id: String = "job-1") = Job(
        jobId = id, behaviorVersion = 2, gameType = "1v1", numberOfRounds = 1,
        bots = listOf(JobBot("Walls", "1.0.0", "sha256:aa", "/work/Walls"), JobBot("SpinBot", "1.0.0", "sha256:bb", "/work/SpinBot")),
    )

    /** Starts the fake worker as a real process in the given mode. */
    private fun fake(mode: String) = WorkerCommand {
        listOf(java, "-cp", System.getProperty("java.class.path"), "dev.robocode.tankroyale.rumble.FakeWorkerKt", mode)
    }

    private val secretEnvironment = mapOf(
        "PATH" to (System.getenv("PATH") ?: ""),
        "GITHUB_TOKEN" to "ghu_FAKEHOSTSECRET123",
        "ROBOCODE_RUMBLE_TOKEN" to "ghr_FAKEREFRESH456",
        "HOME" to "C:/fake-participant-home",
        "USERPROFILE" to "C:/fake-participant-home",
        "DOCKER_HOST" to "unix:///var/run/docker.sock",
        "AWS_SECRET_ACCESS_KEY" to "fake-aws-secret",
    )

    private fun host(limits: WorkerLimits = WorkerLimits(totalTime = Duration.ofSeconds(60)), environment: Map<String, String> = secretEnvironment) =
        WorkerHost(limits, { environment })

    private fun parseEnvironment(report: String): Triple<Map<String, String>, String, String> {
        val parts = report.split("\u0002")
        val variables = parts[0].split("\u0001").filter { it.contains('=') }.associate { it.substringBefore('=') to it.substringAfter('=') }
        return Triple(variables, parts[1].removePrefix("user.home="), parts[2].removePrefix("cwd="))
    }

    // RBW-008

    @Test
    @Tag("RBW-008")
    @Tag("Unit")
    @Tag("Positive")
    fun testRBW008_UnitPositive_onlyAllowlistedVariablesArePassedAndTheFoldersAreTheWorkersOwn(@TempDir dir: Path) {
        val folders = WorkerFolders(dir.resolve("work"), dir.resolve("temp"), dir.resolve("home"))

        val built = WorkerEnvironment.build(secretEnvironment + ("LANG" to "da_DK.UTF-8"), folders)

        assertEquals(secretEnvironment.getValue("PATH"), built["PATH"])
        assertEquals("da_DK.UTF-8", built["LANG"])
        listOf("HOME", "USERPROFILE").forEach { assertEquals(folders.home.toString(), built[it]) }
        listOf("TMP", "TEMP", "TMPDIR").forEach { assertEquals(folders.temp.toString(), built[it]) }
        assertTrue(built.keys.all { it in WorkerEnvironment.ALLOWED || it in setOf("HOME", "USERPROFILE", "TMP", "TEMP", "TMPDIR", "JAVA_TOOL_OPTIONS") }, built.keys.toString())
    }

    @Test
    @Tag("RBW-008")
    @Tag("Unit")
    @Tag("Negative")
    fun testRBW008_UnitNegative_noVariableOutsideTheAllowlistIsPassedWhateverItIsCalled(@TempDir dir: Path) {
        val folders = WorkerFolders(dir.resolve("work"), dir.resolve("temp"), dir.resolve("home"))
        val host = mapOf(
            "GITHUB_TOKEN" to "x", "GH_TOKEN" to "x", "github_pat" to "x", "MY_PASSWORD" to "x", "SECRET" to "x",
            "DOCKER_HOST" to "x", "CONTAINER_HOST" to "x", "SSH_AUTH_SOCK" to "x", "INNOCENT_LOOKING" to "x", "EDITOR" to "x",
        )

        val built = WorkerEnvironment.build(host, folders)

        assertEquals(setOf("HOME", "USERPROFILE", "TMP", "TEMP", "TMPDIR", "JAVA_TOOL_OPTIONS"), built.keys)
        assertFalse(built.values.any { it == "x" })
    }

    // RBW-009

    @Test
    @Tag("RBW-009")
    @Tag("Unit")
    @Tag("Positive")
    fun testRBW009_UnitPositive_aRealWorkerProcessSeesNoSecretAndHasItsOwnFolders() {
        val outcome = host().run(fake("env"), job()) as WorkerOutcome.Failed

        val (variables, home, cwd) = parseEnvironment(outcome.message)
        assertFalse(outcome.message.contains("ghu_FAKEHOSTSECRET123") || outcome.message.contains("ghr_FAKEREFRESH456"))
        assertFalse(outcome.message.contains("fake-participant-home") || outcome.message.contains("fake-aws-secret") || outcome.message.contains("docker.sock"))
        listOf("GITHUB_TOKEN", "ROBOCODE_RUMBLE_TOKEN", "DOCKER_HOST", "AWS_SECRET_ACCESS_KEY").forEach { assertNull(variables[it], it) }
        assertTrue(variables.getValue("HOME").contains("rumble-worker"), variables["HOME"])
        assertTrue(cwd.contains("rumble-worker"), cwd)
        assertFalse(home.contains("fake-participant-home"))
    }

    @Test
    @Tag("RBW-009")
    @Tag("Unit")
    @Tag("Negative")
    fun testRBW009_UnitNegative_aProcessTheWorkerStartsSeesTheSameCleanEnvironment() {
        val outcome = host().run(fake("envchild"), job()) as WorkerOutcome.Failed

        val (variables, _, _) = parseEnvironment(outcome.message)
        assertFalse(outcome.message.contains("ghu_FAKEHOSTSECRET123") || outcome.message.contains("fake-aws-secret"))
        assertNull(variables["GITHUB_TOKEN"])
        assertNull(variables["DOCKER_HOST"])
        assertTrue(variables.getValue("HOME").contains("rumble-worker"))
    }

    // RBW-010

    @Test
    @Tag("RBW-010")
    @Tag("Unit")
    @Tag("Positive")
    fun testRBW010_UnitPositive_aFinishedRefusedOrFailedWorkerGivesItsOwnOutcome() {
        val completed = host().run(fake("ok"), job()) as WorkerOutcome.Completed
        assertEquals(listOf("Walls", "SpinBot"), completed.result.bots.map { it.name })
        assertEquals(listOf(1, 2), completed.result.bots.map { it.rank })

        assertEquals(WorkerOutcome.Refused(ErrorCode.PIN_MISMATCH, "wrong pin"), host().run(fake("refuse"), job()))
        assertEquals(WorkerOutcome.Failed(ErrorCode.BATTLE_FAILED, "no bots"), host().run(fake("fail"), job()))
    }

    @Test
    @Tag("RBW-010")
    @Tag("Unit")
    @Tag("Negative")
    fun testRBW010_UnitNegative_anIncompleteInconsistentUnreadableOrUnstartableWorkerIsNeverACompletedBattle() {
        assertEquals(WorkerOutcome.EndedWithoutFinish(0), host().run(fake("nofinal"), job()))
        assertEquals(WorkerOutcome.EndedWithoutFinish(1), host().run(fake("badexit"), job()))
        assertInstanceOf(WorkerOutcome.UnreadableOutput::class.java, host().run(fake("garbage"), job()))

        val missing = host().run(WorkerCommand { listOf("definitely-not-a-program-rumble") }, job())
        assertInstanceOf(WorkerOutcome.CouldNotStart::class.java, missing)
        assertTrue((missing as WorkerOutcome.CouldNotStart).message.isNotBlank())
    }

    // RBW-011

    @Test
    @Tag("RBW-011")
    @Tag("Unit")
    @Tag("Negative")
    fun testRBW011_UnitNegative_tooMuchOutputOrTooLongALineStopsTheWorkerWithoutAResult() {
        val big = host(WorkerLimits(Duration.ofSeconds(60), maxOutputBytes = 50_000)).run(fake("bigoutput"), job())
        assertInstanceOf(WorkerOutcome.LimitExceeded::class.java, big)

        val long = host(WorkerLimits(Duration.ofSeconds(60), maxLineBytes = 10_000)).run(fake("longline"), job())
        assertInstanceOf(WorkerOutcome.LimitExceeded::class.java, long)
    }

    @Test
    @Tag("RBW-011")
    @Tag("Unit")
    @Tag("Positive")
    fun testRBW011_UnitPositive_aWorkerThatFillsItsDiagnosticOutputStillFinishes() {
        val outcome = host().run(fake("noisystderr"), job())

        assertInstanceOf(WorkerOutcome.Completed::class.java, outcome)
    }

    // RBW-012

    private fun aliveProcess(pid: Long): Boolean = ProcessHandle.of(pid).map { it.isAlive }.orElse(false)

    private fun waitUntilGone(pid: Long) {
        val end = System.nanoTime() + Duration.ofSeconds(10).toNanos()
        while (aliveProcess(pid) && System.nanoTime() < end) Thread.sleep(50)
    }

    @Test
    @Tag("RBW-012")
    @Tag("Unit")
    @Tag("Negative")
    fun testRBW012_UnitNegative_theTimeLimitStopsTheWorkerAndEverythingItStarted(@TempDir dir: Path) {
        val pidFile = dir.resolve("child.pid")

        val outcome = host(WorkerLimits(totalTime = Duration.ofSeconds(6))).run(fake("child"), job(pidFile.toString()))

        assertInstanceOf(WorkerOutcome.TimedOut::class.java, outcome)
        val childPid = Files.readString(pidFile).trim().toLong()
        waitUntilGone(childPid)
        assertFalse(aliveProcess(childPid), "The child $childPid is still running")
    }

    @Test
    @Tag("RBW-012")
    @Tag("Unit")
    @Tag("Positive")
    fun testRBW012_UnitPositive_aWorkerThatFinishesInTimeIsNotStoppedButWhatItLeftBehindIs(@TempDir dir: Path) {
        val pidFile = dir.resolve("leftover.pid")

        val outcome = host().run(fake("childthenfinish"), job(pidFile.toString()))

        assertInstanceOf(WorkerOutcome.Completed::class.java, outcome)
        val childPid = Files.readString(pidFile).trim().toLong()
        waitUntilGone(childPid)
        assertFalse(aliveProcess(childPid), "The leftover child $childPid is still running")
    }

    // RBW-013

    @Test
    @Tag("RBW-013")
    @Tag("Unit")
    @Tag("Positive")
    fun testRBW013_UnitPositive_theUsualCommandHasTheHeapLimitTheFolderSettingsAndTheWorkerArgument(@TempDir dir: Path) {
        val folders = WorkerFolders(dir.resolve("work"), dir.resolve("temp"), dir.resolve("home"))
        val jar = dir.resolve("rumble-worker.jar")

        val command = WorkerCommands.javaJar(jar, heapMegabytes = 512, javaProgram = "java").build(folders)

        assertEquals("java", command.first())
        assertTrue(command.contains("-Xmx512m"))
        assertTrue(command.contains("-Duser.home=${folders.home}"))
        assertTrue(command.contains("-Djava.io.tmpdir=${folders.temp}"))
        assertEquals(listOf("-jar", jar.toAbsolutePath().toString(), "worker"), command.takeLast(3))
    }

    @Test
    @Tag("RBW-013")
    @Tag("Unit")
    @Tag("Negative")
    fun testRBW013_UnitNegative_anyOtherCommandIsUsedUnchanged() {
        var seen: WorkerFolders? = null
        val own = WorkerCommand { folders -> seen = folders; listOf(java, "-cp", System.getProperty("java.class.path"), "dev.robocode.tankroyale.rumble.FakeWorkerKt", "ok") }

        val outcome = host().run(own, job())

        assertInstanceOf(WorkerOutcome.Completed::class.java, outcome)
        assertTrue(seen != null)
        assertFalse(Files.exists(seen!!.work), "The run folders are removed afterwards")
    }
}
