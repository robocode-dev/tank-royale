package dev.robocode.tankroyale.rumble

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import java.io.IOException
import java.nio.channels.FileChannel
import java.nio.channels.FileLock
import java.nio.channels.OverlappingFileLockException
import java.nio.file.AtomicMoveNotSupportedException
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardCopyOption
import java.nio.file.StandardOpenOption
import java.time.Duration
import java.time.Instant
import java.util.UUID

/** What a person or a program can ask of a running contribution session. */
enum class SessionControlRequest { PAUSE, RESUME, STOP, STOP_NOW, RELEASE }

/** The process that owns a session: the session ends after its running battle when this very process is gone. */
@Serializable
data class SessionOwner(val pid: Long, val startedAt: String) {
    /** True while the process with this ID still runs and is the one that started at [startedAt]. */
    fun isAlive(): Boolean {
        val process = ProcessHandle.of(pid).orElse(null) ?: return false
        if (!process.isAlive) return false
        val started = process.info().startInstant().orElse(null) ?: return true
        if (startedAt.isBlank()) return true
        // The start time is what keeps a reused process ID from being taken for the owner.
        val expected = try {
            Instant.parse(startedAt)
        } catch (e: java.time.format.DateTimeParseException) {
            return true
        }
        return Duration.between(started, expected).abs() <= START_TOLERANCE
    }

    companion object {
        private val START_TOLERANCE: Duration = Duration.ofSeconds(2)

        /**
         * The owner for the running process with ID [pid], or null when there is no such process. Where the system does
         * not tell when a process started, the owner is known by its ID alone.
         */
        fun of(pid: Long): SessionOwner? {
            val process = ProcessHandle.of(pid).orElse(null)?.takeIf { it.isAlive } ?: return null
            val started = process.info().startInstant().orElse(null)
            return SessionOwner(pid, started?.toString().orEmpty())
        }
    }
}

/** The result counts of the environment's journal, as a session shows them. */
@Serializable
data class SessionResults(val waiting: Int = 0, val sent: Int = 0, val accepted: Int = 0, val rejected: Int = 0, val setAside: Int = 0)

/** The battle a session runs now. */
@Serializable
data class SessionBattle(
    val battleId: String,
    val gameType: String,
    val participants: List<String>,
    /** When the battle started, so a screen can show how long it has run; empty in a status written before it was kept. */
    val startedAt: String = "",
)

/** How a session ended: its outcome word and when. */
@Serializable
data class SessionEnded(val outcome: String, val at: String, val message: String = "")

/**
 * What a running session says about itself, kept up to date in its session folder. [ended] is set when it ended; a
 * status without it whose session no longer holds the lock belongs to a session that ended unexpectedly.
 */
@Serializable
data class SessionStatus(
    val schemaVersion: Int = 1,
    val environment: String,
    val pid: Long,
    val startedAt: String,
    val owner: SessionOwner? = null,
    /** `starting`, `running`, `waiting`, `pausing`, `paused`, `stopping`, `cancelling`, or `sending` when its battles are over and it sends what waits before it ends. */
    val state: String = "starting",
    val formats: List<String> = emptyList(),
    val battleLimit: Int? = null,
    val battle: SessionBattle? = null,
    val kept: Int = 0,
    val notKept: Int = 0,
    /** The planner's reason while the session waits, as a code. */
    val waitingFor: String? = null,
    /** `none`, `setting-up`, `ready`, `unavailable` or `stopped`. */
    val python: String = "none",
    val results: SessionResults = SessionResults(),
    val updatedAt: String,
    val ended: SessionEnded? = null,
)

/** What a look into the session folder finds. */
sealed interface SessionLook {
    /** No session has run in this environment, or its traces are gone. */
    data object None : SessionLook

    /** A session runs; [status] is what it said last, or null while it is starting. */
    data class Running(val status: SessionStatus?) : SessionLook

    /** The last session ended; [status] holds its summary. */
    data class Ended(val status: SessionStatus) : SessionLook

    /** The last session's process ended without ending the session, such as by a crash. */
    data class EndedUnexpectedly(val status: SessionStatus) : SessionLook
}

/**
 * The session folder of one Rumble environment, as the decision that a contribution session runs as a program of its own
 * sets out: below the environment's folder in the Tank Royale user folder, beside the journal. It holds the lock a running
 * session holds, its status, the log of its events and the control requests for it. Every file is written whole under
 * another name first and then put in place, so a reader never sees half of one.
 */
class SessionFolder(val folder: Path) {
    private val lockFile = folder.resolve("session.lock")
    private val statusFile = folder.resolve("status.json")
    private val eventsFile = folder.resolve("events.jsonl")
    private val previousEventsFile = folder.resolve("events.previous.jsonl")
    private val requests = folder.resolve("requests")

    /** Takes the lock for a new session, or gives null when a session of this environment already runs. */
    fun claim(): RunningSession? {
        Files.createDirectories(requests)
        val channel = FileChannel.open(lockFile, StandardOpenOption.CREATE, StandardOpenOption.WRITE)
        // Another program looking at the folder holds the lock for an instant to find out whether a session runs, so the
        // lock is tried a few times before a session is taken to run.
        var lock: FileLock? = null
        for (attempt in 1..CLAIM_ATTEMPTS) {
            lock = try {
                channel.tryLock()
            } catch (e: OverlappingFileLockException) {
                null
            }
            if (lock != null || attempt == CLAIM_ATTEMPTS) break
            Thread.sleep(CLAIM_PAUSE_MILLIS)
        }
        if (lock == null) {
            channel.close()
            return null
        }
        // From here on a request is asked of this session, so only the requests asked before are earlier ones'.
        val since = System.currentTimeMillis()
        try {
            // The new session starts a new log and no status, so the last session's is never shown as this one's, and it
            // leaves no request meant for an earlier session.
            if (Files.exists(eventsFile)) Files.move(eventsFile, previousEventsFile, StandardCopyOption.REPLACE_EXISTING)
            Files.deleteIfExists(statusFile)
            Files.list(requests).use { paths ->
                paths.iterator().forEach { file -> if (askedAt(file) < since) Files.deleteIfExists(file) }
            }
        } catch (e: Exception) {
            // A folder that cannot be made ready is let go of again, so the lock is never left held. Listing the folder
            // reports its failures unchecked; they are the same failure to use the folder.
            try {
                lock.release()
            } finally {
                channel.close()
            }
            throw if (e is java.io.UncheckedIOException) e.cause ?: IOException(e.message) else e
        }
        return RunningSession(channel, lock)
    }

    /** Looks at the folder from outside: whether a session runs, ended, or ended unexpectedly. */
    fun look(): SessionLook {
        // The lock first: a session writes its ending before it lets go of the lock, so a status read after a free lock
        // is final, unless a new session took the lock meanwhile, which a second look at the lock tells.
        if (running()) return SessionLook.Running(readStatus())
        val status = readStatus() ?: return if (running()) SessionLook.Running(null) else SessionLook.None
        if (status.ended != null) return SessionLook.Ended(status)
        return if (running()) SessionLook.Running(status) else SessionLook.EndedUnexpectedly(status)
    }

    /** Asks the running session for [request]. False when no session runs, so nothing was asked. */
    fun ask(request: SessionControlRequest): Boolean {
        if (!running()) return false
        Files.createDirectories(requests)
        val name = String.format("%015d-%09d-%s.%s", System.currentTimeMillis(), sequence.incrementAndGet(), UUID.randomUUID(), request.name.lowercase())
        writeWhole(requests.resolve(name), "")
        return true
    }

    /** The lines of the running or last session's event log. */
    fun events(): List<String> = if (Files.exists(eventsFile)) Files.readAllLines(eventsFile) else emptyList()

    private fun running(): Boolean {
        if (!Files.exists(lockFile)) return false
        return try {
            FileChannel.open(lockFile, StandardOpenOption.WRITE).use { channel ->
                val lock = channel.tryLock() ?: return true
                lock.release()
                false
            }
        } catch (e: OverlappingFileLockException) {
            // This very program holds it.
            true
        } catch (e: IOException) {
            false
        }
    }

    private fun readStatus(): SessionStatus? = try {
        if (Files.exists(statusFile)) json.decodeFromString(SessionStatus.serializer(), Files.readString(statusFile)) else null
    } catch (e: IOException) {
        null
    } catch (e: IllegalArgumentException) {
        null
    }

    /** The session that holds the lock: it writes its status and events and takes the requests asked of it. */
    inner class RunningSession internal constructor(private val channel: FileChannel, private val lock: FileLock) : AutoCloseable {
        private var closed = false

        /**
         * Writes the status. The folder is only a view of the session, so a write that fails, such as while a reader on
         * Windows holds the file at that very moment, is tried a few times and then left for the next one; it never ends
         * the session. True when it was written.
         */
        @Synchronized
        fun write(status: SessionStatus): Boolean {
            if (closed) return false
            val text = json.encodeToString(SessionStatus.serializer(), status)
            repeat(WRITE_ATTEMPTS) { attempt ->
                try {
                    writeWhole(statusFile, text)
                    return true
                } catch (e: IOException) {
                    if (attempt < WRITE_ATTEMPTS - 1) Thread.sleep(WRITE_PAUSE_MILLIS)
                }
            }
            return false
        }

        /** Adds [event] to the log; a line that cannot be added is left out rather than ending the session. */
        @Synchronized
        fun log(event: JsonObject) {
            if (closed) return
            try {
                Files.writeString(eventsFile, event.toString() + "\n", StandardOpenOption.CREATE, StandardOpenOption.APPEND)
            } catch (e: IOException) {
                // The journal holds what counts; the log is a view.
            }
        }

        /** The requests asked since the last look, oldest first; each is taken once. None after the session let go of the folder. */
        @Synchronized
        fun take(): List<SessionControlRequest> {
            if (closed || !Files.isDirectory(requests)) return emptyList()
            val files = Files.list(requests).use { paths -> paths.iterator().asSequence().filter { !it.fileName.toString().endsWith(".part") }.toList() }
            return files.sortedBy { it.fileName.toString() }.mapNotNull { file ->
                val kind = file.fileName.toString().substringAfterLast('.', "").uppercase().replace('-', '_')
                Files.deleteIfExists(file)
                SessionControlRequest.entries.firstOrNull { it.name == kind }
            }
        }

        @Synchronized
        override fun close() {
            if (closed) return
            closed = true
            try {
                lock.release()
            } finally {
                channel.close()
            }
        }
    }

    private fun writeWhole(target: Path, text: String) {
        val part = target.resolveSibling(target.fileName.toString() + "." + UUID.randomUUID() + ".part")
        Files.writeString(part, text)
        try {
            Files.move(part, target, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING)
        } catch (e: AtomicMoveNotSupportedException) {
            Files.move(part, target, StandardCopyOption.REPLACE_EXISTING)
        }
    }

    companion object {
        const val FOLDER = "session"
        private val sequence = java.util.concurrent.atomic.AtomicLong()

        /** When the request file [file] was asked, from its name; 0 for a name that holds no time, so it counts as old. */
        private fun askedAt(file: Path): Long = file.fileName.toString().substringBefore('-').toLongOrNull() ?: 0
        private const val CLAIM_ATTEMPTS = 10
        private const val CLAIM_PAUSE_MILLIS = 50L
        private const val WRITE_ATTEMPTS = 5
        private const val WRITE_PAUSE_MILLIS = 20L
        private val json = Json { ignoreUnknownKeys = true; encodeDefaults = true }

        /** The session folder of [environment] below [base], the Tank Royale user folder. */
        fun of(environment: RumbleEnvironment, base: Path) = SessionFolder(environment.stateRoot(base).resolve(FOLDER))
    }
}
