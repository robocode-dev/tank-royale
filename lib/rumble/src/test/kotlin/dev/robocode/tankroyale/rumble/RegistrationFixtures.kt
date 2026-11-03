package dev.robocode.tankroyale.rumble

import java.io.IOException
import java.util.Base64

const val SECRET_TOKEN = "ghu_REGISTRATIONSECRET789"

/** Plays GitHub and the Rumble result repository: the account, its registration file, open issues and the workflow. */
class FakeRumbleGitHub(
    private val login: String = "alice",
    private val dataRepository: String = "rumble-data-test",
) : GitHubApi {
    val requests = mutableListOf<Triple<String, String, String?>>() // method, path, token
    val postedBodies = mutableListOf<String>()
    var tokenAccepted = true
    var registeredClientId: String? = null
    var offline = false
    var openIssues = mutableListOf<String>() // raw JSON objects
    var closedIssues = mutableListOf<String>() // raw JSON objects
    var comments: String = "[]"
    var createStatus = 201
    var createdNumber = 7
    var userStatus = 200
    var contentsStatus: Int? = null
    var contentsBody: String? = null

    /** The workflow registers the account once the registration file has been asked for this many times. */
    var workflowRegistersAfterFileChecks: Int? = null
    private var fileChecks = 0

    private val owner = RumbleEnvironments.owner

    override fun get(path: String, token: String?): ApiResponse {
        requests += Triple("GET", path, token)
        if (offline) throw IOException("offline")
        if (token != null && !tokenAccepted) return ApiResponse(401, "{}")
        return when {
            path == "/user" -> ApiResponse(userStatus, """{"login":"$login"}""")
            path == "/repos/$owner/$dataRepository/contents/clients/$login.json" -> contents()
            path.startsWith("/repos/$owner/$dataRepository/issues?") && path.contains("state=closed") -> ApiResponse(200, "[" + closedIssues.joinToString(",") + "]")
            path.startsWith("/repos/$owner/$dataRepository/issues?") -> ApiResponse(200, "[" + openIssues.joinToString(",") + "]")
            path.startsWith("/repos/$owner/$dataRepository/issues/") && path.contains("/comments") -> ApiResponse(200, comments)
            else -> ApiResponse(404, "{}")
        }
    }

    override fun postJson(path: String, token: String, json: String): ApiResponse {
        requests += Triple("POST", path, token)
        postedBodies += json
        if (offline) throw IOException("offline")
        if (!tokenAccepted) return ApiResponse(401, "{}")
        if (path != "/repos/$owner/$dataRepository/issues") return ApiResponse(404, "{}")
        if (createStatus != 201) return ApiResponse(createStatus, "{}")
        return ApiResponse(201, """{"number":$createdNumber,"html_url":"https://github.com/$owner/$dataRepository/issues/$createdNumber"}""")
    }

    override fun patchJson(path: String, token: String, json: String): ApiResponse = ApiResponse(404, "{}")

    private fun contents(): ApiResponse {
        contentsStatus?.let { return ApiResponse(it, "{}") }
        contentsBody?.let { return ApiResponse(200, it) }
        fileChecks++
        val after = workflowRegistersAfterFileChecks
        if (registeredClientId == null && after != null && fileChecks >= after) registeredClientId = "$login-1a2b3c4d"
        val id = registeredClientId ?: return ApiResponse(404, """{"message":"Not Found"}""")
        return ApiResponse(200, fileJson(login, id))
    }

    companion object {
        /** The answer GitHub's contents interface gives for a registration file. */
        fun fileJson(account: String, clientId: String): String {
            val text = """{"schemaVersion":1,"account":"$account","clientIds":["$clientId"]}"""
            val encoded = Base64.getMimeEncoder(60, "\n".toByteArray()).encodeToString(text.toByteArray())
            return """{"type":"file","encoding":"base64","content":${kotlinx.serialization.json.JsonPrimitive(encoded)}}"""
        }

        fun issueJson(number: Int, title: String, pullRequest: Boolean = false) =
            """{"number":$number,"title":"$title","html_url":"https://github.com/robocode-dev/rumble-data-test/issues/$number"""" +
                (if (pullRequest) ""","pull_request":{}""" else "") + "}"
    }
}

fun tokens(access: String = SECRET_TOKEN) = GitHubTokens(access, "ghr_REGISTRATIONREFRESH", null, null)
