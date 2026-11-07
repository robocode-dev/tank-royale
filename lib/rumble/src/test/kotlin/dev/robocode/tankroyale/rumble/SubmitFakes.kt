package dev.robocode.tankroyale.rumble

import dev.robocode.tankroyale.botproject.StagedSubmission

/** A stand-in for GitHub in the wizard's tests: it answers what the test sets and records what it is asked. */
class FakeSendPort : SendPort {
    var readiness: SubmissionReadiness = SubmissionReadiness.Ready("alice", "alice/rumble-bots")
    var prepared: SendPrepared = SendPrepared.Ready(LauncherState.Same, ContentState.NEW, null)
    var result: SubmissionResult = SubmissionResult.Submitted("https://github.com/x/y/pull/7", 7, "submit/MyBot-1.0.0", SubmissionOutcome.OPENED)
    var progress: BotProgress = BotProgress.WaitingForChecks

    val readinessCalls = mutableListOf<Unit>()
    val prepared_for = mutableListOf<StagedSubmission>()
    val sent = mutableListOf<Pair<StagedSubmission, Boolean>>()
    val progressCalls = mutableListOf<Triple<Int, String, String>>()

    override fun readiness(): SubmissionReadiness {
        readinessCalls += Unit
        return readiness
    }

    override fun prepare(submission: StagedSubmission, account: String): SendPrepared {
        prepared_for += submission
        return prepared
    }

    override fun send(submission: StagedSubmission, authorCertifiesLicense: Boolean): SubmissionResult {
        sent += submission to authorCertifiesLicense
        return result
    }

    override fun progress(number: Int, name: String, version: String): BotProgress {
        progressCalls += Triple(number, name, version)
        return progress
    }
}

/** Time moved by hand: a task runs only when the test says so. */
class ManualScheduler : AccountScheduler {
    class Task(val seconds: Long, val task: () -> Unit) : AutoCloseable {
        var closed = false
        override fun close() {
            closed = true
        }
    }

    val tasks = mutableListOf<Task>()
    override fun after(seconds: Long, task: () -> Unit): AutoCloseable = Task(seconds, task).also { tasks += it }
    val open: List<Task> get() = tasks.filter { !it.closed }

    /** Runs the next task that is still waiting. */
    fun fire() {
        val next = open.first()
        next.closed = true
        next.task()
    }
}
