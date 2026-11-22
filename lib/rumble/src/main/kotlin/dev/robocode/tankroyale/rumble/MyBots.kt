package dev.robocode.tankroyale.rumble

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.booleanOrNull
import kotlinx.serialization.json.contentOrNull
import java.io.IOException
import java.util.concurrent.Executor

/** A pull request the program sent for a bot: the branch `submit/<name>-<version>` names the bot and its version. */
data class SentPullRequest(val number: Int, val name: String, val version: String, val open: Boolean, val merged: Boolean, val url: String)

/** Where a bot of My bots stands. */
sealed interface MyBotStanding {
    /** Where its newest pull request stands, or that this version is in the Rumble. */
    data class Progress(val progress: BotProgress) : MyBotStanding

    /** The bot is in the catalog but not active any more; [status] is the catalog's word for it, such as `retired`. */
    data class NotActive(val status: String) : MyBotStanding
}

/**
 * One row of My bots. [version] is the newest version the account sent or owns, [language] the catalog's platform when
 * the catalog knows the bot, [inRumble] the version in the Rumble when the row shows a newer one on its way, and [page]
 * the page Open shows: the newest pull request, else the bot's folder in the bot repository.
 */
data class MyBotRow(
    val name: String,
    val version: String,
    val language: String?,
    val standing: MyBotStanding,
    val inRumble: String?,
    val page: String?,
)

/** Builds the rows of My bots from the two readings, without a window and without GitHub. */
object MyBots {
    // The program titles its pull requests "Add <name> <version>" or "Update <name> to <version>"; names and versions have no spaces.
    private val ADD = Regex("^Add (\\S+) (\\S+)$")
    private val UPDATE = Regex("^Update (\\S+) to (\\S+)$")

    /**
     * True for a row that is done with: a retired bot, or a sent bot whose pull request was closed without being
     * merged. My bots hides these unless the person asks to see them. Every other row shows, a disqualified bot too.
     */
    fun isInactive(row: MyBotRow): Boolean =
        row.standing == MyBotStanding.NotActive("retired") || row.standing == MyBotStanding.Progress(BotProgress.Closed)

    /** The rows to show: all of them, or without the inactive ones. */
    fun shown(rows: List<MyBotRow>, showInactive: Boolean): List<MyBotRow> = if (showInactive) rows else rows.filterNot { isInactive(it) }

    /**
     * The bot name and version of a pull request the program sent: read from its title, and only when its branch is
     * one the program names for that bot, `submit/<name>-` followed by the version it was first sent with. Sending a
     * new version while the pull request is open changes the title and keeps the branch, so the title holds the
     * version. A pull request whose title was changed by hand is not recognised. Null for any other pull request.
     */
    fun nameAndVersion(branch: String, title: String): Pair<String, String>? {
        val match = ADD.matchEntire(title) ?: UPDATE.matchEntire(title) ?: return null
        val (name, version) = match.groupValues[1] to match.groupValues[2]
        return if (branch.startsWith("submit/$name-") && branch.length > "submit/$name-".length) name to version else null
    }

    /**
     * One row per bot name of [account]: its own catalog entries and the pull requests it sent. An open pull request
     * shows its state, read by [progressOf], before the version in the Rumble; with none open, an active catalog entry
     * shows that it is in the Rumble, a merged pull request newer than the catalog shows that it is merged, a catalog
     * entry that is not active shows its status, and a pull request that was closed shows that. Entries of other
     * accounts are never listed. [owners] are the names the catalog may give the account as owner: its login and the
     * owner records that list it. Rows are sorted by name.
     */
    fun rows(
        owners: Set<String>,
        catalog: Catalog,
        pulls: List<SentPullRequest>,
        botsRepositoryPage: String,
        progressOf: (SentPullRequest) -> BotProgress,
    ): List<MyBotRow> {
        val owned = catalog.bots.filter { bot -> owners.any { it.equals(bot.owner, ignoreCase = true) } }
        // The catalog's newest entry of a bot is the one added last; the catalog's order breaks a tie.
        val position = catalog.bots.withIndex().associate { (index, bot) -> bot to index }
        val names = (owned.map { it.name } + pulls.map { it.name }).toSortedSet(String.CASE_INSENSITIVE_ORDER)
        return names.map { name ->
            val entries = owned.filter { it.name.equals(name, ignoreCase = true) }
            val active = entries.lastOrNull { it.isActive }
            val latest = entries.maxWithOrNull(compareBy<CatalogBot>({ it.addedAt }, { position[it] ?: 0 }))
            // An open pull request is shown before a newer one that was closed or merged.
            val sent = pulls.filter { it.name.equals(name, ignoreCase = true) }.sortedByDescending { it.number }
            val newest = sent.firstOrNull { it.open } ?: sent.firstOrNull()
            val language = latest?.platform
            val folder = (active ?: latest)?.let { "$botsRepositoryPage/tree/${catalog.commit}/${it.path}" }
            val displayName = active?.name ?: latest?.name ?: newest!!.name
            val newestInCatalog = newest != null && entries.any { it.version == newest.version }
            when {
                newest != null && newest.open ->
                    MyBotRow(displayName, newest.version, language, MyBotStanding.Progress(progressOf(newest)), active?.version?.takeIf { it != newest.version }, newest.url)
                newest != null && newest.merged && !newestInCatalog ->
                    MyBotRow(displayName, newest.version, language, MyBotStanding.Progress(BotProgress.Merged), active?.version, newest.url)
                active != null ->
                    MyBotRow(displayName, active.version, language, MyBotStanding.Progress(BotProgress.InCatalog(active.version)), null, folder)
                latest != null -> latest.let { last ->
                    MyBotRow(displayName, last.version, language, MyBotStanding.NotActive(last.status), null, folder)
                }
                else -> MyBotRow(displayName, newest!!.version, null, MyBotStanding.Progress(if (newest.merged) BotProgress.Merged else BotProgress.Closed), null, newest.url)
            }
        }
    }
}

/** What reading My bots gave. */
sealed interface MyBotsReading {
    data class Read(val account: String, val rows: List<MyBotRow>) : MyBotsReading

    /** No saved sign-in, or GitHub no longer accepts it. */
    data class SignInNeeded(val message: String) : MyBotsReading

    /** The list could not be read; [message] says why in plain words. */
    data class Failed(val message: String) : MyBotsReading
}

/** What the My bots screen needs of GitHub, so a test can stand in for it. */
fun interface MyBotsPort {
    fun read(): MyBotsReading
}

/**
 * Reads My bots through the saved sign-in: the signed-in account, the Rumble's catalog and the pull requests the
 * account sent to the bot repository, and where each open one stands. It only reads; it never comments, sends or
 * changes anything.
 */
class LibraryMyBotsPort(
    private val environment: RumbleEnvironment,
    private val flow: SignInFlow,
    private val api: GitHubApi,
    private val catalogSource: CatalogSource = GitHubCatalogSource(environment),
    private val progress: BotProgressReading = SubmissionProgress(environment, api),
) : MyBotsPort {
    private val repository = "${RumbleEnvironments.owner}/${environment.botsRepository}"

    override fun read(): MyBotsReading {
        val tokens = when (val renewal = flow.renewSaved()) {
            is SavedRenewal.Renewed -> renewal.tokens
            is SavedRenewal.SignInNeeded -> return MyBotsReading.SignInNeeded(renewal.message)
            is SavedRenewal.Failed -> return MyBotsReading.Failed(renewal.message)
        }
        return try {
            val account = account(tokens.accessToken)
            val snapshot = RumbleSnapshot.read(api, environment, catalogSource, tokens.accessToken)
            val catalog = snapshot.catalog ?: return MyBotsReading.Failed(snapshot.problem ?: "The Rumble's catalog could not be read.")
            val pulls = sentPullRequests(tokens.accessToken, account)
            val owners = MyBots.ownersOf(account, snapshot.rules?.owners.orEmpty())
            val rows = MyBots.rows(owners, catalog, pulls, "https://github.com/$repository") { pull ->
                progress.read(tokens, pull.number, pull.name, pull.version, catalog)
            }
            MyBotsReading.Read(account, rows)
        } catch (e: SignInRefused) {
            MyBotsReading.SignInNeeded(e.message.orEmpty())
        } catch (e: IOException) {
            MyBotsReading.Failed(e.message.orEmpty())
        }
    }

    private fun account(token: String): String {
        val answer = api.get("/user", token)
        if (answer.status == 401) throw SignInRefused()
        if (answer.status != 200) throw IOException(unexpected(answer.status))
        return (json(answer.body) as? JsonObject)?.string("login") ?: throw IOException("GitHub did not say which account is signed in.")
    }

    /**
     * The pull requests of the bot repository that [account] sent with the program, newest first: up to [MAX_PAGES]
     * pages of a hundred of the whole repository's pull requests, read until a page is not full.
     */
    private fun sentPullRequests(token: String, account: String): List<SentPullRequest> {
        val found = mutableListOf<SentPullRequest>()
        for (page in 1..MAX_PAGES) {
            val answer = api.get("/repos/$repository/pulls?state=all&sort=created&direction=desc&per_page=100&page=$page", token)
            if (answer.status == 401) throw SignInRefused()
            if (answer.status == 429 || (answer.status == 403 && answer.body.contains("rate limit", ignoreCase = true))) {
                throw IOException("GitHub is limiting requests. Try again later.")
            }
            if (answer.status != 200) throw IOException(unexpected(answer.status))
            found += MyBots.sentPullRequests(answer.body, account, repository)
            if (MyBots.pullRequestCount(answer.body) < 100) break
        }
        return found
    }

    private companion object {
        const val MAX_PAGES = 10
    }

    private fun unexpected(status: Int) = "GitHub gave an unexpected answer (HTTP $status). Try again later."

    private fun json(text: String) = try {
        Json.parseToJsonElement(text)
    } catch (e: Exception) {
        null
    }

    private fun JsonObject.string(key: String): String? = (this[key] as? JsonPrimitive)?.takeIf { it.isString }?.contentOrNull
}

/** Reads GitHub's list of pull requests: those [account] sent with the program's branch names, in [repository]. */
internal fun MyBots.sentPullRequests(body: String, account: String, repository: String): List<SentPullRequest> {
    fun JsonObject.string(key: String): String? = (this[key] as? JsonPrimitive)?.takeIf { it.isString }?.contentOrNull
    val pulls = (try { Json.parseToJsonElement(body) } catch (e: Exception) { null }) as? JsonArray
        ?: throw IOException("GitHub's list of pull requests could not be understood.")
    return pulls.mapNotNull { element ->
        val pull = element as? JsonObject ?: return@mapNotNull null
        val author = (pull["user"] as? JsonObject)?.string("login")
        if (!account.equals(author, ignoreCase = true)) return@mapNotNull null
        val branch = (pull["head"] as? JsonObject)?.string("ref") ?: return@mapNotNull null
        val (name, version) = MyBots.nameAndVersion(branch, pull.string("title").orEmpty()) ?: return@mapNotNull null
        val number = (pull["number"] as? JsonPrimitive)?.contentOrNull?.toIntOrNull() ?: return@mapNotNull null
        val url = pull.string("html_url")?.takeIf { RumbleLinks.isGitHubPage(it) } ?: "https://github.com/$repository/pull/$number"
        val merged = pull.string("merged_at") != null || (pull["merged"] as? JsonPrimitive)?.booleanOrNull == true
        SentPullRequest(number, name, version, open = pull.string("state") == "open", merged = merged, url = url)
    }
}

/** The state of the My bots screen. */
sealed interface MyBotsState {
    data object Reading : MyBotsState
    data class Shown(val account: String, val rows: List<MyBotRow>) : MyBotsState
    data class SignInNeeded(val details: String) : MyBotsState
    data class Failed(val details: String) : MyBotsState
}

/**
 * The behaviour of the My bots screen: it reads the list once when started, away from the screen's thread, and hands
 * the state to [onState] through [post]. A reading that arrives after [close] is thrown away. There is no refresh:
 * opening the screen again reads the list again.
 */
class MyBotsController(
    private val port: MyBotsPort,
    private val background: Executor,
    private val post: Executor,
    private val onState: (MyBotsState) -> Unit,
) {
    @Volatile
    var state: MyBotsState = MyBotsState.Reading
        private set

    @Volatile
    private var closed = false

    fun start() {
        background.execute {
            val next = try {
                when (val reading = port.read()) {
                    is MyBotsReading.Read -> MyBotsState.Shown(reading.account, reading.rows)
                    is MyBotsReading.SignInNeeded -> MyBotsState.SignInNeeded(reading.message)
                    is MyBotsReading.Failed -> MyBotsState.Failed(reading.message)
                }
            } catch (e: Exception) {
                MyBotsState.Failed(e.message.orEmpty())
            }
            post.execute {
                if (!closed) {
                    state = next
                    onState(next)
                }
            }
        }
    }

    fun close() {
        closed = true
    }
}

/** The resource keys for what My bots shows, so the screen holds no sentence of its own. */
object MyBotsTexts {
    /** The key and its arguments for where a row's bot stands. */
    fun forStanding(standing: MyBotStanding): Pair<String, List<String>> = when (standing) {
        // The catalog's own words for a bot that is not active; a word this program does not know is shown as it is.
        is MyBotStanding.NotActive -> when (standing.status) {
            "retired" -> "rumble.mybots.status.retired" to emptyList()
            "superseded" -> "rumble.mybots.status.superseded" to emptyList()
            "disqualified" -> "rumble.mybots.status.disqualified" to emptyList()
            else -> "rumble.mybots.not_active" to listOf(standing.status)
        }
        is MyBotStanding.Progress -> when (val p = standing.progress) {
            BotProgress.WaitingForChecks -> "rumble.submit.progress_waiting_checks" to emptyList()
            is BotProgress.BeingChecked -> "rumble.submit.progress_being_checked" to emptyList()
            is BotProgress.SentBack -> "rumble.mybots.sent_back" to listOf(p.reason)
            BotProgress.WaitingForModerator -> "rumble.submit.progress_waiting_moderator" to emptyList()
            BotProgress.Accepted -> "rumble.submit.progress_accepted" to emptyList()
            BotProgress.Merged -> "rumble.submit.progress_merged" to emptyList()
            is BotProgress.InCatalog -> "rumble.mybots.in_rumble" to emptyList()
            BotProgress.Closed -> "rumble.submit.progress_closed" to emptyList()
            is BotProgress.SignInNeeded -> "rumble.submit.progress_sign_in" to emptyList()
            is BotProgress.Failed -> "rumble.submit.progress_failed" to emptyList()
        }
    }

    /** Every key the screen can show, for the test that each is in every language. */
    fun allKeys(): List<String> = listOf(
        "rumble.mybots.title", "rumble.mybots.reading", "rumble.mybots.signed_in_as", "rumble.mybots.none",
        "rumble.mybots.show_inactive", "rumble.mybots.hidden",
        "rumble.mybots.column.name", "rumble.mybots.column.version", "rumble.mybots.column.language", "rumble.mybots.column.state",
        "rumble.mybots.not_active", "rumble.mybots.status.retired", "rumble.mybots.status.superseded", "rumble.mybots.status.disqualified",
        "rumble.mybots.sent_back", "rumble.mybots.in_rumble", "rumble.mybots.in_rumble_now",
        "rumble.mybots.unknown_language", "rumble.mybots.sign_in_needed", "rumble.mybots.failed", "rumble.mybots.details",
        "rumble.mybots.button.open", "rumble.mybots.button.update", "rumble.mybots.button.submit_new", "rumble.mybots.button.account",
        "rumble.submit.progress_waiting_checks", "rumble.submit.progress_being_checked", "rumble.submit.progress_waiting_moderator",
        "rumble.submit.progress_accepted", "rumble.submit.progress_merged", "rumble.submit.progress_closed",
        "rumble.submit.progress_sign_in", "rumble.submit.progress_failed",
    )
}

/** How many pull requests a page of GitHub's list holds, to know whether another page may follow. */
internal fun MyBots.pullRequestCount(body: String): Int =
    ((try { Json.parseToJsonElement(body) } catch (e: Exception) { null }) as? JsonArray)?.size ?: 0

/**
 * The names the catalog may give [account] as owner: the id of every owner record that lists the login among its
 * accounts, so a person signed in with another account of the same owner still finds the bots, and the login itself,
 * as for a bot whose owner has no record yet, unless another owner's record has that id without listing the login.
 */
fun MyBots.ownersOf(account: String, records: List<OwnerRecord>): Set<String> {
    val listing = records.filter { record -> record.accounts.any { it.equals(account, ignoreCase = true) } }.map { it.ownerId }.toSet()
    val takenByAnother = records.any { it.ownerId.equals(account, ignoreCase = true) && it.ownerId !in listing }
    return if (takenByAnother) listing else listing + account
}
