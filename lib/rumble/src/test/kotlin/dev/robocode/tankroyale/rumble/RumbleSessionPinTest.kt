package dev.robocode.tankroyale.rumble

import dev.robocode.tankroyale.common.rules.CURRENT_BEHAVIOR_VERSION
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertInstanceOf
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Assumptions.assumeTrue
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import java.io.IOException
import java.time.Duration
import java.time.Instant

class RumbleSessionPinTest {

    /** A result repository held in memory; a path in [failing] cannot be fetched, and every read is counted. */
    private class Data(vararg files: Pair<String, String>) : ResultDataSource {
        val files = mutableMapOf(*files)
        val failing = mutableSetOf<String>()
        var busy = false
        val reads = mutableListOf<String>()
        override fun fileText(path: String): String? {
            reads += path
            if (path in failing) throw IOException("The connection was lost.")
            if (busy) throw CatalogUnreadable("The result repository answered 503.")
            return files[path]
        }
    }

    private val formats = listOf("1v1", "twinduel", "melee")

    private fun pin(
        behavior: Int = CURRENT_BEHAVIOR_VERSION,
        schema: Int = 1,
        oneVsOne: String = """{ "rounds": 35, "battlefield": [800, 600], "participants": 2 }""",
        melee: String = """{ "rounds": 35, "battlefield": [1000, 1000], "participants": 10 }""",
        extra: String = "",
    ) = """
        {
          "schemaVersion": $schema,
          "behaviorVersion": $behavior,
          "tankRoyaleVersion": "1.0.0",
          "image": "ghcr.io/robocode-dev/tank-royale:1.0.0",
          "gameTypes": {
            "1v1": $oneVsOne,
            "twinduel": { "rounds": 75, "battlefield": [800, 800], "participants": 4 },
            "melee": $melee$extra
          }
        }
    """.trimIndent()

    private fun prepare(text: String?, chosen: List<String> = formats, version: Int = CURRENT_BEHAVIOR_VERSION): SessionPin =
        SessionPins.prepare(if (text == null) Data() else Data(EnginePin.PATH to text), chosen, version)

    private fun refused(pin: SessionPin) = assertInstanceOf(SessionPin.Refused::class.java, pin)

    private fun advice(format: String, vararg groups: List<String>, schema: Int = 1, gameType: String = format) =
        MatchmakingAdvice.path(format) to """
            { "gameType": "$gameType", "priorityPairs": [${groups.joinToString { g -> """{ "bots": [${g.joinToString { "\"$it\"" }}], "have": 0, "reason": "new-bot" }""" }}],
              "projectionId": "x", "schemaVersion": $schema, "targetSamplesPerPairing": 6 }
        """.trimIndent()

    // RCL-121

    @Test
    @Tag("Positive")
    fun testRCL121_UnitPositive_aValidPinGivesTheBehaviorVersionAndEachFormatsRoundsAndBattleSize() {
        val ready = assertInstanceOf(SessionPin.Ready::class.java, prepare(pin()))

        assertEquals(CURRENT_BEHAVIOR_VERSION, ready.pin.behaviorVersion)
        assertEquals(mapOf("1v1" to 35, "twinduel" to 75, "melee" to 35), ready.roundsPerFormat)
        assertEquals(mapOf("1v1" to 2, "twinduel" to 4, "melee" to 10), ready.participantCounts)
        assertEquals(listOf(1000, 1000), ready.pin.gameTypes.getValue("melee").battlefield)
        assertEquals(JournalEngine(CURRENT_BEHAVIOR_VERSION, "1.4.0", "1.4.1"), ready.engine("1.4.0", "1.4.1"))
        val oneFormat = assertInstanceOf(SessionPin.Ready::class.java, prepare(pin(), listOf("1v1")))
        assertEquals(mapOf("1v1" to 35), oneFormat.roundsPerFormat)
    }

    @Test
    @Tag("Negative")
    fun testRCL121_UnitNegative_aPinThatCannotBeReadOrUnderstoodRefusesTheSessionWithItsReason() {
        val unreachable = Data(EnginePin.PATH to pin()).apply { failing += EnginePin.PATH }
        val busy = Data(EnginePin.PATH to pin()).apply { this.busy = true }

        assertEquals(SessionPin.Refused(PinRefusal.MISSING), prepare(null))
        assertEquals(PinRefusal.UNREADABLE, refused(SessionPins.prepare(unreachable, formats)).reason)
        assertEquals(PinRefusal.UNREADABLE, refused(SessionPins.prepare(busy, formats)).reason)
        assertEquals(listOf("The result repository answered 503."), refused(SessionPins.prepare(busy, formats)).values)
        assertEquals(PinRefusal.NOT_UNDERSTOOD, refused(prepare("this is not a pin")).reason)
        assertEquals(PinRefusal.NOT_UNDERSTOOD, refused(prepare("""{ "schemaVersion": 1, "behaviorVersion": 2 }""")).reason)
        assertEquals(SessionPin.Refused(PinRefusal.UNSUPPORTED_SCHEMA, listOf("2", "1")), prepare(pin(schema = 2)))
        // A newer schema says so whatever else it changed, so the screen can say to update the program.
        assertEquals(
            SessionPin.Refused(PinRefusal.UNSUPPORTED_SCHEMA, listOf("2", "1")),
            prepare(pin(schema = 2, oneVsOne = """{ "rounds": 35, "battlefield": { "w": 800, "h": 600 }, "participants": 2, "turnTimeout": 10 }""")),
        )
        assertEquals(SessionPin.Refused(PinRefusal.NOT_UNDERSTOOD, listOf("schemaVersion")), prepare(pin().replace("\"schemaVersion\": 1", "\"schemaVersion\": \"1\"")))
        assertEquals(SessionPin.Refused(PinRefusal.NOT_UNDERSTOOD, listOf("the pin is not a JSON object")), prepare("[1, 2]"))
        // Every number is written as a number, in the formats too.
        assertEquals(
            SessionPin.Refused(PinRefusal.NOT_UNDERSTOOD, listOf("game type 1v1")),
            prepare(pin(oneVsOne = """{ "rounds": "35", "battlefield": [800, 600], "participants": 2 }""")),
        )
        assertEquals(
            SessionPin.Refused(PinRefusal.NOT_UNDERSTOOD, listOf("game type 1v1")),
            prepare(pin(oneVsOne = """{ "rounds": 35, "battlefield": ["800", 600], "participants": 2 }""")),
        )
        // A path that leaves its folder is never fetched.
        val github = GitHubResultDataSource(RumbleEnvironments.TEST)
        listOf("matchmaking/../engine.json", "../x", "./engine.json").forEach { assertThrows(CatalogUnreadable::class.java, { github.fileUrl(it) }, it) }
        assertEquals("https://raw.githubusercontent.com/robocode-dev/rumble-data-test/main/engine.json", github.fileUrl(EnginePin.PATH).toString())
        // A failure without a message still says what failed.
        val silent = object : ResultDataSource {
            override fun fileText(path: String): String = throw IOException()
        }
        assertEquals(SessionPin.Refused(PinRefusal.UNREADABLE, listOf("IOException")), SessionPins.prepare(silent, formats))
        assertEquals(PinRefusal.NOT_UNDERSTOOD, refused(prepare(pin(behavior = 0), version = 0)).reason)
        assertEquals(
            SessionPin.Refused(PinRefusal.NOT_UNDERSTOOD, listOf("game type 1v1")),
            prepare(pin(oneVsOne = """{ "rounds": 0, "battlefield": [800, 600], "participants": 2 }""")),
        )
        assertEquals(PinRefusal.NOT_UNDERSTOOD, refused(prepare(pin(oneVsOne = """{ "rounds": 35, "battlefield": [800], "participants": 2 }"""))).reason)
        assertEquals(PinRefusal.NOT_UNDERSTOOD, refused(prepare(pin(oneVsOne = """{ "rounds": 35, "battlefield": [800, 600], "participants": 1 }"""))).reason)
    }

    // RCL-122

    @Test
    @Tag("Negative")
    fun testRCL122_UnitNegative_aPinThisProgramCannotRunUnderIsRefusedNamingWhatDiffers() {
        assertEquals(SessionPin.Refused(PinRefusal.BEHAVIOR_VERSION_DIFFERS, listOf("1", "2")), prepare(pin(behavior = 1), version = 2))
        assertEquals(SessionPin.Refused(PinRefusal.BEHAVIOR_VERSION_DIFFERS, listOf("3", "2")), prepare(pin(behavior = 3), version = 2))
        // A newer behavior version is reported as such even when the pin also pins a rule this program does not run.
        assertEquals(
            SessionPin.Refused(PinRefusal.BEHAVIOR_VERSION_DIFFERS, listOf("3", "2")),
            prepare(pin(behavior = 3, oneVsOne = """{ "rounds": 35, "battlefield": [800, 600], "participants": 2, "turnTimeout": 10 }"""), version = 2),
        )
        assertEquals(SessionPin.Refused(PinRefusal.FORMAT_NOT_PINNED, listOf("classic")), prepare(pin(), listOf("1v1", "classic")))
        assertEquals(
            SessionPin.Refused(PinRefusal.FORMAT_NOT_RUNNABLE, listOf("skirmish", "800x600, 4 bots", "")),
            prepare(pin(extra = """, "skirmish": { "rounds": 10, "battlefield": [800, 600], "participants": 4 }"""), listOf("skirmish")),
        )
        // The worker runs classic too, so a classic format it can run is not refused.
        assertInstanceOf(
            SessionPin.Ready::class.java,
            prepare(pin(extra = """, "classic": { "rounds": 10, "battlefield": [800, 600], "participants": 4 }"""), listOf("classic")),
        )
        assertEquals(
            SessionPin.Refused(PinRefusal.FORMAT_NOT_RUNNABLE, listOf("1v1", "1000x1000, 2 bots", "800x600, 2 bots")),
            prepare(pin(oneVsOne = """{ "rounds": 35, "battlefield": [1000, 1000], "participants": 2 }""")),
        )
        assertEquals(
            SessionPin.Refused(PinRefusal.FORMAT_NOT_RUNNABLE, listOf("melee", "1000x1000, 5 bots", "1000x1000, 10 to any number of bots")),
            prepare(pin(melee = """{ "rounds": 35, "battlefield": [1000, 1000], "participants": 5 }""")),
        )
        // A chosen format that pins a rule the worker would not apply is not taken as pinned.
        assertEquals(
            SessionPin.Refused(PinRefusal.NOT_UNDERSTOOD, listOf("game type 1v1 pins gunCoolingRate, turnTimeout")),
            prepare(pin(oneVsOne = """{ "rounds": 35, "battlefield": [800, 600], "participants": 2, "turnTimeout": 10, "gunCoolingRate": 0.2 }""")),
        )
        // A format that is not chosen is not checked at all, not even for impossible values or unknown rules.
        val unchosen = """{ "rounds": 0, "battlefield": [1000], "participants": 5, "turnTimeout": 10 }"""
        assertInstanceOf(SessionPin.Ready::class.java, prepare(pin(melee = unchosen), listOf("1v1")))
        // Nor for a shape this program does not read.
        val otherShape = """{ "rounds": "many", "battlefield": { "w": 1000, "h": 1000 } }"""
        assertInstanceOf(SessionPin.Ready::class.java, prepare(pin(melee = otherShape), listOf("1v1")))
        assertEquals(SessionPin.Refused(PinRefusal.NOT_UNDERSTOOD, listOf("game type melee")), prepare(pin(melee = otherShape), listOf("melee")))
    }

    // RCL-123

    @Test
    @Tag("Positive")
    fun testRCL123_UnitPositive_adviceIsReadBestFirstAndReadAgainOnlyAfterTheRefreshTime() {
        val data = Data(advice("1v1", listOf("A 1.0", "B 1.0"), listOf("C 1.0", "D 1.0")), advice("twinduel", listOf("T 1.0", "U 1.0")))
        var now = Instant.parse("2026-11-16T10:00:00Z")
        val keeper = AdviceKeeper(data, formats, Duration.ofMinutes(5)) { now }

        val first = keeper.current()
        now = now.plusSeconds(60)
        data.files += advice("1v1", listOf("E 1.0", "F 1.0"))
        val early = keeper.current()
        now = now.plusSeconds(240)
        val later = keeper.current()

        assertEquals(mapOf("1v1" to listOf(listOf("A 1.0", "B 1.0"), listOf("C 1.0", "D 1.0")), "twinduel" to listOf(listOf("T 1.0", "U 1.0"))), first)
        assertEquals(first, early, "nothing is read again before the refresh time")
        assertEquals(6, data.reads.size, "each format was read twice")
        assertEquals(listOf(listOf("E 1.0", "F 1.0")), later["1v1"])
    }

    @Test
    @Tag("Negative")
    fun testRCL123_UnitNegative_missingUnknownOrUnreachableAdviceGivesNoneAndAFailedReReadKeepsTheLast() {
        assertEquals(AdviceRead.None, MatchmakingAdvice.read(Data(), "1v1"))
        assertEquals(AdviceRead.None, MatchmakingAdvice.read(Data(advice("1v1", listOf("A 1.0", "B 1.0"), schema = 2)), "1v1"))
        assertEquals(AdviceRead.None, MatchmakingAdvice.read(Data(advice("1v1", listOf("A 1.0", "B 1.0"), gameType = "melee")), "1v1"))
        assertEquals(AdviceRead.None, MatchmakingAdvice.read(Data(MatchmakingAdvice.path("1v1") to "not advice"), "1v1"))
        assertInstanceOf(AdviceRead.Failed::class.java, MatchmakingAdvice.read(Data().apply { failing += MatchmakingAdvice.path("1v1") }, "1v1"))
        assertInstanceOf(AdviceRead.Failed::class.java, MatchmakingAdvice.read(Data().apply { busy = true }, "1v1"))

        val data = Data(advice("1v1", listOf("A 1.0", "B 1.0")), advice("melee", listOf("M 1.0", "N 1.0")))
        var now = Instant.parse("2026-11-16T10:00:00Z")
        val keeper = AdviceKeeper(data, listOf("1v1", "melee"), Duration.ofMinutes(5)) { now }
        keeper.current()
        data.failing += MatchmakingAdvice.path("1v1")
        data.files.remove(MatchmakingAdvice.path("melee"))
        now = now.plus(Duration.ofMinutes(5))

        assertEquals(mapOf("1v1" to listOf(listOf("A 1.0", "B 1.0"))), keeper.current(), "the failed format keeps its advice, the withdrawn one has none")

        // A failed read does not start the refresh time: the next use, a moment later, reads again and gets the advice.
        val outage = Data(advice("1v1", listOf("A 1.0", "B 1.0"))).apply { busy = true }
        val recovering = AdviceKeeper(outage, listOf("1v1"), Duration.ofMinutes(5)) { now }
        assertEquals(emptyMap<String, List<List<String>>>(), recovering.current())
        outage.busy = false
        assertEquals(mapOf("1v1" to listOf(listOf("A 1.0", "B 1.0"))), recovering.current())

        // Only the format whose read failed is read again; the others wait for their refresh time.
        val partly = Data(advice("1v1", listOf("A 1.0", "B 1.0")), advice("melee", listOf("M 1.0", "N 1.0"))).apply { failing += MatchmakingAdvice.path("melee") }
        val retrying = AdviceKeeper(partly, listOf("1v1", "melee"), Duration.ofMinutes(5)) { now }
        retrying.current()
        partly.failing.clear()
        val again = retrying.current()
        assertEquals(listOf("1v1", "melee", "melee").map { MatchmakingAdvice.path(it) }, partly.reads)
        assertEquals(setOf("1v1", "melee"), again.keys)

        // An interrupted read, as when the session is cancelled, is a failed read and keeps the thread's interruption.
        val interrupted = object : ResultDataSource {
            override fun fileText(path: String): String = throw InterruptedException()
        }
        assertInstanceOf(AdviceRead.Failed::class.java, MatchmakingAdvice.read(interrupted, "1v1"))
        assertTrue(Thread.interrupted(), "the interruption is kept")
        assertEquals(PinRefusal.UNREADABLE, refused(SessionPins.prepare(interrupted, formats)).reason)
        assertTrue(Thread.interrupted(), "the interruption is kept")
    }

    // RCL-124

    @Test
    @Tag("Integration")
    @Tag("Positive")
    fun testRCL124_IntegrationPositive_theRealPublicPinAndAdviceFilesAreUnderstood() {
        assumeTrue(System.getProperty("rumble.live") == "true", "Set -Drumble.live=true to read the public Rumble")
        for (environment in listOf(RumbleEnvironments.PUBLIC, RumbleEnvironments.TEST)) {
            val source = GitHubResultDataSource(environment)
            // Read as the session reads it. A pin whose behavior version differs from this program's is read again under
            // its own version, so a pin that is behind this program still shows whether everything else is understood.
            val first = SessionPins.prepare(source, formats)
            val pinned = if (first is SessionPin.Refused && first.reason == PinRefusal.BEHAVIOR_VERSION_DIFFERS) {
                SessionPins.prepare(source, formats, first.values[0].toInt())
            } else first
            val ready = assertInstanceOf(SessionPin.Ready::class.java, pinned, "${environment.name}: $pinned")
            assertTrue(ready.pin.behaviorVersion >= 1)
            assertEquals(formats.toSet(), ready.pin.gameTypes.keys)
            for (format in formats) {
                // A published advice file must be understood; only an absent one may give no advice.
                val published = source.fileText(MatchmakingAdvice.path(format)) != null
                val read = MatchmakingAdvice.read(source, format)
                if (published) {
                    val found = assertInstanceOf(AdviceRead.Found::class.java, read, "$format in ${environment.dataRepository}")
                    assertTrue(found.groups.all { it.size >= 2 }, found.toString())
                } else {
                    assertEquals(AdviceRead.None, read)
                }
            }
        }
    }
}
