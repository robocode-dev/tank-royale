package dev.robocode.tankroyale.rumble

import dev.robocode.tankroyale.botproject.BotProjects
import dev.robocode.tankroyale.botproject.BotValidator
import dev.robocode.tankroyale.botproject.Finding
import dev.robocode.tankroyale.botproject.Inspection
import dev.robocode.tankroyale.botproject.BotStaging
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

    /** The person went on. Sending is not built yet, so nothing was sent. */
    data class SendingComesLater(val folder: Path, val name: String) : SubmitState
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
    data class Staged(val review: ReviewInfo) : SubmitStaged
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
                SubmitStaged.Staged(ReviewInfo(staged.name, staged.version, staged.language, staged.license, staged.isTeam, staged.files.map { it.path }))
            }
            is StagingResult.Rejected -> SubmitStaged.Rejected(result.findings)
            is StagingResult.Refused -> SubmitStaged.Refused(result.message)
        }
}

/**
 * The behavior of the first three steps of the Submit or update bot wizard, without any window: choose the folder,
 * check it, and review what will be published. The screen shows [state] whenever [onState] is called and passes its
 * button presses to the methods below, each of which returns at once.
 *
 * All work on the disk runs on [background]. All changes of state, and every call of [onState], run on [post], one at a
 * time and in order, which in the desktop program is the screen's event queue. A result that arrives after the person
 * went back, chose another folder or closed is thrown away. The two confirmations start unticked every time the review
 * is shown and nothing in the program ticks them; the person cannot go on without both.
 */
class SubmitController(
    private val port: SubmitPort,
    private val background: Executor,
    private val post: Executor,
    private val onState: (SubmitState) -> Unit,
) {
    // Everything below is touched only on [post].
    private var epoch = 0

    @Volatile
    var state: SubmitState = SubmitState.Choose()
        private set

    /** The person chose [folder] in step 1. The folder is read, nothing else happens. */
    fun choose(folder: Path) = post.execute {
        epoch++
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

    /** Step 2: checks the chosen folder, again and again as the person fixes it. */
    fun check() = post.execute {
        val folder = folderOf(state) ?: return@execute
        epoch++
        val mine = epoch
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
        epoch++
        val mine = epoch
        show(SubmitState.Working(checked.folder, staging = true))
        work({ port.stage(checked.folder) }) { staged ->
            if (mine != epoch) return@work
            show(
                when (staged) {
                    is SubmitStaged.Staged -> SubmitState.Review(checked.folder, staged.review)
                    is SubmitStaged.Rejected -> SubmitState.Problems(checked.folder, staged.findings, emptyList())
                    is SubmitStaged.Refused -> SubmitState.Problems(checked.folder, listOf(Finding("STAGING_REFUSED", checked.folder.toString(), staged.message, staged.message)), emptyList())
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

    /** Goes on from the review. Does nothing unless both confirmations are ticked, and sends nothing in any case. */
    fun next() = post.execute {
        val review = state as? SubmitState.Review ?: return@execute
        if (review.canContinue) show(SubmitState.SendingComesLater(review.folder, review.review.name))
    }

    /** Goes back one step. The confirmations are not kept, and going back to step 1 reads the folder again. */
    fun back() = post.execute {
        val now = state
        val folder = folderOf(now) ?: return@execute
        epoch++
        when (now) {
            is SubmitState.Review -> show(SubmitState.Checked(folder, foundOf(now.review), emptyList()))
            is SubmitState.SendingComesLater -> show(SubmitState.Checked(folder, FoundBot(now.name, null, null, false), emptyList()))
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

    private fun foundOf(review: ReviewInfo) = FoundBot(review.name, review.version, review.language, review.isTeam)

    /** The person closes the wizard: results still on their way are thrown away. */
    fun close() = post.execute { epoch++ }

    private fun folderOf(state: SubmitState): Path? = when (state) {
        is SubmitState.Choose -> state.folder
        is SubmitState.Working -> state.folder
        is SubmitState.Problems -> state.folder
        is SubmitState.Checked -> state.folder
        is SubmitState.Review -> state.folder
        is SubmitState.SendingComesLater -> state.folder
    }

    private fun show(next: SubmitState) {
        state = next
        onState(next)
    }

    /** Runs [call] on [background], and its result on [post]. A call that throws shows the folder as no bot with a plain code. */
    private fun <T> work(call: () -> T, then: (T) -> Unit) {
        background.execute {
            val result = try {
                call()
            } catch (e: RuntimeException) {
                null
            }
            post.execute {
                @Suppress("UNCHECKED_CAST")
                if (result != null) then(result as T) else failed()
            }
        }
    }

    private fun failed() {
        val folder = folderOf(state) ?: return
        show(SubmitState.Choose(folder, null, Finding("READ_FAILED", folder.toString(), "", "")))
    }
}
