package dev.robocode.tankroyale.rumble

/**
 * The keys of the texts the Submit or update bot wizard shows. The texts are in the desktop program's resource files;
 * this object is the one list of which keys a state and a problem code need, so a test can check that every one of
 * them has a text. Nothing here is a sentence.
 */
object SubmitTexts {
    /** The key of the heading of [state]. */
    fun forState(state: SubmitState): String = when (state) {
        is SubmitState.Choose -> "rumble.submit.step_choose"
        is SubmitState.Working -> if (state.staging) "rumble.submit.working_stage" else "rumble.submit.working_check"
        is SubmitState.Problems -> "rumble.submit.problems"
        is SubmitState.Checked -> "rumble.submit.checked"
        is SubmitState.Review -> "rumble.submit.step_review"
        is SubmitState.SendingComesLater -> "rumble.submit.later"
    }

    /** The key of the text that says what a problem code means. Every code the library can find has one. */
    fun forCode(code: String): String = "rumble.submit.finding." + code.lowercase()

    /** The codes the wizard itself adds to the library's own. */
    val WIZARD_CODES: List<String> = listOf("STAGING_REFUSED", "READ_FAILED")

    /** The keys that are not about a state or a code: step titles, lines of the review, the notice, the confirmations and the buttons. */
    val OTHER: List<String> = listOf(
        "rumble.submit.step_check", "rumble.submit.choose_help", "rumble.submit.choose_none", "rumble.submit.found", "rumble.submit.found_team",
        "rumble.submit.unknown", "rumble.submit.file", "rumble.submit.details", "rumble.submit.fix", "rumble.submit.notes",
        "rumble.submit.review_intro", "rumble.submit.review_name", "rumble.submit.review_language", "rumble.submit.review_license",
        "rumble.submit.review_no_license", "rumble.submit.review_files", "rumble.submit.public_notice",
        "rumble.submit.confirm_public", "rumble.submit.confirm_author",
        "rumble.submit.button.choose", "rumble.submit.button.check", "rumble.submit.button.check_again", "rumble.submit.button.review",
        "rumble.submit.button.back", "rumble.submit.button.next", "rumble.submit.button.close",
    )

    /** Every state the wizard can show, for checking that each has a text. */
    val ALL_STATES: List<SubmitState> = listOf(
        SubmitState.Choose(), SubmitState.Working(java.nio.file.Path.of("."), staging = false), SubmitState.Working(java.nio.file.Path.of("."), staging = true),
        SubmitState.Problems(java.nio.file.Path.of("."), emptyList(), emptyList()),
        SubmitState.Checked(java.nio.file.Path.of("."), FoundBot("", null, null, false), emptyList()),
        SubmitState.Review(java.nio.file.Path.of("."), ReviewInfo("", null, "", null, false, emptyList())),
        SubmitState.SendingComesLater(java.nio.file.Path.of("."), ""),
    )
}
