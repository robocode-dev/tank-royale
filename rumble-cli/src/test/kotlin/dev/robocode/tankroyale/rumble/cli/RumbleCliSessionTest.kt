package dev.robocode.tankroyale.rumble.cli

import dev.robocode.tankroyale.rumble.RumbleEnvironments
import dev.robocode.tankroyale.rumble.SessionBattle
import dev.robocode.tankroyale.rumble.SessionControlRequest
import dev.robocode.tankroyale.rumble.SessionEnded
import dev.robocode.tankroyale.rumble.SessionFolder
import dev.robocode.tankroyale.rumble.SessionOwner
import dev.robocode.tankroyale.rumble.SessionStatus
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.ByteArrayOutputStream
import java.io.PrintStream
import java.nio.file.Files
import java.nio.file.Path

class RumbleCliSessionTest {

    private val env = RumbleEnvironments.TEST

    private class Run(val exit: Int, val out: String) {
        val json: JsonObject get() = Json.parseToJsonElement(out).jsonObject
        val outcome: String get() = json["outcome"]!!.jsonPrimitive.content
    }

    private fun run(state: Path, vararg args: String, json: Boolean = true): Run {
        val out = ByteArrayOutputStream()
        val io = Io(PrintStream(out, true, Charsets.UTF_8), PrintStream(ByteArrayOutputStream(), true, Charsets.UTF_8), { state })
        val all = arrayOf("session", *args, "-e", "test") + if (json) arrayOf("--json") else emptyArray()
        return Run(RumbleCli.run(all, io, Services()), out.toString(Charsets.UTF_8))
    }

    private fun status(ended: SessionEnded? = null) = SessionStatus(
        environment = "test", pid = 4242, startedAt = "2026-11-18T10:00:00Z", owner = SessionOwner(77, "2026-11-18T09:00:00Z"),
        state = "running", formats = listOf("1v1"), battleLimit = 5, battle = SessionBattle("b1", "1v1", listOf("Alpha 1.0", "Beta 1.0")),
        kept = 2, updatedAt = "2026-11-18T10:05:00Z", ended = ended,
    )

    // RCI-058

    @Test
    @Tag("RCI-058")
    @Tag("Unit")
    @Tag("Positive")
    fun testRCI058_UnitPositive_sessionShowsAndControlsARunningSessionAndReportsOneThatEnded(@TempDir dir: Path) {
        val folder = SessionFolder.of(env, dir)
        val running = folder.claim()!!
        running.write(status())

        val shown = run(dir, "status")
        assertEquals(ExitCodes.DONE, shown.exit, shown.out)
        assertEquals("running", shown.outcome)
        val session = shown.json["session"]!!.jsonObject
        assertEquals(4242, session["pid"]!!.jsonPrimitive.content.toInt())
        assertEquals("b1", session["battle"]!!.jsonObject["battleId"]!!.jsonPrimitive.content)
        val text = run(dir, "status", json = false).out
        assertTrue(text.contains("A contribution session runs (process 4242"), text)
        assertTrue(text.contains("Battle 1v1: Alpha 1.0 vs Beta 1.0"), text)
        assertTrue(text.contains("process 77 ends"), text)

        val commands = listOf(
            "pause" to SessionControlRequest.PAUSE, "resume" to SessionControlRequest.RESUME, "stop" to SessionControlRequest.STOP,
            "stop-now" to SessionControlRequest.STOP_NOW, "release" to SessionControlRequest.RELEASE,
        )
        for ((command, request) in commands) {
            val asked = run(dir, command)
            assertEquals(ExitCodes.DONE, asked.exit, asked.out)
            assertEquals("asked", asked.outcome)
            assertEquals(listOf(request), running.take(), command)
        }

        // The summary of a session that ended, and one whose process ended without ending it.
        running.write(status(SessionEnded("finished", "2026-11-18T11:00:00Z", "The session ran its 5 battles.")))
        running.close()
        val ended = run(dir, "status")
        assertEquals("ended", ended.outcome)
        assertEquals("finished", ended.json["session"]!!.jsonObject["ended"]!!.jsonObject["outcome"]!!.jsonPrimitive.content)
        assertTrue(run(dir, "status", json = false).out.contains("The last contribution session ended at 2026-11-18T11:00:00Z (finished): The session ran its 5 battles."))

        folder.claim()!!.use { it.write(status()) }
        val crashed = run(dir, "status")
        assertEquals(ExitCodes.DONE, crashed.exit)
        assertEquals("ended-unexpectedly", crashed.outcome)
        assertTrue(run(dir, "status", json = false).out.contains("ended without ending the session"))
    }

    @Test
    @Tag("RCI-058")
    @Tag("Unit")
    @Tag("Negative")
    fun testRCI058_UnitNegative_controlCommandsAreRefusedWhenNoSessionRuns(@TempDir dir: Path) {
        val none = run(dir, "status")
        assertEquals(ExitCodes.DONE, none.exit)
        assertEquals("no-session", none.outcome)

        for (command in listOf("pause", "resume", "stop", "stop-now", "release")) {
            val refused = run(dir, command)
            assertEquals(ExitCodes.REFUSED, refused.exit, command)
            assertEquals("no-session", refused.outcome)
        }
        // A session that ended is not running either, and no request is left for the next one.
        val folder = SessionFolder.of(env, dir)
        folder.claim()!!.use { it.write(status(SessionEnded("finished", "2026-11-18T11:00:00Z"))) }
        assertEquals("no-session", run(dir, "stop").outcome)
        val requests = folder.folder.resolve("requests")
        assertFalse(Files.list(requests).use { it.findAny().isPresent }, "no request was written")
    }

    // RCL-171

    @Test
    @Tag("RCL-171")
    @Tag("Unit")
    @Tag("Positive")
    fun testRCL171_UnitPositive_sendAgainLiftsTheStopWhenNoSessionRuns(@TempDir dir: Path) {
        val sent = env.stateRoot(dir).resolve("ranked").resolve("sent-batches.jsonl")
        // The list of sent batches as a session leaves it after the Rumble refused a batch.
        Files.createDirectories(sent.parent)
        Files.writeString(sent, """{"kind":"refused","reason":"refused","version":"1.4.0"}""" + "\n")

        val lifted = run(dir, "send-again")
        assertEquals(ExitCodes.DONE, lifted.exit, lifted.out)
        assertEquals("lifted", lifted.outcome)
        assertEquals(null, dev.robocode.tankroyale.rumble.SentBatches.read(sent).refusal)
        assertEquals("not-stopped", run(dir, "send-again").outcome)
    }

    @Test
    @Tag("RCL-171")
    @Tag("Unit")
    @Tag("Negative")
    fun testRCL171_UnitNegative_sendAgainIsRefusedWhileASessionRuns(@TempDir dir: Path) {
        SessionFolder.of(env, dir).claim()!!.use {
            val asked = run(dir, "send-again")
            assertEquals(ExitCodes.REFUSED, asked.exit, asked.out)
            assertEquals("session-running", asked.outcome)
        }
    }
}
