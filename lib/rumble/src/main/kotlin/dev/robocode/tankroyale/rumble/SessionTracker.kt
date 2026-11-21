package dev.robocode.tankroyale.rumble

import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.contentOrNull
import java.time.Instant

/**
 * Keeps a running session's status and event log in its session folder up to date from the events the session reports,
 * the same JSON events the command line writes with `--json`. It owns the lock until [close].
 */
class SessionTracker(
    private val running: SessionFolder.RunningSession,
    environment: RumbleEnvironment,
    owner: SessionOwner?,
    formats: List<String>,
    battleLimit: Int?,
    private val clock: () -> Instant = Instant::now,
) : AutoCloseable {
    @Volatile
    var status: SessionStatus = SessionStatus(
        environment = environment.name, pid = ProcessHandle.current().pid(), startedAt = clock().toString(), owner = owner,
        formats = formats, battleLimit = battleLimit, updatedAt = clock().toString(),
    )
        private set

    /** The journal's result counts, once the journal is open. */
    @Volatile
    var results: (() -> SessionResults)? = null

    init {
        running.write(status)
    }

    /** Logs [event] and changes the status by it. */
    @Synchronized
    fun record(event: JsonObject) {
        running.log(event)
        val kind = (event["event"] as? JsonPrimitive)?.contentOrNull ?: return
        fun text(name: String) = (event[name] as? JsonPrimitive)?.contentOrNull
        val now = status
        val counts = kind in COUNTING
        status = when (kind) {
            "battle-started" -> now.copy(
                state = if (now.state == "pausing" || now.state == "stopping" || now.state == "cancelling") now.state else "running",
                waitingFor = null,
                battle = SessionBattle(
                    text("battleId").orEmpty(), text("gameType").orEmpty(),
                    (event["participants"] as? JsonArray)?.mapNotNull { (it as? JsonPrimitive)?.contentOrNull }.orEmpty(),
                    startedAt = clock().toString(),
                ),
            )
            "battle-kept" -> now.copy(kept = now.kept + 1, battle = null)
            "battle-not-kept" -> now.copy(notKept = now.notKept + 1, battle = null)
            "waiting" -> now.copy(state = "waiting", waitingFor = text("reason"), battle = null)
            "pausing" -> now.copy(state = "pausing")
            "paused" -> now.copy(state = "paused", battle = null)
            "resumed" -> now.copy(state = "running")
            "resuming" -> if (now.state == "pausing") now.copy(state = "running") else now
            "stopping" -> now.copy(state = "stopping")
            "cancelling" -> now.copy(state = "cancelling")
            "released" -> now.copy(owner = null)
            "python-setting-up" -> now.copy(python = "setting-up")
            "python-ready" -> now.copy(python = "ready")
            "python-unavailable" -> now.copy(python = "unavailable")
            "python-stopped" -> now.copy(python = "stopped")
            else -> now
        }
        save(counts)
    }

    /** Records that the session ended with [outcome] and [message]; the status then stays as its summary. */
    @Synchronized
    fun end(outcome: String, message: String) {
        status = status.copy(battle = null, ended = SessionEnded(outcome, clock().toString(), message))
        save(counts = true)
    }

    /** The requests asked of the session since the last look. */
    fun take(): List<SessionControlRequest> = running.take()

    /** Saves the status; [counts] reads the journal's counts again, which only some events change. */
    private fun save(counts: Boolean) {
        val fresh = if (counts) runCatching { results?.invoke() }.getOrNull() else null
        status = status.copy(results = fresh ?: status.results, updatedAt = clock().toString())
        running.write(status)
    }

    override fun close() = running.close()

    private companion object {
        /** The events after which the journal's counts may have changed. */
        val COUNTING = setOf(
            "started", "battle-kept", "battle-not-kept", "batch-sent", "not-sent", "result-accepted", "result-rejected",
            "batch-refused", "set-aside",
        )
    }
}
