package dev.robocode.tankroyale.rumble

import java.io.IOException
import java.nio.file.Path

/** How asking to send results again after a refused batch went. */
enum class SendAgainOutcome {
    /** Sending was stopped and may go on now; the next session sends what waits. */
    LIFTED,

    /** Sending was not stopped, so there was nothing to lift. */
    NOT_STOPPED,

    /** A session of the environment runs and holds the list of sent batches; it must be stopped first. */
    SESSION_RUNNING,

    /** The list of sent batches or the session folder could not be read or written. */
    FAILED,
}

/**
 * Lets a contributor send results again after the Rumble refused a whole batch, once they fixed what it refused. It
 * holds the environment's session lock while it writes, so it never writes beside a running session and a session
 * cannot start meanwhile, and it leaves the last session's status, log and requests as they are.
 */
object SendAgain {
    /** Lifts the stop of the environment whose own state folder is [stateRoot]. */
    fun lift(stateRoot: Path): SendAgainOutcome {
        val folder = SessionFolder(stateRoot.resolve(SessionFolder.FOLDER))
        val held = try {
            folder.holdBriefly()
        } catch (e: IOException) {
            return SendAgainOutcome.FAILED
        } ?: return SendAgainOutcome.SESSION_RUNNING
        return held.use {
            try {
                val sent = SentBatches.open(stateRoot.resolve("ranked").resolve("sent-batches.jsonl"))
                if (sent.refusal == null) SendAgainOutcome.NOT_STOPPED else { sent.lift(); SendAgainOutcome.LIFTED }
            } catch (e: JournalUnusable) {
                SendAgainOutcome.FAILED
            }
        }
    }
}
