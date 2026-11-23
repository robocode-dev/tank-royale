package dev.robocode.tankroyale.rumble

import org.junit.jupiter.api.Assertions.assertArrayEquals
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertInstanceOf
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardOpenOption
import java.time.Instant
import java.util.concurrent.Executor

/** Results and queue without a window: the reading that never writes, the counts and batch rows, and the screen's behaviour. */
class RumbleResultsQueueTest {

    @TempDir
    lateinit var dir: Path

    private val env = RumbleEnvironments.TEST
    private val page = "https://github.com/robocode-dev/rumble-data-test"
    private val ranked get() = dir.resolve("ranked")
    private val journalFile get() = ranked.resolve("journal.jsonl")
    private val sentFile get() = ranked.resolve("sent-batches.jsonl")

    /** A journal and a list of sent batches as a session leaves them: batch b1 answered, b2 waiting for its receipt, two waiting results and one set aside. */
    private fun session() {
        var now = Instant.parse("2026-11-22T10:00:00Z")
        val journal = RankedJournal.open(env, journalFile) { now }
        listOf("a", "b", "c", "d", "e", "f").forEach { journal.add(JournalFixtures.record(it)) }
        journal.add(JournalFixtures.record("old", behaviorVersion = 6))
        val sent = SentBatches.open(sentFile)
        sent.begin("b1", listOf("a", "b"))
        journal.handOver("a"); journal.handOver("b")
        sent.sentAs("b1", 41)
        journal.accept("a")
        journal.reject("b", MoveReason.RUMBLE_REJECTED, listOf("duplicate battle"))
        sent.finish("b1")
        now = Instant.parse("2026-11-22T11:00:00Z")
        sent.begin("b2", listOf("c"))
        journal.handOver("c")
        sent.sentAs("b2", 42)
        journal.setAsideOtherEpochs(7)
    }

    private fun read(signedIn: Boolean = true) = LibraryResultsPort(env, dir) { signedIn }.read()

    @Test
    @Tag("RCL-151")
    @Tag("Unit")
    @Tag("Positive")
    fun testRCL151_UnitPositive_theJournalAndTheSentBatchesAreReadWithoutAByteChanging() {
        session()
        val before = Files.readAllBytes(journalFile) to Files.readAllBytes(sentFile)
        val results = RankedJournal.read(env, journalFile)
        assertEquals(listOf("a", "b", "c", "d", "e", "f", "old"), results.map { it.battleId })
        assertEquals(RecordState.REJECTED, results[1].state)
        assertEquals(listOf("duplicate battle"), results[1].detail)
        assertEquals(listOf("2026-11-22T10:00:00Z"), results[0].handedOver)
        assertEquals(listOf("b1", "b2"), SentBatches.read(sentFile).batches.map { it.batchId })
        assertInstanceOf(ResultsReading.Read::class.java, read())
        assertArrayEquals(before.first, Files.readAllBytes(journalFile), "the journal is not changed")
        assertArrayEquals(before.second, Files.readAllBytes(sentFile), "the list of sent batches is not changed")
    }

    @Test
    @Tag("RCL-151")
    @Tag("Unit")
    @Tag("Negative")
    fun testRCL151_UnitNegative_aTornLastLineIsLeftOutAndKeptAnotherRumblesJournalIsRefusedAndNoFilesMeanNothingKept() {
        assertEquals(ResultCounts(), (read() as ResultsReading.Read).overview.counts, "nothing is kept yet")
        assertFalse(Files.exists(journalFile), "reading makes no file")
        session()
        // A session is writing a line: its start is in the file, its newline is not yet.
        Files.write(journalFile, """{"kind":"move","battleId":"d","state":"HANDED""".toByteArray(), StandardOpenOption.APPEND)
        Files.write(sentFile, """{"kind":"batch","batchId":"b3","battle""".toByteArray(), StandardOpenOption.APPEND)
        val torn = Files.readAllBytes(journalFile) to Files.readAllBytes(sentFile)
        val overview = (read() as ResultsReading.Read).overview
        assertEquals(3, overview.counts.waiting, "the torn move of d is left out, so d still waits")
        assertEquals(listOf("b2", "b1"), overview.batches.map { it.batchId })
        assertArrayEquals(torn.first, Files.readAllBytes(journalFile), "a torn line is never cut off by the screen")
        assertArrayEquals(torn.second, Files.readAllBytes(sentFile))
        val other = LibraryResultsPort(RumbleEnvironments.PUBLIC, dir) { true }.read()
        assertInstanceOf(ResultsReading.Failed::class.java, other, "a journal of the test Rumble is not read as the public one's")
        Files.writeString(journalFile, "not a journal\n")
        assertInstanceOf(ResultsReading.Failed::class.java, read())
    }

    @Test
    @Tag("RCL-152")
    @Tag("Unit")
    @Tag("Positive")
    fun testRCL152_UnitPositive_theResultsAreCountedAndTheBatchesListedNewestFirstWithWhereEachStands() {
        session()
        val overview = (read() as ResultsReading.Read).overview
        assertEquals(ResultCounts(kept = 7, waiting = 3, sent = 1, accepted = 1, rejected = 1, setAside = 1), overview.counts)
        val (newest, oldest) = overview.batches
        assertEquals(BatchRow("b2", 1, "2026-11-22T11:00:00Z", BatchStanding.WaitingForReceipt, emptyList(), "$page/issues/42"), newest)
        assertEquals(BatchRow("b1", 2, "2026-11-22T10:00:00Z", BatchStanding.Answered(1, 1, 0), listOf("duplicate battle"), "$page/issues/41"), oldest)
    }

    @Test
    @Tag("RCL-152")
    @Tag("Unit")
    @Tag("Negative")
    fun testRCL152_UnitNegative_aBatchWithoutAnIssueHasNoPageAndAFailedReadingIsSaidAndALateOneThrownAway() {
        val sent = SentBatchesRead(listOf(SentBatches.Batch("b9", listOf("x"), null)), emptySet(), null)
        val row = ResultsQueue.overview(emptyList(), sent, page, signedIn = true).batches.single()
        assertNull(row.issuePage)
        assertEquals(BatchStanding.WaitingForReceipt, row.standing)

        // Results the Rumble refused in b1 are sent again in b2 and accepted there: b1 does not claim them, keeps its own
        // time, and the list stays newest first.
        val resent = listOf(
            KeptResult("p", RecordState.ACCEPTED, null, emptyList(), listOf("2026-11-22T10:00:00Z", "2026-11-22T12:00:00.5Z")),
            KeptResult("q", RecordState.ACCEPTED, null, emptyList(), listOf("2026-11-22T10:00:00Z", "2026-11-22T12:00:00.5Z")),
            KeptResult("r", RecordState.ACCEPTED, null, emptyList(), listOf("2026-11-22T11:00:00Z")),
        )
        val batches = SentBatchesRead(
            listOf(SentBatches.Batch("b1", listOf("p", "q"), 41), SentBatches.Batch("bx", listOf("r"), 43), SentBatches.Batch("b2", listOf("p", "q"), 44)),
            setOf("b1", "bx", "b2"), null,
        )
        val rows = ResultsQueue.overview(resent, batches, page, signedIn = true).batches
        assertEquals(listOf("b2", "bx", "b1"), rows.map { it.batchId })
        assertEquals(listOf("2026-11-22T12:00:00.5Z", "2026-11-22T11:00:00Z", "2026-11-22T10:00:00Z"), rows.map { it.sentAt })
        assertEquals(BatchStanding.Answered(2, 0, 0), rows[0].standing)
        assertEquals(BatchStanding.Answered(0, 0, 2), rows[2].standing, "the refused batch does not claim the results accepted later")

        // A journal of a schema this program does not know is refused, not read as if it were known.
        Files.createDirectories(ranked)
        Files.writeString(journalFile, """{"kind":"header","schemaVersion":2,"environment":"test"}""" + "\n")
        assertInstanceOf(ResultsReading.Failed::class.java, read())

        val now = Executor { it.run() }
        val shown = mutableListOf<ResultsState>()
        ResultsController({ ResultsReading.Failed("The journal could not be read.") }, now, now) { shown += it }.start()
        assertEquals(listOf<ResultsState>(ResultsState.Failed("The journal could not be read.")), shown)
        val later = mutableListOf<Runnable>()
        shown.clear()
        val controller = ResultsController({ throw IllegalStateException("boom") }, now, { later += it }) { shown += it }
        controller.start()
        controller.close()
        later.forEach { it.run() }
        assertTrue(shown.isEmpty(), "a reading after closing is thrown away")
    }

    @Test
    @Tag("RCL-153")
    @Tag("Unit")
    @Tag("Positive")
    fun testRCL153_UnitPositive_whatHoldsResultsBackIsKnown() {
        session()
        val signedOut = (read(signedIn = false) as ResultsReading.Read).overview
        assertTrue(ResultsQueue.waitsToBeSent(signedOut))
        assertTrue(ResultsQueue.needsSignIn(signedOut))
        assertFalse(ResultsQueue.needsSignIn((read(signedIn = true) as ResultsReading.Read).overview))
        assertEquals(1, signedOut.counts.setAside)
        val sent = SentBatches.open(sentFile)
        sent.refuse("The batch was not signed by its client.")
        assertEquals("The batch was not signed by its client.", (read() as ResultsReading.Read).overview.refusal)
    }
}
