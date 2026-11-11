package dev.robocode.tankroyale.rumble

import dev.robocode.tankroyale.botproject.StagedFile
import dev.robocode.tankroyale.botproject.StagedSubmission
import dev.robocode.tankroyale.botproject.TreeHash
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.contentOrNull
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Path
import java.security.MessageDigest
import java.util.Base64

/**
 * Plays GitHub for sending a bot: the account, its copy of the bot repository, the App installation, and the Rumble's
 * bot repository with its pull requests. Git objects are real enough to be compared: a blob's identifier is computed
 * the way Git computes it, and the Rumble's repository and the copy share one object store, as a fork network does.
 */
class FakeBotGitHub(val login: String = "alice") : GitHubApi {
    data class Entry(val mode: String, val sha: String)
    class Pull(val number: Int, var state: String, val branch: String, val owner: String, var title: String, var body: String, val files: List<String>)
    class Request(val method: String, val path: String, val token: String?, val body: String?)

    enum class Copy { NONE, FORK, PLAIN_REPOSITORY, FORK_OF_SOMETHING_ELSE }
    enum class Install { ALL, SELECTED_WITH_COPY, SELECTED_WITHOUT_COPY, NONE }

    val owner = RumbleEnvironments.owner
    val repo = "rumble-bots-test"
    val up = "/repos/$owner/$repo"
    val fk = "/repos/$login/$repo"

    val requests = mutableListOf<Request>()
    val writes get() = requests.filter { it.method != "GET" }
    var tokenAccepted = true
    var offline = false
    var rateLimited = false
    var copy = Copy.FORK
    var install = Install.SELECTED_WITH_COPY

    /** GitHub stores a different blob than the one sent. */
    var corruptBlobIds = false

    /** GitHub loses the executable mode when it builds a tree. */
    var dropModes = false
    var treeStatus = 201
    var pullStatus = 201

    /** The status GitHub gives when a branch is made in the copy, such as 403 when it brings a changed workflow file. */
    var refStatus = 201

    /** The files the Rumble's main branch has changed since the copy's, as GitHub's comparison lists them; null when it does not answer. */
    var upstreamAhead: List<String>? = emptyList()

    private val blobs = mutableMapOf<String, ByteArray>()
    private val trees = mutableMapOf<String, Map<String, Entry>>()
    private class Commit(val tree: String, val parents: List<String>)
    private val commits = mutableMapOf<String, Commit>()
    val refs = mapOf(up to mutableMapOf<String, String>(), fk to mutableMapOf<String, String>())
    val pulls = mutableListOf<Pull>()
    private var counter = 0

    /** Puts [files] (path to mode and bytes) on the Rumble's main branch, and the same on the copy's. */
    fun seedUpstream(files: Map<String, Pair<String, ByteArray>>) {
        val flat = files.mapValues { (_, file) ->
            val sha = blobId(file.second)
            blobs[sha] = file.second
            Entry(file.first, sha)
        }
        val tree = storeTree(flat)
        val commit = storeCommit(tree, emptyList(), "seed")
        refs.getValue(up)["main"] = commit
        refs.getValue(fk)["main"] = commit
    }

    fun upstreamMain(): String = refs.getValue(up).getValue("main")

    /** The files below [folder] on a branch of the copy: path to mode and identifier. */
    fun filesOn(branch: String, folder: String): Map<String, Entry> =
        trees.getValue(commits.getValue(refs.getValue(fk).getValue(branch)).tree).filterKeys { it.startsWith("$folder/") }

    fun parentsOf(commit: String): List<String> = commits.getValue(commit).parents

    fun commitCount() = commits.size

    fun addPull(number: Int, state: String, branch: String, files: List<String>, owner: String = login) {
        pulls += Pull(number, state, branch, owner, "title", "body", files)
    }

    override fun get(path: String, token: String?): ApiResponse {
        requests += Request("GET", path, token, null)
        gate(token)?.let { return it }
        return when {
            path == "/user" -> ok("""{"login":"$login"}""")
            path == fk -> repository()
            path == "/user/installations?per_page=100" -> installations()
            path.startsWith("/user/installations/") && path.contains("/repositories") -> selectedRepositories()
            path == up -> ok("""{"default_branch":"main","html_url":"https://github.com/$owner/$repo"}""")
            path.contains("/git/ref/heads/") -> ref(path)
            path.contains("/git/commits/") -> commit(path)
            path.contains("/git/trees/") -> tree(path)
            path.startsWith("$up/compare/") -> upstreamAhead?.let { files ->
                ok("""{"status":"${if (files.isEmpty()) "identical" else "ahead"}","files":[${files.joinToString(",") { """{"filename":"$it"}""" }}]}""")
            } ?: ApiResponse(500, """{"message":"Server Error"}""")
            path.startsWith("$up/pulls?") -> pullList(path)
            path.startsWith("$up/pulls/") && path.contains("/files") -> pullFiles(path)
            else -> ApiResponse(404, """{"message":"Not Found"}""")
        }
    }

    override fun postJson(path: String, token: String, json: String): ApiResponse {
        requests += Request("POST", path, token, json)
        gate(token)?.let { return it }
        val body = Json.parseToJsonElement(json) as JsonObject
        return when (path) {
            "$fk/git/blobs" -> {
                val bytes = Base64.getDecoder().decode(body.str("content"))
                val sha = blobId(bytes)
                blobs[sha] = bytes
                created("""{"sha":"${if (corruptBlobIds) sha.reversed() else sha}"}""")
            }
            "$fk/git/trees" -> if (treeStatus != 201) ApiResponse(treeStatus, """{"message":"x"}""") else newTree(body)
            "$fk/git/commits" -> {
                val parents = (body["parents"] as JsonArray).map { (it as JsonPrimitive).content }
                created("""{"sha":"${storeCommit(body.str("tree"), parents, body.str("message"))}"}""")
            }
            "$fk/git/refs" -> if (refStatus != 201) ApiResponse(refStatus, """{"message":"Resource not accessible by integration"}""") else {
                val branch = body.str("ref").removePrefix("refs/heads/")
                val sha = body.str("sha")
                if (branch in refs.getValue(fk)) ApiResponse(422, """{"message":"Reference already exists"}""")
                else { refs.getValue(fk)[branch] = sha; created("""{"ref":"refs/heads/$branch"}""") }
            }
            "$up/pulls" -> if (pullStatus != 201) ApiResponse(pullStatus, """{"message":"x"}""") else newPull(body)
            else -> ApiResponse(404, """{"message":"Not Found"}""")
        }
    }

    override fun patchJson(path: String, token: String, json: String): ApiResponse {
        requests += Request("PATCH", path, token, json)
        gate(token)?.let { return it }
        val body = Json.parseToJsonElement(json) as JsonObject
        if (path.startsWith("$fk/git/refs/heads/")) {
            val branch = path.removePrefix("$fk/git/refs/heads/")
            val current = refs.getValue(fk)[branch] ?: return ApiResponse(404, "{}")
            val sha = body.str("sha")
            val forced = (body["force"] as? JsonPrimitive)?.content == "true"
            if (!forced && current !in commits.getValue(sha).parents) return ApiResponse(422, """{"message":"Update is not a fast forward"}""")
            refs.getValue(fk)[branch] = sha
            return ok("""{"ref":"refs/heads/$branch"}""")
        }
        val number = Regex("""$up/pulls/(\d+)""").matchEntire(path)?.groupValues?.get(1)?.toInt() ?: return ApiResponse(404, "{}")
        val pull = pulls.firstOrNull { it.number == number } ?: return ApiResponse(404, "{}")
        pull.title = body.str("title")
        pull.body = body.str("body")
        return ok("{}")
    }

    private fun gate(token: String?): ApiResponse? {
        if (offline) throw IOException("offline")
        if (token != null && !tokenAccepted) return ApiResponse(401, "{}")
        if (rateLimited) return ApiResponse(403, """{"message":"API rate limit exceeded"}""")
        return null
    }

    private fun repository(): ApiResponse = when (copy) {
        Copy.NONE -> ApiResponse(404, """{"message":"Not Found"}""")
        Copy.FORK -> ok("""{"fork":true,"parent":{"full_name":"$owner/$repo"},"html_url":"https://github.com/$login/$repo"}""")
        Copy.PLAIN_REPOSITORY -> ok("""{"fork":false,"html_url":"https://github.com/$login/$repo"}""")
        Copy.FORK_OF_SOMETHING_ELSE -> ok("""{"fork":true,"parent":{"full_name":"someone/$repo"},"html_url":"https://github.com/$login/$repo"}""")
    }

    private fun installations(): ApiResponse = when (install) {
        Install.NONE -> ok("""{"installations":[]}""")
        Install.ALL -> ok("""{"installations":[{"id":77,"app_slug":"tank-royale-rumble-contrib-test","repository_selection":"all","account":{"login":"$login"}}]}""")
        else -> ok("""{"installations":[{"id":77,"app_slug":"tank-royale-rumble-contrib-test","repository_selection":"selected","account":{"login":"$login"}}]}""")
    }

    private fun selectedRepositories(): ApiResponse =
        if (install == Install.SELECTED_WITH_COPY) ok("""{"repositories":[{"full_name":"$login/$repo"}]}""")
        else ok("""{"repositories":[{"full_name":"$login/another-repository"}]}""")

    private fun base(path: String) = if (path.startsWith(up)) up else fk

    private fun ref(path: String): ApiResponse {
        val branch = path.substringAfter("/git/ref/heads/")
        val sha = refs.getValue(base(path))[branch] ?: return ApiResponse(404, """{"message":"Not Found"}""")
        return ok("""{"object":{"sha":"$sha"}}""")
    }

    private fun commit(path: String): ApiResponse {
        val commit = commits[path.substringAfter("/git/commits/")] ?: return ApiResponse(404, "{}")
        return ok("""{"tree":{"sha":"${commit.tree}"}}""")
    }

    private fun tree(path: String): ApiResponse {
        val flat = trees[path.substringAfter("/git/trees/").substringBefore("?")] ?: return ApiResponse(404, "{}")
        val entries = flat.entries.sortedBy { it.key }.joinToString(",") { """{"path":"${it.key}","mode":"${it.value.mode}","type":"blob","sha":"${it.value.sha}"}""" }
        return ok("""{"truncated":false,"tree":[$entries]}""")
    }

    private fun newTree(body: JsonObject): ApiResponse {
        val flat = trees.getValue(body.str("base_tree")).toMutableMap()
        for (item in body["tree"] as JsonArray) {
            val entry = item as JsonObject
            val path = entry.str("path")
            if (entry["sha"] is JsonNull) flat.remove(path)
            else flat[path] = Entry(if (dropModes) "100644" else entry.str("mode"), entry.str("sha"))
        }
        return created("""{"sha":"${storeTree(flat)}"}""")
    }

    private fun newPull(body: JsonObject): ApiResponse {
        val (account, branch) = body.str("head").split(":", limit = 2)
        val commit = refs.getValue(fk)[branch] ?: return ApiResponse(422, """{"message":"head invalid"}""")
        val head = trees.getValue(commits.getValue(commit).tree)
        val main = trees.getValue(commits.getValue(upstreamMain()).tree)
        val changed = (head.keys + main.keys).filter { head[it] != main[it] }.sorted()
        val number = pulls.size + 1
        pulls += Pull(number, "open", branch, account, body.str("title"), body.str("body"), changed)
        return created("""{"number":$number,"html_url":"https://github.com/$owner/$repo/pull/$number"}""")
    }

    private fun pullList(path: String): ApiResponse {
        val all = path.contains("state=all")
        val head = Regex("""head=([^&]+)""").find(path)?.groupValues?.get(1)
        val list = pulls.filter { (all || it.state == "open") && (head == null || head == "${it.owner}:${it.branch}") }
        val items = list.joinToString(",") {
            val sha = refs.getValue(fk)[it.branch] ?: "0".repeat(40)
            """{"number":${it.number},"html_url":"https://github.com/$owner/$repo/pull/${it.number}","head":{"ref":"${it.branch}","sha":"$sha","repo":{"owner":{"login":"${it.owner}"}}}}"""
        }
        return ok("[$items]")
    }

    private fun pullFiles(path: String): ApiResponse {
        val number = Regex("""/pulls/(\d+)/files""").find(path)?.groupValues?.get(1)?.toInt()
        val pull = pulls.firstOrNull { it.number == number } ?: return ApiResponse(404, "{}")
        return ok("[" + pull.files.joinToString(",") { """{"filename":"$it"}""" } + "]")
    }

    private fun storeTree(flat: Map<String, Entry>): String {
        val sha = sha1("tree" + flat.entries.sortedBy { it.key }.joinToString("|") { "${it.key}:${it.value.mode}:${it.value.sha}" })
        trees[sha] = flat
        return sha
    }

    private fun storeCommit(tree: String, parents: List<String>, message: String): String {
        val sha = sha1("commit${counter++}$tree${parents.joinToString()}$message")
        commits[sha] = Commit(tree, parents)
        return sha
    }

    private fun ok(body: String) = ApiResponse(200, body)
    private fun created(body: String) = ApiResponse(201, body)
    private fun JsonObject.str(key: String) = (getValue(key) as JsonPrimitive).contentOrNull!!

    companion object {
        fun sha1(text: String) = MessageDigest.getInstance("SHA-1").digest(text.toByteArray()).joinToString("") { "%02x".format(it) }

        /** The identifier Git gives a file with these bytes. */
        fun blobId(bytes: ByteArray): String {
            val digest = MessageDigest.getInstance("SHA-1")
            digest.update("blob ${bytes.size}".toByteArray())
            digest.update(0)
            digest.update(bytes)
            return digest.digest().joinToString("") { "%02x".format(it) }
        }
    }
}

/** A staged bot written to disk below [dir], described like the real staging describes it. */
fun stagedBot(
    dir: Path,
    name: String = "Scout",
    version: String? = "1.0.3",
    language: String = "java",
    files: Map<String, String> = mapOf("Scout.json" to "{}\n", "Scout.sh" to "#!/bin/sh\n", "Scout.java" to "class Scout {}\n"),
    executable: Set<String> = setOf("Scout.sh"),
    converted: Set<String> = setOf("Scout.java"),
    notIncluded: List<dev.robocode.tankroyale.botproject.NotIncluded> = emptyList(),
    license: String? = "MIT",
): StagedSubmission {
    val botFolder = dir.resolve("bots").resolve(language).resolve(name)
    Files.createDirectories(botFolder)
    files.forEach { (path, text) -> Files.write(botFolder.resolve(path), text.toByteArray()) }
    return StagedSubmission(
        name = name, version = version, language = language, license = license, authors = listOf("Alice"), isTeam = false,
        stagingFolder = dir, botFolder = botFolder,
        files = files.map { (path, text) -> StagedFile(path, text.length.toLong(), path in executable, path in converted) },
        notIncluded = notIncluded,
        treeHash = TreeHash.of(files.mapValues { it.value.toByteArray() }),
    )
}

/** The files of [stagedBot] as they appear in the Rumble's bot repository. */
fun uploaded(bot: StagedSubmission, folder: String = "bots/${bot.language}/${bot.name}", executable: Set<String> = setOf("Scout.sh")): Map<String, Pair<String, ByteArray>> =
    bot.files.associate { "$folder/${it.path}" to ((if (it.path in executable) "100755" else "100644") to Files.readAllBytes(bot.botFolder.resolve(it.path))) }
