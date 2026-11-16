package dev.robocode.tankroyale.rumble

import dev.robocode.tankroyale.common.rules.CURRENT_BEHAVIOR_VERSION
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.add
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.put
import kotlinx.serialization.json.putJsonArray
import kotlinx.serialization.json.putJsonObject
import java.io.IOException
import java.nio.ByteBuffer
import java.nio.channels.FileChannel
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardOpenOption
import java.time.Duration
import java.time.Instant
import java.util.UUID

/** Why results were not sent, or a batch was not taken. The screens and the command line turn these into text. */
enum class UploadProblem {
    /** There is no usable sign-in; the contributor must sign in again. */
    SIGN_IN_NEEDED,

    /** The sign-in could not be renewed now; a later try may work. */
    SIGN_IN_FAILED,

    /** A waiting record has no registered client ID, so it cannot be sent. */
    NO_CLIENT_ID,

    /** The Rumble's engine pin refuses this program or the records' formats; the values are the pin's refusal. */
    PIN_REFUSED,

    /** GitHub did not take the batch. */
    SEND_FAILED,

    /** GitHub is limiting requests. */
    RATE_LIMITED,

    /** An earlier batch was refused before its records could be read, so sending stops until the cause is fixed; the value is the Rumble's reason. */
    BATCH_REFUSED,
}

/** What sending and collecting receipts report, in plain codes. */
sealed interface UploadEvent {
    data class BatchSent(val batchId: String, val issue: Int, val results: Int) : UploadEvent

    /** Nothing more was sent now; the records wait and are tried again. */
    data class NotSent(val reason: UploadProblem, val values: List<String> = emptyList()) : UploadEvent
    data class RecordAccepted(val battleId: String) : UploadEvent
    data class RecordRejected(val battleId: String, val reason: String) : UploadEvent

    /** The Rumble refused the whole batch before reading its records; they wait again and sending stops. */
    data class BatchRefused(val batchId: String, val issue: Int, val reason: String) : UploadEvent

    /** Records of another engine epoch were set aside and will never be sent. */
    data class SetAside(val battleIds: List<String>) : UploadEvent
}

/** A sign-in token to send with, or why there is none. */
sealed interface UploadAccess {
    class Ready(val token: String) : UploadAccess {
        override fun toString() = "Ready(token=hidden)"
    }

    data class Unavailable(val reason: UploadProblem, val message: String) : UploadAccess
}

/** Gives the token of the contributor's sign-in to the environment's App. */
interface UploadSignIn {
    fun access(): UploadAccess

    /** GitHub did not accept the token given last; the next [access] must not give it again. */
    fun refused()
}

/** The saved sign-in, renewed only when its token has run out or GitHub refused it. */
class SavedSignInUpload(private val saved: SavedSignIn, private val clock: () -> Instant = Instant::now) : UploadSignIn {
    private var tokens: GitHubTokens? = null

    @Synchronized
    override fun access(): UploadAccess {
        tokens?.takeIf { !it.isExpired(clock()) }?.let { return UploadAccess.Ready(it.accessToken) }
        return when (val renewal = saved.renew()) {
            is SavedRenewal.Renewed -> {
                tokens = renewal.tokens
                UploadAccess.Ready(renewal.tokens.accessToken)
            }
            is SavedRenewal.SignInNeeded -> UploadAccess.Unavailable(UploadProblem.SIGN_IN_NEEDED, renewal.message)
            is SavedRenewal.Failed -> UploadAccess.Unavailable(UploadProblem.SIGN_IN_FAILED, renewal.message)
        }
    }

    @Synchronized
    override fun refused() {
        tokens = null
    }
}

@Serializable
private data class OutboxLine(
    val kind: String,
    val batchId: String = "",
    val battleIds: List<String> = emptyList(),
    val issue: Int? = null,
    val reason: String? = null,
)

/**
 * The batches this computer has sent or is about to send, and the issue each became, so receipts are found again after
 * a crash; and whether sending is stopped because the Rumble refused a batch. Like the journal it is only added to, each
 * line is flushed to the disk, a write that fails is taken back, and a torn last line is cut off and never read. The
 * journal stays the truth about each record; this file only says where to look for its receipt.
 */
class SentBatches private constructor(private val file: Path) {
    class Batch(val batchId: String, val battleIds: List<String>, val issue: Int?)

    private val batches = LinkedHashMap<String, Batch>()
    private val done = mutableSetOf<String>()
    private var unusable: String? = null

    /** The reason the Rumble gave for refusing a batch, while sending is stopped for it; null when sending may go on. */
    @Volatile
    var refusal: String? = null
        private set

    /** The batches whose receipt has not been taken yet. */
    @Synchronized
    fun open(): List<Batch> = batches.values.filter { it.batchId !in done }

    @Synchronized
    internal fun begin(batchId: String, battleIds: List<String>) {
        append(OutboxLine("batch", batchId, battleIds))
        batches[batchId] = Batch(batchId, battleIds, null)
    }

    @Synchronized
    internal fun sentAs(batchId: String, issue: Int) {
        append(OutboxLine("issue", batchId, issue = issue))
        batches[batchId] = Batch(batchId, batches.getValue(batchId).battleIds, issue)
    }

    @Synchronized
    internal fun finish(batchId: String) {
        append(OutboxLine("done", batchId))
        done += batchId
    }

    /** Sending stops for [reason] until [lift]; it stays stopped across restarts. */
    @Synchronized
    internal fun refuse(reason: String) {
        append(OutboxLine("refused", reason = reason))
        refusal = reason
    }

    /** The contributor fixed the cause of a refusal; sending may go on. */
    @Synchronized
    internal fun lift() {
        if (refusal == null) return
        append(OutboxLine("lifted"))
        refusal = null
    }

    private fun append(line: OutboxLine) {
        unusable?.let { throw JournalUnusable(it) }
        val bytes = (json.encodeToString(OutboxLine.serializer(), line) + "\n").toByteArray(Charsets.UTF_8)
        try {
            FileChannel.open(file, StandardOpenOption.CREATE, StandardOpenOption.WRITE).use { channel ->
                val before = channel.size()
                try {
                    channel.position(before)
                    val buffer = ByteBuffer.wrap(bytes)
                    while (buffer.hasRemaining()) channel.write(buffer)
                    channel.force(true)
                } catch (e: IOException) {
                    // A line written in part is taken back, so the next line never joins it.
                    try {
                        channel.truncate(before)
                        channel.force(true)
                    } catch (t: IOException) {
                        unusable = "The list of sent batches at $file could not be repaired after a failed write: ${t.message}"
                    }
                    throw e
                }
            }
        } catch (e: IOException) {
            throw JournalUnusable("The list of sent batches at $file cannot be written: ${e.message}", e)
        }
    }

    companion object {
        private val json = Json { ignoreUnknownKeys = true }

        /** Opens the file, or starts it. A torn last line is cut off; any other line that cannot be read is skipped. */
        fun open(file: Path): SentBatches {
            val sent = SentBatches(file)
            try {
                Files.createDirectories(file.toAbsolutePath().parent)
                if (Files.exists(file)) {
                    val bytes = Files.readAllBytes(file)
                    // Everything after the last newline is a line the program did not finish writing: cut off, never read.
                    val kept = bytes.lastIndexOf('\n'.code.toByte()) + 1
                    if (kept < bytes.size) {
                        FileChannel.open(file, StandardOpenOption.WRITE).use { it.truncate(kept.toLong()); it.force(true) }
                    }
                    for (raw in String(bytes, 0, kept, Charsets.UTF_8).lineSequence().filter { it.isNotBlank() }) {
                        val line = try {
                            json.decodeFromString(OutboxLine.serializer(), raw)
                        } catch (e: IllegalArgumentException) {
                            continue
                        }
                        when (line.kind) {
                            "batch" -> sent.batches[line.batchId] = Batch(line.batchId, line.battleIds, null)
                            "issue" -> sent.batches[line.batchId]?.let { sent.batches[line.batchId] = Batch(it.batchId, it.battleIds, line.issue) }
                            "done" -> sent.done += line.batchId
                            "refused" -> sent.refusal = line.reason ?: "The Rumble refused a batch."
                            "lifted" -> sent.refusal = null
                        }
                    }
                }
            } catch (e: IOException) {
                throw JournalUnusable("The list of sent batches at $file cannot be read: ${e.message}", e)
            }
            return sent
        }
    }
}

/**
 * Sends the journal's waiting records in batches as the signed-in contributor and acknowledges each record only by its
 * published receipt, as the decision on signed-in sending sets out. A batch's records are handed over in the journal
 * before its issue is opened, so nothing is lost: a send that fails returns them to waiting, and a batch whose issue was
 * never recorded is sent again, which the Rumble answers with the same receipt.
 */
class ResultUploader(
    private val environment: RumbleEnvironment,
    private val journal: RankedJournal,
    private val sent: SentBatches,
    private val api: GitHubApi,
    private val signIn: UploadSignIn,
    private val data: ResultDataSource,
    private val onEvent: (UploadEvent) -> Unit,
    private val behaviorVersion: Int = CURRENT_BEHAVIOR_VERSION,
    private val maxResults: Int = MAX_RESULTS,
    private val maxCharacters: Int = MAX_CHARACTERS,
    private val newBatchId: () -> String = { UUID.randomUUID().toString() },
) {
    private val repository = "/repos/${RumbleEnvironments.owner}/${environment.dataRepository}"

    /** The number of journal records waiting to be sent. */
    fun waitingCount(): Int = journal.waiting().size

    /** The contributor fixed what made the Rumble refuse a batch, so sending may go on. */
    fun liftRefusal() = sent.lift()

    /** Reports a failure of sending that is not the journal's, such as an answer that cannot be read, without stopping a session. */
    internal fun failed(e: RuntimeException) = onEvent(UploadEvent.NotSent(UploadProblem.SEND_FAILED, listOf(e.message ?: e.javaClass.simpleName)))

    /**
     * Sends every waiting record, and again every batch that was handed over but never became an issue. Returns the
     * batches sent. Records of a format the pin does not let this program run wait, while the other formats are sent.
     */
    @Synchronized
    fun send(): Int {
        sent.refusal?.let {
            onEvent(UploadEvent.NotSent(UploadProblem.BATCH_REFUSED, listOf(it)))
            return 0
        }
        val unsent = sent.open().filter { it.issue == null }
        if (unsent.isEmpty() && journal.waiting().isEmpty()) return 0
        val token = when (val access = signIn.access()) {
            is UploadAccess.Ready -> access.token
            is UploadAccess.Unavailable -> {
                onEvent(UploadEvent.NotSent(access.reason, listOf(access.message)))
                return 0
            }
        }
        // The pin is read once; every format is checked against that same reading.
        val pinText = Memo(data)
        val epoch = when (val prepared = SessionPins.prepare(pinText, emptyList(), behaviorVersion)) {
            is SessionPin.Ready -> prepared.pin.behaviorVersion
            is SessionPin.Refused -> {
                onEvent(UploadEvent.NotSent(UploadProblem.PIN_REFUSED, listOf(prepared.reason.name) + prepared.values))
                return 0
            }
        }
        val formats = mutableMapOf<String, SessionPin.Ready?>()
        fun pinFor(format: String): SessionPin.Ready? = formats.getOrPut(format) {
            when (val prepared = SessionPins.prepare(pinText, listOf(format), behaviorVersion)) {
                is SessionPin.Ready -> prepared
                is SessionPin.Refused -> {
                    onEvent(UploadEvent.NotSent(UploadProblem.PIN_REFUSED, listOf(prepared.reason.name) + prepared.values))
                    null
                }
            }
        }
        var count = 0
        // A batch handed over before a crash, or whose send had no clear answer, is sent again under its own ID.
        for (batch in unsent) {
            val byId = journal.entries().associateBy { it.record.battleId }
            val records = batch.battleIds.mapNotNull { id ->
                val entry = byId[id] ?: return@mapNotNull null
                when (entry.state) {
                    RecordState.WAITING -> entry.record.also { journal.handOver(id) }
                    RecordState.HANDED_OVER -> entry.record
                    else -> null
                }
            }
            val pins = records.map { it.gameType }.distinct().associateWith { pinFor(it) }
            if (records.isEmpty() || pins.values.any { it == null }) {
                // Nothing of it is left to send, or a format is no longer pinned: its records wait again.
                records.forEach { journal.returnToWaiting(it.battleId) }
                sent.finish(batch.batchId)
                continue
            }
            if (!post(batch.batchId, records, pins.mapValues { it.value!! }, token)) return count
            count++
        }
        val setAside = journal.setAsideOtherEpochs(epoch)
        if (setAside.isNotEmpty()) onEvent(UploadEvent.SetAside(setAside))
        val waiting = journal.waiting().map { it.record }
        val withoutClient = waiting.filter { it.client.id.isBlank() }
        if (withoutClient.isNotEmpty()) onEvent(UploadEvent.NotSent(UploadProblem.NO_CLIENT_ID, withoutClient.map { it.battleId }))
        val sendable = waiting.filter { it.client.id.isNotBlank() && pinFor(it.gameType) != null }
        val pins = sendable.map { it.gameType }.distinct().associateWith { pinFor(it)!! }
        for (records in batches(sendable, pins)) {
            val batchId = newBatchId()
            sent.begin(batchId, records.map { it.battleId })
            records.forEach { journal.handOver(it.battleId) }
            if (!post(batchId, records, pins, token)) return count
            count++
        }
        return count
    }

    /** Reads the receipt of every batch sent and not yet answered, and moves each of its records by its receipt line. */
    @Synchronized
    fun collectReceipts() {
        val token = (signIn.access() as? UploadAccess.Ready)?.token
        for (batch in sent.open()) {
            val issue = batch.issue ?: continue
            val comments = when (val read = commentsOf(issue, token)) {
                is Comments.Read -> read.pages
                Comments.Gone -> {
                    // The issue was deleted or moved, so no receipt will come; the records are sent again, which is safe.
                    returnHandedOver(batch)
                    sent.finish(batch.batchId)
                    continue
                }
                Comments.Unreachable -> return
                Comments.NotNow -> continue
            }
            val byId = journal.entries().associateBy { it.record.battleId }
            fun handedOver(id: String) = byId[id]?.state == RecordState.HANDED_OVER
            when (val receipt = comments.firstNotNullOfOrNull { Receipts.read(it) }) {
                null -> continue
                is Receipt.Refused -> {
                    returnHandedOver(batch)
                    sent.finish(batch.batchId)
                    sent.refuse(receipt.reason)
                    onEvent(UploadEvent.BatchRefused(batch.batchId, issue, receipt.reason))
                }
                is Receipt.Processed -> {
                    for (id in batch.battleIds) {
                        if (!handedOver(id)) continue
                        when (val line = receipt.lines[id]) {
                            null -> journal.returnToWaiting(id) // The receipt does not name it, so it is sent again.
                            Receipts.ACCEPTED -> {
                                journal.accept(id)
                                onEvent(UploadEvent.RecordAccepted(id))
                            }
                            else -> {
                                journal.reject(id, MoveReason.RUMBLE_REJECTED, listOf(line))
                                onEvent(UploadEvent.RecordRejected(id, line))
                            }
                        }
                    }
                    sent.finish(batch.batchId)
                }
            }
        }
    }

    private fun returnHandedOver(batch: SentBatches.Batch) {
        val byId = journal.entries().associateBy { it.record.battleId }
        for (id in batch.battleIds) if (byId[id]?.state == RecordState.HANDED_OVER) journal.returnToWaiting(id)
    }

    private sealed interface Comments {
        class Read(val pages: List<String>) : Comments
        data object Gone : Comments
        data object Unreachable : Comments
        data object NotNow : Comments
    }

    /** The issue's comments, page by page, up to [MAX_COMMENT_PAGES]: the issues are public and anybody may comment before the receipt. */
    private fun commentsOf(issue: Int, token: String?): Comments {
        val pages = mutableListOf<String>()
        for (page in 1..MAX_COMMENT_PAGES) {
            val answer = try {
                api.get("$repository/issues/$issue/comments?per_page=100&page=$page", token)
            } catch (e: IOException) {
                return Comments.Unreachable
            }
            if (answer.status == 401 && token != null) signIn.refused()
            if (answer.status == 404 || answer.status == 410) return Comments.Gone
            if (answer.status != 200) return Comments.NotNow
            pages += answer.body
            val count = (try { Json.parseToJsonElement(answer.body) } catch (e: IllegalArgumentException) { null } as? JsonArray)?.size ?: 0
            if (count < 100) break
        }
        return Comments.Read(pages)
    }

    /**
     * Opens the batch's issue. A clear refusal returns its records to waiting and closes the batch. An answer that does
     * not say whether the issue was opened (no answer, a server error, a body that cannot be read) leaves the batch open
     * and handed over, so it is sent again under the same ID, which the Rumble answers like the first. Returns false when
     * the batch was not recorded as sent, so nothing more is sent now.
     */
    private fun post(batchId: String, records: List<RankedRecord>, pins: Map<String, SessionPin.Ready>, token: String): Boolean {
        val body = buildJsonObject {
            put("title", "$TITLE_PREFIX $batchId")
            put("body", "```json\n" + envelope(records, pins) + "\n```\n")
        }
        val answer = try {
            api.postJson("$repository/issues", token, body.toString())
        } catch (e: IOException) {
            onEvent(UploadEvent.NotSent(UploadProblem.SEND_FAILED, listOf(e.message ?: e.javaClass.simpleName)))
            return false
        }
        if (answer.status == 201) {
            val issue = try {
                ((Json.parseToJsonElement(answer.body) as? JsonObject)?.get("number") as? JsonPrimitive)?.contentOrNull?.toIntOrNull()
            } catch (e: IllegalArgumentException) {
                null
            }
            if (issue == null) {
                onEvent(UploadEvent.NotSent(UploadProblem.SEND_FAILED, listOf("HTTP 201 without an issue number")))
                return false
            }
            sent.sentAs(batchId, issue)
            onEvent(UploadEvent.BatchSent(batchId, issue, records.size))
            return true
        }
        val reason = when {
            answer.status == 401 -> UploadProblem.SIGN_IN_NEEDED.also { signIn.refused() }
            answer.status == 429 || (answer.status == 403 && answer.body.contains("rate limit", ignoreCase = true)) -> UploadProblem.RATE_LIMITED
            else -> UploadProblem.SEND_FAILED
        }
        if (answer.status in 400..499) {
            // GitHub said no, so no issue was opened: the records wait again.
            records.forEach { journal.returnToWaiting(it.battleId) }
            sent.finish(batchId)
        }
        onEvent(UploadEvent.NotSent(reason, listOf("HTTP ${answer.status}")))
        return false
    }

    /** The records in batches of one client each, at most [maxResults] records and [maxCharacters] characters of envelope. */
    internal fun batches(records: List<RankedRecord>, pins: Map<String, SessionPin.Ready>): List<List<RankedRecord>> {
        val result = mutableListOf<List<RankedRecord>>()
        for ((client, group) in records.groupBy { it.client }) {
            val frame = envelopeOf(client, emptyList()).length
            var current = mutableListOf<RankedRecord>()
            var size = frame
            for (record in group) {
                val text = submission(record, pins.getValue(record.gameType)).toString().length
                // Each record after the first adds a comma too.
                if (current.isNotEmpty() && (current.size + 1 > maxResults || size + text + 1 > maxCharacters)) {
                    result += current
                    current = mutableListOf()
                    size = frame
                }
                size += text + if (current.isEmpty()) 0 else 1
                current += record
            }
            if (current.isNotEmpty()) result += current
        }
        return result
    }

    /** The submission envelope of [records], which share one client: the shape the result data validates. */
    internal fun envelope(records: List<RankedRecord>, pins: Map<String, SessionPin.Ready>): String =
        envelopeOf(records.first().client, records.map { submission(it, pins.getValue(it.gameType)) })

    private fun envelopeOf(client: JournalClient, results: List<JsonElement>): String =
        buildJsonObject {
            put("schemaVersion", 1)
            put("clientId", client.id)
            put("clientVersion", client.version)
            putJsonArray("results") { results.forEach { add(it) } }
        }.toString()

    private fun submission(record: RankedRecord, pin: SessionPin.Ready): JsonElement {
        val battlefield = pin.pin.gameTypes.getValue(record.gameType).battlefield
        val team = record.gameType == "twinduel"
        return buildJsonObject {
            put("battleId", record.battleId)
            put("completedAt", record.timestamp)
            putJsonObject("client") { put("id", record.client.id); put("version", record.client.version) }
            putJsonObject("engine") { put("behaviorVersion", record.engine.behaviorVersion) }
            put("gameType", record.gameType)
            put("rounds", record.rounds)
            put("arenaWidth", battlefield[0])
            put("arenaHeight", battlefield[1])
            putJsonArray("participants") {
                for (p in record.participants) {
                    add(buildJsonObject {
                        put("name", p.name); put("version", p.version); put("isTeam", team); put("rank", p.rank)
                        put("totalScore", p.totalScore); put("survival", p.survival); put("lastSurvivorBonus", p.lastSurvivorBonus)
                        put("bulletDamage", p.bulletDamage); put("bulletKillBonus", p.bulletKillBonus); put("ramDamage", p.ramDamage)
                        put("ramKillBonus", p.ramKillBonus); put("firstPlaces", p.firstPlaces); put("secondPlaces", p.secondPlaces)
                        put("thirdPlaces", p.thirdPlaces)
                    })
                }
            }
        }
    }

    /** Reads each file of the result repository once, so every format of one send is checked against the same pin. */
    private class Memo(private val source: ResultDataSource) : ResultDataSource {
        private val read = mutableMapOf<String, String?>()
        override fun fileText(path: String): String? = read.getOrPut(path) { source.fileText(path) }
    }

    companion object {
        const val TITLE_PREFIX = "[result]"
        const val MAX_RESULTS = 60

        /** Well below GitHub's limit of 65536 characters for an issue body; a batch of 60 results is about 45000. */
        const val MAX_CHARACTERS = 50_000

        /** At most this many pages of a hundred comments are read to find a receipt. */
        private const val MAX_COMMENT_PAGES = 10
    }
}

/** What a receipt on a result issue says. */
internal sealed interface Receipt {
    /** Each record's line, by battle ID: [Receipts.ACCEPTED] or the reason it was rejected. */
    data class Processed(val lines: Map<String, String>) : Receipt

    /** The batch was refused before its records could be read. */
    data class Refused(val reason: String) : Receipt
}

internal object Receipts {
    const val ACCEPTED = "accepted"
    private const val PROCESSED = "Processed result batch:"
    private const val REFUSED = "Rejected result batch before individual records could be read:"

    /** Only the receiving workflow's own comments count: the issues are public and anybody can comment. */
    private const val WORKFLOW_ACCOUNT = "github-actions[bot]"
    private val line = Regex("""^([0-9A-Za-z-]+): (accepted|rejected: (.*))$""")

    /** The receipt among the issue's [comments], or null when there is none yet. */
    fun read(comments: String): Receipt? {
        val list = try {
            Json.parseToJsonElement(comments) as? JsonArray
        } catch (e: IllegalArgumentException) {
            null
        } ?: return null
        val bodies = list.mapNotNull { it as? JsonObject }
            .filter { ((it["user"] as? JsonObject)?.get("login") as? JsonPrimitive)?.contentOrNull == WORKFLOW_ACCOUNT }
            .mapNotNull { (it["body"] as? JsonPrimitive)?.contentOrNull?.trimStart() }
        bodies.firstOrNull { it.startsWith(REFUSED) }?.let { body ->
            return Receipt.Refused(blockOf(body).firstOrNull { it.isNotBlank() }?.trim()?.take(300) ?: "The batch was refused.")
        }
        val processed = bodies.firstOrNull { it.startsWith(PROCESSED) } ?: return null
        val lines = blockOf(processed).mapNotNull { line.matchEntire(it.trim()) }
            .associate { it.groupValues[1] to (if (it.groupValues[2] == ACCEPTED) ACCEPTED else it.groupValues[3].take(300)) }
        return Receipt.Processed(lines)
    }

    /** The lines inside the comment's fenced block. */
    private fun blockOf(body: String): List<String> =
        body.substringAfter("```text", "").substringBefore("```").lines()
}

/**
 * Decides when a session sends and collects receipts: between battles, when enough results wait or some time has passed,
 * so a battle is never held up by more than the calls themselves.
 */
class AutomaticUpload(
    private val uploader: ResultUploader,
    private val sendAt: Int = 20,
    private val sendAfter: Duration = Duration.ofMinutes(15),
    private val receiptsAfter: Duration = Duration.ofMinutes(2),
    /** The least time between two sends, so a send that fails is not tried again at every look of a waiting session. */
    private val sendGap: Duration = Duration.ofMinutes(1),
    private val clock: () -> Instant = Instant::now,
) {
    private var lastSend: Instant? = null
    private var lastReceipts: Instant? = null

    /**
     * Called between battles and while the session waits. Sends when [sendAt] results wait or [sendAfter] has passed since
     * the last send, never twice within [sendGap], and collects receipts every [receiptsAfter].
     */
    fun betweenBattles() {
        val now = clock()
        val last = lastSend ?: now.also { lastSend = it }
        val gapPassed = !now.isBefore(last.plus(sendGap))
        val enough = uploader.waitingCount() >= sendAt && (gapPassed || last == now)
        if (enough || !now.isBefore(last.plus(sendAfter))) {
            guarded { uploader.send() }
            lastSend = now
        }
        val receipts = lastReceipts
        if (receipts == null || !now.isBefore(receipts.plus(receiptsAfter))) {
            guarded { uploader.collectReceipts() }
            lastReceipts = now
        }
    }

    /** Called when the session ends: everything waiting is sent and the receipts that are there are read. */
    fun finish() {
        guarded { uploader.send() }
        guarded { uploader.collectReceipts() }
    }

    /**
     * A failure of sending is reported and never ends the session, because the records are safe in the journal and are
     * sent at a later try. A journal that cannot be written is not such a failure and still ends it.
     */
    private fun guarded(work: () -> Unit) {
        try {
            work()
        } catch (e: RuntimeException) {
            uploader.failed(e)
        }
    }
}
