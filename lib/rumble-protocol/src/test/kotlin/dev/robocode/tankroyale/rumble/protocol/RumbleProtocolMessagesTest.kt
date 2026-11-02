package dev.robocode.tankroyale.rumble.protocol

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test

class RumbleProtocolMessagesTest {

    private val job = Job(
        jobId = "job-1", behaviorVersion = 2, gameType = "1v1", numberOfRounds = 3,
        bots = listOf(JobBot("Walls", "1.0.0", "sha256:aa", "/work/Walls"), JobBot("Spin\nBot", "2.1", "sha256:bb", "/work/SpinBot")),
    )

    private val outcome = BotOutcome("Walls", "1.0.0", false, 1, 900, 500, 100, 200, 50, 30, 20, 2, 1, 0)

    private val events = listOf(
        WorkerEvent.Started(jobId = "job-1"),
        WorkerEvent.Result(jobId = "job-1", numberOfRounds = 3, bots = listOf(outcome)),
        WorkerEvent.Refused(jobId = null, code = ErrorCode.INVALID_JOB, message = "bad\njob"),
        WorkerEvent.Failed(jobId = "job-1", code = ErrorCode.BATTLE_FAILED, message = "no bots"),
        WorkerEvent.Finished(jobId = "job-1"),
    )

    // RBW-001

    @Test
    @Tag("RBW-001")
    @Tag("Unit")
    @Tag("Positive")
    fun testRBW001_UnitPositive_aJobAndEveryEventRoundTripAsOneVersionedLine() {
        val jobLine = MessageCodec.write(job)
        assertEquals(job, MessageCodec.readJob(jobLine))
        assertTrue(jobLine.contains("\"schemaVersion\":$SUPPORTED_SCHEMA_VERSION"))
        assertFalse(jobLine.contains('\n') || jobLine.contains('\r'))

        events.forEach { event ->
            val line = MessageCodec.write(event)
            assertEquals(event, MessageCodec.readEvent(line))
            assertEquals(SUPPORTED_SCHEMA_VERSION, event.schemaVersion)
            assertTrue(line.contains("\"schemaVersion\":$SUPPORTED_SCHEMA_VERSION"), line)
            assertFalse(line.contains('\n') || line.contains('\r'), line)
        }
    }

    @Test
    @Tag("RBW-001")
    @Tag("Unit")
    @Tag("Negative")
    fun testRBW001_UnitNegative_textWithLineBreaksNeverBreaksTheOneLineRule() {
        val line = MessageCodec.write(WorkerEvent.Failed(jobId = "j", code = ErrorCode.BATTLE_FAILED, message = "a\r\nb\nc"))

        assertEquals(1, line.lines().size)
        assertEquals("a\r\nb\nc", (MessageCodec.readEvent(line) as WorkerEvent.Failed).message)
    }

    // RBW-002

    @Test
    @Tag("RBW-002")
    @Tag("Unit")
    @Tag("Negative")
    fun testRBW002_UnitNegative_unknownVersionsAndInvalidLinesAreRefusedWholeInPlainWords() {
        val goodJob = MessageCodec.write(job)

        val newer = assertThrows(MessageRejected::class.java) { MessageCodec.readJob(goodJob.replace("\"schemaVersion\":1", "\"schemaVersion\":2")) }
        assertEquals(ErrorCode.UNKNOWN_VERSION, newer.code)
        assertTrue(newer.message!!.contains("version 2") && newer.message!!.contains("version 1") && newer.message!!.contains("Update the program"), newer.message)

        val newerEvent = assertThrows(MessageRejected::class.java) { MessageCodec.readEvent("""{"schemaVersion":9,"type":"started","jobId":"j"}""") }
        assertEquals(ErrorCode.UNKNOWN_VERSION, newerEvent.code)

        listOf(
            "not json at all",
            "[1,2,3]",
            """{"jobId":"j"}""",
            """{"schemaVersion":1,"jobId":"j","behaviorVersion":2,"gameType":"1v1","numberOfRounds":3}""",
            """{"schemaVersion":1,"jobId":"","behaviorVersion":2,"gameType":"1v1","numberOfRounds":3,"bots":[]}""",
            """{"schemaVersion":1,"jobId":"j","behaviorVersion":0,"gameType":"1v1","numberOfRounds":3,"bots":[{"name":"a","version":"1","sourceHash":"h","folder":"f"}]}""",
        ).forEach { line ->
            val rejected = assertThrows(MessageRejected::class.java, { MessageCodec.readJob(line) }, line)
            assertEquals(ErrorCode.INVALID_JOB, rejected.code, line)
            assertTrue(rejected.message!!.isNotBlank())
        }

        listOf("""{"schemaVersion":1,"type":"mystery","jobId":"j"}""", """{"schemaVersion":1,"type":"started"}""").forEach { line ->
            assertThrows(MessageRejected::class.java, { MessageCodec.readEvent(line) }, line)
        }
    }
}
