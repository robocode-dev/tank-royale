package dev.robocode.tankroyale.rumble

import java.io.IOException
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.time.Duration

/** GitHub's answer to an API request: the HTTP status and the raw JSON body. */
class ApiResponse(val status: Int, val body: String)

/** The part of GitHub's API that Rumble uses. Tests use fixtures; the program uses [HttpGitHubApi]. */
interface GitHubApi {
    /** Reads [path], for example `/user`. [token] is the signed-in token, or null for a public read. */
    fun get(path: String, token: String?): ApiResponse

    /** Sends [json] to [path] as the signed-in account. */
    fun postJson(path: String, token: String, json: String): ApiResponse
}

/**
 * Talks to GitHub's API over HTTPS. The token is sent only in the authorization header of a request to
 * [BASE_URL], never in an address.
 */
class HttpGitHubApi(
    private val client: HttpClient = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(15)).build(),
) : GitHubApi {

    override fun get(path: String, token: String?): ApiResponse = send(request("GET", path, token, null))

    override fun postJson(path: String, token: String, json: String): ApiResponse = send(request("POST", path, token, json))

    internal fun request(method: String, path: String, token: String?, json: String?): HttpRequest {
        require(path.startsWith("/")) { "An API path starts with a slash" }
        val builder = HttpRequest.newBuilder(URI.create(BASE_URL + path))
            .timeout(Duration.ofSeconds(30))
            .header("Accept", "application/vnd.github+json")
            .header("X-GitHub-Api-Version", "2022-11-28")
            .header("User-Agent", "robocode-tank-royale")
        if (token != null) builder.header("Authorization", "Bearer $token")
        if (json == null) {
            builder.method(method, HttpRequest.BodyPublishers.noBody())
        } else {
            builder.header("Content-Type", "application/json")
            builder.method(method, HttpRequest.BodyPublishers.ofString(json))
        }
        return builder.build()
    }

    private fun send(request: HttpRequest): ApiResponse {
        val response = try {
            client.send(request, HttpResponse.BodyHandlers.ofString())
        } catch (e: InterruptedException) {
            Thread.currentThread().interrupt()
            throw IOException("Interrupted while waiting for GitHub.")
        }
        return ApiResponse(response.statusCode(), response.body())
    }

    companion object {
        const val BASE_URL = "https://api.github.com"
    }
}
