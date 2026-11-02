package dev.robocode.tankroyale.rumble.protocol

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.contentOrNull

/** The message schema version this library writes and understands. */
const val SUPPORTED_SCHEMA_VERSION = 1

/** One bot in a job: the host has already placed it in [folder] and verified it against [sourceHash]. */
@Serializable
data class JobBot(val name: String, val version: String, val sourceHash: String, val folder: String)

/**
 * What the worker is asked to run: one battle. [behaviorVersion] is the engine pin, the behavior version the running
 * server must report; [gameType] is the display name of a game type, for example `classic` or `1v1`.
 */
@Serializable
data class Job(
    val schemaVersion: Int = SUPPORTED_SCHEMA_VERSION,
    val jobId: String,
    val behaviorVersion: Int,
    val gameType: String,
    val numberOfRounds: Int,
    val bots: List<JobBot>,
)

/** One bot's result in a finished battle. */
@Serializable
data class BotOutcome(
    val name: String,
    val version: String,
    val isTeam: Boolean,
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

/** What the worker tells the host. Every event carries the schema version it is written in. */
@Serializable
sealed interface WorkerEvent {
    val schemaVersion: Int

    /** The worker accepted the job and is about to start the battle. */
    @Serializable
    @SerialName("started")
    data class Started(override val schemaVersion: Int = SUPPORTED_SCHEMA_VERSION, val jobId: String) : WorkerEvent

    /** The battle finished; [bots] are in final rank order. */
    @Serializable
    @SerialName("result")
    data class Result(
        override val schemaVersion: Int = SUPPORTED_SCHEMA_VERSION,
        val jobId: String,
        val numberOfRounds: Int,
        val bots: List<BotOutcome>,
    ) : WorkerEvent

    /** The job was not run because of what it asked: see [ErrorCode]. [jobId] is null when it could not be read. */
    @Serializable
    @SerialName("refused")
    data class Refused(
        override val schemaVersion: Int = SUPPORTED_SCHEMA_VERSION,
        val jobId: String?,
        val code: String,
        val message: String,
    ) : WorkerEvent

    /** The job was accepted but the battle did not complete: see [ErrorCode]. */
    @Serializable
    @SerialName("failed")
    data class Failed(
        override val schemaVersion: Int = SUPPORTED_SCHEMA_VERSION,
        val jobId: String?,
        val code: String,
        val message: String,
    ) : WorkerEvent

    /** The last event of a run that completed; nothing follows it. */
    @Serializable
    @SerialName("finished")
    data class Finished(override val schemaVersion: Int = SUPPORTED_SCHEMA_VERSION, val jobId: String) : WorkerEvent
}

/** The stable codes in refused and failed events. A caller branches on these, never on the message text. */
object ErrorCode {
    const val UNKNOWN_VERSION = "unknown-version"
    const val INVALID_JOB = "invalid-job"
    const val UNKNOWN_GAME_TYPE = "unknown-game-type"
    const val PIN_MISMATCH = "pin-mismatch"
    const val BATTLE_FAILED = "battle-failed"
    const val INTERNAL_ERROR = "internal-error"
}

/** The exit codes of the worker, which repeat the outcome for a caller that reads no events. */
object ExitCode {
    const val FINISHED = 0
    const val FAILED = 1
    const val REFUSED = 2
}

/** Raised when a line cannot be read as a message; [code] is an [ErrorCode] and [message] says what was wrong. */
class MessageRejected(val code: String, message: String) : Exception(message, null, false, false)

/** Writes and reads messages, one per line. */
object MessageCodec {
    private val json = Json {
        classDiscriminator = "type"
        encodeDefaults = true
        ignoreUnknownKeys = true
    }

    fun write(job: Job): String = json.encodeToString(Job.serializer(), job)

    fun write(event: WorkerEvent): String = json.encodeToString(WorkerEvent.serializer(), event)

    fun readJob(line: String): Job {
        val tree = checkedObject(line)
        return decode { json.decodeFromJsonElement(Job.serializer(), tree) }.also {
            if (it.jobId.isBlank() || it.bots.isEmpty() || it.numberOfRounds < 1 || it.behaviorVersion < 1) {
                throw MessageRejected(ErrorCode.INVALID_JOB, "The job needs an id, a behavior version, at least one round and at least one bot.")
            }
        }
    }

    fun readEvent(line: String): WorkerEvent {
        val tree = checkedObject(line)
        return decode { json.decodeFromJsonElement(WorkerEvent.serializer(), tree) }
    }

    private fun <T> decode(block: () -> T): T =
        try {
            block()
        } catch (e: SerializationException) {
            throw MessageRejected(ErrorCode.INVALID_JOB, "The message is not valid: ${e.message?.lineSequence()?.firstOrNull()}")
        } catch (e: IllegalArgumentException) {
            throw MessageRejected(ErrorCode.INVALID_JOB, "The message is not valid: ${e.message?.lineSequence()?.firstOrNull()}")
        }

    /** Parses [line] as an object and refuses a schema version this library does not understand. */
    private fun checkedObject(line: String): JsonObject {
        val tree = try {
            json.parseToJsonElement(line) as? JsonObject
        } catch (e: SerializationException) {
            null
        } ?: throw MessageRejected(ErrorCode.INVALID_JOB, "The message is not a JSON object on one line.")
        val version = (tree["schemaVersion"] as? JsonPrimitive)?.contentOrNull?.toIntOrNull()
            ?: throw MessageRejected(ErrorCode.INVALID_JOB, "The message has no schema version.")
        if (version != SUPPORTED_SCHEMA_VERSION) {
            throw MessageRejected(
                ErrorCode.UNKNOWN_VERSION,
                "The message uses schema version $version, but this program understands version $SUPPORTED_SCHEMA_VERSION. Update the program.",
            )
        }
        return tree
    }
}
