package build.release

import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse

import build.release.generateReleaseNotes
import java.io.File
import org.json.simple.JSONValue

fun createRelease(projectDir: File, version: String, token: String) {

    var releaseNotes = generateReleaseNotes(projectDir, version)
    val quotedReleaseNotes = JSONValue.escape(releaseNotes)

    val body = """{
        "tag_name": "v$version",
        "target_commitish": "master",
        "name": "$version",
        "body": "$quotedReleaseNotes",
        "draft": true,
        "prerelease": true,
        "discussion_category_name": "Announcements",
        "generate_release_notes": false
    }"""

    val request = HttpRequest.newBuilder()
        .uri(URI("https://api.github.com/repos/robocode-dev/tank-royale/releases"))
        .header("Accept", "application/vnd.github+json")
        .header("Authorization", "Bearer $token")
        .POST(HttpRequest.BodyPublishers.ofString(body))
        .build()

    val response = HttpClient
        .newBuilder()
        .build()
        .send(request, HttpResponse.BodyHandlers.ofString())

    println("statusCode: ${response.statusCode()}, body: ${response.body()}")
}
