package dev.robocode.tankroyale.rumble

import dev.robocode.tankroyale.botproject.StagedSubmission
import java.io.IOException

/** What the Rumble's catalog says about the content of the staged copy, found by its checksum. */
enum class ContentState {
    /** The catalog holds no bot of this name and version, so this is a new version to send. */
    NEW,

    /** The catalog holds this name and version with exactly this content, so there is nothing to send. */
    ALREADY_IN_RUMBLE,

    /** The catalog holds this name and version with other content; a new version is needed. */
    DIFFERENT_CONTENT,

    /** The catalog could not be read, so nothing was compared. The bot repository checks it anyway. */
    NOT_CHECKED,
}

/** An open pull request of the account for the bot, found again through GitHub, and where it stands. */
data class ExistingPullRequest(val number: Int, val version: String, val progress: BotProgress)

/** What is known about the staged copy just before it is sent, all of it advice. */
sealed interface SendPrepared {
    data class Ready(val launchers: LauncherState, val content: ContentState, val existing: ExistingPullRequest?) : SendPrepared

    data class SignInNeeded(val message: String) : SendPrepared

    data class Failed(val message: String) : SendPrepared
}

/**
 * What the last steps of the wizard need of GitHub. Every call may take a while and may block. The program uses
 * [LibrarySendPort]; a test uses a fake that records what it is asked. A token never leaves the port.
 */
interface SendPort {
    /** Whether the signed-in account has its own copy of the bot repository with the sign-in App installed. */
    fun readiness(): SubmissionReadiness

    /** Compares the launchers and the content of [submission], and looks for the account's open pull request for it. */
    fun prepare(submission: StagedSubmission, account: String): SendPrepared

    /** Sends [submission]. Without [authorCertifiesLicense] nothing is sent. */
    fun send(submission: StagedSubmission, authorCertifiesLicense: Boolean): SubmissionResult

    /** Where the pull request [number] for the bot [name] [version] stands. */
    fun progress(number: Int, name: String, version: String): BotProgress
}

/**
 * The wizard's GitHub work, made of the libraries the command line uses, on the same saved sign-in. The saved
 * sign-in is renewed once and the new token is kept in memory for the life of this object, as the decision that the
 * desktop program uses the libraries in its own process accepts; it is renewed again when GitHub stops accepting it.
 */
class LibrarySendPort(
    private val environment: RumbleEnvironment,
    private val flow: SignInFlow,
    private val api: GitHubApi,
    private val sending: BotSending = BotSubmission(environment, api),
    private val reading: BotProgressReading = SubmissionProgress(environment, api),
    private val catalogSource: CatalogSource = GitHubCatalogSource(environment),
) : SendPort {

    private class NoSignIn(val reason: String, val failed: Boolean) : Exception(reason, null, false, false)

    @Volatile
    private var kept: GitHubTokens? = null

    private fun tokens(renew: Boolean = false): GitHubTokens {
        kept?.takeIf { !renew }?.let { return it }
        return when (val renewal = flow.renewSaved()) {
            is SavedRenewal.Renewed -> renewal.tokens.also { kept = it }
            is SavedRenewal.SignInNeeded -> throw NoSignIn(renewal.message, failed = false)
            is SavedRenewal.Failed -> throw NoSignIn(renewal.message, failed = true)
        }
    }

    /** Runs [call] with the kept token; when GitHub says the sign-in is not accepted, renews once and tries again. */
    private fun <T> withSignIn(refused: (T) -> Boolean, call: (GitHubTokens) -> T): T {
        val first = call(tokens())
        if (!refused(first)) return first
        return call(tokens(renew = true))
    }

    override fun readiness(): SubmissionReadiness =
        try {
            withSignIn({ it is SubmissionReadiness.SignInNeeded }) { sending.checkReadiness(it) }
        } catch (e: NoSignIn) {
            if (e.failed) SubmissionReadiness.Failed(e.reason) else SubmissionReadiness.SignInNeeded(e.reason)
        }

    override fun prepare(submission: StagedSubmission, account: String): SendPrepared =
        try {
            val tokens = tokens()
            val launchers = sending.compareLaunchers(tokens, submission)
            val catalog = readCatalog()
            val content = content(submission, catalog)
            SendPrepared.Ready(launchers, content, existing(tokens, account, submission.name, catalog))
        } catch (e: NoSignIn) {
            if (e.failed) SendPrepared.Failed(e.reason) else SendPrepared.SignInNeeded(e.reason)
        } catch (e: SignInRefused) {
            kept = null
            SendPrepared.SignInNeeded(e.message.orEmpty())
        } catch (e: IOException) {
            SendPrepared.Failed(e.message.orEmpty())
        }

    override fun send(submission: StagedSubmission, authorCertifiesLicense: Boolean): SubmissionResult =
        try {
            withSignIn({ it is SubmissionResult.SignInNeeded }) { sending.submit(it, submission, authorCertifiesLicense) }
        } catch (e: NoSignIn) {
            if (e.failed) SubmissionResult.Failed(e.reason) else SubmissionResult.SignInNeeded(e.reason)
        }

    override fun progress(number: Int, name: String, version: String): BotProgress =
        try {
            val catalog = readCatalog()
            withSignIn({ it is BotProgress.SignInNeeded }) { reading.read(it, number, name, version, catalog) }
        } catch (e: NoSignIn) {
            if (e.failed) BotProgress.Failed(e.reason) else BotProgress.SignInNeeded(e.reason)
        }

    private fun readCatalog(): Catalog? =
        try {
            Catalog.parse(catalogSource.catalogText(RumbleHead.commit(api, environment)))
        } catch (e: CatalogUnreadable) {
            null
        } catch (e: IOException) {
            null
        }

    /** The checksum of the staged copy against what the catalog holds for the same name and version. */
    private fun content(submission: StagedSubmission, catalog: Catalog?): ContentState {
        if (catalog == null) return ContentState.NOT_CHECKED
        val same = catalog.bots.filter { it.name == submission.name && it.version == submission.version }
        return when {
            same.isEmpty() -> ContentState.NEW
            same.any { it.sourceHash == submission.treeHash } -> ContentState.ALREADY_IN_RUMBLE
            else -> ContentState.DIFFERENT_CONTENT
        }
    }

    /** The account's pull request for the bot, when it is still open; a failure to look is not a reason to stop. */
    private fun existing(tokens: GitHubTokens, account: String, name: String, catalog: Catalog?): ExistingPullRequest? {
        var sent: SentBot? = null
        try {
            sent = reading.find(tokens, account, name)
        } catch (e: SignInRefused) {
            throw e
        } catch (e: IOException) {
            // Advice only: a Rumble that cannot be looked at does not stop the send.
        }
        if (sent == null) return null
        val progress = reading.read(tokens, sent.pullRequestNumber, name, sent.version, catalog)
        return if (progress.isOpen) ExistingPullRequest(sent.pullRequestNumber, sent.version, progress) else null
    }
}

/** True while the pull request can still change: it is neither merged, in the catalog nor closed, and no problem hides it. */
val BotProgress.isOpen: Boolean
    get() = when (this) {
        BotProgress.WaitingForChecks, is BotProgress.BeingChecked, is BotProgress.SentBack, BotProgress.WaitingForModerator, BotProgress.Accepted -> true
        BotProgress.Merged, is BotProgress.InCatalog, BotProgress.Closed, is BotProgress.SignInNeeded, is BotProgress.Failed -> false
    }
