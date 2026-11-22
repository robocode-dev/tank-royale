package dev.robocode.tankroyale.rumble

/** Where the screen points a person to fix why a session could not run. */
enum class ContributeFix { ACCOUNT }

/**
 * The keys of the texts the Contribute battles screen shows. The texts are in the desktop program's resource files; this
 * object is the one list of which keys the states, the outcomes and the problems need, so a test can check that every
 * one of them has a text in every language. Nothing here is a sentence.
 */
object ContributeTexts {
    /** The outcome words a session ends with, as the command line answers them. Any other word is shown with its message. */
    val OUTCOMES = listOf(
        "finished", "stopped", "cancelled", "gave-up", "nothing-to-run", "journal-failed", "journal-unusable",
        "no-sign-in", "sign-in-unusable", "sign-in-refused", "sign-in-failed", "account-unknown", "not-registered",
        "registration-unreadable", "catalog-unreadable", "pin-refused", "worker-unavailable", "session-folder-unusable",
        "session-running", "bad-input", "failed",
    )

    /** The states a running session reports. */
    val STATES = listOf("starting", "running", "waiting", "pausing", "paused", "stopping", "cancelling", "sending")

    /** Whether Python is ready, as a running session reports it; `none` is not shown. */
    val PYTHON = listOf("setting-up", "ready", "unavailable", "stopped")

    fun forOutcome(outcome: String): String = "rumble.contribute.outcome." + (outcome.takeIf { it in OUTCOMES } ?: "other").replace('-', '_')

    fun forState(state: String): String = "rumble.contribute.state." + (state.takeIf { it in STATES } ?: "running")

    fun forPython(python: String): String? = python.takeIf { it in PYTHON }?.let { "rumble.contribute.python." + it.replace('-', '_') }

    fun forWaiting(reason: String?): String =
        "rumble.contribute.waiting." + (WaitReason.entries.firstOrNull { it.name == reason }?.name?.lowercase() ?: "other")

    fun forProblem(problem: ContributeProblem): String = "rumble.contribute.problem." + problem.name.lowercase()

    fun forProgram(problem: ProgramProblem): String = "rumble.contribute.program." + problem.name.lowercase()

    fun forRequest(request: SessionControlRequest): String = "rumble.contribute.asked." + request.name.lowercase()

    fun forMode(mode: ContributeMode): String = "rumble.contribute.mode." + mode.name.lowercase()

    fun forClose(choice: CloseChoice): String = "rumble.contribute.close." + choice.name.lowercase()

    /**
     * Whether an outcome is one the person asked for or expects, so its text says all there is and the program's own
     * message, which is in English, is not shown as details.
     */
    fun ordinary(outcome: String): Boolean = good(outcome) || outcome == "cancelled"

    /** Whether an outcome means the session ended as it should, which the screen shows as good news. */
    fun good(outcome: String): Boolean = outcome == "finished" || outcome == "stopped"

    /** Where the person can fix why a session ended, when there is such a place. */
    fun fixFor(outcome: String): ContributeFix? = when (outcome) {
        "no-sign-in", "sign-in-unusable", "sign-in-refused", "not-registered", "account-unknown" -> ContributeFix.ACCOUNT
        else -> null
    }

    /** Every key the screen can ask for, so a test can check each has a text. */
    fun allKeys(): List<String> =
        OUTCOMES.map { forOutcome(it) } + forOutcome("other") + STATES.map { forState(it) } + PYTHON.mapNotNull { forPython(it) } +
            WaitReason.entries.map { forWaiting(it.name) } + forWaiting(null) + ContributeProblem.entries.map { forProblem(it) } +
            ProgramProblem.entries.map { forProgram(it) } + SessionControlRequest.entries.map { forRequest(it) } +
            ContributeMode.entries.map { forMode(it) } + CloseChoice.entries.map { forClose(it) }
}
