package dev.robocode.tankroyale.rumble

import java.nio.file.Path

/** Whether a line of the wizard is good news, bad news or neither, so the screen can show it in green or red. */
enum class Tone { GOOD, BAD, NEUTRAL }

/**
 * The keys of the texts the Submit or update bot wizard shows. The texts are in the desktop program's resource files;
 * this object is the one list of which keys a state, a problem code, a readiness case, an ending and a progress state
 * need, so a test can check that every one of them has a text. Nothing here is a sentence.
 */
object SubmitTexts {
    /** The key of the heading of [state]. */
    fun forState(state: SubmitState): String = when (state) {
        is SubmitState.Choose -> "rumble.submit.step_choose"
        is SubmitState.Working -> if (state.staging) "rumble.submit.working_stage" else "rumble.submit.working_check"
        is SubmitState.Problems -> "rumble.submit.problems"
        is SubmitState.Checked -> "rumble.submit.checked"
        is SubmitState.Review -> "rumble.submit.step_review"
        is SubmitState.Copy -> "rumble.submit.step_copy"
        is SubmitState.SendReady -> "rumble.submit.step_send"
        is SubmitState.Sending -> "rumble.submit.working_send"
        is SubmitState.SendEnded -> "rumble.submit.step_follow"
    }

    /** The key of the text that says what a problem code means. Every code the library can find has one. */
    fun forCode(code: String): String = "rumble.submit.finding." + code.lowercase()

    /** The key of the text for what was found about the account's copy; null means the look is still going on. */
    fun forReadiness(readiness: SubmissionReadiness?): String = when (readiness) {
        null -> "rumble.submit.copy_checking"
        is SubmissionReadiness.Ready -> "rumble.submit.copy_ready"
        is SubmissionReadiness.NoCopy -> "rumble.submit.copy_none"
        is SubmissionReadiness.AppNotInstalled -> "rumble.submit.copy_app"
        is SubmissionReadiness.NotRumblesRepository -> "rumble.submit.copy_not_rumbles"
        is SubmissionReadiness.CopyBehind -> "rumble.submit.copy_behind"
        is SubmissionReadiness.SignInNeeded -> "rumble.submit.copy_sign_in"
        is SubmissionReadiness.Failed -> "rumble.submit.copy_failed"
    }

    fun toneOf(readiness: SubmissionReadiness?): Tone = when (readiness) {
        is SubmissionReadiness.Ready -> Tone.GOOD
        null, is SubmissionReadiness.NoCopy, is SubmissionReadiness.AppNotInstalled, is SubmissionReadiness.CopyBehind -> Tone.NEUTRAL
        is SubmissionReadiness.NotRumblesRepository, is SubmissionReadiness.SignInNeeded, is SubmissionReadiness.Failed -> Tone.BAD
    }

    /** The key of the text for what the checksum of the staged copy says about the Rumble's catalog. */
    fun forContent(content: ContentState): String = when (content) {
        ContentState.NEW -> "rumble.submit.content_new"
        ContentState.ALREADY_IN_RUMBLE -> "rumble.submit.content_already"
        ContentState.DIFFERENT_CONTENT -> "rumble.submit.content_different"
        ContentState.NOT_CHECKED -> "rumble.submit.content_not_checked"
    }

    fun toneOf(content: ContentState): Tone = when (content) {
        ContentState.NEW -> Tone.GOOD
        ContentState.ALREADY_IN_RUMBLE, ContentState.NOT_CHECKED -> Tone.NEUTRAL
        ContentState.DIFFERENT_CONTENT -> Tone.BAD
    }

    /** The key of the text for the launcher comparison. It is advice and never stops a send. */
    fun forLaunchers(launchers: LauncherState): String = when (launchers) {
        LauncherState.Same -> "rumble.submit.launchers_same"
        is LauncherState.Differs -> "rumble.submit.launchers_differ"
        is LauncherState.NotChecked -> "rumble.submit.launchers_not_checked"
    }

    /** The key of the text for how sending ended. */
    fun forResult(result: SubmissionResult): String = when (result) {
        is SubmissionResult.Submitted -> when (result.outcome) {
            SubmissionOutcome.OPENED -> "rumble.submit.ended_opened"
            SubmissionOutcome.RESUMED -> "rumble.submit.ended_resumed"
            SubmissionOutcome.UNCHANGED -> "rumble.submit.ended_unchanged"
            SubmissionOutcome.UPDATED -> "rumble.submit.ended_updated"
        }
        is SubmissionResult.SetupNeeded -> "rumble.submit.ended_setup"
        is SubmissionResult.Refused -> "rumble.submit.ended_refused"
        is SubmissionResult.SignInNeeded -> "rumble.submit.ended_sign_in"
        is SubmissionResult.Failed -> "rumble.submit.ended_failed"
    }

    fun toneOf(result: SubmissionResult): Tone = if (result is SubmissionResult.Submitted) Tone.GOOD else Tone.BAD

    /** The key of the text for where a pull request stands. */
    fun forProgress(progress: BotProgress): String = when (progress) {
        BotProgress.WaitingForChecks -> "rumble.submit.progress_waiting_checks"
        is BotProgress.BeingChecked -> "rumble.submit.progress_being_checked"
        is BotProgress.SentBack -> "rumble.submit.progress_sent_back"
        BotProgress.WaitingForModerator -> "rumble.submit.progress_waiting_moderator"
        BotProgress.Accepted -> "rumble.submit.progress_accepted"
        BotProgress.Merged -> "rumble.submit.progress_merged"
        is BotProgress.InCatalog -> "rumble.submit.progress_in_catalog"
        BotProgress.Closed -> "rumble.submit.progress_closed"
        is BotProgress.SignInNeeded -> "rumble.submit.progress_sign_in"
        is BotProgress.Failed -> "rumble.submit.progress_failed"
    }

    fun toneOf(progress: BotProgress): Tone = when (progress) {
        is BotProgress.InCatalog, BotProgress.Accepted, BotProgress.Merged -> Tone.GOOD
        is BotProgress.SentBack, BotProgress.Closed, is BotProgress.SignInNeeded, is BotProgress.Failed -> Tone.BAD
        BotProgress.WaitingForChecks, is BotProgress.BeingChecked, BotProgress.WaitingForModerator -> Tone.NEUTRAL
    }

    /**
     * The pre-check's codes the fifth step says in its own words; a version already published is told by the content
     * line instead. Each text takes the finding's values in order.
     */
    val RULE_CODES: List<String> = listOf(
        SubmissionPrecheck.ACCOUNT_BANNED, SubmissionPrecheck.BOT_DISQUALIFIED, SubmissionPrecheck.OWNED_BY_ANOTHER, SubmissionPrecheck.LOWER_VERSION,
    )

    /** The key of the text for a pre-check finding, or null when the fifth step leaves it to the content line. */
    fun forRule(code: String): String? = code.takeIf { it in RULE_CODES }?.let { "rumble.submit.rule." + it.replace('-', '_') }

    /**
     * The tone of the fifth step's content line: good only while the bot can be sent, so a line about a new version is
     * never shown as a success beside a rule that stops the send.
     */
    fun contentTone(state: SubmitState.SendReady): Tone {
        val tone = toneOf(state.ready.content)
        return if (tone == Tone.GOOD && !state.canSend) Tone.NEUTRAL else tone
    }

    /**
     * The key of the note that the Rumble's rules were not read, when the catalog was: the content line then says nothing
     * about them. When neither was read, the content line already says so.
     */
    fun forRulesNotChecked(ready: SendPrepared.Ready): String? =
        "rumble.submit.rules_not_checked".takeIf { ready.rules == null && ready.content != ContentState.NOT_CHECKED }

    /**
     * The moderation's reason as plain text: GitHub's comments are Markdown, so code marks, emphasis and line breaks are
     * removed, and a link is kept as its text followed by its address.
     */
    fun plainReason(reason: String): String = reason
        .replace(Regex("\\[([^\\]]*)]\\(([^)\\s]*)\\)"), "$1 ($2)")
        .replace("`", "").replace("**", "").replace("__", "")
        .replace(Regex("\\s+"), " ")
        .trim()

    /** The codes the wizard itself adds to the library's own. */
    val WIZARD_CODES: List<String> = listOf("STAGING_REFUSED", "READ_FAILED")

    /** The keys that are not about a state, a code, a case, an ending or a progress state: lines of the review and the later steps, the notice, the confirmations and the buttons. */
    val OTHER: List<String> = listOf(
        "rumble.submit.step_check", "rumble.submit.choose_help", "rumble.submit.choose_none", "rumble.submit.found", "rumble.submit.found_team",
        "rumble.submit.unknown", "rumble.submit.file", "rumble.submit.details", "rumble.submit.fix", "rumble.submit.notes", "rumble.submit.status_success", "rumble.submit.status_failed",
        "rumble.submit.review_intro", "rumble.submit.review_name", "rumble.submit.review_language", "rumble.submit.review_license",
        "rumble.submit.review_no_license", "rumble.submit.review_files", "rumble.submit.public_notice",
        "rumble.submit.confirm_public", "rumble.submit.confirm_author", "rumble.submit.send_intro", "rumble.submit.existing", "rumble.submit.progress_label", "rumble.submit.progress_reading",
        "rumble.submit.progress_reason", "rumble.submit.rules_not_checked", "rumble.submit.rule.account_banned", "rumble.submit.rule.bot_disqualified", "rumble.submit.rule.owned_by_another",
        "rumble.submit.rule.lower_version",
        "rumble.submit.button.choose", "rumble.submit.button.check", "rumble.submit.button.check_again", "rumble.submit.button.review",
        "rumble.submit.button.back", "rumble.submit.button.next", "rumble.submit.button.close", "rumble.submit.button.open_fork",
        "rumble.submit.button.open_install", "rumble.submit.button.open_copy", "rumble.submit.button.check_now", "rumble.submit.button.send", "rumble.submit.button.update",
        "rumble.submit.button.open_pr",
    )

    private val here: Path = Path.of(".")
    private val review = ReviewInfo("", null, "", null, false, emptyList())

    /** Every state the wizard can show, for checking that each has a text. */
    val ALL_STATES: List<SubmitState> = listOf(
        SubmitState.Choose(), SubmitState.Working(here, staging = false), SubmitState.Working(here, staging = true),
        SubmitState.Problems(here, emptyList(), emptyList()),
        SubmitState.Checked(here, FoundBot("", null, null, false), emptyList()),
        SubmitState.Review(here, review),
        SubmitState.Copy(here, review),
        SubmitState.SendReady(here, review, "", SendPrepared.Ready(LauncherState.Same, ContentState.NEW, null)),
        SubmitState.Sending(here, review),
        SubmitState.SendEnded(here, review, SubmissionResult.Failed(""), LauncherState.Same),
    )

    /** Every case of the look for the account's copy. */
    val ALL_READINESS: List<SubmissionReadiness?> = listOf(
        null, SubmissionReadiness.Ready("", ""), SubmissionReadiness.NoCopy("", ""), SubmissionReadiness.AppNotInstalled("", ""),
        SubmissionReadiness.NotRumblesRepository("", ""), SubmissionReadiness.CopyBehind("", ""), SubmissionReadiness.SignInNeeded(""), SubmissionReadiness.Failed(""),
    )

    /** Every way sending can end. */
    val ALL_RESULTS: List<SubmissionResult> =
        SubmissionOutcome.entries.map { SubmissionResult.Submitted("", 0, "", it) } + listOf(
            SubmissionResult.SetupNeeded(SubmissionReadiness.Failed("")), SubmissionResult.Refused(""), SubmissionResult.SignInNeeded(""), SubmissionResult.Failed(""),
        )

    /** Every state a pull request can be in. */
    val ALL_PROGRESS: List<BotProgress> = listOf(
        BotProgress.WaitingForChecks, BotProgress.BeingChecked(), BotProgress.SentBack(""), BotProgress.WaitingForModerator, BotProgress.Accepted,
        BotProgress.Merged, BotProgress.InCatalog(""), BotProgress.Closed, BotProgress.SignInNeeded(""), BotProgress.Failed(""),
    )

    val ALL_LAUNCHERS: List<LauncherState> = listOf(LauncherState.Same, LauncherState.Differs(emptyList()), LauncherState.NotChecked(""))
}
