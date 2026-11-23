package dev.robocode.tankroyale.rumble

import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import java.io.IOException
import java.nio.ByteBuffer
import java.nio.channels.FileChannel
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardOpenOption
import java.time.Instant

/** Who ran the battle: the contributor's registered client and the version of the program. */
@Serializable
data class JournalClient(val id: String, val version: String)

/** The engine that ran the battle. Only [behaviorVersion] decides whether a record can be sent with the Rumble's current pin. */
@Serializable
data class JournalEngine(val behaviorVersion: Int, val serverVersion: String, val runnerVersion: String)

/** One bot's result in a completed battle, as the result data expects it. Score shares are never stored. */
@Serializable
data class JournalParticipant(
    val name: String,
    val version: String,
    val sourceHash: String,
    val rank: Int,
    val totalScore: Int,
    val survival: Int,
    val lastSurvivorBonus: Int,
    val bulletDamage: Int,
    val bulletKillBonus: Int,
    val ramDamage: Int,
    val ramKillBonus: Int,
    val firstPlaces: Int,
    val secondPlaces: Int,
    val thirdPlaces: Int,
)

/** One completed ranked battle. A battle that did not complete is never a record. */
@Serializable
data class RankedRecord(
    val schemaVersion: Int = SUPPORTED_SCHEMA_VERSION,
    val battleId: String,
    val gameType: String,
    /** When the battle completed, as an ISO-8601 instant. */
    val timestamp: String,
    val client: JournalClient,
    val engine: JournalEngine,
    /** The commit of the bot repository whose catalog the bots came from. */
    val botsRepoCommit: String,
    val rounds: Int,
    val participants: List<JournalParticipant>,
    /** SHA-256 of the replay kept as evidence, when there is one. */
    val replayHash: String? = null,
) {
    companion object {
        const val SUPPORTED_SCHEMA_VERSION = 1
    }
}

/** Where a kept record stands. A record is never removed; it only moves on. */
enum class RecordState {
    /** Kept and not yet sent. */
    WAITING,

    /** Put into a batch for the contributor to send. */
    HANDED_OVER,

    /** The Rumble published it and said so in a receipt. */
    ACCEPTED,

    /** The Rumble refused it; it stays available for diagnosis and retry. */
    REJECTED,

    /** Not sent, with a reason code; it stays available. */
    SET_ASIDE,
}

/** Codes for why a record moved on without a receipt. */
object MoveReason {
    /** The record was made on another behavior version than the Rumble's current pin. */
    const val ENGINE_EPOCH = "engine-epoch"

    /** The Rumble's receipt rejected the record; the detail is the receipt's reason. */
    const val RUMBLE_REJECTED = "rumble-rejected"
}

/** A record together with where it stands. */
data class JournalEntry(val record: RankedRecord, val state: RecordState, val reason: String?, val detail: List<String>)

/**
 * A result as the journal keeps it, read without opening the journal for writing: its state, the reason and detail of
 * its last move, and every time it was handed over to be sent, oldest first, as ISO-8601 instants. A result is handed
 * over again when it is sent again in a later batch, so the n-th time belongs to the n-th batch that lists it.
 */
data class KeptResult(val battleId: String, val state: RecordState, val reason: String?, val detail: List<String>, val handedOver: List<String>)

/** What adding a record did. */
sealed interface AddResult {
    /** The record is written, flushed to the disk and kept. */
    data object Kept : AddResult

    /** A record with this battle ID was kept before; nothing was written. */
    data object AlreadyKept : AddResult
}

/** What reading the journal found besides the records. */
data class JournalReadReport(
    /** Line numbers (from 1) of lines that are not a record or a move, for example the torn end of an interrupted write. */
    val unreadableLines: List<Int>,
    /** True when the file ends in the middle of a line, which is what an interrupted write leaves. */
    val tornEnd: Boolean,
)

/** Raised when the journal cannot be used, with a message the person can act on. */
class JournalUnusable(message: String, cause: Throwable? = null) : Exception(message, cause)

@Serializable
private data class JournalLine(
    val kind: String,
    val schemaVersion: Int? = null,
    val environment: String? = null,
    val record: RankedRecord? = null,
    val battleId: String? = null,
    val state: String? = null,
    val reason: String? = null,
    val detail: List<String> = emptyList(),
    val at: String? = null,
)

/**
 * The durable journal of completed ranked battles of one Rumble environment.
 *
 * It is a file of lines that is only ever added to: a header, then a record for each completed battle, then a move line
 * each time a record moves on. Nothing already written is changed, so an interrupted write can leave at most one torn line
 * at the end, which is never taken for a record and never costs an earlier one. A record is reported kept only after its
 * line is written and flushed to the disk.
 *
 * There is no way to keep, drop or hide a record by how well the battle went: a record leaves the waiting state only by
 * being handed over for a batch or set aside for its engine epoch, and it comes back to waiting after a failure.
 *
 * The line is flushed with the file's own flush, which the operating system is asked for; the entry of a new file in its
 * folder is not synced. A write that fails is taken back, so a later line is never glued onto half of an earlier one.
 *
 * One program writes a journal at a time; the file is not locked against two writers.
 */
class RankedJournal private constructor(
    val environment: RumbleEnvironment,
    private val file: Path,
    private val clock: () -> Instant,
    private val hooks: JournalHooks,
) {
    /** Set when a failed write could not be taken back; the journal then refuses every write until it is opened again. */
    private var unusable: String? = null

    private val order = mutableListOf<String>()
    private val records = mutableMapOf<String, RankedRecord>()
    private val states = mutableMapOf<String, Triple<RecordState, String?, List<String>>>()
    private var report = JournalReadReport(emptyList(), false)

    /** All records, oldest first, each with where it stands. */
    @Synchronized
    fun entries(): List<JournalEntry> = order.map { id ->
        val (state, reason, detail) = states.getValue(id)
        JournalEntry(records.getValue(id), state, reason, detail)
    }

    /** The records that are kept and not yet sent. */
    @Synchronized
    fun waiting(): List<JournalEntry> = entries().filter { it.state == RecordState.WAITING }

    /** What reading found besides the records, as it was when the journal was opened. */
    @Synchronized
    fun readReport(): JournalReadReport = report

    /** Keeps [record]: written, flushed to the disk, and only then returned. */
    @Synchronized
    fun add(record: RankedRecord): AddResult {
        if (record.battleId in records) return AddResult.AlreadyKept
        require(record.schemaVersion == RankedRecord.SUPPORTED_SCHEMA_VERSION) { "This program keeps records of schema version ${RankedRecord.SUPPORTED_SCHEMA_VERSION}." }
        append(JournalLine(kind = "record", record = record))
        order += record.battleId
        records[record.battleId] = record
        states[record.battleId] = Triple(RecordState.WAITING, null, emptyList())
        return AddResult.Kept
    }

    /** A record is put into a batch that the contributor will send. */
    fun handOver(battleId: String) = move(battleId, RecordState.HANDED_OVER, null, emptyList(), setOf(RecordState.WAITING))

    /** The batch was not sent, or was interrupted: the record waits again. */
    fun returnToWaiting(battleId: String) =
        move(battleId, RecordState.WAITING, null, emptyList(), setOf(RecordState.HANDED_OVER, RecordState.REJECTED))

    /** The Rumble published the record's facts and gave a successful receipt for it. */
    fun accept(battleId: String) = move(battleId, RecordState.ACCEPTED, null, emptyList(), setOf(RecordState.HANDED_OVER))

    /** The Rumble refused the record; [reason] is the code it gave. The record stays available. */
    fun reject(battleId: String, reason: String, detail: List<String> = emptyList()) =
        move(battleId, RecordState.REJECTED, reason, detail, setOf(RecordState.HANDED_OVER))

    /**
     * Sets aside every waiting record that was made on another behavior version than [currentBehaviorVersion], the Rumble's
     * current pin, so incompatible facts are never sent. Returns the battle IDs set aside.
     */
    @Synchronized
    fun setAsideOtherEpochs(currentBehaviorVersion: Int): List<String> {
        val ids = order.filter { states.getValue(it).first == RecordState.WAITING && records.getValue(it).engine.behaviorVersion != currentBehaviorVersion }
        for (id in ids) {
            val epoch = records.getValue(id).engine.behaviorVersion
            move(id, RecordState.SET_ASIDE, MoveReason.ENGINE_EPOCH, listOf(epoch.toString(), currentBehaviorVersion.toString()), setOf(RecordState.WAITING))
        }
        return ids
    }

    @Synchronized
    private fun move(battleId: String, to: RecordState, reason: String?, detail: List<String>, from: Set<RecordState>) {
        val current = states[battleId]?.first ?: throw JournalUnusable("The journal holds no record of the battle $battleId.")
        if (current !in from) throw JournalUnusable("A record that is $current cannot become $to.")
        append(JournalLine(kind = "move", battleId = battleId, state = to.name, reason = reason, detail = detail, at = clock().toString()))
        states[battleId] = Triple(to, reason, detail)
    }

    private fun append(line: JournalLine) =
        appendBytes((json.encodeToString(JournalLine.serializer(), line) + "\n").toByteArray(StandardCharsets.UTF_8))

    private fun appendBytes(bytes: ByteArray) {
        unusable?.let { throw JournalUnusable(it) }
        try {
            FileChannel.open(file, StandardOpenOption.WRITE, StandardOpenOption.APPEND).use { channel ->
                val before = channel.size()
                try {
                    val buffer = ByteBuffer.wrap(bytes)
                    while (buffer.hasRemaining()) hooks.write(channel, buffer)
                    hooks.force(channel)
                } catch (e: IOException) {
                    // Whatever part of the line reached the file is taken back, so that the next line is never glued onto it.
                    try {
                        channel.truncate(before)
                    } catch (undo: IOException) {
                        unusable = "The journal holds a half-written line that could not be removed. Close the program and open the journal again."
                    }
                    throw e
                }
            }
        } catch (e: IOException) {
            throw JournalUnusable("The journal could not be written: ${e.message}", e)
        }
    }

    private fun load() {
        val bytes = Files.readAllBytes(file)
        val tornEnd = bytes.isNotEmpty() && bytes.last() != '\n'.code.toByte()
        val text = String(bytes, StandardCharsets.UTF_8)
        val unreadable = mutableListOf<Int>()
        text.split('\n').forEachIndexed { index, raw ->
            if (raw.isBlank()) return@forEachIndexed
            val line = try {
                json.decodeFromString(JournalLine.serializer(), raw)
            } catch (e: SerializationException) {
                unreadable += index + 1
                return@forEachIndexed
            } catch (e: IllegalArgumentException) {
                unreadable += index + 1
                return@forEachIndexed
            }
            when (line.kind) {
                "header" -> Unit
                "record" -> line.record?.let {
                    if (it.battleId !in records) {
                        order += it.battleId
                        records[it.battleId] = it
                        states[it.battleId] = Triple(RecordState.WAITING, null, emptyList())
                    }
                } ?: unreadable.add(index + 1)
                "move" -> {
                    val state = line.state?.let { s -> RecordState.entries.firstOrNull { it.name == s } }
                    val id = line.battleId
                    if (id != null && state != null && id in records) states[id] = Triple(state, line.reason, line.detail) else unreadable += index + 1
                }
                else -> unreadable += index + 1
            }
        }
        report = JournalReadReport(unreadable, tornEnd)
        // A torn end is closed with a newline so that the next line is never glued onto it; the torn bytes stay in the file.
        if (tornEnd) appendBytes(byteArrayOf('\n'.code.toByte()))
    }

    companion object {
        private val json = Json { ignoreUnknownKeys = true; encodeDefaults = true }

        private fun headerLine(environment: RumbleEnvironment): ByteArray {
            val header = JournalLine(kind = "header", schemaVersion = 1, environment = environment.name)
            return (json.encodeToString(JournalLine.serializer(), header) + "\n").toByteArray(StandardCharsets.UTF_8)
        }

        /**
         * Reads the journal of [environment] in [file] and never writes to it, so it can be read while a contribution
         * session adds to it: a last line without its newline is one still being written and is left out, a line that
         * cannot be read is skipped, and a missing file is an empty journal. A journal made for another environment is
         * refused with [EnvironmentRefused]; a file that is not a journal with [JournalUnusable].
         */
        fun read(environment: RumbleEnvironment, file: Path): List<KeptResult> {
            if (Files.notExists(file)) return emptyList()
            val bytes = try {
                Files.readAllBytes(file)
            } catch (e: IOException) {
                throw JournalUnusable("The journal could not be read: ${e.message}", e)
            }
            val whole = bytes.lastIndexOf('\n'.code.toByte()) + 1
            val lines = String(bytes, 0, whole, StandardCharsets.UTF_8).split('\n').filter { it.isNotBlank() }
            if (lines.isEmpty()) return emptyList()
            val header = try {
                json.decodeFromString(JournalLine.serializer(), lines.first())
            } catch (e: SerializationException) {
                throw JournalUnusable("The file does not start like a Rumble journal.", e)
            } catch (e: IllegalArgumentException) {
                throw JournalUnusable("The file does not start like a Rumble journal.", e)
            }
            if (header.kind != "header" || header.environment == null) throw JournalUnusable("The file does not start like a Rumble journal.")
            if (header.schemaVersion != 1) throw JournalUnusable("The journal uses schema version ${header.schemaVersion}, which this program does not know. Update the program.")
            if (header.environment != environment.name) {
                throw EnvironmentRefused("This journal belongs to the ${header.environment} Rumble, not the ${environment.displayName} Rumble.")
            }
            val order = mutableListOf<String>()
            val kept = mutableMapOf<String, KeptResult>()
            for (raw in lines.drop(1)) {
                val line = try {
                    json.decodeFromString(JournalLine.serializer(), raw)
                } catch (e: SerializationException) {
                    continue
                } catch (e: IllegalArgumentException) {
                    continue
                }
                when (line.kind) {
                    "record" -> line.record?.takeIf { it.battleId !in kept }?.let {
                        order += it.battleId
                        kept[it.battleId] = KeptResult(it.battleId, RecordState.WAITING, null, emptyList(), emptyList())
                    }
                    "move" -> {
                        val state = line.state?.let { s -> RecordState.entries.firstOrNull { it.name == s } } ?: continue
                        val before = kept[line.battleId ?: continue] ?: continue
                        val handedOver = if (state == RecordState.HANDED_OVER) before.handedOver + line.at.orEmpty() else before.handedOver
                        kept[before.battleId] = KeptResult(before.battleId, state, line.reason, line.detail, handedOver)
                    }
                }
            }
            return order.map { kept.getValue(it) }
        }

        /**
         * Opens the journal of [environment] in [file], making it with its header when it does not exist. A journal made for
         * another environment is refused, so a record made for the test Rumble is never sent to the public one.
         */
        fun open(environment: RumbleEnvironment, file: Path, clock: () -> Instant = Instant::now): RankedJournal =
            openWith(environment, file, clock, JournalHooks())

        internal fun openWith(environment: RumbleEnvironment, file: Path, clock: () -> Instant, hooks: JournalHooks): RankedJournal {
            val journal = RankedJournal(environment, file, clock, hooks)
            try {
                file.parent?.let { Files.createDirectories(it) }
                val header = headerLine(environment)
                val existing = if (Files.exists(file)) Files.readAllBytes(file) else ByteArray(0)
                // A file that holds only the start of this environment's own header is what a write cut short leaves; no record
                // can be in it yet, so the header is written again. Any other file is never overwritten.
                val tornHeader = existing.isNotEmpty() && '\n'.code.toByte() !in existing &&
                    String(header, StandardCharsets.UTF_8).startsWith(String(existing, StandardCharsets.UTF_8))
                if (existing.isEmpty() || tornHeader) {
                    FileChannel.open(file, StandardOpenOption.CREATE, StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING).use {
                        it.write(ByteBuffer.wrap(header))
                        it.force(true)
                    }
                }
                journal.checkHeader()
                journal.load()
            } catch (e: IOException) {
                throw JournalUnusable("The journal could not be opened: ${e.message}", e)
            }
            return journal
        }
    }

    private fun checkHeader() {
        val first = Files.newBufferedReader(file, StandardCharsets.UTF_8).use { it.readLine() }.orEmpty()
        val header = try {
            json.decodeFromString(JournalLine.serializer(), first)
        } catch (e: SerializationException) {
            throw JournalUnusable("The file does not start like a Rumble journal.", e)
        }
        if (header.kind != "header" || header.environment == null) throw JournalUnusable("The file does not start like a Rumble journal.")
        if (header.schemaVersion != 1) throw JournalUnusable("The journal uses schema version ${header.schemaVersion}, which this program does not know. Update the program.")
        if (header.environment != environment.name) {
            throw EnvironmentRefused("This journal belongs to the ${header.environment} Rumble, not the ${environment.displayName} Rumble.")
        }
    }
}

/** What the journal does to the file; the tests replace them to let a write fail half way. */
internal class JournalHooks(
    val write: (FileChannel, ByteBuffer) -> Unit = { channel, buffer -> channel.write(buffer) },
    val force: (FileChannel) -> Unit = { channel -> channel.force(true) },
)
