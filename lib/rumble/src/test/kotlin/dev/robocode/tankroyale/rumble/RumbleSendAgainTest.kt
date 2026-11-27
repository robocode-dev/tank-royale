package dev.robocode.tankroyale.rumble

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.nio.file.Path
import java.util.concurrent.Executor

/** Sending goes on after the Rumble refused a whole batch: by a new program version, or by the person's own choice. */
class RumbleSendAgainTest {

    @TempDir
    lateinit var dir: Path

    private val sameThread = Executor { it.run() }

    private fun sentFile(stateRoot: Path) = stateRoot.resolve("ranked").resolve("sent-batches.jsonl")

    // RCL-170

    @Test
    @Tag("RCL-170")
    @Tag("Unit")
    @Tag("Positive")
    fun testRCL170_UnitPositive_aRefusalRecordsItsVersionAndAnotherVersionLiftsIt() {
        val file = sentFile(dir)
        SentBatches.open(file).refuse("Rejected result batch before individual records could be read", "1.4.0")

        // The version is kept across a restart and in the screen's reading.
        val reopened = SentBatches.open(file)
        assertEquals("1.4.0", reopened.refusedVersion)
        assertEquals("1.4.0", SentBatches.read(file).refusedVersion)

        assertTrue(reopened.liftForNewVersion("1.5.0"))
        assertNull(reopened.refusal)
        assertNull(SentBatches.open(file).refusal, "the lift is kept across a restart")
        assertNull(SentBatches.read(file).refusedVersion)
    }

    @Test
    @Tag("RCL-170")
    @Tag("Unit")
    @Tag("Negative")
    fun testRCL170_UnitNegative_theSameVersionKeepsTheStopAndARefusalWithoutAVersionIsLifted() {
        val file = sentFile(dir)
        val sent = SentBatches.open(file).apply { refuse("refused", "1.5.0") }
        assertFalse(sent.liftForNewVersion("1.5.0"))
        assertEquals("refused", SentBatches.open(file).refusal)

        // A refusal an older program recorded without its version is lifted by any version.
        val older = sentFile(dir.resolve("older"))
        SentBatches.open(older).refuse("refused")
        assertTrue(SentBatches.open(older).liftForNewVersion("1.5.0"))

        // Nothing to lift when sending was not stopped.
        assertFalse(SentBatches.open(sentFile(dir.resolve("none"))).liftForNewVersion("1.5.0"))
    }

    // RCL-171

    @Test
    @Tag("RCL-171")
    @Tag("Unit")
    @Tag("Positive")
    fun testRCL171_UnitPositive_thePersonLiftsTheStopWhenNoSessionRunsAndTheScreenShowsItWent() {
        SentBatches.open(sentFile(dir)).refuse("refused", "1.4.0")

        assertEquals(SendAgainOutcome.LIFTED, SendAgain.lift(dir))
        assertNull(SentBatches.read(sentFile(dir)).refusal)
        assertEquals(SendAgainOutcome.NOT_STOPPED, SendAgain.lift(dir))

        // The screen asks through its port and then shows the new reading with how it went.
        SentBatches.open(sentFile(dir)).refuse("refused", "1.4.0")
        var shown: ResultsState? = null
        val controller = ResultsController(LibraryResultsPort(RumbleEnvironments.TEST, dir) { true }, sameThread, sameThread) { shown = it }
        controller.start()
        assertEquals("1.4.0", (shown as ResultsState.Shown).overview.refusedVersion)
        controller.sendAgain()
        val after = shown as ResultsState.Shown
        assertEquals(SendAgainOutcome.LIFTED, after.sentAgain)
        assertNull(after.overview.refusal)
    }

    @Test
    @Tag("RCL-171")
    @Tag("Unit")
    @Tag("Negative")
    fun testRCL171_UnitNegative_whileASessionRunsTheStopStaysAndThePersonIsToldToStopItFirst() {
        SentBatches.open(sentFile(dir)).refuse("refused", "1.4.0")
        val session = SessionFolder(dir.resolve(SessionFolder.FOLDER)).claim()!!
        session.use {
            assertEquals(SendAgainOutcome.SESSION_RUNNING, SendAgain.lift(dir))
            assertEquals("refused", SentBatches.read(sentFile(dir)).refusal)
        }
        // Once the session ended, it can be lifted.
        assertEquals(SendAgainOutcome.LIFTED, SendAgain.lift(dir))
    }
}
