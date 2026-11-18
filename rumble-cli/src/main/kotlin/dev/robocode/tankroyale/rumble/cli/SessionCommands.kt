package dev.robocode.tankroyale.rumble.cli

import com.github.ajalt.clikt.core.Context
import com.github.ajalt.clikt.core.CoreCliktCommand
import dev.robocode.tankroyale.rumble.RumbleEnvironment
import dev.robocode.tankroyale.rumble.SessionControlRequest
import dev.robocode.tankroyale.rumble.SessionFolder
import dev.robocode.tankroyale.rumble.SessionLook
import dev.robocode.tankroyale.rumble.SessionStatus
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObjectBuilder
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.put
import java.io.IOException

/** The `session` commands: following and controlling the contribution session of an environment, wherever it was started. */
internal class SessionGroup : CoreCliktCommand(name = "session") {
    override fun help(context: Context) =
        "Follow and control the contribution session of an environment, whether it was started here, in another terminal or by the desktop program."

    override fun run() {
        // No work of its own; the commands do it.
    }

    companion object {
        fun commands(io: Io) = listOf(
            SessionStatusCommand(io),
            SessionControlCommand(io, "pause", SessionControlRequest.PAUSE, "Pauses the running session after the battle that is running."),
            SessionControlCommand(io, "resume", SessionControlRequest.RESUME, "Lets a paused session go on."),
            SessionControlCommand(io, "stop", SessionControlRequest.STOP, "Stops the running session after the battle that is running, which is kept and sent."),
            SessionControlCommand(io, "stop-now", SessionControlRequest.STOP_NOW, "Ends the running session now; nothing of the battle that is running is kept."),
            SessionControlCommand(
                io, "release", SessionControlRequest.RELEASE,
                "Lets the running session go on when the program that started it ends, until its battles are done or it is stopped.",
            ),
        )
    }
}

/** What the session commands have in common: the session folder of the chosen environment and the shape of their answer. */
internal abstract class SessionLeaf(name: String, io: Io) : RumbleCli.EnvironmentLeaf(name, io) {
    private val answerCommand = "session $name"

    protected fun folder(env: RumbleEnvironment) = SessionFolder.of(env, base())

    protected fun reply(env: RumbleEnvironment, code: Int, outcome: String, message: String, extra: JsonObjectBuilder.() -> Unit = {}): Reply {
        val json = head(answerCommand) {
            put("environment", env.name)
            put("outcome", outcome)
            put("message", message)
            extra()
        }
        return Reply(code, json, "Environment: ${env.displayName} (${env.name})\n$message")
    }
}

internal class SessionStatusCommand(io: Io) : SessionLeaf("status", io) {
    override fun help(context: Context) =
        "Shows the running contribution session of an environment, or the summary of the last one. It only reads."

    override fun run() {
        val env = environment()
        val look = try {
            folder(env).look()
        } catch (e: IOException) {
            return answer(reply(env, ExitCodes.FAILED, "session-folder-unusable", "The session folder could not be read: ${e.message}"))
        }
        val (outcome, status) = when (look) {
            SessionLook.None -> return answer(reply(env, ExitCodes.DONE, "no-session", "No contribution session has run in this environment."))
            is SessionLook.Running -> "running" to look.status
            is SessionLook.Ended -> "ended" to look.status
            is SessionLook.EndedUnexpectedly -> "ended-unexpectedly" to look.status
        }
        answer(reply(env, ExitCodes.DONE, outcome, describe(outcome, status)) {
            status?.let { put("session", statusJson.encodeToJsonElement(SessionStatus.serializer(), it).jsonObject) }
        })
    }

    private fun describe(outcome: String, status: SessionStatus?): String {
        if (status == null) return "A contribution session is starting."
        return buildString {
            when (outcome) {
                "running" -> {
                    append("A contribution session runs (process ${status.pid}, started ${status.startedAt}): ${status.state}")
                    status.waitingFor?.let { append(", waiting (${it.lowercase()})") }
                    append(".")
                    status.battle?.let { append("\nBattle ${it.gameType}: ${it.participants.joinToString(" vs ")}") }
                    if (status.owner != null) append("\nIt stops after its battle when process ${status.owner!!.pid} ends.")
                }
                "ended" -> {
                    val ended = status.ended!!
                    append("The last contribution session ended at ${ended.at} (${ended.outcome})")
                    append(if (ended.message.isNotBlank()) ": ${io.outside(ended.message)}" else ".")
                }
                else -> append(
                    "The last contribution session (process ${status.pid}) ended without ending the session, such as by a crash. " +
                        "What it kept is in the journal and is sent by the next session.",
                )
            }
            append("\nFormats: ${status.formats.joinToString()}; battles: ${status.battleLimit ?: "until stopped"}.")
            append("\nKept this session: ${status.kept}; not kept: ${status.notKept}.")
            val r = status.results
            append("\nResults: ${r.waiting} waiting, ${r.sent} sent, ${r.accepted} accepted, ${r.rejected} rejected, ${r.setAside} kept apart.")
            if (status.python != "none") append("\nPython: ${status.python}.")
        }
    }

    private companion object {
        val statusJson = Json { encodeDefaults = true }
    }
}

internal class SessionControlCommand(
    io: Io,
    name: String,
    private val request: SessionControlRequest,
    private val helpText: String,
) : SessionLeaf(name, io) {
    override fun help(context: Context) = helpText

    override fun run() {
        val env = environment()
        val asked = try {
            folder(env).ask(request)
        } catch (e: IOException) {
            return answer(reply(env, ExitCodes.FAILED, "session-folder-unusable", "The session folder could not be written: ${e.message}"))
        }
        if (!asked) return answer(reply(env, ExitCodes.REFUSED, "no-session", "No contribution session runs in this environment."))
        answer(reply(env, ExitCodes.DONE, "asked", "Asked the running session to ${verb()}. Follow it with rumble session status."))
    }

    private fun verb() = when (request) {
        SessionControlRequest.PAUSE -> "pause after the battle that is running"
        SessionControlRequest.RESUME -> "go on"
        SessionControlRequest.STOP -> "stop after the battle that is running"
        SessionControlRequest.STOP_NOW -> "end now"
        SessionControlRequest.RELEASE -> "go on when the program that started it ends"
    }
}
