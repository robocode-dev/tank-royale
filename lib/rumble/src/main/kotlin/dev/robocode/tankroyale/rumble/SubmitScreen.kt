package dev.robocode.tankroyale.rumble

import dev.robocode.tankroyale.botproject.BotProjects
import dev.robocode.tankroyale.botproject.BotValidator
import dev.robocode.tankroyale.botproject.Finding
import dev.robocode.tankroyale.botproject.Inspection
import dev.robocode.tankroyale.botproject.BotStaging
import dev.robocode.tankroyale.botproject.StagedSubmission
import dev.robocode.tankroyale.botproject.StagingResult
import java.nio.file.Path
import java.util.concurrent.Executor

/** What the wizard read from a bot or team folder's own configuration. */
data class FoundBot(val name: String, val version: String?, val language: String?, val isTeam: Boolean)

/** What will be published for a bot, exactly: the staged copy described for the person. */
data class ReviewInfo(
    val name: String,
    val version: String?,
    val language: String,
    val license: String?,
    val isTeam: Boolean,
    /** Every file that will be published, slash separated, in order. */
    val files: List<String>,
)

/** The steps of the Submit or update bot wizard that this version has. A state never holds a token or a path outside the chosen folder. */
sealed interface SubmitState {
    /** Step 1. [found] is what was read from [folder], and [problem] says why [folder] is no bot. */
    data class Choose(val folder: Path? = null, val found: FoundBot? = null, val problem: Finding? = null) : SubmitState

    /** The folder is being read or staged, away from the screen's thread. */
    data class Working(val folder: Path, val staging: Boolean) : SubmitState

    /** Step 2 failed: every problem at once. [notes] are things to know that do not stop a submission. */
    data class Problems(val folder: Path, val findings: List<Finding>, val notes: List<String>) : SubmitState

    /** Step 2 passed. */
    data class Checked(val folder: Path, val found: FoundBot, val notes: List<String>) : SubmitState

    /** Step 3. The person may go on only with [publicConfirmed] and [authorConfirmed] both ticked. */
    data class Review(val folder: Path, val review: ReviewInfo, val publicConfirmed: Boolean = false, val authorConfirmed: Boolean = false) : SubmitState {
        val canContinue: Boolean get() = publicConfirmed && authorConfirmed
    }

    /** Step 4. [readiness] is null while the account's copy of the bot repository is being looked for. */
    data class Copy(val folder: Path, val review: ReviewInfo, val readiness: SubmissionReadiness? = null) : SubmitState

    /** Step 5, before sending: what is known about the staged copy. The person sends from here. */
    data class SendReady(val folder: Path, val review: ReviewInfo, val account: String, val ready: SendPrepared.Ready) : SubmitState {
        /** Nothing is offered to send when the Rumble already holds this version, or holds the name and version with other content. */
        val canSend: Boolean get() = ready.content != ContentState.ALREADY_IN_RUMBLE && ready.content != ContentState.DIFFERENT_CONTENT
    }

    /** The staged copy is being sent. */
    data class Sending(val folder: Path, val review: ReviewInfo) : SubmitState

    /** Steps 5 and 6 after sending: how it ended and, for a pull request, where it stands. */
    data class SendEnded(
        val folder: Path,
        val review: ReviewInfo,
        val result: SubmissionResult,
        val launchers: LauncherState,
        val progress: BotProgress? = null,
    ) : SubmitState
}

sealed interface SubmitInspected {
    data class Bot(val found: FoundBot) : SubmitInspected
    data class NotABot(val problem: Finding) : SubmitInspected
}

sealed interface SubmitChecked {
    data class Passed(val notes: List<String>) : SubmitChecked
    data class Failed(val findings: List<Finding>, val notes: List<String>) : SubmitChecked
}

sealed interface SubmitStaged {
    data class Staged(val review: ReviewInfo, val submission: StagedSubmission) : SubmitStaged
    data class Rejected(val findings: List<Finding>) : SubmitStaged

    /** Staging could not start, for example because of the staging folder. The message is the library's own sentence. */
    data class Refused(val message: String) : SubmitStaged
}

/** What the wizard needs of the disk. Every call may take a while and may block. A test uses real folders or a wrapper. */
interface SubmitPort {
    fun inspect(folder: Path): SubmitInspected
    fun check(folder: Path): SubmitChecked
    fun stage(folder: Path): SubmitStaged
}

/** The wizard's disk work, made of the bot project library. The original folder is only read; the copy goes in [stagingFolder]. */
class LibrarySubmitPort(private val stagingFolder: Path) : SubmitPort {

    override fun inspect(folder: Path): SubmitInspected =
        when (val inspection = BotProjects.inspect(folder)) {
            is Inspection.NotAProject -> SubmitInspected.NotABot(Finding(inspection.code, inspection.file, inspection.reason, inspection.reason))
            is Inspection.Found -> SubmitInspected.Bot(FoundBot(inspection.project.name ?: inspection.project.folderName, inspection.project.version, inspection.project.language, inspection.project.isTeam))
        }

    override fun check(folder: Path): SubmitChecked {
        val report = BotValidator.validate(folder)
        return if (report.passes) SubmitChecked.Passed(report.notes) else SubmitChecked.Failed(report.findings, report.notes)
    }

    override fun stage(folder: Path): SubmitStaged =
        when (val result = BotStaging.stage(folder, stagingFolder)) {
            is StagingResult.Staged -> {
                val staged = result.submission
                SubmitStaged.Staged(ReviewInfo(staged.name, staged.version, staged.language, staged.license, staged.isTeam, staged.files.map { it.path }), staged)
            }
            is StagingResult.Rejected -> SubmitStaged.Rejected(result.findings)
            is StagingResult.Refused -> SubmitStaged.Refused(result.message)
        }
}

/**
 * The behavior of the Submit or update bot wizard, without any window: choose the folder, check it, review what will
 * be published, make sure the account has its own copy of the bot repository, send, and follow the pull request. The
 * screen shows [state] whenever [onState] is called and passes its button presses to the methods below, each of which
 * returns at once.
 *
 * All work on the disk or on GitHub runs on [background]. All changes of state, and every call of [onState], run on
 * [post], one at a time and in order, which in the desktop program is the screen's event queue. A result that arrives
 * after the person went back, chose another folder or closed is thrown away, and so is every check still waiting for
 * its turn. The two confirmations start unticked every time the review is shown and nothing in the program ticks them;
 * the person cannot go on without both, and only the staged copy that was reviewed is ever sent.
 */
class SubmitController(
    private val port: SubmitPort,
    private val sendPort: SendPort,
    private val background: Executor,
    private val post: Executor,
    private val scheduler: AccountScheduler,
    private val onState: (SubmitState) -> Unit,
) {
    // Everything below is touched only on [post].
    private var epoch = 0
    private var scheduled: AutoCloseable? = null

    /** The staged copy of the review that is shown, and whether the author confirmed the license for it. */
    private var staged: StagedSubmission? = null
    private var certified = false

    private fun newEpoch(): Int {
        scheduled?.close()
        scheduled = null
        return ++epoch
    }

    @Volatile
    var state: SubmitState = SubmitState.Choose()
        private set

    /** The person chose [folder] in step 1. The folder is read, nothing else happens. */
    fun choose(folder: Path) = post.execute {
        val mine = newEpoch()
        show(SubmitState.Working(folder, staging = false))
        work({ port.inspect(folder) }) { inspected ->
            if (mine == epoch) show(
                when (inspected) {
                    is SubmitInspected.Bot -> SubmitState.Choose(folder, inspected.found)
                    is SubmitInspected.NotABot -> SubmitState.Choose(folder, null, inspected.problem)
                },
            )
        }
    }

    /** Step 2: checks the chosen folder, again and again as the person fixes it. */
    fun check() = post.execute {
        val folder = folderOf(state) ?: return@execute
        val mine = newEpoch()
        show(SubmitState.Working(folder, staging = false))
        work({ port.inspect(folder) to port.check(folder) }) { (inspected, checked) ->
            if (mine != epoch) return@work
            show(
                when {
                    inspected is SubmitInspected.NotABot -> SubmitState.Choose(folder, null, inspected.problem)
                    checked is SubmitChecked.Failed -> SubmitState.Problems(folder, checked.findings, checked.notes)
                    inspected is SubmitInspected.Bot && checked is SubmitChecked.Passed -> SubmitState.Checked(folder, inspected.found, checked.notes)
                    else -> SubmitState.Choose(folder)
                },
            )
        }
    }

    /** Step 3: stages a separate copy and shows what will be published. Works only after a passing check. */
    fun review() = post.execute {
        val checked = state as? SubmitState.Checked ?: return@execute
        val mine = newEpoch()
        show(SubmitState.Working(checked.folder, staging = true))
        work({ port.stage(checked.folder) }) { result ->
            if (mine != epoch) return@work
            show(
                when (result) {
                    is SubmitStaged.Staged -> {
                        staged = result.submission
                        SubmitState.Review(checked.folder, result.review)
                    }
                    is SubmitStaged.Rejected -> SubmitState.Problems(checked.folder, result.findings, emptyList())
                    is SubmitStaged.Refused -> SubmitState.Problems(checked.folder, listOf(Finding("STAGING_REFUSED", checked.folder.toString(), result.message, result.message)), emptyList())
                },
            )
        }
    }

    /** The person ticks or unticks that they understand that everything in the files becomes public. */
    fun setPublicConfirmed(value: Boolean) = post.execute {
        (state as? SubmitState.Review)?.let { show(it.copy(publicConfirmed = value)) }
    }

    /** The person ticks or unticks that they are the author and may publish the bot under the license in its files. */
    fun setAuthorConfirmed(value: Boolean) = post.execute {
        (state as? SubmitState.Review)?.let { show(it.copy(authorConfirmed = value)) }
    }

    /**
     * Goes on from the review to the check of the account's copy. Does nothing unless both confirmations are ticked, and
     * sends nothing: sending is a separate press, [send]. The author's confirmation is kept only to be handed to the
     * library at that press.
     */
    fun next() = post.execute {
        val review = state as? SubmitState.Review ?: return@execute
        if (!review.canContinue) return@execute
        certified = review.authorConfirmed
        checkCopy(review.folder, review.review)
    }

    /** Looks again, at once: for the copy and the App, for the state of a pull request that is open, or for a sent one. */
    fun checkNow() = post.execute {
        when (val now = state) {
            is SubmitState.Copy -> checkCopy(now.folder, now.review)
            is SubmitState.SendReady -> refreshExisting(now)
            is SubmitState.SendEnded -> (now.result as? SubmissionResult.Submitted)?.let { follow(now, it.pullRequestNumber) }
            else -> Unit
        }
    }

    /** Sends the reviewed staged copy, with the author's confirmation from the review. Does nothing unless sending is offered. */
    fun send() = post.execute {
        val ready = state as? SubmitState.SendReady ?: return@execute
        val submission = staged ?: return@execute
        if (!ready.canSend || !certified) return@execute
        val mine = newEpoch()
        show(SubmitState.Sending(ready.folder, ready.review))
        work({ sendPort.send(submission, certified) }, then = { result ->
            if (mine == epoch) {
                val ended = SubmitState.SendEnded(ready.folder, ready.review, result, ready.ready.launchers)
                show(ended)
                if (result is SubmissionResult.Submitted) follow(ended, result.pullRequestNumber)
            }
        }, onThrow = {
            if (mine == epoch) show(SubmitState.SendEnded(ready.folder, ready.review, SubmissionResult.Failed(""), ready.ready.launchers))
        })
    }

    private fun checkCopy(folder: Path, review: ReviewInfo) {
        val mine = newEpoch()
        show(SubmitState.Copy(folder, review))
        askReadiness(mine, folder, review)
    }

    private fun askReadiness(mine: Int, folder: Path, review: ReviewInfo) {
        work({ sendPort.readiness() }, then = { readiness ->
            if (mine != epoch) return@work
            if (readiness is SubmissionReadiness.Ready) {
                prepare(mine, folder, review, readiness.account)
                return@work
            }
            show(SubmitState.Copy(folder, review, readiness))
            // The person makes the copy or installs the App in the browser, and this finds it by itself.
            if (readiness is SubmissionReadiness.NoCopy || readiness is SubmissionReadiness.AppNotInstalled) {
                scheduled = scheduler.after(COPY_CHECK_SECONDS) { post.execute { if (mine == epoch) askReadiness(mine, folder, review) } }
            }
        }, onThrow = {
            if (mine == epoch) show(SubmitState.Copy(folder, review, SubmissionReadiness.Failed("")))
        })
    }

    private fun prepare(mine: Int, folder: Path, review: ReviewInfo, account: String) {
        val submission = staged ?: return
        work({ sendPort.prepare(submission, account) }, then = { prepared ->
            if (mine != epoch) return@work
            val next = when (prepared) {
                is SendPrepared.Ready -> SubmitState.SendReady(folder, review, account, prepared)
                is SendPrepared.SignInNeeded -> SubmitState.Copy(folder, review, SubmissionReadiness.SignInNeeded(prepared.message))
                is SendPrepared.Failed -> SubmitState.Copy(folder, review, SubmissionReadiness.Failed(prepared.message))
            }
            show(next)
            if (next is SubmitState.SendReady && next.ready.existing?.progress?.isOpen == true) scheduleRefresh(mine)
        }, onThrow = {
            if (mine == epoch) show(SubmitState.Copy(folder, review, SubmissionReadiness.Failed("")))
        })
    }

    /** Reads again where the account's open pull request for the bot stands, without changing anything. */
    private fun refreshExisting(ready: SubmitState.SendReady) {
        val existing = ready.ready.existing ?: return
        scheduled?.close()
        scheduled = null
        val mine = epoch
        work({ sendPort.progress(existing.number, ready.review.name, existing.version) }, then = { progress ->
            if (mine != epoch) return@work
            val now = state as? SubmitState.SendReady ?: return@work
            show(now.copy(ready = now.ready.copy(existing = existing.copy(progress = progress))))
            if (progress.isOpen) scheduleRefresh(mine)
        }, onThrow = {})
    }

    private fun scheduleRefresh(mine: Int) {
        scheduled = scheduler.after(FOLLOW_SECONDS) {
            post.execute { (state as? SubmitState.SendReady)?.let { if (mine == epoch) refreshExisting(it) } }
        }
    }

    /** Reads where the sent pull request stands, and keeps reading, calmly, until it cannot change any more or the person leaves. */
    private fun follow(ended: SubmitState.SendEnded, number: Int) {
        val version = ended.review.version ?: return
        scheduled?.close()
        scheduled = null
        val mine = epoch
        work({ sendPort.progress(number, ended.review.name, version) }, then = { progress ->
            if (mine != epoch) return@work
            val now = state as? SubmitState.SendEnded ?: return@work
            show(now.copy(progress = progress))
            if (progress.isOpen || progress == BotProgress.Merged) {
                scheduled = scheduler.after(FOLLOW_SECONDS) {
                    post.execute { if (mine == epoch) (state as? SubmitState.SendEnded)?.let { follow(it, number) } }
                }
            }
        }, onThrow = {
            if (mine == epoch) (state as? SubmitState.SendEnded)?.let { show(it.copy(progress = BotProgress.Failed(""))) }
        })
    }

    /** Goes back one step. The confirmations are not kept, and going back to step 1 reads the folder again. */
    fun back() = post.execute {
        val now = state
        val folder = folderOf(now) ?: return@execute
        newEpoch()
        when (now) {
            is SubmitState.Review -> show(SubmitState.Checked(folder, foundOf(now.review), emptyList()))
            is SubmitState.Copy -> backToReview(folder, now.review)
            is SubmitState.SendReady -> backToReview(folder, now.review)
            is SubmitState.SendEnded -> if (now.result !is SubmissionResult.Submitted) backToReview(folder, now.review)
            is SubmitState.Sending -> Unit
            is SubmitState.Checked, is SubmitState.Problems -> {
                val mine = epoch
                show(SubmitState.Working(folder, staging = false))
                work({ port.inspect(folder) }) { inspected ->
                    if (mine == epoch) show(
                        when (inspected) {
                            is SubmitInspected.Bot -> SubmitState.Choose(folder, inspected.found)
                            is SubmitInspected.NotABot -> SubmitState.Choose(folder, null, inspected.problem)
                        },
                    )
                }
            }
            else -> show(SubmitState.Choose(folder))
        }
    }

    /** The review is shown again for the same staged copy, with both confirmations unticked. */
    private fun backToReview(folder: Path, review: ReviewInfo) {
        certified = false
        show(SubmitState.Review(folder, review))
    }

    private fun foundOf(review: ReviewInfo) = FoundBot(review.name, review.version, review.language, review.isTeam)

    /** The person closes the wizard: results still on their way are thrown away. */
    fun close() = post.execute {
        newEpoch()
        staged = null
        certified = false
    }

    private fun folderOf(state: SubmitState): Path? = when (state) {
        is SubmitState.Choose -> state.folder
        is SubmitState.Working -> state.folder
        is SubmitState.Problems -> state.folder
        is SubmitState.Checked -> state.folder
        is SubmitState.Review -> state.folder
        is SubmitState.Copy -> state.folder
        is SubmitState.SendReady -> state.folder
        is SubmitState.Sending -> state.folder
        is SubmitState.SendEnded -> state.folder
    }

    private fun show(next: SubmitState) {
        state = next
        onState(next)
    }

    /** Runs [call] on [background], and its result on [post]. A call that throws shows the folder as no bot with a plain code. */
    private fun <T> work(call: () -> T, onThrow: () -> Unit = { failed() }, then: (T) -> Unit) {
        background.execute {
            val result = try {
                call()
            } catch (e: RuntimeException) {
                null
            }
            post.execute {
                @Suppress("UNCHECKED_CAST")
                if (result != null) then(result as T) else onThrow()
            }
        }
    }

    private companion object {
        /** The pace of the repeated looks for the copy and the App, and of the following of a pull request. */
        const val COPY_CHECK_SECONDS = 5L
        const val FOLLOW_SECONDS = 30L
    }

    private fun failed() {
        val folder = folderOf(state) ?: return
        show(SubmitState.Choose(folder, null, Finding("READ_FAILED", folder.toString(), "", "")))
    }
}
