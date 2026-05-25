package build.release

import org.json.JSONObject
import java.io.File
import java.net.URI
import java.net.URLEncoder
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.nio.charset.StandardCharsets
import java.nio.file.Path

const val JAR_MIME_TYPE = "application/java-archive"
const val ZIP_MIME_TYPE = "application/zip"

fun createRelease(projectDir: File, version: String, token: String) {

    val release = prepareRelease(projectDir, version, token)
    val releaseId = JSONObject(release)["id"].toString().toInt()

    // GUI
    uploadAsset(
        projectDir, releaseId, token, "gui/build/libs/robocode-tankroyale-gui-$version.jar",
        JAR_MIME_TYPE, "GUI (jar)"
    )

    // Server
    uploadAsset(
        projectDir, releaseId, token, "server/build/libs/robocode-tankroyale-server-$version.jar",
        JAR_MIME_TYPE, "Server (jar)"
    )

    // Runner
    uploadAsset(
        projectDir, releaseId, token, "runner/build/libs/robocode-tankroyale-runner-$version.jar",
        JAR_MIME_TYPE, "Battle Runner (jar)"
    )

//    // Booter
//    uploadAsset(projectDir, releaseId, token, "booter/build/libs/robocode-tankroyale-booter-$version.jar",
//        JAR_MIME_TYPE, "Booter (jar)")

    // Sample Bots
    val sampleBotsDir = File(projectDir, "sample-bots")
    sampleBotsDir.listFiles { f -> f.isDirectory }?.forEach { subprojectDir ->
        val lang = subprojectDir.name
        val readableName = when (lang) {
            "csharp" -> "CSharp"
            "java" -> "Java"
            "python" -> "Python"
            "typescript" -> "TypeScript"
            else -> lang.replaceFirstChar { it.uppercase() }
        }
        val zipFile = File(subprojectDir, "build/sample-bots-$readableName-$version.zip")
        if (zipFile.exists()) {
            uploadAsset(
                projectDir, releaseId, token, "sample-bots/$lang/build/sample-bots-$readableName-$version.zip",
                ZIP_MIME_TYPE, "Sample bots for $readableName (zip)"
            )
        }
    }
}

/**
 * Dispatches a GitHub Actions workflow in this repository using the workflow_dispatch event.
 *
 * @param token A GitHub token with workflow:write permission (e.g., GITHUB_TOKEN from Actions).
 * @param workflowFileName The workflow file name located under .github/workflows (e.g., "package-release.yml").
 * @param ref The git ref (branch or tag) to run the workflow on. Defaults to "main".
 * @param inputs Optional key/value inputs to pass to the workflow_dispatch event.
 */
fun dispatchWorkflow(
    token: String,
    workflowFileName: String,
    ref: String = "main",
    inputs: Map<String, String>? = null
) {
    val uri =
        URI("https://api.github.com/repos/robocode-dev/tank-royale/actions/workflows/$workflowFileName/dispatches")
    val inputsJson = inputs?.entries
        ?.joinToString(separator = ",") { "\"${it.key}\": \"${it.value}\"" }
        ?.let { "\n        ,\"inputs\": { $it }" }
        ?: ""
    val body = """{
        "ref": "$ref"$inputsJson
    }"""

    val request = HttpRequest.newBuilder()
        .uri(uri)
        .header("Accept", "application/vnd.github+json")
        .header("Authorization", "Bearer $token")
        .POST(HttpRequest.BodyPublishers.ofString(body))
        .build()

    val response = HttpClient
        .newBuilder()
        .build()
        .send(request, HttpResponse.BodyHandlers.ofString())

    println("Dispatch workflow '$workflowFileName' on '$ref' statusCode: ${'$'}{response.statusCode()}, body: ${'$'}{response.body()}")

    check(response.statusCode() == 204) { "Could not dispatch workflow '$workflowFileName'" }
}

private fun prepareRelease(projectDir: File, version: String, token: String): String /* JSON result */ {
    val releaseNotes = generateReleaseNotes(projectDir, version)
    val quotedReleaseNotes = JSONObject.quote(releaseNotes)
    val tag = "v$version"

    val client = HttpClient.newBuilder().build()

    // Try to fetch existing release by tag
    val getRequest = HttpRequest.newBuilder()
        .uri(URI("https://api.github.com/repos/robocode-dev/tank-royale/releases/tags/$tag"))
        .header("Accept", "application/vnd.github+json")
        .header("Authorization", "Bearer $token")
        .GET()
        .build()

    var response = client.send(getRequest, HttpResponse.BodyHandlers.ofString())
    println("GET release by tag statusCode: ${response.statusCode()}, body: ${response.body()}")

    if (response.statusCode() == 200) {
        return response.body()
    }

    // If not found, create the release with tag v<version>
    if (response.statusCode() == 404) {
        val body = """{
        "tag_name": "v$version",
        "target_commitish": "main",
        "name": "$version",
        "body": $quotedReleaseNotes,
        "draft": true,
        "prerelease": true,
        "discussion_category_name": "Announcements",
        "generate_release_notes": false
    }"""

        val postRequest = HttpRequest.newBuilder()
            .uri(URI("https://api.github.com/repos/robocode-dev/tank-royale/releases"))
            .header("Accept", "application/vnd.github+json")
            .header("Authorization", "Bearer $token")
            .POST(HttpRequest.BodyPublishers.ofString(body))
            .build()

        response = client.send(postRequest, HttpResponse.BodyHandlers.ofString())
        println("POST create release statusCode: ${response.statusCode()}, body: ${response.body()}")
        check(response.statusCode() == 201) { "Could not create release" }
        return response.body()
    }

    throw IllegalStateException("Failed to query or create release for tag '$tag'. status=${response.statusCode()}, body=${response.body()}")
}

private fun uploadAsset(
    projectDir: File,
    releaseId: Int,
    token: String,
    filepath: String,
    contentType: String,
    label: String?
) {
    val filePath = Path.of("$projectDir/$filepath")
    val name = filePath.fileName.toString()

    deleteExistingAsset(releaseId, name, token)

    val uploadUrl = "https://uploads.github.com/repos/robocode-dev/tank-royale/releases/$releaseId/assets" +
            pathParam("?name", name) +
            pathParam("&label", label)

    println("Uploading $name to $uploadUrl")

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
        println("WARNING: Could not upload release asset '$name'. status=${response.statusCode()}, body=${response.body()}")
    }
}

private fun deleteExistingAsset(releaseId: Int, name: String, token: String) {
    val client = HttpClient.newBuilder().build()
    val listRequest = HttpRequest.newBuilder()
        .uri(URI("https://api.github.com/repos/robocode-dev/tank-royale/releases/$releaseId/assets"))
        .header("Accept", "application/vnd.github+json")
        .header("Authorization", "Bearer $token")
        .GET()
        .build()

    val response = client.send(listRequest, HttpResponse.BodyHandlers.ofString())
    if (response.statusCode() == 200) {
        val assets = org.json.JSONArray(response.body())
        for (i in 0 until assets.length()) {
            val asset = assets.getJSONObject(i)
            val assetName = asset.getString("name")
            if (assetName.equals(name, ignoreCase = true)) {
                val assetId = asset.getInt("id")
                println("Deleting existing asset '$assetName' (ID: $assetId)")
                val deleteRequest = HttpRequest.newBuilder()
                    .uri(URI("https://api.github.com/repos/robocode-dev/tank-royale/releases/assets/$assetId"))
                    .header("Accept", "application/vnd.github+json")
                    .header("Authorization", "Bearer $token")
                    .DELETE()
                    .build()
                val deleteResponse = client.send(deleteRequest, HttpResponse.BodyHandlers.ofString())
                if (deleteResponse.statusCode() != 204) {
                    println("WARNING: Could not delete asset '$assetName'. status=${deleteResponse.statusCode()}")
                }
            }
        }
    }
}

private fun pathParam(label: String, value: String?) = if (value == null) "" else "$label=${urlEncode(value)}"

private fun urlEncode(url: String) = URLEncoder.encode(url, StandardCharsets.UTF_8)
