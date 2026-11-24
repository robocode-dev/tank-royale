package dev.robocode.tankroyale.rumble

import dev.robocode.tankroyale.common.rules.CURRENT_BEHAVIOR_VERSION
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertInstanceOf
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Assumptions.assumeTrue
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Path
import java.time.Duration
import java.time.Instant

class RumbleResultUploadTest {

    private val environment = RumbleEnvironments.TEST
    private val issues = "/repos/robocode-dev/${environment.dataRepository}/issues"
    private val clientA = JournalClient("client-a", "1.4.0")
    private val clientB = JournalClient("client-b", "1.4.0")

    private val pinText = """
        { "schemaVersion": 1, "behaviorVersion": $CURRENT_BEHAVIOR_VERSION, "gameTypes": {
            "1v1": { "rounds": 35, "battlefield": [800, 600], "participants": 2 },
            "twinduel": { "rounds": 75, "battlefield": [800, 800], "participants": 4 },
            "melee": { "rounds": 35, "battlefield": [1000, 1000], "participants": 10 } } }
    """.trimIndent()

    private inner class Data(var text: String? = pinText) : ResultDataSource {
        override fun fileText(path: String) = if (path == EnginePin.PATH) text else null
    }

    /** GitHub's issues of the result repository, as the receiving workflow answers them. */
    private inner class FakeGitHub : GitHubApi {
        val posted = mutableListOf<JsonObject>()
        val comments = mutableMapOf<Int, String>()

        /** Further pages of comments, after the first, by issue. */
        val pages = mutableMapOf<Int, List<String>>()
        var postStatus = 201
        var postBody: String? = null
        var postFails = false
        val commentStatus = mutableMapOf<Int, Int>()
        var tokensSeen = mutableListOf<String?>()
        var onPost: () -> Unit = {}

        override fun get(path: String, token: String?): ApiResponse {
            tokensSeen += token
            val number = Regex("""$issues/(\d+)/comments.*""").matchEntire(path)?.groupValues?.get(1)?.toInt() ?: return ApiResponse(404, "{}")
            commentStatus[number]?.let { return ApiResponse(it, "{}") }
            val page = Regex("""[?&]page=(\d+)""").find(path)?.groupValues?.get(1)?.toInt() ?: 1
            return ApiResponse(200, if (page == 1) comments[number] ?: "[]" else pages[number]?.getOrNull(page - 2) ?: "[]")
        }

        override fun postJson(path: String, token: String, json: String): ApiResponse {
            tokensSeen += token
            assertEquals(issues, path)
            onPost()
            if (postFails) throw IOException("The connection was lost.")
            if (postStatus != 201) return ApiResponse(postStatus, postBody ?: "{}")
            posted += Json.parseToJsonElement(json).jsonObject
            return ApiResponse(201, postBody ?: """{ "number": ${posted.size}, "html_url": "x" }""")
        }

        override fun patchJson(path: String, token: String, json: String): ApiResponse = error("Sending never changes an issue")

        fun envelope(issue: Int): JsonObject {
            val body = posted[issue - 1]["body"]!!.jsonPrimitive.content
            assertEquals(1, Regex("```json").findAll(body).count(), "exactly one fenced json block")
            return Json.parseToJsonElement(body.substringAfter("```json\n").substringBefore("\n```")).jsonObject
        }

        fun battleIds(issue: Int) = envelope(issue)["results"]!!.jsonArray.map { it.jsonObject["battleId"]!!.jsonPrimitive.content }
    }

    private class Access(var token: String? = "token-1") : UploadSignIn {
        var refusals = 0
        override fun access(): UploadAccess = token?.let { UploadAccess.Ready(it) } ?: UploadAccess.Unavailable(UploadProblem.SIGN_IN_NEEDED, "Sign in again.")
        override fun refused() {
            refusals++
            token = null
        }
    }

    private fun participant(name: String, rank: Int) =
        JournalParticipant(name, "1.0", "sha256:" + "a".repeat(64), rank, 100 / rank, 1, 2, 3, 4, 5, 6, if (rank == 1) 30 else 5, if (rank == 1) 5 else 30, 0)

    private var counter = 0
    private fun record(gameType: String = "1v1", client: JournalClient = clientA, behavior: Int = CURRENT_BEHAVIOR_VERSION, names: List<String> = listOf("Alpha", "Beta")) =
        RankedRecord(
            battleId = "00000000-0000-4000-8000-" + (++counter).toString().padStart(12, '0'), gameType = gameType, timestamp = "2026-11-16T10:00:${(counter % 60).toString().padStart(2, '0')}Z",
            client = client, engine = JournalEngine(behavior, "1.4.0", "1.4.0"), botsRepoCommit = "c".repeat(40),
            rounds = if (gameType == "twinduel") 75 else 35, participants = names.mapIndexed { i, n -> participant(n, i + 1) },
        )

    private class Setup(val journal: RankedJournal, val sent: SentBatches, val github: GitHubApi, val events: MutableList<UploadEvent>, val uploader: ResultUploader)

    private fun setup(
        dir: Path,
        github: GitHubApi = FakeGitHub(),
        access: UploadSignIn = Access(),
        data: ResultDataSource = Data(),
        maxCharacters: Int = ResultUploader.MAX_CHARACTERS,
        records: List<RankedRecord> = emptyList(),
    ): Setup {
        val journal = RankedJournal.open(environment, dir.resolve("journal.jsonl"))
        records.forEach { journal.add(it) }
        val sent = SentBatches.open(dir.resolve("batches.jsonl"))
        val events = mutableListOf<UploadEvent>()
        var batch = 0
        val uploader = ResultUploader(
            environment, journal, sent, github, access, data, { events += it }, maxCharacters = maxCharacters,
            newBatchId = { "batch-${++batch}" },
        )
        return Setup(journal, sent, github, events, uploader)
    }

    private fun states(journal: RankedJournal) = journal.entries().associate { it.record.battleId to it.state }

    private fun receipt(vararg lines: String, by: String = "github-actions[bot]") = """
        [ { "user": { "login": "$by" }, "body": ${JsonPrimitive("Processed result batch:\n\n```text\n" + lines.joinToString("\n") + "\n```\n")} } ]
    """.trimIndent()

    private fun refusedReceipt(reason: String) = """
        [ { "user": { "login": "github-actions[bot]" }, "body": ${JsonPrimitive("Rejected result batch before individual records could be read:\n\n```text\n$reason\n```\n")} } ]
    """.trimIndent()

    // RCL-125

    @Test
    @Tag("RCL-125")
    @Tag("Unit")
    @Tag("Positive")
    fun testRCL125_UnitPositive_everyWaitingRecordIsInExactlyOneBatchInTheShapeTheResultDataValidates(@TempDir dir: Path) {
        val oneVsOne = record()
        val twin = record("twinduel", names = listOf("Team A", "Team B"))
        val other = record(client = clientB)
        val github = FakeGitHub()
        val s = setup(dir, github, records = listOf(oneVsOne, twin, other))

        assertEquals(2, s.uploader.send())

        assertEquals(listOf(oneVsOne.battleId, twin.battleId), github.battleIds(1), "one client's records in the order kept")
        assertEquals(listOf(other.battleId), github.battleIds(2))
        val envelope = github.envelope(1)
        assertEquals(1, envelope["schemaVersion"]!!.jsonPrimitive.content.toInt())
        assertEquals("client-a", envelope["clientId"]!!.jsonPrimitive.content)
        assertEquals("1.4.0", envelope["clientVersion"]!!.jsonPrimitive.content)
        val first = envelope["results"]!!.jsonArray[0].jsonObject
        assertEquals(oneVsOne.timestamp, first["completedAt"]!!.jsonPrimitive.content)
        assertEquals("""{"id":"client-a","version":"1.4.0"}""", first["client"].toString())
        assertEquals("""{"behaviorVersion":$CURRENT_BEHAVIOR_VERSION}""", first["engine"].toString())
        assertEquals(listOf("1v1", "35", "800", "600"), listOf("gameType", "rounds", "arenaWidth", "arenaHeight").map { first[it]!!.jsonPrimitive.content })
        val alpha = first["participants"]!!.jsonArray[0].jsonObject
        assertEquals(
            setOf("name", "version", "isTeam", "rank", "totalScore", "survival", "lastSurvivorBonus", "bulletDamage", "bulletKillBonus", "ramDamage", "ramKillBonus", "firstPlaces", "secondPlaces", "thirdPlaces"),
            alpha.keys,
        )
        assertEquals("false", alpha["isTeam"]!!.jsonPrimitive.content)
        val team = envelope["results"]!!.jsonArray[1].jsonObject
        assertEquals(listOf("800", "800"), listOf("arenaWidth", "arenaHeight").map { team[it]!!.jsonPrimitive.content })
        assertTrue(team["participants"]!!.jsonArray.all { it.jsonObject["isTeam"]!!.jsonPrimitive.content == "true" })
    }

    @Test
    @Tag("RCL-125")
    @Tag("Unit")
    @Tag("Negative")
    fun testRCL125_UnitNegative_batchesStayWithinTheirBoundsAndRecordsOfAnotherEpochAreSetAsideAndNeverSent(@TempDir dir: Path) {
        val many = List(130) { record() }
        val github = FakeGitHub()
        val s = setup(dir.resolve("many"), github, records = many)
        s.uploader.send()
        assertEquals(listOf(60, 60, 10), github.posted.indices.map { github.battleIds(it + 1).size })
        assertEquals(many.map { it.battleId }, github.posted.indices.flatMap { github.battleIds(it + 1) }, "each record once, in order")

        val bySize = FakeGitHub()
        val small = setup(dir.resolve("small"), bySize, maxCharacters = 3000, records = List(10) { record() })
        small.uploader.send()
        assertTrue(bySize.posted.size > 1)
        bySize.posted.indices.forEach { assertTrue(bySize.envelope(it + 1).toString().length <= 3000) }
        assertEquals(10, bySize.posted.indices.sumOf { bySize.battleIds(it + 1).size })

        val old = record(behavior = CURRENT_BEHAVIOR_VERSION - 1)
        val current = record()
        val epochs = FakeGitHub()
        val e = setup(dir.resolve("epoch"), epochs, records = listOf(old, current))
        e.uploader.send()
        assertEquals(listOf(current.battleId), epochs.battleIds(1))
        assertEquals(RecordState.SET_ASIDE, states(e.journal)[old.battleId])
        assertTrue(e.events.contains(UploadEvent.SetAside(listOf(old.battleId))))

        // An old-epoch record in a batch that is sent again after a crash is set aside too, never sent.
        val oldInBatch = record(behavior = CURRENT_BEHAVIOR_VERSION - 1)
        val newInBatch = record()
        val crashed = setup(dir.resolve("epoch-crash"), records = listOf(oldInBatch, newInBatch))
        crashed.sent.begin("batch-old", listOf(oldInBatch.battleId, newInBatch.battleId))
        crashed.journal.handOver(oldInBatch.battleId)
        crashed.journal.handOver(newInBatch.battleId)
        val resent = FakeGitHub()
        val after = setup(dir.resolve("epoch-crash"), resent)
        after.uploader.send()
        assertEquals(listOf(newInBatch.battleId), resent.battleIds(1))
        assertEquals(RecordState.SET_ASIDE, states(after.journal)[oldInBatch.battleId])
    }

    // RCL-126

    @Test
    @Tag("RCL-126")
    @Tag("Unit")
    @Tag("Positive")
    fun testRCL126_UnitPositive_aBatchIsSentAsAResultIssueOnlyAfterItsRecordsAreHandedOver(@TempDir dir: Path) {
        val records = listOf(record(), record())
        val github = FakeGitHub()
        lateinit var s: Setup
        val statesAtPost = mutableListOf<Map<String, RecordState>>()
        github.onPost = { statesAtPost += states(s.journal) }
        s = setup(dir, github, records = records)

        s.uploader.send()

        assertEquals(listOf(records.associate { it.battleId to RecordState.HANDED_OVER }), statesAtPost)
        assertEquals("[result] batch-1", github.posted.single()["title"]!!.jsonPrimitive.content)
        assertEquals(records.map { it.battleId }, github.battleIds(1))
        assertEquals(listOf(UploadEvent.BatchSent("batch-1", 1, 2)), s.events)
        assertEquals(listOf("token-1"), github.tokensSeen)
        assertEquals(0, s.uploader.send(), "nothing waits, so nothing more is sent")
    }

    @Test
    @Tag("RCL-126")
    @Tag("Unit")
    @Tag("Negative")
    fun testRCL126_UnitNegative_aCrashAfterABatchWasClosedAndBeforeItsRecordsWaitedAgainNeverLosesARecord(@TempDir dir: Path) {
        // The issue was deleted, or the Rumble refused the batch: the batch was closed, and the program stopped before
        // its records were returned to waiting, so they stood handed over with no open batch to send them.
        for (refused in listOf(false, true)) {
            val crashed = dir.resolve(if (refused) "refused" else "gone")
            val a = record()
            val before = setup(crashed, records = listOf(a))
            before.sent.begin("batch-old", listOf(a.battleId))
            before.journal.handOver(a.battleId)
            before.sent.sentAs("batch-old", 7)
            before.sent.finish("batch-old")
            if (refused) before.sent.refuse("validation failed")
            assertEquals(RecordState.HANDED_OVER, states(before.journal)[a.battleId])

            val github = FakeGitHub()
            val after = setup(crashed, github)
            after.uploader.send()
            if (refused) {
                assertEquals(RecordState.WAITING, states(after.journal)[a.battleId], "it waits again, also while sending is stopped")
                assertTrue(github.posted.isEmpty(), "nothing is sent while a refusal stands")
            } else {
                assertEquals(listOf(a.battleId), github.battleIds(1), "it is sent again in a new batch")
                assertEquals(RecordState.HANDED_OVER, states(after.journal)[a.battleId])
            }
        }
        // A record handed over in a batch that is still open is left to that batch.
        val open = dir.resolve("open")
        val b = record()
        val s = setup(open, records = listOf(b))
        s.sent.begin("batch-open", listOf(b.battleId))
        s.journal.handOver(b.battleId)
        s.sent.sentAs("batch-open", 8)
        val github = FakeGitHub()
        val again = setup(open, github)
        again.uploader.send()
        assertTrue(github.posted.isEmpty(), "an open batch with an issue is not sent again")
        assertEquals(RecordState.HANDED_OVER, states(again.journal)[b.battleId])
    }

    @Test
    @Tag("RCL-126")
    @Tag("Unit")
    @Tag("Negative")
    fun testRCL126_UnitNegative_aFailedSendOrACrashBeforeTheIssueWasRecordedNeverLosesARecord(@TempDir dir: Path) {
        // GitHub said no, so no issue was opened: the records wait again.
        for ((status, body, reason) in listOf(
            Triple(403, """{"message":"API rate limit exceeded"}""", UploadProblem.RATE_LIMITED),
            Triple(429, "{}", UploadProblem.RATE_LIMITED),
            Triple(403, """{"message":"Resource not accessible by integration"}""", UploadProblem.SEND_FAILED),
            Triple(422, "{}", UploadProblem.SEND_FAILED),
        )) {
            val github = FakeGitHub().apply { postStatus = status; postBody = body }
            val s = setup(dir.resolve("status-$status-${reason.name}"), github, records = listOf(record()))
            assertEquals(0, s.uploader.send())
            assertEquals(listOf(RecordState.WAITING), states(s.journal).values.toList())
            assertEquals(UploadEvent.NotSent(reason, listOf("HTTP $status")), s.events.single())
            assertEquals(emptyList<SentBatches.Batch>(), s.sent.open())
        }
        // No clear answer: the issue may have been opened, so the batch stays handed over and is sent again under its own ID.
        for (unclear in listOf("lost", "server", "unreadable")) {
            val github = FakeGitHub().apply {
                when (unclear) {
                    "lost" -> postFails = true
                    "server" -> postStatus = 502
                    else -> postBody = "<html>a proxy page</html>"
                }
            }
            val records = listOf(record(), record())
            val u = setup(dir.resolve("unclear-$unclear"), github, records = records)
            assertEquals(0, u.uploader.send())
            assertTrue(states(u.journal).values.all { it == RecordState.HANDED_OVER }, unclear)
            assertEquals(UploadProblem.SEND_FAILED, (u.events.single() as UploadEvent.NotSent).reason)
            github.postFails = false; github.postStatus = 201; github.postBody = null
            github.posted.clear()
            assertEquals(1, u.uploader.send())
            assertEquals("[result] batch-1", github.posted.single()["title"]!!.jsonPrimitive.content, "the same batch, not a new one")
            assertEquals(records.map { it.battleId }, github.battleIds(1))
        }

        // The program stopped after handing a batch over and before its issue was recorded.
        val crashed = dir.resolve("crash")
        val a = record()
        val b = record()
        val c = record()
        val before = setup(crashed, records = listOf(a, b, c))
        before.sent.begin("batch-old", listOf(a.battleId, b.battleId))
        before.journal.handOver(a.battleId)
        // b was not handed over yet when it stopped; a third record waited outside the batch.
        val github = FakeGitHub()
        val after = setup(crashed, github)
        after.uploader.send()
        assertEquals("[result] batch-old", github.posted[0]["title"]!!.jsonPrimitive.content)
        assertEquals(listOf(a.battleId, b.battleId), github.battleIds(1), "the batch is sent again as it was")
        assertEquals(listOf(c.battleId), github.battleIds(2))
        assertTrue(states(after.journal).values.all { it == RecordState.HANDED_OVER })
        assertEquals(listOf(1, 2), SentBatches.open(crashed.resolve("batches.jsonl")).open().map { it.issue })

        // A torn last line is cut off by its bytes and never read, even after letters of more than one byte.
        val torn = dir.resolve("torn.jsonl")
        val good = """{"kind":"batch","batchId":"første-batch","battleIds":["a"]}""" + "\n"
        Files.write(torn, (good + """{"kind":"issue","batchId":"første-batch","issue":7}""").toByteArray(Charsets.UTF_8))
        val reopened = SentBatches.open(torn)
        assertEquals(listOf(null), reopened.open().map { it.issue }, "the torn line is not taken as written")
        assertEquals(good, Files.readString(torn), "the good line is kept whole")
    }

    // RCL-127

    @Test
    @Tag("RCL-127")
    @Tag("Unit")
    @Tag("Positive")
    fun testRCL127_UnitPositive_eachRecordIsAcceptedRejectedOrSentAgainByItsOwnReceiptLine(@TempDir dir: Path) {
        val accepted = record()
        val rejected = record()
        val unnamed = record()
        val github = FakeGitHub()
        val s = setup(dir, github, records = listOf(accepted, rejected, unnamed))
        s.uploader.send()

        s.uploader.collectReceipts()
        assertEquals(RecordState.HANDED_OVER, states(s.journal)[accepted.battleId], "no receipt yet: the batch stays handed over")
        github.comments[1] = receipt("${accepted.battleId}: accepted", "${rejected.battleId}: rejected: unknown or inactive bot `Alpha 1.0`")
        s.uploader.collectReceipts()

        val entries = s.journal.entries().associateBy { it.record.battleId }
        assertEquals(RecordState.ACCEPTED, entries.getValue(accepted.battleId).state)
        assertEquals(RecordState.REJECTED, entries.getValue(rejected.battleId).state)
        assertEquals(MoveReason.RUMBLE_REJECTED, entries.getValue(rejected.battleId).reason)
        assertEquals(listOf("unknown or inactive bot `Alpha 1.0`"), entries.getValue(rejected.battleId).detail)
        assertEquals(RecordState.WAITING, entries.getValue(unnamed.battleId).state, "a record the receipt does not name is sent again")
        assertTrue(s.events.contains(UploadEvent.RecordAccepted(accepted.battleId)))
        assertTrue(s.events.contains(UploadEvent.RecordRejected(rejected.battleId, "unknown or inactive bot `Alpha 1.0`")))
        assertEquals(emptyList<SentBatches.Batch>(), s.sent.open(), "the batch is answered")

        s.uploader.send()
        assertEquals(listOf(unnamed.battleId), github.battleIds(2), "the rejected record is not sent again by itself")
    }

    @Test
    @Tag("RCL-127")
    @Tag("Unit")
    @Tag("Negative")
    fun testRCL127_UnitNegative_aRefusedBatchWaitsAgainAndStopsSendingAndOtherAccountsCannotWriteAReceipt(@TempDir dir: Path) {
        val a = record()
        val github = FakeGitHub()
        val s = setup(dir, github, records = listOf(a))
        s.uploader.send()

        github.comments[1] = receipt("${a.battleId}: accepted", by = "someone-else")
        s.uploader.collectReceipts()
        assertEquals(RecordState.HANDED_OVER, states(s.journal)[a.battleId], "a comment by another account is not a receipt")

        github.comments[1] = refusedReceipt("validation failed: clientId `client-a` is not registered to `outsider`")
        s.uploader.collectReceipts()
        assertEquals(RecordState.WAITING, states(s.journal)[a.battleId])
        assertTrue(s.events.contains(UploadEvent.BatchRefused("batch-1", 1, "validation failed: clientId `client-a` is not registered to `outsider`")))

        s.events.clear()
        assertEquals(0, s.uploader.send())
        assertEquals(1, github.posted.size, "nothing more is sent after a refused batch")
        assertEquals(UploadEvent.NotSent(UploadProblem.BATCH_REFUSED, listOf("validation failed: clientId `client-a` is not registered to `outsider`")), s.events.single())

        // The stop holds after a restart, so a refused batch is not sent again at every start, until it is lifted.
        val restarted = setup(dir, github)
        assertEquals(0, restarted.uploader.send())
        assertEquals(1, github.posted.size)
        restarted.uploader.liftRefusal()
        assertEquals(1, restarted.uploader.send())
        assertEquals(listOf(a.battleId), github.battleIds(2))
    }

    @Test
    @Tag("RCL-127")
    @Tag("Unit")
    @Tag("Negative")
    fun testRCL127_UnitNegative_aDeletedIssueDoesNotStopOtherReceiptsAndAReceiptAfterManyCommentsIsFound(@TempDir dir: Path) {
        val first = record()
        val second = record(client = clientB)
        val github = FakeGitHub()
        val s = setup(dir, github, records = listOf(first, second))
        s.uploader.send()
        github.commentStatus[1] = 404
        // A hundred comments by others come before the receipt, which is on the second page.
        github.comments[2] = JsonArray(List(100) { Json.parseToJsonElement("""{ "user": { "login": "visitor" }, "body": "hello" }""") }).toString()
        github.pages[2] = listOf(receipt("${second.battleId}: accepted"))

        s.uploader.collectReceipts()

        assertEquals(RecordState.WAITING, states(s.journal)[first.battleId], "the deleted issue's record is sent again")
        assertEquals(RecordState.ACCEPTED, states(s.journal)[second.battleId], "the batch after it is still read")
        assertEquals(emptyList<SentBatches.Batch>(), s.sent.open())
    }

    // RCL-128

    @Test
    @Tag("RCL-128")
    @Tag("Unit")
    @Tag("Negative")
    fun testRCL128_UnitNegative_withoutAUsableSignInPinOrClientIdNothingIsSentAndTheReasonIsGiven(@TempDir dir: Path) {
        val noSignIn = FakeGitHub()
        val n = setup(dir.resolve("none"), noSignIn, access = Access(null), records = listOf(record()))
        n.uploader.send()
        assertEquals(0, noSignIn.posted.size)
        assertEquals(UploadEvent.NotSent(UploadProblem.SIGN_IN_NEEDED, listOf("Sign in again.")), n.events.single())
        assertEquals(RecordState.WAITING, states(n.journal).values.single())

        val refusedToken = FakeGitHub().apply { postStatus = 401 }
        val access = Access()
        val r = setup(dir.resolve("refused"), refusedToken, access = access, records = listOf(record()))
        r.uploader.send()
        assertEquals(1, access.refusals, "a refused token is not given again")
        assertEquals(UploadProblem.SIGN_IN_NEEDED, (r.events.single() as UploadEvent.NotSent).reason)
        assertEquals(RecordState.WAITING, states(r.journal).values.single())

        val behind = FakeGitHub()
        val p = setup(dir.resolve("pin"), behind, data = Data(pinText.replace("\"behaviorVersion\": $CURRENT_BEHAVIOR_VERSION", "\"behaviorVersion\": 1")), records = listOf(record()))
        p.uploader.send()
        assertEquals(0, behind.posted.size)
        assertEquals(UploadEvent.NotSent(UploadProblem.PIN_REFUSED, listOf("BEHAVIOR_VERSION_DIFFERS", "1", "$CURRENT_BEHAVIOR_VERSION")), p.events.single())
        assertEquals(RecordState.WAITING, states(p.journal).values.single(), "records are not set aside when the pin is behind the program")

        // A format the pin no longer names holds back only its own records.
        val retired = FakeGitHub()
        val melee = record("melee", names = List(10) { "Bot$it" })
        val duel = record()
        val f = setup(dir.resolve("format"), retired, data = Data(pinText.replace(Regex(""",\s*"melee": \{[^}]*\}"""), "")), records = listOf(melee, duel))
        f.uploader.send()
        assertEquals(listOf(duel.battleId), retired.battleIds(1))
        assertEquals(RecordState.WAITING, states(f.journal)[melee.battleId])
        assertTrue(f.events.contains(UploadEvent.NotSent(UploadProblem.PIN_REFUSED, listOf("FORMAT_NOT_PINNED", "melee"))))

        val noClient = FakeGitHub()
        val unregistered = record(client = JournalClient("", "1.4.0"))
        val c = setup(dir.resolve("client"), noClient, records = listOf(unregistered, record()))
        c.uploader.send()
        assertEquals(1, noClient.battleIds(1).size)
        assertTrue(c.events.contains(UploadEvent.NotSent(UploadProblem.NO_CLIENT_ID, listOf(unregistered.battleId))))
        assertEquals(RecordState.WAITING, states(c.journal)[unregistered.battleId])
    }

    @Test
    @Tag("RCL-128")
    @Tag("Unit")
    @Tag("Positive")
    fun testRCL128_UnitPositive_theSavedSignInIsRenewedOnlyWhenItsTokenRanOutOrWasRefused(@TempDir base: Path) {
        var renewals = 0
        val github = object : GitHubAuthTransport {
            override fun postForm(url: String, form: Map<String, String>): AuthResponse {
                renewals++
                return tokenAnswer(access = "access-$renewals", refresh = "refresh-$renewals", expiresIn = 28800)
            }
        }
        val saved = SavedSignIn(SignInStore(environment, base, SignInIdentity("alice", "/home/alice", "Linux")), GitHubSignIn(environment, github, MutableClock(START)))
        assertEquals(SaveResult.Saved, saved.remember(GitHubTokens("access-0", "refresh-0", null, null)))
        var now = START
        val upload = SavedSignInUpload(saved) { now }

        val first = upload.access() as UploadAccess.Ready
        val again = upload.access() as UploadAccess.Ready
        upload.refused()
        val afterRefusal = upload.access() as UploadAccess.Ready
        now = now.plus(Duration.ofHours(9))
        val afterExpiry = upload.access() as UploadAccess.Ready

        assertEquals(listOf("access-1", "access-1", "access-2", "access-3"), listOf(first, again, afterRefusal, afterExpiry).map { it.token })
        assertEquals(3, renewals)
        assertFalse(first.toString().contains("access"), "the token is never shown")

        val nobody = SavedSignInUpload(SavedSignIn(SignInStore(environment, base.resolve("empty"), SignInIdentity("bob", "/home/bob", "Linux")), GitHubSignIn(environment, github, MutableClock(START))))
        assertEquals(UploadProblem.SIGN_IN_NEEDED, (nobody.access() as UploadAccess.Unavailable).reason)
    }

    // RCL-129

    @Test
    @Tag("RCL-129")
    @Tag("Unit")
    @Tag("Positive")
    fun testRCL129_UnitPositive_automaticSendingRunsWhenEnoughWaitOrTimeHasPassedAndReceiptsAtTheirOwnInterval(@TempDir dir: Path) {
        val github = FakeGitHub()
        val s = setup(dir, github)
        var now = Instant.parse("2026-11-16T10:00:00Z")
        val automatic = AutomaticUpload(s.uploader, sendAt = 3, sendAfter = Duration.ofMinutes(15), receiptsAfter = Duration.ofMinutes(2), sendGap = Duration.ofMinutes(1), clock = { now })

        s.journal.add(record()); automatic.betweenBattles()
        s.journal.add(record()); now = now.plusSeconds(30); automatic.betweenBattles()
        assertEquals(0, github.posted.size, "two results wait, fewer than the three it sends at")
        s.journal.add(record()); now = now.plusSeconds(40); automatic.betweenBattles()
        assertEquals(1, github.posted.size, "three results wait")
        s.journal.add(record()); now = now.plus(Duration.ofMinutes(15)); automatic.betweenBattles()
        assertEquals(2, github.posted.size, "the set time passed")

        // A failed send is not tried again at every look of a waiting session.
        github.postStatus = 500
        repeat(3) { s.journal.add(record()) }
        val before = github.tokensSeen.count { it != null }
        now = now.plusSeconds(10); automatic.betweenBattles()
        now = now.plusSeconds(10); automatic.betweenBattles()
        val posts = github.tokensSeen.size - before
        assertTrue(posts <= 3, "at most one send and the receipt looks within the gap, not one per look: $posts")
        automatic.finish()
        assertEquals(2, github.posted.size)
    }

    @Test
    @Tag("RCL-129")
    @Tag("Unit")
    @Tag("Negative")
    fun testRCL129_UnitNegative_aSessionSendsBetweenBattlesAndEndsWhenTheListOfSentBatchesCannotBeWritten(@TempDir dir: Path) {
        var calls = 0
        val failing = SessionSetup(
            commit = "a".repeat(40), source = object : CatalogSource {
                override fun catalogText(commit: String) = error("not used")
                override fun archive(commit: String) = error("not used")
            },
            botCache = dir.resolve("cache"), host = WorkerHost(WorkerLimits()), command = WorkerCommand { listOf("none") },
            journal = RankedJournal.open(environment, dir.resolve("journal.jsonl")), engine = JournalEngine(CURRENT_BEHAVIOR_VERSION, "1", "1"),
            client = clientA, roundsPerFormat = emptyMap(), pollMillis = 1, sleep = {},
            betweenBattles = { calls++; throw JournalUnusable("The list of sent batches cannot be written.") },
        )
        val end = SessionRunner(failing, SessionControl()) {}.run { SessionRequest(emptyList(), emptySet(), SelectionMode.ALL, listOf("1v1"), mapOf("1v1" to 2)) }

        assertEquals(SessionEnd.JournalFailed("The list of sent batches cannot be written."), end)
        assertEquals(1, calls, "it ran while the session waited")

        // After the person asked to end at once, nothing more is started over the network.
        val stopping = FakeGitHub()
        val st = setup(dir.resolve("stop"), stopping, records = listOf(record(), record()))
        val stopped = AutomaticUpload(st.uploader, sendAt = 1, stop = { true })
        stopped.betweenBattles()
        stopped.finish()
        assertEquals(0, stopping.posted.size)
        assertEquals(0, stopping.tokensSeen.size, "no request at all")

        // Any other failure of sending is reported and the session goes on.
        val broken = object : GitHubApi {
            override fun get(path: String, token: String?): ApiResponse = throw IllegalStateException("broken answer")
            override fun postJson(path: String, token: String, json: String): ApiResponse = throw IllegalStateException("broken answer")
            override fun patchJson(path: String, token: String, json: String): ApiResponse = error("not used")
        }
        val s = setup(dir.resolve("broken"), broken, records = listOf(record()))
        AutomaticUpload(s.uploader, sendAt = 1).betweenBattles()
        assertTrue(s.events.contains(UploadEvent.NotSent(UploadProblem.SEND_FAILED, listOf("broken answer"))), s.events.toString())
    }

    // RCL-130

    @Test
    @Tag("RCL-130")
    @Tag("Integration")
    @Tag("Positive")
    fun testRCL130_IntegrationPositive_oneRealBatchIsSentAndReceiptedInTheTestRumble(@TempDir dir: Path) {
        assumeTrue(System.getProperty("rumble.live.send") == "true", "Set -Drumble.live.send=true with the outside test account signed in to send to the test Rumble")
        val saved = SavedSignIn(SignInStore.inUserFolder(environment), GitHubSignIn(environment, HttpGitHubAuthTransport()))
        val access = SavedSignInUpload(saved)
        val token = (access.access() as? UploadAccess.Ready)?.token ?: error("No usable sign-in for the test Rumble on this computer")
        val api = HttpGitHubApi()
        val account = Json.parseToJsonElement(api.get("/user", token).body).jsonObject["login"]!!.jsonPrimitive.content
        val registration = Registration(environment, api, object : RegistrationMemory {
            override fun load(account: String): RegistrationRecord? = null
            override fun save(record: RegistrationRecord) = true
        }).readRegistrationFile(account, token) ?: error("$account is not registered in the test Rumble")
        val data = GitHubResultDataSource(environment)
        val catalog = Json.parseToJsonElement(data.fileText("catalog.json")!!).jsonObject["bots"]!!.jsonArray.map { it.jsonObject }
            .filter { it["status"]!!.jsonPrimitive.content == "active" && (it["teamMembers"] as? JsonArray).isNullOrEmpty() }
            .filter { (it["gameTypes"] as? JsonArray)?.any { g -> g.jsonPrimitive.content == "1v1" } ?: true }
            .take(2)
        assumeTrue(catalog.size == 2, "The test catalog has two active bots for 1v1")
        val pin = SessionPins.prepare(data, listOf("1v1")) as? SessionPin.Ready ?: error("The test Rumble's pin refuses this program")
        val record = RankedRecord(
            battleId = java.util.UUID.randomUUID().toString(), gameType = "1v1", timestamp = Instant.now().toString(), client = JournalClient(registration, "integration"),
            engine = JournalEngine(pin.pin.behaviorVersion, "integration", "integration"), botsRepoCommit = "c".repeat(40), rounds = pin.roundsPerFormat.getValue("1v1"),
            participants = catalog.mapIndexed { i, bot ->
                JournalParticipant(bot["name"]!!.jsonPrimitive.content, bot["version"]!!.jsonPrimitive.content, bot["sourceHash"]!!.jsonPrimitive.content, i + 1,
                    if (i == 0) 80 else 20, 0, 0, 0, 0, 0, 0, if (i == 0) 35 else 0, if (i == 0) 0 else 35, 0)
            },
        )
        val journal = RankedJournal.open(environment, dir.resolve("journal.jsonl"))
        journal.add(record)
        val events = mutableListOf<UploadEvent>()
        val uploader = ResultUploader(environment, journal, SentBatches.open(dir.resolve("batches.jsonl")), api, access, data, { events += it })

        assertEquals(1, uploader.send(), events.toString())
        val deadline = Instant.now().plus(Duration.ofMinutes(10))
        while (journal.entries().single().state == RecordState.HANDED_OVER && Instant.now().isBefore(deadline)) {
            Thread.sleep(15_000)
            uploader.collectReceipts()
        }
        val state = journal.entries().single().state
        println("Receipt for ${record.battleId}: $state ${journal.entries().single().detail} $events")
        assertTrue(state == RecordState.ACCEPTED || state == RecordState.REJECTED, "the receipt answered the record: $state")
        assertInstanceOf(UploadEvent.BatchSent::class.java, events.first())
        Files.deleteIfExists(dir.resolve("journal.jsonl"))
    }
}
