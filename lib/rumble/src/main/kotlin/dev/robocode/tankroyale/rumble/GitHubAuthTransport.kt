package dev.robocode.tankroyale.rumble

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import java.io.IOException
import java.net.URI
import java.net.URLEncoder
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.time.Duration

/** GitHub's answer to a sign-in request: the HTTP status and the fields of its JSON answer as text. */
class AuthResponse(val status: Int, val fields: Map<String, String>)

/** Where the sign-in requests go. Tests use fixtures; the program uses [HttpGitHubAuthTransport]. */
interface GitHubAuthTransport {
    /** Sends [form] to [url] and returns GitHub's answer. Throws [IOException] when GitHub cannot be reached. */
    fun postForm(url: String, form: Map<String, String>): AuthResponse
}

/** Talks to github.com over HTTPS. It sends no credential other than what the request itself holds. */
class HttpGitHubAuthTransport(
    private val client: HttpClient = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(15)).build(),
) : GitHubAuthTransport {

    override fun postForm(url: String, form: Map<String, String>): AuthResponse {
        val response = try {
            client.send(buildRequest(url, form), HttpResponse.BodyHandlers.ofString())
        } catch (e: InterruptedException) {
            Thread.currentThread().interrupt()
            throw IOException("Interrupted while waiting for GitHub.")
        }
        return AuthResponse(response.statusCode(), parseFields(response.body()))
    }

    internal fun buildRequest(url: String, form: Map<String, String>): HttpRequest =
        HttpRequest.newBuilder(URI.create(url))
            .timeout(Duration.ofSeconds(30))
            .header("Accept", "application/json")
            .header("Content-Type", "application/x-www-form-urlencoded")
            .header("User-Agent", "robocode-tank-royale")
            .POST(HttpRequest.BodyPublishers.ofString(encode(form)))
            .build()

    private fun encode(form: Map<String, String>): String =
        form.entries.joinToString("&") { (key, value) ->
            URLEncoder.encode(key, Charsets.UTF_8) + "=" + URLEncoder.encode(value, Charsets.UTF_8)
        }

    private fun parseFields(body: String): Map<String, String> =
        try {
            (Json.parseToJsonElement(body) as? JsonObject).orEmpty()
                .mapNotNull { (key, value) -> (value as? JsonPrimitive)?.let { key to it.content } }
                .toMap()
        } catch (e: Exception) {
            emptyMap()
        }
}
