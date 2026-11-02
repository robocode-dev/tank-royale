package dev.robocode.tankroyale.rumble

import java.io.InputStream
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.time.Duration

/** Where a Rumble's catalog and bot sources come from. Tests use fixtures; the program uses [GitHubCatalogSource]. */
interface CatalogSource {
    /** The text of `bots/index.json` at [commit]. */
    fun catalogText(commit: String): String

    /** The zip archive of the whole bot repository at [commit]. The caller closes the stream. */
    fun archive(commit: String): InputStream

    /** The text of the file at [path] in the bot repository at [commit], or null when the file does not exist there. */
    fun fileText(commit: String, path: String): String? = throw CatalogUnreadable("This source cannot read repository files.")
}

/** Reads the public bot repository of an environment from GitHub. No credential is needed or sent. */
class GitHubCatalogSource(
    private val environment: RumbleEnvironment,
    private val client: HttpClient = HttpClient.newBuilder()
        .followRedirects(HttpClient.Redirect.NORMAL)
        .connectTimeout(Duration.ofSeconds(15))
        .build(),
) : CatalogSource {

    override fun catalogText(commit: String): String {
        val response = client.send(request(catalogUrl(commit)), HttpResponse.BodyHandlers.ofString())
        return checked(response.statusCode(), "catalog") { response.body() }
    }

    override fun archive(commit: String): InputStream {
        val response = client.send(request(archiveUrl(commit)), HttpResponse.BodyHandlers.ofInputStream())
        if (response.statusCode() != 200) response.body().close()
        return checked(response.statusCode(), "bot archive") { response.body() }
    }

    override fun fileText(commit: String, path: String): String? {
        val response = client.send(request(fileUrl(commit, path)), HttpResponse.BodyHandlers.ofString())
        if (response.statusCode() == 404) return null
        return checked(response.statusCode(), path) { response.body() }
    }

    internal fun fileUrl(commit: String, path: String): URI {
        if (!pathPattern.matches(path)) throw CatalogUnreadable("'$path' is not a repository file path.")
        return URI.create(
            "https://raw.githubusercontent.com/${RumbleEnvironments.owner}/${environment.botsRepository}/" +
                "${requireCommit(commit)}/$path"
        )
    }

    internal fun catalogUrl(commit: String): URI = URI.create(
        "https://raw.githubusercontent.com/${RumbleEnvironments.owner}/${environment.botsRepository}/" +
            "${requireCommit(commit)}/bots/index.json"
    )

    internal fun archiveUrl(commit: String): URI = URI.create(
        "https://codeload.github.com/${RumbleEnvironments.owner}/${environment.botsRepository}/zip/${requireCommit(commit)}"
    )

    private fun request(uri: URI): HttpRequest =
        HttpRequest.newBuilder(uri).timeout(Duration.ofSeconds(60)).header("User-Agent", "robocode-tank-royale").GET().build()

    private fun <T> checked(status: Int, what: String, body: () -> T): T {
        if (status != 200) throw CatalogUnreadable("The Rumble $what could not be fetched (HTTP $status).")
        return body()
    }

    companion object {
        private val commitPattern = Regex("[0-9a-f]{7,40}")
        private val pathPattern = Regex("[A-Za-z0-9._-]+(?:/[A-Za-z0-9._-]+)*")

        /** A commit is hexadecimal only, so it can never change which address is fetched. */
        fun requireCommit(commit: String): String {
            if (!commitPattern.matches(commit)) throw CatalogUnreadable("'$commit' is not a commit identifier.")
            return commit
        }
    }
}
