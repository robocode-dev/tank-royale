package dev.robocode.tankroyale.rumble

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.nio.file.Path

/** What a session says about the bots of a battle while they start: its status, its log and which reports are news. */
class RumbleSessionBootTest {

    @TempDir
    lateinit var dir: Path

    private fun event(kind: String, vararg more: Pair<String, Any>): JsonObject = buildJsonObject {
        put("event", kind)
        more.forEach { (name, value) -> if (value is Int) put(name, value) else if (value is Long) put(name, value) else put(name, value.toString()) }
    }

    private fun starting(connected: Int, expected: Int, elapsedMillis: Long) =
        event("bots-starting", "battleId" to "b1", "connected" to connected, "expected" to expected, "elapsedMillis" to elapsedMillis, "limitMillis" to 90_000L)

    private fun tracker(folder: SessionFolder) = SessionTracker(folder.claim()!!, RumbleEnvironments.TEST, null, listOf("twinduel"), null)

    @Test
    @Tag("RCL-160")
    @Tag("Unit")
    @Tag("Positive")
    fun testRCL160_UnitPositive_theStatusCarriesTheStartingBotsUntilTheyAllConnectedAndTheLogGetsOnlyTheNews() {
        val folder = SessionFolder(dir.resolve("session"))
        tracker(folder).use { tracker ->
            tracker.record(event("battle-started", "battleId" to "b1", "gameType" to "twinduel"))
            assertNull(tracker.status.boot)

            // The reports of one battle: the log keeps the news, the status keeps every one of them.
            val log = StartingLog()
            listOf(starting(0, 4, 500), starting(0, 4, 1000), starting(1, 4, 1700), starting(1, 4, 2500), starting(1, 4, 3500)).forEach {
                val connected = it["connected"].toString().toInt()
                tracker.record(it, log = log.isNews(connected))
            }
            assertEquals(SessionBoot(connected = 1, expected = 4, elapsedSeconds = 3, limitSeconds = 90), tracker.status.boot)
            assertEquals(listOf("battle-started", "bots-starting", "bots-starting"), folder.events().map { Json.parseToJsonElement(it).let { e -> (e as JsonObject)["event"].toString().trim('"') } })

            // All connected: the starting is over and the status says nothing more about it.
            tracker.record(starting(4, 4, 18_000), log = log.isNews(4))
            assertNull(tracker.status.boot)
        }
    }

    @Test
    @Tag("RCL-160")
    @Tag("Unit")
    @Tag("Negative")
    fun testRCL160_UnitNegative_everyEndingOfTheBattleClearsTheStartingBotsAndAnOlderStatusHasNone() {
        val folder = SessionFolder(dir.resolve("session"))
        tracker(folder).use { tracker ->
            for (ending in listOf(event("battle-kept", "battleId" to "b1"), event("battle-not-kept", "battleId" to "b1", "reason" to "TIMED_OUT"), event("waiting", "reason" to "NO_BATTLE"), event("paused"), event("finishing"))) {
                tracker.record(event("battle-started", "battleId" to "b1", "gameType" to "twinduel"))
                tracker.record(starting(2, 4, 9_000))
                assertEquals(2, tracker.status.boot?.connected, "starting before ${ending["event"]}")
                tracker.record(ending)
                assertNull(tracker.status.boot, "cleared by ${ending["event"]}")
            }
            // A battle that begins clears what the one before left, and the session's end clears it too.
            tracker.record(starting(1, 4, 2_000))
            tracker.record(event("battle-started", "battleId" to "b2", "gameType" to "1v1"))
            assertNull(tracker.status.boot)
            tracker.record(starting(1, 4, 2_000))
            tracker.end("battles-done", "")
            assertNull(tracker.status.boot)
        }
        // A report that comes late, after its battle ended or for another battle, shows nothing.
        tracker(SessionFolder(dir.resolve("late"))).use { tracker ->
            tracker.record(event("battle-started", "battleId" to "b1", "gameType" to "twinduel"))
            tracker.record(event("battle-not-kept", "battleId" to "b1", "reason" to "TIMED_OUT"))
            tracker.record(starting(1, 4, 2_000))
            assertNull(tracker.status.boot, "a report after the battle ended")
            tracker.record(event("battle-started", "battleId" to "b2", "gameType" to "twinduel"))
            tracker.record(starting(1, 4, 2_000))
            assertNull(tracker.status.boot, "a report of the battle before")
        }
        // A status as an older program wrote it, without the field, is read with no progress.
        val old = """{"environment":"test","pid":1,"startedAt":"2026-11-23T10:00:00Z","updatedAt":"2026-11-23T10:01:00Z"}"""
        assertNull(Json { ignoreUnknownKeys = true }.decodeFromString(SessionStatus.serializer(), old).boot)
    }

    @Test
    @Tag("RCL-160")
    @Tag("Unit")
    @Tag("Negative")
    fun testRCL160_UnitNegative_aNewBattleStartsOverAndTheFirstReportIsAlwaysNews() {
        val log = StartingLog()
        assertTrue(log.isNews(0))
        assertFalse(log.isNews(0))
        assertTrue(log.isNews(2))
        assertFalse(log.isNews(2))
        log.reset()
        assertTrue(log.isNews(2), "the next battle's first report is news also when it has the same count")
    }

    @Test
    @Tag("RCL-163")
    @Tag("Unit")
    @Tag("Positive")
    fun testRCL163_UnitPositive_theStatusListsEachBotLeftOutOnceWithWhyAndABotThatIsOnlyUnreachableIsNotListed() {
        val folder = SessionFolder(dir.resolve("left-out"))
        tracker(folder).use { tracker ->
            tracker.record(event("bot-not-placed", "bot" to "Beta 1.0", "reason" to "DID_NOT_CONNECT"))
            tracker.record(event("bot-not-placed", "bot" to "Beta 1.0", "reason" to "DID_NOT_CONNECT"))
            tracker.record(event("bot-not-placed", "bot" to "Gamma 1.0", "reason" to "SOURCE_UNAVAILABLE"))
            tracker.record(event("bot-not-placed", "bot" to "Delta 1.0", "reason" to "HASH_MISMATCH"))
            assertEquals(listOf(SessionLeftOut("Beta 1.0", "DID_NOT_CONNECT"), SessionLeftOut("Delta 1.0", "HASH_MISMATCH")), tracker.status.leftOut)
        }
        // Every reason has a text, and a reason the screen does not know gets the general one.
        assertEquals("rumble.contribute.left_out.did_not_connect", ContributeTexts.forLeftOut("DID_NOT_CONNECT"))
        assertEquals("rumble.contribute.left_out.other", ContributeTexts.forLeftOut("SOURCE_UNAVAILABLE"))
        assertEquals("rumble.contribute.left_out.other", ContributeTexts.forLeftOut("SOMETHING_NEW"))
    }
}
