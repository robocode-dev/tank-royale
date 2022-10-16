package build.release

import org.gradle.internal.file.impl.DefaultFileMetadata.file
import org.json.JSONObject
import java.io.File
import java.net.URI
import java.net.URLEncoder
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Path


fun createRelease(projectDir: File, version: String, token: String) {

    val release = prepareRelease(projectDir, version, token)
    val releaseId = JSONObject(release).get("id").toString().toInt()

//    println("releaseId: $releaseId");

    // GUI Application
    uploadAsset(projectDir, releaseId, token, "gui-app/build/libs/robocode-tankroyale-gui-$version.jar",
        "application/java-archive", "GUI Application (jar)")

    // Server
    Files.delete(Path.of("$projectDir/server/build/libs/robocode-tankroyale-server-$version.jar"))
    Files.copy(
        Path.of("$projectDir/server/build/libs/robocode-tankroyale-server-$version-proguard.jar"),
        Path.of("$projectDir/server/build/libs/robocode-tankroyale-server-$version.jar")
    )
    uploadAsset(projectDir, releaseId, token, "server/build/libs/robocode-tankroyale-server-$version.jar",
        "application/java-archive", "Server (jar)")

    // Booter
    Files.delete(Path.of("$projectDir/booter/build/libs/robocode-tankroyale-booter-$version.jar"))
    Files.copy(
        Path.of("$projectDir/booter/build/libs/robocode-tankroyale-booter-$version-proguard.jar"),
        Path.of("$projectDir/booter/build/libs/robocode-tankroyale-booter-$version.jar")
    )
    uploadAsset(projectDir, releaseId, token, "booter/build/libs/robocode-tankroyale-booter-$version.jar",
        "application/java-archive", "Booter (jar)")

    // Sample Bots for C#
    uploadAsset(projectDir, releaseId, token, "sample-bots/csharp/build/sample-bots-csharp-$version.zip",
        "application/zip", "Sample bots for C# (zip)")

    // Sample Bots for Java
    uploadAsset(projectDir, releaseId, token, "sample-bots/java/build/sample-bots-java-$version.zip",
        "application/zip", "Sample bots for Java (zip)")
}

private fun prepareRelease(projectDir: File, version: String, token: String): String /* JSON result */ {

    var releaseNotes = generateReleaseNotes(projectDir, version)
    val quotedReleaseNotes = JSONObject.quote(releaseNotes)

    val body = """{
        "tag_name": "v$version",
        "target_commitish": "master",
        "name": "$version",
        "body": $quotedReleaseNotes,
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

    if (response.statusCode() != 201) {
        throw IllegalStateException("Could not create release")
    }
    return response.body()
}

private fun uploadAsset(projectDir: File, releaseId: Int, token: String, filepath: String, contentType: String, label: String?) {
    val filePath = Path.of("$projectDir/$filepath")
    val name = filePath.fileName.toString()

    val uploadUrl = "https://uploads.github.com/repos/robocode-dev/tank-royale/releases/$releaseId/assets" +
            pathParam("?name", name) +
            pathParam("&label", label)

    println(uploadUrl)

    val request = HttpRequest.newBuilder()
        .uri(URI(uploadUrl))
        .POST(HttpRequest.BodyPublishers.ofFile(filePath))
        .header("Accept", "application/vnd.github+json")
        .header("Content-Type", contentType)
        .header("Authorization", "Bearer $token")
        .build()

    val response = HttpClient
        .newBuilder()
        .build()
        .send(request, HttpResponse.BodyHandlers.ofString())

    println("statusCode: ${response.statusCode()}, body: ${response.body()}")

    if (response.statusCode() != 201) {
        throw IllegalStateException("Could not upload release asset")
    }
}

private fun pathParam(label: String, value: String?) = if (value == null) "" else "$label=${urlEncode(value)}"

private fun urlEncode(url: String) = URLEncoder.encode(url, StandardCharsets.UTF_8)