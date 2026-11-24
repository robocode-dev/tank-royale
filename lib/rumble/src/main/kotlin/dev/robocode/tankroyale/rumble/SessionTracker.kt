package dev.robocode.tankroyale.rumble

import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.contentOrNull
import java.time.Instant

/**
 * Tells which reports of a battle's starting bots are news for the log and the command line: the first of a battle and
 * every one whose number of connected bots differs from the one before. The others only move the seconds on a screen.
 */
class StartingLog {
    private var last = -1

    /** A new battle begins, so its first report is news. */
    @Synchronized
    fun reset() {
        last = -1
    }

    /** Whether a report with [connected] bots is news; it becomes the one the next is compared with. */
    @Synchronized
    fun isNews(connected: Int): Boolean = (connected != last).also { last = connected }
}

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

    /** Logs [event], unless [log] is false, and changes the status by it. */
    @Synchronized
    fun record(event: JsonObject, log: Boolean = true) {
        if (log) running.log(event)
        val kind = (event["event"] as? JsonPrimitive)?.contentOrNull ?: return
        fun text(name: String) = (event[name] as? JsonPrimitive)?.contentOrNull
        val now = status
        val counts = kind in COUNTING
        fun number(name: String) = text(name)?.toLongOrNull() ?: 0L
        // The starting of a battle's bots is shown until they have all connected, and never past the battle's end.
        status = when (kind) {
            // A report that arrives after its battle ended, or for another battle, shows nothing.
            "bots-starting" -> now.copy(
                boot = if (now.battle == null || now.battle.battleId != text("battleId") || number("connected") >= number("expected")) null
                else SessionBoot(number("connected").toInt(), number("expected").toInt(), (number("elapsedMillis") / 1000).toInt(), (number("limitMillis") / 1000).toInt()),
            )
            "battle-started" -> now.copy(
                state = if (now.state == "pausing" || now.state == "stopping" || now.state == "cancelling") now.state else "running",
                waitingFor = null,
                boot = null,
                battle = SessionBattle(
                    text("battleId").orEmpty(), text("gameType").orEmpty(),
                    (event["participants"] as? JsonArray)?.mapNotNull { (it as? JsonPrimitive)?.contentOrNull }.orEmpty(),
                    startedAt = clock().toString(),
                ),
            )
            // A bot that cannot be placed now is tried again; every other reason leaves it out for the rest of the session.
            "bot-not-placed" -> {
                val bot = text("bot").orEmpty()
                val reason = text("reason").orEmpty()
                if (bot.isEmpty() || reason == NotPlacedReason.SOURCE_UNAVAILABLE.name || now.leftOut.any { it.bot == bot }) now
                else now.copy(leftOut = now.leftOut + SessionLeftOut(bot, reason))
            }
            "battle-kept" -> now.copy(kept = now.kept + 1, battle = null, boot = null)
            "battle-not-kept" -> now.copy(notKept = now.notKept + 1, battle = null, boot = null)
            "waiting" -> now.copy(state = "waiting", waitingFor = text("reason"), battle = null, boot = null)
            "pausing" -> now.copy(state = "pausing")
            "paused" -> now.copy(state = "paused", battle = null, boot = null)
            "resumed" -> now.copy(state = "running")
            "resuming" -> if (now.state == "pausing") now.copy(state = "running") else now
            "stopping" -> now.copy(state = "stopping")
            "cancelling" -> now.copy(state = "cancelling")
            "finishing" -> now.copy(state = "sending", battle = null, boot = null)
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
        status = status.copy(battle = null, boot = null, ended = SessionEnded(outcome, clock().toString(), message))
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
