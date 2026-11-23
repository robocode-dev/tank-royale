package dev.robocode.tankroyale.rumble

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.nio.file.Files
import java.nio.file.Path
import java.time.Instant
import java.util.concurrent.TimeUnit

class RumbleRankedJournalTest {

    private val public = RumbleEnvironments.PUBLIC
    private val test = RumbleEnvironments.TEST
    private val clock = { Instant.parse("2026-11-13T10:00:00Z") }

    private fun open(file: Path, environment: RumbleEnvironment = public) = RankedJournal.open(environment, file, clock)

    private fun ids(journal: RankedJournal) = journal.entries().map { it.record.battleId }

    // RCL-108

    @Test
    @Tag("Positive")
    fun testRCL108_IntegrationPositive_everyRecordTheStoppedProcessReportedKeptIsReadBackExactly(@TempDir dir: Path) {
        val file = dir.resolve("journal.jsonl")
        val java = Path.of(System.getProperty("java.home"), "bin", if (System.getProperty("os.name").startsWith("Windows")) "java.exe" else "java").toString()
        val process = ProcessBuilder(java, "-cp", System.getProperty("java.class.path"), "dev.robocode.tankroyale.rumble.JournalWriterChild", file.toString())
            .redirectErrorStream(true).start()
        val kept = mutableListOf<String>()
        process.inputStream.bufferedReader().use { reader ->
            while (kept.size < 40) {
                val line = reader.readLine() ?: break
                if (line.startsWith("kept ")) kept += line.removePrefix("kept ")
            }
            process.destroyForcibly()
            process.waitFor(30, TimeUnit.SECONDS)
        }
        assertTrue(kept.size >= 40, "the child reported ${kept.size} records kept")

        val journal = open(file)

        val read = ids(journal)
        assertEquals(kept, read.take(kept.size), "every record reported kept is read back, in order")
        for (id in kept) assertEquals(JournalFixtures.record(id), journal.entries().first { it.record.battleId == id }.record)
    }

    @Test
    @Tag("Negative")
    fun testRCL108_IntegrationNegative_aRecordIsNotKeptWhenTheFileCannotBeWrittenAndTheJournalSaysSo(@TempDir dir: Path) {
        val file = dir.resolve("journal.jsonl")
        val journal = open(file)
        journal.add(JournalFixtures.record("one"))
        Files.delete(file)

        assertThrows(JournalUnusable::class.java) { journal.add(JournalFixtures.record("two")) }
        assertEquals(listOf("one"), ids(journal), "a record that could not be written is not listed as kept")
    }


    @Test
    @Tag("Negative")
    fun testRCL108_IntegrationNegative_aWriteThatFailsHalfWayIsTakenBackAndNeverGluesAKeptRecordOntoIt(@TempDir dir: Path) {
        val file = dir.resolve("journal.jsonl")
        var failing = false
        val hooks = JournalHooks(write = { channel, buffer ->
            if (failing) {
                val half = buffer.remaining() / 2
                val part = ByteArray(half)
                buffer.get(part)
                channel.write(java.nio.ByteBuffer.wrap(part))
                throw java.io.IOException("the disk is full")
            }
            channel.write(buffer)
        })
        val journal = RankedJournal.openWith(public, file, clock, hooks)
        journal.add(JournalFixtures.record("one"))
        val size = Files.size(file)

        failing = true
        assertThrows(JournalUnusable::class.java) { journal.add(JournalFixtures.record("two")) }
        assertEquals(size, Files.size(file), "the half-written line was taken back")
        failing = false
        assertEquals(AddResult.Kept, journal.add(JournalFixtures.record("three")))

        val reopened = open(file)
        assertEquals(listOf("one", "three"), ids(reopened))
        assertEquals(emptyList<Int>(), reopened.readReport().unreadableLines)
        assertFalse(reopened.readReport().tornEnd)
    }

    @Test
    @Tag("Negative")
    fun testRCL108_IntegrationNegative_aFlushThatFailsLeavesNoLineAndAMoveThatFailsChangesNoState(@TempDir dir: Path) {
        val file = dir.resolve("journal.jsonl")
        var failing = false
        val hooks = JournalHooks(force = { channel -> if (failing) throw java.io.IOException("the flush failed") else channel.force(true) })
        val journal = RankedJournal.openWith(public, file, clock, hooks)
        journal.add(JournalFixtures.record("one"))
        val size = Files.size(file)

        failing = true
        assertThrows(JournalUnusable::class.java) { journal.add(JournalFixtures.record("two")) }
        assertThrows(JournalUnusable::class.java) { journal.handOver("one") }

        assertEquals(size, Files.size(file), "a line that could not be flushed is not left in the file")
        assertEquals(listOf("one"), ids(journal))
        assertEquals(RecordState.WAITING, journal.entries().single().state)
        failing = false
        journal.handOver("one")
        assertEquals(RecordState.HANDED_OVER, open(file).entries().single().state)
    }

    // RCL-109

    @Test
    @Tag("Positive")
    fun testRCL109_UnitPositive_aLastLineCutAtAnyPositionNeverCostsAnEarlierRecord(@TempDir dir: Path) {
        val source = dir.resolve("source.jsonl")
        val journal = open(source)
        journal.add(JournalFixtures.record("one"))
        journal.add(JournalFixtures.record("two"))
        val before = Files.readAllBytes(source)
        journal.add(JournalFixtures.record("three"))
        val whole = Files.readAllBytes(source)
        val lastLine = whole.size - before.size - 1 // without its newline

        for (cut in 0..lastLine) {
            val file = dir.resolve("cut-$cut.jsonl")
            Files.write(file, whole.copyOf(before.size + cut))
            val reopened = open(file)

            val read = ids(reopened)
            assertEquals(listOf("one", "two"), read.take(2), "cut at $cut")
            if (cut < lastLine) {
                assertFalse("three" in read, "a cut line is not a record, cut at $cut")
                if (cut > 0) {
                    assertTrue(reopened.readReport().tornEnd, "cut at $cut")
                    assertEquals(listOf(4), reopened.readReport().unreadableLines, "cut at $cut")
                }
            } else {
                assertTrue("three" in read, "a whole line is a record")
                assertEquals(JournalFixtures.record("three"), reopened.entries().last().record)
                assertTrue(reopened.readReport().tornEnd, "the line has no newline yet")
            }
        }
    }


    @Test
    @Tag("Positive")
    fun testRCL109_UnitPositive_aHeaderCutAtAnyPositionIsWrittenAgainAndTheJournalIsUsable(@TempDir dir: Path) {
        val whole = dir.resolve("whole.jsonl")
        open(whole)
        val header = Files.readAllBytes(whole)

        for (cut in 0 until header.size) {
            val file = dir.resolve("header-$cut.jsonl")
            Files.write(file, header.copyOf(cut))

            val journal = open(file)
            journal.add(JournalFixtures.record("one"))

            assertEquals(listOf("one"), ids(open(file)), "header cut at $cut")
        }
    }

    @Test
    @Tag("Positive")
    fun testRCL109_UnitPositive_aMoveLineCutAtAnyPositionLeavesTheStateAsItWasAndCostsNoRecord(@TempDir dir: Path) {
        val source = dir.resolve("source.jsonl")
        val journal = open(source)
        journal.add(JournalFixtures.record("one"))
        journal.add(JournalFixtures.record("two"))
        val before = Files.readAllBytes(source)
        journal.handOver("one")
        val whole = Files.readAllBytes(source)

        for (cut in 0 until whole.size - before.size - 1) {
            val file = dir.resolve("move-$cut.jsonl")
            Files.write(file, whole.copyOf(before.size + cut))

            val reopened = open(file)

            assertEquals(listOf("one", "two"), ids(reopened), "move cut at $cut")
            assertEquals(RecordState.WAITING, reopened.entries().first().state, "a cut move is not applied, cut at $cut")
        }
    }

    @Test
    @Tag("Negative")
    fun testRCL109_UnitNegative_theNextRecordAfterATornEndIsOnALineOfItsOwnAndAForeignFileIsRefused(@TempDir dir: Path) {
        val file = dir.resolve("journal.jsonl")
        val journal = open(file)
        journal.add(JournalFixtures.record("one"))
        val bytes = Files.readAllBytes(file)
        Files.write(file, bytes + "{\"kind\":\"record\",\"record\":{\"battleId\":\"cu".toByteArray())

        val reopened = open(file)
        reopened.add(JournalFixtures.record("two"))
        val again = open(file)

        assertEquals(listOf("one", "two"), ids(again))
        assertFalse(again.readReport().tornEnd, "the torn end was closed by a newline, so the file now ends on a line")
        assertEquals(1, again.readReport().unreadableLines.size)

        val foreign = dir.resolve("foreign.txt")
        Files.writeString(foreign, "hello\nworld\n")
        val refused = assertThrows(JournalUnusable::class.java) { open(foreign) }
        assertTrue(refused.message!!.contains("does not start like a Rumble journal"), refused.message)
        assertEquals("hello\nworld\n", Files.readString(foreign), "a foreign file is not written to")
        val partial = dir.resolve("partial.txt")
        val start = "{\"kind\":\"header\",\"schemaVersion\":1,\"environment\":\"nobody\""
        Files.writeString(partial, start)
        assertThrows(JournalUnusable::class.java) { open(partial) }
        assertEquals(start, Files.readString(partial), "a file that is not this journal's own start is never overwritten")
    }

    // RCL-110

    @Test
    @Tag("Positive")
    fun testRCL110_UnitPositive_aJournalOpensForItsOwnEnvironmentAndKeepsItsRecordsThere(@TempDir dir: Path) {
        val file = dir.resolve("journal.jsonl")
        open(file, test).add(JournalFixtures.record("one"))

        assertEquals(listOf("one"), ids(open(file, test)))
    }

    @Test
    @Tag("Negative")
    fun testRCL110_UnitNegative_aJournalIsRefusedForTheOtherEnvironmentAndNothingIsWritten(@TempDir dir: Path) {
        val file = dir.resolve("journal.jsonl")
        open(file, test).add(JournalFixtures.record("one"))
        val before = Files.readAllBytes(file)

        val refused = assertThrows(EnvironmentRefused::class.java) { open(file, public) }

        assertTrue(refused.message!!.contains(test.name) && refused.message!!.contains(public.displayName), refused.message)
        assertEquals(before.toList(), Files.readAllBytes(file).toList())

        val future = dir.resolve("future.jsonl")
        Files.writeString(future, "{\"kind\":\"header\",\"schemaVersion\":2,\"environment\":\"public\"}\n")
        val unknown = assertThrows(JournalUnusable::class.java) { open(future) }
        assertTrue(unknown.message!!.contains("Update the program"))
    }

    // RCL-111

    @Test
    @Tag("Positive")
    fun testRCL111_UnitPositive_theSameBattleIsKeptOnceInTheSameRunAndAfterReopening(@TempDir dir: Path) {
        val file = dir.resolve("journal.jsonl")
        val journal = open(file)
        assertEquals(AddResult.Kept, journal.add(JournalFixtures.record("one")))
        val size = Files.size(file)

        assertEquals(AddResult.AlreadyKept, journal.add(JournalFixtures.record("one")))
        assertEquals(AddResult.AlreadyKept, open(file).add(JournalFixtures.record("one")))

        assertEquals(size, Files.size(file), "nothing was written")
        assertEquals(listOf("one"), ids(open(file)))
    }

    @Test
    @Tag("Negative")
    fun testRCL111_UnitNegative_aDifferentBattleIsKeptAndARecordOfAnUnknownSchemaIsRefused(@TempDir dir: Path) {
        val journal = open(dir.resolve("journal.jsonl"))

        assertEquals(AddResult.Kept, journal.add(JournalFixtures.record("one")))
        assertEquals(AddResult.Kept, journal.add(JournalFixtures.record("two")))
        assertThrows(IllegalArgumentException::class.java) { journal.add(JournalFixtures.record("three").copy(schemaVersion = 2)) }
        assertEquals(listOf("one", "two"), ids(journal))
    }

    // RCL-112

    @Test
    @Tag("Positive")
    fun testRCL112_UnitPositive_aMoveIsALineAddedAfterTheOthersAndTheStateSurvivesReopening(@TempDir dir: Path) {
        val file = dir.resolve("journal.jsonl")
        val journal = open(file)
        journal.add(JournalFixtures.record("one"))
        journal.add(JournalFixtures.record("two"))
        journal.add(JournalFixtures.record("three"))
        val before = Files.readAllBytes(file)

        journal.handOver("one")
        journal.handOver("two")
        journal.accept("one")
        journal.reject("two", "bad-schema", listOf("field rounds"))
        journal.handOver("three")
        journal.returnToWaiting("three")

        val after = Files.readAllBytes(file)
        assertEquals(before.toList(), after.copyOf(before.size).toList(), "the lines before are unchanged")
        val states = open(file).entries().associate { it.record.battleId to it }
        assertEquals(RecordState.ACCEPTED, states.getValue("one").state)
        assertEquals(RecordState.REJECTED, states.getValue("two").state)
        assertEquals("bad-schema", states.getValue("two").reason)
        assertEquals(listOf("field rounds"), states.getValue("two").detail)
        assertEquals(RecordState.WAITING, states.getValue("three").state)
        assertEquals(listOf("three"), open(file).waiting().map { it.record.battleId })
    }

    @Test
    @Tag("Negative")
    fun testRCL112_UnitNegative_aMoveThatDoesNotFitOrNamesNoRecordIsRefusedAndWritesNothing(@TempDir dir: Path) {
        val file = dir.resolve("journal.jsonl")
        val journal = open(file)
        journal.add(JournalFixtures.record("one"))

        val refusedAt = Files.size(file)
        assertThrows(JournalUnusable::class.java) { journal.accept("one") } // never handed over
        assertThrows(JournalUnusable::class.java) { journal.returnToWaiting("one") } // already waiting
        assertThrows(JournalUnusable::class.java) { journal.handOver("nobody") }
        assertEquals(refusedAt, Files.size(file), "a refused move writes nothing")
        journal.handOver("one")
        journal.accept("one")
        assertThrows(JournalUnusable::class.java) { journal.returnToWaiting("one") } // a receipt is final
        assertThrows(JournalUnusable::class.java) { journal.reject("one", "late") }

        assertEquals(RecordState.ACCEPTED, journal.entries().single().state)
    }

    // RCL-113

    @Test
    @Tag("Positive")
    fun testRCL113_UnitPositive_recordsOfAnotherEpochAreSetAsideWithTheirReasonAndStayListed(@TempDir dir: Path) {
        val file = dir.resolve("journal.jsonl")
        val journal = open(file)
        journal.add(JournalFixtures.record("old", behaviorVersion = 6))
        journal.add(JournalFixtures.record("current", behaviorVersion = 7))
        journal.add(JournalFixtures.record("newer", behaviorVersion = 8))

        val setAside = journal.setAsideOtherEpochs(7)

        assertEquals(listOf("old", "newer"), setAside)
        val reopened = open(file)
        val old = reopened.entries().first { it.record.battleId == "old" }
        assertEquals(RecordState.SET_ASIDE, old.state)
        assertEquals(MoveReason.ENGINE_EPOCH, old.reason)
        assertEquals(listOf("6", "7"), old.detail)
        assertEquals(listOf("current"), reopened.waiting().map { it.record.battleId })
        assertEquals(3, reopened.entries().size, "nothing is removed")
    }

    @Test
    @Tag("Negative")
    fun testRCL113_UnitNegative_recordsOfTheCurrentEpochAndRecordsAlreadyOnTheirWayAreNotSetAside(@TempDir dir: Path) {
        val journal = open(dir.resolve("journal.jsonl"))
        journal.add(JournalFixtures.record("current", behaviorVersion = 7))
        journal.add(JournalFixtures.record("sent", behaviorVersion = 6))
        journal.handOver("sent")

        assertEquals(emptyList<String>(), journal.setAsideOtherEpochs(7))
        assertEquals(RecordState.WAITING, journal.entries().first { it.record.battleId == "current" }.state)
        assertEquals(RecordState.HANDED_OVER, journal.entries().first { it.record.battleId == "sent" }.state)
    }

    // RCL-114

    private fun publicMethods(type: Class<*>) =
        type.methods.filter { it.declaringClass == type && !it.isSynthetic && !it.name.contains('$') }

    @Test
    @Tag("Positive")
    fun testRCL114_UnitPositive_theOnlyOperationsAreTheOnesTiedToBatchesReceiptsAndTheEngineEpoch() {
        val operations = publicMethods(RankedJournal::class.java).map { it.name }.toSet()
        val opening = publicMethods(RankedJournal.Companion::class.java).map { it.name }.toSet()

        assertEquals(
            setOf("add", "handOver", "returnToWaiting", "accept", "reject", "setAsideOtherEpochs", "entries", "waiting", "readReport", "getEnvironment"),
            operations,
        )
        assertEquals(setOf("open", "read"), opening)
    }

    @Test
    @Tag("Negative")
    fun testRCL114_UnitNegative_noOperationTakesAnythingButAnIdentityAReasonOrAWholeRecordToAdd() {
        val allowed = setOf<Class<*>>(String::class.java, Int::class.javaPrimitiveType!!, List::class.java, RankedRecord::class.java)

        for (m in publicMethods(RankedJournal::class.java)) {
            for (p in m.parameterTypes) assertTrue(p in allowed, "${m.name} takes a $p, which could choose records by outcome")
        }
        assertTrue(RankedJournal::class.java.constructors.none { !it.isSynthetic }, "a journal is made only by open")
        val forbiddenNames = Regex("(?i)remove|delete|drop|hide|skip|discard|filter|select|choose|prune|clear|purge|erase|truncate|compact|forget|exclude|only|reset|keepIf|best|worst|win|score|rank")
        for (m in publicMethods(RankedJournal::class.java) + publicMethods(RankedJournal.Companion::class.java)) {
            assertFalse(forbiddenNames.containsMatchIn(m.name), "${m.name} reads like a way to choose records")
        }
    }
}
