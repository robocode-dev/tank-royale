package dev.robocode.tankroyale.rumble

import dev.robocode.tankroyale.botproject.StagedSubmission
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.booleanOrNull
import kotlinx.serialization.json.contentOrNull
import java.io.IOException
import java.nio.file.Files
import java.security.MessageDigest
import java.util.Base64

/** Whether the signed-in account can send a bot to the Rumble from its own copy of the bot repository. */
sealed interface SubmissionReadiness {
    /** The account has a copy of the Rumble's bot repository, [copy] (`account/name`), and the sign-in App is installed on it. */
    data class Ready(val account: String, val copy: String) : SubmissionReadiness

    /** The account has no copy yet; the person makes one on [pageUrl]. */
    data class NoCopy(val account: String, val pageUrl: String) : SubmissionReadiness

    /** The account has the copy but the Rumble sign-in App is not installed on it; the person installs it on [installUrl]. */
    data class AppNotInstalled(val account: String, val installUrl: String) : SubmissionReadiness

    /** The account has a repository of the same name that is not a copy of the Rumble's bot repository. */
    data class NotRumblesRepository(val account: String, val repositoryUrl: String) : SubmissionReadiness

    /** GitHub did not accept the sign-in; the person must sign in again. */
    data class SignInNeeded(val message: String) : SubmissionReadiness

    /** Something else went wrong; [message] says what in plain words. */
    data class Failed(val message: String) : SubmissionReadiness
}

/** What sending a bot did to the account's pull request for it. */
enum class SubmissionOutcome {
    /** A new pull request was opened. */
    OPENED,

    /** An earlier attempt had left the branch with exactly these files, and the pull request was opened from it. */
    RESUMED,

    /** The account's open pull request already held exactly these files, so nothing was changed. */
    UNCHANGED,

    /** The account's open pull request got one new commit and a new title and text. */
    UPDATED,
}

/** How sending a bot to the Rumble ended. */
sealed interface SubmissionResult {
    data class Submitted(val pullRequestUrl: String, val pullRequestNumber: Int, val branch: String, val outcome: SubmissionOutcome) : SubmissionResult

    /** The account is not ready; [readiness] says what the person must do first. Nothing was changed on GitHub. */
    data class SetupNeeded(val readiness: SubmissionReadiness) : SubmissionResult

    /** The bot was not sent because of what was asked, for example the author's confirmation is missing. */
    data class Refused(val message: String) : SubmissionResult

    /** GitHub did not accept the sign-in; the person must sign in again. */
    data class SignInNeeded(val message: String) : SubmissionResult

    /** Something else went wrong; [message] says what in plain words. */
    data class Failed(val message: String) : SubmissionResult
}

/**
 * Sends a staged bot to the Rumble as a pull request from the signed-in account's own copy of the bot repository,
 * the way the outside-account experiment proved by hand.
 *
 * The files are committed through GitHub's low-level Git interface, because only that can set the executable mode
 * and put many files in one commit. Nothing becomes visible before every committed file has been compared with the
 * staged one: the blobs, the tree and the commit are loose objects until the branch is created or moved, which is
 * the last step before the pull request. An account has at most one open pull request per bot, and no branch is ever
 * forced, overwritten or deleted.
 */
/** What the commands need of sending a bot, so a test can stand in for GitHub. */
interface BotSending {
    fun checkReadiness(tokens: GitHubTokens): SubmissionReadiness

    /** Compares the bot's launchers with the Rumble's starter launchers. Advice only; it never throws and never stops a send. */
    fun compareLaunchers(tokens: GitHubTokens, submission: StagedSubmission): LauncherState

    fun submit(tokens: GitHubTokens, submission: StagedSubmission, authorCertifiesLicense: Boolean): SubmissionResult
}

/** What every way of sending a bot says before it sends. A bot is sent to a public repository, so nothing in it is private. */
object SourceNotice {
    const val TEXT = "Everything in these files becomes public the moment the bot is sent: anyone can read and copy the source, " +
        "and it stays in the history even if the pull request is closed."
}

class BotSubmission(
    private val environment: RumbleEnvironment,
    private val api: GitHubApi,
) : BotSending {
    private val owner = RumbleEnvironments.owner
    private val repository = environment.botsRepository
    private val upstream = "/repos/$owner/$repository"

    private class Stop(val signInNeeded: Boolean, message: String) : Exception(message, null, false, false)

    /** Checks, by reading only, whether the account can send a bot. */
    override fun compareLaunchers(tokens: GitHubTokens, submission: StagedSubmission): LauncherState =
        LauncherCheck(environment, api).compare(tokens, submission)

    override fun checkReadiness(tokens: GitHubTokens): SubmissionReadiness =
        try {
            readiness(tokens.accessToken)
        } catch (stop: Stop) {
            if (stop.signInNeeded) SubmissionReadiness.SignInNeeded(stop.message.orEmpty()) else SubmissionReadiness.Failed(stop.message.orEmpty())
        } catch (e: IOException) {
            SubmissionReadiness.Failed(OFFLINE)
        }

    /**
     * Sends [submission]. [authorCertifiesLicense] must be true only when the author has confirmed that they agree to
     * publish the source under its declared license; without it nothing is sent.
     */
    override fun submit(tokens: GitHubTokens, submission: StagedSubmission, authorCertifiesLicense: Boolean): SubmissionResult {
        if (!authorCertifiesLicense) {
            return SubmissionResult.Refused("The author has not confirmed that the source may be published under its license. Nothing was sent.")
        }
        val version = submission.version
        if (!safe(submission.name) || version == null || !safe(version) || !safe(submission.language)) {
            return SubmissionResult.Refused(
                "The bot's name and version may hold only letters, digits, dots, hyphens and underscores. Nothing was sent."
            )
        }
        if (submission.files.isEmpty()) return SubmissionResult.Refused("The staged copy holds no files. Nothing was sent.")
        val files = try {
            readFiles(submission)
        } catch (e: IOException) {
            return SubmissionResult.Failed("A file of the staged copy could not be read. Stage the bot again.")
        }
        return try {
            send(tokens.accessToken, submission, version, files)
        } catch (stop: Stop) {
            if (stop.signInNeeded) SubmissionResult.SignInNeeded(stop.message.orEmpty()) else SubmissionResult.Failed(stop.message.orEmpty())
        } catch (e: IOException) {
            SubmissionResult.Failed(OFFLINE)
        }
    }

    // ---- Readiness ----

    private fun readiness(token: String): SubmissionReadiness {
        val account = whoAmI(token)
        val slug = environment.signInAppSlug ?: throw Stop(false, "This Rumble has no sign-in App yet.")
        val answer = api.get("/repos/$account/$repository", token)
        if (answer.status == 404) return SubmissionReadiness.NoCopy(account, "https://github.com/$owner/$repository/fork")
        expect(answer, 200)
        val repo = obj(answer.body)
        val parent = (repo?.get("parent") as? JsonObject)?.string("full_name")
        val isCopy = (repo?.get("fork") as? JsonPrimitive)?.booleanOrNull == true && parent.equals("$owner/$repository", ignoreCase = true)
        if (!isCopy) {
            return SubmissionReadiness.NotRumblesRepository(account, repo?.string("html_url") ?: "https://github.com/$account/$repository")
        }
        return if (appInstalledOnCopy(account, slug, token)) SubmissionReadiness.Ready(account, "$account/$repository")
        else SubmissionReadiness.AppNotInstalled(account, "https://github.com/apps/$slug/installations/new")
    }

    private fun appInstalledOnCopy(account: String, slug: String, token: String): Boolean {
        val answer = expect(api.get("/user/installations?per_page=100", token), 200)
        val installations = (obj(answer.body)?.get("installations") as? JsonArray).orEmpty().mapNotNull { it as? JsonObject }
            .filter { it.string("app_slug").equals(slug, ignoreCase = true) }
        for (installation in installations) {
            val holder = (installation["account"] as? JsonObject)?.string("login")
            if (installation.string("repository_selection") == "all" && holder.equals(account, ignoreCase = true)) return true
            val id = (installation["id"] as? JsonPrimitive)?.contentOrNull?.toLongOrNull() ?: continue
            val repositories = expect(api.get("/user/installations/$id/repositories?per_page=100", token), 200)
            val names = (obj(repositories.body)?.get("repositories") as? JsonArray).orEmpty().mapNotNull { (it as? JsonObject)?.string("full_name") }
            if (names.any { it.equals("$account/$repository", ignoreCase = true) }) return true
        }
        return false
    }

    // ---- Sending ----

    /** What GitHub records for one file: its mode and its Git blob identifier. */
    private data class Entry(val mode: String, val sha: String)

    private class OpenPullRequest(val number: Int, val url: String, val branch: String, val headSha: String)

    private fun send(token: String, submission: StagedSubmission, version: String, files: Map<String, ByteArray>): SubmissionResult {
        val ready = readiness(token)
        if (ready !is SubmissionReadiness.Ready) return SubmissionResult.SetupNeeded(ready)
        val account = ready.account
        val copy = "/repos/${ready.copy}"
        val name = submission.name
        val folder = "bots/${submission.language}/$name"

        val defaultBranch = obj(expect(api.get(upstream, token), 200).body)?.string("default_branch") ?: throw Stop(false, unexpected("the repository"))
        val headSha = refSha(upstream, defaultBranch, token) ?: throw Stop(false, unexpected("the repository's main branch"))
        val headTree = commitTree(upstream, headSha, token)
        val existingUpstream = folderEntries(upstream, headTree, folder, token)

        val expected = submission.files.associate { file ->
            val path = "$folder/${file.path}"
            path to Entry(if (file.executable) EXECUTABLE else FILE, blobId(files.getValue(file.path)))
        }
        val title = if (existingUpstream.isEmpty()) "Add $name $version" else "Update $name to $version"
        val text = pullRequestText(submission, name, version, folder)
        val commitMessage = title

        val open = findOpenPullRequest(account, name, folder, token)
        if (open != null) {
            val tree = commitTree(copy, open.headSha, token)
            val onBranch = folderEntries(copy, tree, folder, token)
            if (onBranch == expected) return SubmissionResult.Submitted(open.url, open.number, open.branch, SubmissionOutcome.UNCHANGED)
            val commit = commit(copy, tree, open.headSha, onBranch, expected, submission, files, folder, commitMessage, token)
            moveBranch(copy, open.branch, commit, token)
            val changed = api.patchJson("$upstream/pulls/${open.number}", token, json("title" to title, "body" to text))
            expect(changed, 200)
            return SubmissionResult.Submitted(open.url, open.number, open.branch, SubmissionOutcome.UPDATED)
        }

        val base = "submit/$name-$version"
        for (attempt in 1..MAX_BRANCH_NAMES) {
            val branch = if (attempt == 1) base else "$base-$attempt"
            val existing = refSha(copy, branch, token)
            if (existing == null) {
                val commit = commit(copy, headTree, headSha, existingUpstream, expected, submission, files, folder, commitMessage, token)
                createBranch(copy, branch, commit, token)
                return createPullRequest(account, branch, defaultBranch, title, text, SubmissionOutcome.OPENED, token)
            }
            val used = hasPullRequest(account, branch, token)
            if (!used && folderEntries(copy, commitTree(copy, existing, token), folder, token) == expected) {
                return createPullRequest(account, branch, defaultBranch, title, text, SubmissionOutcome.RESUMED, token)
            }
        }
        throw Stop(false, "Too many earlier branches for this bot and version exist in your copy. Delete the old ones on GitHub and try again.")
    }

    private fun createPullRequest(
        account: String, branch: String, defaultBranch: String, title: String, text: String, outcome: SubmissionOutcome, token: String,
    ): SubmissionResult {
        val answer = api.postJson("$upstream/pulls", token, json("title" to title, "head" to "$account:$branch", "base" to defaultBranch, "body" to text))
        expect(answer, 201, refused = "GitHub did not open the pull request. If your copy is behind the Rumble's bot repository, update it on GitHub and try again.")
        val created = obj(answer.body)
        val number = (created?.get("number") as? JsonPrimitive)?.contentOrNull?.toIntOrNull()
        val url = created?.string("html_url")
        if (number == null || url == null) throw Stop(false, unexpected("the new pull request"))
        return SubmissionResult.Submitted(url, number, branch, outcome)
    }

    /**
     * The account's open pull request for this bot, if there is one: on a branch that starts with `submit/<Name>-` and
     * changing only files in the bot's folder. The second test tells `Foo` from a bot called `Foo-Bar`.
     */
    private fun findOpenPullRequest(account: String, name: String, folder: String, token: String): OpenPullRequest? {
        val answer = expect(api.get("$upstream/pulls?state=open&per_page=100", token), 200)
        val prefix = "submit/$name-"
        return (parse(answer.body) as? JsonArray).orEmpty().mapNotNull { it as? JsonObject }.firstNotNullOfOrNull { pull ->
            val head = pull["head"] as? JsonObject ?: return@firstNotNullOfOrNull null
            val fromAccount = ((head["repo"] as? JsonObject)?.get("owner") as? JsonObject)?.string("login").equals(account, ignoreCase = true)
            val branch = head.string("ref")
            val sha = head.string("sha")
            val number = (pull["number"] as? JsonPrimitive)?.contentOrNull?.toIntOrNull()
            val url = pull.string("html_url")
            if (fromAccount && branch != null && branch.startsWith(prefix) && sha != null && number != null && url != null &&
                changesOnly(number, folder, token)
            ) {
                OpenPullRequest(number, url, branch, sha)
            } else null
        }
    }

    private fun changesOnly(number: Int, folder: String, token: String): Boolean {
        val answer = expect(api.get("$upstream/pulls/$number/files?per_page=100", token), 200)
        val names = (parse(answer.body) as? JsonArray).orEmpty().mapNotNull { (it as? JsonObject)?.string("filename") }
        return names.isNotEmpty() && names.all { it.startsWith("$folder/") }
    }

    /** True when any pull request, open, closed or merged, was ever opened from the account's [branch]. */
    private fun hasPullRequest(account: String, branch: String, token: String): Boolean {
        val answer = expect(api.get("$upstream/pulls?state=all&head=$account:$branch&per_page=1", token), 200)
        return (parse(answer.body) as? JsonArray).orEmpty().isNotEmpty()
    }

    // ---- Commits ----

    /**
     * Builds one commit on [parentSha] whose bot folder is exactly [expected]: files that are in [existing] but not in
     * [expected] are removed, and everything outside the folder is kept. The committed files are read back and
     * compared with [expected] before the commit is returned, so a difference stops the sending before any branch
     * is created or moved.
     */
    private fun commit(
        repo: String, baseTree: String, parentSha: String, existing: Map<String, Entry>, expected: Map<String, Entry>,
        submission: StagedSubmission, files: Map<String, ByteArray>, folder: String, message: String, token: String,
    ): String {
        val tree = mutableListOf<JsonElement>()
        for (file in submission.files) {
            val path = "$folder/${file.path}"
            val entry = expected.getValue(path)
            if (existing[path] != entry) {
                val blob = api.postJson(
                    "$repo/git/blobs", token,
                    json("content" to Base64.getEncoder().encodeToString(files.getValue(file.path)), "encoding" to "base64"),
                )
                expect(blob, 201, refused = COPY_REFUSED)
                if (obj(blob.body)?.string("sha") != entry.sha) throw Stop(false, DIFFERENT)
            }
            tree += JsonObject(mapOf("path" to JsonPrimitive(path), "mode" to JsonPrimitive(entry.mode), "type" to JsonPrimitive("blob"), "sha" to JsonPrimitive(entry.sha)))
        }
        for ((path, entry) in existing) {
            if (path !in expected) {
                tree += JsonObject(mapOf("path" to JsonPrimitive(path), "mode" to JsonPrimitive(entry.mode), "type" to JsonPrimitive("blob"), "sha" to JsonNull))
            }
        }
        val created = api.postJson("$repo/git/trees", token, Json.encodeToString(JsonObject.serializer(), JsonObject(mapOf("base_tree" to JsonPrimitive(baseTree), "tree" to JsonArray(tree)))))
        expect(created, 201, refused = COPY_REFUSED)
        val treeSha = obj(created.body)?.string("sha") ?: throw Stop(false, unexpected("the new tree"))

        val commit = api.postJson("$repo/git/commits", token, Json.encodeToString(JsonObject.serializer(), JsonObject(mapOf("message" to JsonPrimitive(message), "tree" to JsonPrimitive(treeSha), "parents" to JsonArray(listOf(JsonPrimitive(parentSha)))))))
        expect(commit, 201, refused = COPY_REFUSED)
        val commitSha = obj(commit.body)?.string("sha") ?: throw Stop(false, unexpected("the new commit"))

        if (folderEntries(repo, commitTree(repo, commitSha, token), folder, token) != expected) throw Stop(false, DIFFERENT)
        return commitSha
    }

    private fun createBranch(repo: String, branch: String, sha: String, token: String) {
        expect(api.postJson("$repo/git/refs", token, json("ref" to "refs/heads/$branch", "sha" to sha)), 201, refused = COPY_REFUSED)
    }

    /** Moves the branch forward to [sha]; GitHub refuses a move that is not a fast-forward, and nothing is forced. */
    private fun moveBranch(repo: String, branch: String, sha: String, token: String) {
        val body = Json.encodeToString(JsonObject.serializer(), JsonObject(mapOf("sha" to JsonPrimitive(sha), "force" to JsonPrimitive(false))))
        expect(api.patchJson("$repo/git/refs/heads/$branch", token, body), 200, refused = COPY_REFUSED)
    }

    private fun refSha(repo: String, branch: String, token: String): String? {
        val answer = api.get("$repo/git/ref/heads/$branch", token)
        if (answer.status == 404) return null
        expect(answer, 200)
        return (obj(answer.body)?.get("object") as? JsonObject)?.string("sha") ?: throw Stop(false, unexpected("the branch"))
    }

    private fun commitTree(repo: String, commitSha: String, token: String): String {
        val answer = expect(api.get("$repo/git/commits/$commitSha", token), 200)
        return (obj(answer.body)?.get("tree") as? JsonObject)?.string("sha") ?: throw Stop(false, unexpected("the commit"))
    }

    /** The files below [folder] in the tree, as full path to mode and blob identifier. */
    private fun folderEntries(repo: String, treeSha: String, folder: String, token: String): Map<String, Entry> {
        val answer = expect(api.get("$repo/git/trees/$treeSha?recursive=1", token), 200)
        val tree = obj(answer.body)
        if ((tree?.get("truncated") as? JsonPrimitive)?.booleanOrNull == true) throw Stop(false, "GitHub's list of the repository's files was too long to read.")
        return (tree?.get("tree") as? JsonArray).orEmpty().mapNotNull { it as? JsonObject }
            .filter { it.string("type") == "blob" && it.string("path").orEmpty().startsWith("$folder/") }
            .associate { it.string("path")!! to Entry(it.string("mode").orEmpty(), it.string("sha").orEmpty()) }
    }

    // ---- Text and helpers ----

    private fun pullRequestText(submission: StagedSubmission, name: String, version: String, folder: String): String {
        val converted = submission.files.count { it.lineEndingsChanged }
        val executable = submission.files.filter { it.executable }.map { "`" + code(it.path) + "`" }
        val changes = buildList {
            if (converted > 0) add(if (converted == 1) "1 file converted to Unix line endings" else "$converted files converted to Unix line endings")
            executable.forEach { add("$it set as executable") }
        }
        return listOf(
            "## Bot submission: `$name` `$version`",
            "Sent from Robocode Tank Royale. The author has confirmed that they can publish this source under the declared license.",
            listOf(
                "- Folder: `$folder`",
                "- License: `${code(submission.license ?: "none declared")}`",
                "- Files: ${submission.files.size}",
                "- Content hash: `${code(submission.treeHash)}`",
            ).joinToString("\n"),
            "Checked by the program: the bot repository's rules for one bot folder.",
            "Not checked by the program: running the bot, and its name, ownership and version against the catalog. The repository's own checks do that.",
            "Changed by the program: " + (if (changes.isEmpty()) "nothing." else changes.joinToString("; ") + "."),
        ).joinToString("\n\n")
    }

    /** Text that is safe inside a code span: no backticks, line breaks or other control characters, and not too long. */
    private fun code(text: String): String = text.filter { it != '`' && !it.isISOControl() }.take(MAX_CODE)

    private fun readFiles(submission: StagedSubmission): Map<String, ByteArray> =
        submission.files.associate { it.path to Files.readAllBytes(submission.botFolder.resolve(it.path)) }

    /** The Git blob identifier of [bytes], as Git computes it. */
    private fun blobId(bytes: ByteArray): String {
        val digest = MessageDigest.getInstance("SHA-1")
        digest.update("blob ${bytes.size}".toByteArray(Charsets.US_ASCII))
        digest.update(0)
        digest.update(bytes)
        return digest.digest().joinToString("") { "%02x".format(it) }
    }

    private fun whoAmI(token: String): String {
        val answer = expect(api.get("/user", token), 200)
        val login = obj(answer.body)?.string("login")
        if (login == null || !ACCOUNT.matches(login)) throw Stop(false, "GitHub did not say which account is signed in.")
        return login
    }

    /** Returns [answer] when its status is one of [ok]; otherwise stops with a plain-word reason. */
    private fun expect(answer: ApiResponse, vararg ok: Int, refused: String? = null): ApiResponse {
        if (answer.status in ok) return answer
        throw when {
            answer.status == 401 -> Stop(true, "GitHub did not accept the sign-in. Sign in again.")
            answer.status == 429 || (answer.status == 403 && answer.body.contains("rate limit", ignoreCase = true)) ->
                Stop(false, "GitHub is limiting requests. Try again later.")
            answer.status == 403 -> Stop(false, "GitHub did not allow this. Check that the Rumble sign-in App is installed on your copy of the bot repository.")
            refused != null && (answer.status == 404 || answer.status == 409 || answer.status == 422) -> Stop(false, refused)
            else -> Stop(false, "GitHub gave an unexpected answer (HTTP ${answer.status}). Try again later.")
        }
    }

    /** A name or version that is safe in a branch name, a folder name and a public text. */
    private fun safe(text: String): Boolean =
        SAFE_NAME.matches(text) && !text.contains("..") && !text.endsWith(".") && !text.endsWith(".lock")

    private fun unexpected(what: String) = "GitHub's answer about $what could not be understood. Try again later."

    private fun json(vararg fields: Pair<String, String>): String =
        Json.encodeToString(JsonObject.serializer(), JsonObject(fields.associate { it.first to JsonPrimitive(it.second) }))

    private fun parse(text: String): JsonElement? = try {
        Json.parseToJsonElement(text)
    } catch (e: Exception) {
        null
    }

    private fun obj(text: String): JsonObject? = parse(text) as? JsonObject

    private fun JsonObject.string(key: String): String? = (this[key] as? JsonPrimitive)?.takeIf { it.isString }?.contentOrNull

    companion object {
        private const val FILE = "100644"
        private const val EXECUTABLE = "100755"
        private const val MAX_BRANCH_NAMES = 50
        private const val MAX_CODE = 100
        private const val OFFLINE = "GitHub cannot be reached. Try again when you are online."
        private const val COPY_REFUSED =
            "GitHub refused the change in your copy of the bot repository. If your copy is behind the Rumble's bot repository, update it on GitHub and try again."
        private const val DIFFERENT =
            "GitHub stored the files differently from the staged copy, so nothing was published. Stage the bot again and try once more."
        private val SAFE_NAME = Regex("[A-Za-z0-9][A-Za-z0-9._-]{0,99}")
        private val ACCOUNT = Regex("[A-Za-z0-9](?:[A-Za-z0-9-]{0,38})")
    }
}
