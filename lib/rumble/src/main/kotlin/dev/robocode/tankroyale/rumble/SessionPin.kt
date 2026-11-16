package dev.robocode.tankroyale.rumble

import dev.robocode.tankroyale.common.rules.CURRENT_BEHAVIOR_VERSION
import dev.robocode.tankroyale.common.rules.GAME_TYPE_PRESETS
import dev.robocode.tankroyale.common.rules.GameType
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import java.io.IOException
import java.io.UncheckedIOException
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.time.Duration
import java.time.Instant

/** Reads files of an environment's result repository. */
interface ResultDataSource {
    /**
     * The text of the file at [path] on the result repository's main branch, or null when there is no such file. Throws
     * [IOException] or [CatalogUnreadable] when the file cannot be read now.
     */
    fun fileText(path: String): String?
}

/** Reads the public result repository of an environment from GitHub. No credential is needed or sent. */
class GitHubResultDataSource(
    private val environment: RumbleEnvironment,
    private val client: HttpClient = HttpClient.newBuilder()
        .followRedirects(HttpClient.Redirect.NORMAL)
        .connectTimeout(Duration.ofSeconds(15))
        .build(),
) : ResultDataSource {

    override fun fileText(path: String): String? {
        val request = HttpRequest.newBuilder(fileUrl(path)).timeout(Duration.ofSeconds(60))
            .header("User-Agent", "robocode-tank-royale").GET().build()
        val response = client.send(request, HttpResponse.BodyHandlers.ofString())
        if (response.statusCode() == 404) return null
        if (response.statusCode() != 200) throw CatalogUnreadable("The Rumble result file $path could not be fetched (HTTP ${response.statusCode()}).")
        return response.body()
    }

    internal fun fileUrl(path: String): URI {
        if (!pathPattern.matches(path)) throw CatalogUnreadable("'$path' is not a repository file path.")
        return URI.create("https://raw.githubusercontent.com/${RumbleEnvironments.owner}/${environment.dataRepository}/main/$path")
    }

    private companion object {
        val pathPattern = Regex("[A-Za-z0-9._-]+(?:/[A-Za-z0-9._-]+)*")
    }
}

private sealed interface ResultFetch {
    /** The file's text, or null when there is no such file. */
    data class Text(val text: String?) : ResultFetch
    data class Failed(val message: String) : ResultFetch
}

/** Fetches a result file, turning every way it can fail now into [ResultFetch.Failed]. */
private fun fetchResultFile(source: ResultDataSource, path: String): ResultFetch =
    try {
        ResultFetch.Text(source.fileText(path))
    } catch (e: IOException) {
        ResultFetch.Failed(e.message.orEmpty())
    } catch (e: UncheckedIOException) {
        ResultFetch.Failed(e.message.orEmpty())
    } catch (e: CatalogUnreadable) {
        ResultFetch.Failed(e.message.orEmpty())
    } catch (e: InterruptedException) {
        // Cancelling the session interrupts its thread; the interruption is kept for the session to see.
        Thread.currentThread().interrupt()
        ResultFetch.Failed("The read was interrupted.")
    }

/** One game format as the Rumble pins it. */
@Serializable
data class PinnedFormat(val rounds: Int, val battlefield: List<Int>, val participants: Int)

/** The Rumble's engine pin, `engine.json` of the result repository. Fields this program does not use are ignored. */
@Serializable
data class EnginePin(val schemaVersion: Int, val behaviorVersion: Int, val gameTypes: Map<String, PinnedFormat>) {
    companion object {
        const val PATH = "engine.json"
        const val SUPPORTED_SCHEMA_VERSION = 1
    }
}

/** Why a session cannot run under the Rumble's engine pin. The values name what differs; the screens say what to do. */
enum class PinRefusal {
    /** The pin could not be fetched now. Values: the reason. */
    UNREADABLE,

    /** The result repository holds no pin. */
    MISSING,

    /** The pin is not the shape this program reads, or holds a value that cannot be. Values: the reason. */
    NOT_UNDERSTOOD,

    /** Values: the pin's schema version, and the one this program reads. */
    UNSUPPORTED_SCHEMA,

    /** Values: the pin's behavior version, and this program's. Running would make results the Rumble rejects. */
    BEHAVIOR_VERSION_DIFFERS,

    /** Values: the chosen format the pin does not name. */
    FORMAT_NOT_PINNED,

    /** Values: the format, the pin's battle (`800x600, 2 bots`), and what this program runs for it, or nothing when it runs no such format. */
    FORMAT_NOT_RUNNABLE,
}

/** A session's engine pin, ready to run or refused before any battle. */
sealed interface SessionPin {
    /** Every chosen format can run under [pin]; the session takes its rounds, battle sizes and behavior version from it. */
    data class Ready(val pin: EnginePin, val formats: List<String>) : SessionPin {
        val roundsPerFormat: Map<String, Int> get() = formats.associateWith { pin.gameTypes.getValue(it).rounds }
        val participantCounts: Map<String, Int> get() = formats.associateWith { pin.gameTypes.getValue(it).participants }

        /** The engine of each record: the pin's behavior version and the versions of this program. */
        fun engine(serverVersion: String, runnerVersion: String) = JournalEngine(pin.behaviorVersion, serverVersion, runnerVersion)
    }

    data class Refused(val reason: PinRefusal, val values: List<String> = emptyList()) : SessionPin
}

object SessionPins {
    private val json = Json { ignoreUnknownKeys = true }

    /** The game type the battle worker runs for each Rumble format. */
    private val workerFormats = mapOf("1v1" to GameType.ONE_VS_ONE, "twinduel" to GameType.TWIN_DUEL, "melee" to GameType.MELEE)

    /** Reads the pin from [source] and checks that this program can run every one of [formats] under it. */
    /** The settings of a pinned format this program runs; a chosen format that pins any other setting is not understood. */
    private val formatSettings = setOf("rounds", "battlefield", "participants")

    fun prepare(source: ResultDataSource, formats: List<String>, behaviorVersion: Int = CURRENT_BEHAVIOR_VERSION): SessionPin {
        val text = when (val fetched = fetchResultFile(source, EnginePin.PATH)) {
            is ResultFetch.Failed -> return SessionPin.Refused(PinRefusal.UNREADABLE, listOf(fetched.message))
            is ResultFetch.Text -> fetched.text ?: return SessionPin.Refused(PinRefusal.MISSING)
        }
        val pin = try {
            json.decodeFromString(EnginePin.serializer(), text)
        } catch (e: IllegalArgumentException) {
            // A SerializationException is one of these too.
            return SessionPin.Refused(PinRefusal.NOT_UNDERSTOOD, listOf(e.message.orEmpty()))
        }
        // A rule the worker would not apply, such as a turn timeout, must not pass as pinned when the battles run another.
        val pinnedSettings = json.parseToJsonElement(text).jsonObject["gameTypes"]?.jsonObject.orEmpty()
        for (name in formats) {
            val unknown = pinnedSettings[name]?.jsonObject?.keys.orEmpty() - formatSettings
            if (unknown.isNotEmpty()) return SessionPin.Refused(PinRefusal.NOT_UNDERSTOOD, listOf("game type $name pins ${unknown.sorted().joinToString()}"))
        }
        return check(pin, formats, behaviorVersion)
    }

    internal fun check(pin: EnginePin, formats: List<String>, behaviorVersion: Int): SessionPin {
        if (pin.schemaVersion != EnginePin.SUPPORTED_SCHEMA_VERSION) {
            return SessionPin.Refused(PinRefusal.UNSUPPORTED_SCHEMA, listOf(pin.schemaVersion.toString(), EnginePin.SUPPORTED_SCHEMA_VERSION.toString()))
        }
        if (pin.behaviorVersion < 1) return SessionPin.Refused(PinRefusal.NOT_UNDERSTOOD, listOf("behaviorVersion ${pin.behaviorVersion}"))
        // Only the chosen formats matter: a format nobody chose never refuses a session.
        for (name in formats) {
            val format = pin.gameTypes[name] ?: continue
            if (format.rounds < 1 || format.participants < 2 || format.battlefield.size != 2 || format.battlefield.any { it < 1 }) {
                return SessionPin.Refused(PinRefusal.NOT_UNDERSTOOD, listOf("game type $name"))
            }
        }
        if (pin.behaviorVersion != behaviorVersion) {
            return SessionPin.Refused(PinRefusal.BEHAVIOR_VERSION_DIFFERS, listOf(pin.behaviorVersion.toString(), behaviorVersion.toString()))
        }
        for (name in formats) {
            val format = pin.gameTypes[name] ?: return SessionPin.Refused(PinRefusal.FORMAT_NOT_PINNED, listOf(name))
            val pinned = "${format.battlefield[0]}x${format.battlefield[1]}, ${format.participants} bots"
            val preset = workerFormats[name]?.let { GAME_TYPE_PRESETS[it] }
                ?: return SessionPin.Refused(PinRefusal.FORMAT_NOT_RUNNABLE, listOf(name, pinned, ""))
            val max = preset.maxNumberOfParticipants
            val runs = "${preset.arenaWidth}x${preset.arenaHeight}, " +
                (if (max == preset.minNumberOfParticipants) "$max bots" else "${preset.minNumberOfParticipants} to ${max ?: "any number of"} bots")
            val fits = format.battlefield == listOf(preset.arenaWidth, preset.arenaHeight) &&
                format.participants >= preset.minNumberOfParticipants && (max == null || format.participants <= max)
            if (!fits) return SessionPin.Refused(PinRefusal.FORMAT_NOT_RUNNABLE, listOf(name, pinned, runs))
        }
        return SessionPin.Ready(pin, formats)
    }
}

/** The published advice of one format: the groups of entries that should meet, best first. */
@Serializable
private data class NeededBattles(val schemaVersion: Int, val gameType: String, val priorityPairs: List<NeededGroup>)

@Serializable
private data class NeededGroup(val bots: List<String>)

/** What reading one format's advice found. */
sealed interface AdviceRead {
    data class Found(val groups: List<List<String>>) : AdviceRead

    /** No advice is published, or it is in a form this program does not read; the session runs by coverage. */
    data object None : AdviceRead

    /** The advice could not be fetched now. */
    data class Failed(val message: String) : AdviceRead
}

object MatchmakingAdvice {
    const val SUPPORTED_SCHEMA_VERSION = 1
    private val json = Json { ignoreUnknownKeys = true }

    fun path(format: String) = "matchmaking/matches_needed-$format.json"

    fun read(source: ResultDataSource, format: String): AdviceRead {
        val text = when (val fetched = fetchResultFile(source, path(format))) {
            is ResultFetch.Failed -> return AdviceRead.Failed(fetched.message)
            is ResultFetch.Text -> fetched.text ?: return AdviceRead.None
        }
        val needed = try {
            json.decodeFromString(NeededBattles.serializer(), text)
        } catch (e: IllegalArgumentException) {
            // A SerializationException is one of these too.
            return AdviceRead.None
        }
        if (needed.schemaVersion != SUPPORTED_SCHEMA_VERSION || needed.gameType != format) return AdviceRead.None
        return AdviceRead.Found(needed.priorityPairs.map { it.bots })
    }
}

/**
 * Keeps a session's advice. It is read on first use and again when [refreshAfter] has passed; a format whose advice
 * cannot be fetched keeps what it had and is tried again at the next use, and a format with no advice published has none.
 * It is used by the session's own thread, between battles.
 */
class AdviceKeeper(
    private val source: ResultDataSource,
    private val formats: List<String>,
    private val refreshAfter: Duration = Duration.ofMinutes(5),
    private val clock: () -> Instant = Instant::now,
) {
    private var advice: Map<String, List<List<String>>> = emptyMap()
    private var readAt: Instant? = null

    /** The advice per format for the planner. */
    fun current(): Map<String, List<List<String>>> {
        val now = clock()
        val last = readAt
        if (last == null || !now.isBefore(last.plus(refreshAfter))) {
            val next = advice.toMutableMap()
            var failed = false
            for (format in formats) {
                when (val read = MatchmakingAdvice.read(source, format)) {
                    is AdviceRead.Found -> next[format] = read.groups
                    AdviceRead.None -> next.remove(format)
                    is AdviceRead.Failed -> failed = true
                }
            }
            advice = next
            // After a failed read the refresh time does not start, so a short outage costs one battle without advice, not minutes.
            if (!failed) readAt = now
        }
        return advice
    }
}
