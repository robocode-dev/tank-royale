package dev.robocode.tankroyale.rumble

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.contentOrNull
import java.io.IOException

/** Finds the current commit of an environment's public bot repository, which needs no sign-in. */
object RumbleHead {
    private val SHA = Regex("[0-9a-f]{40}")

    /** The commit the repository's default branch points at now. Throws [IOException] with a plain message otherwise. */
    fun commit(api: GitHubApi, environment: RumbleEnvironment): String {
        val answer = api.get("/repos/${RumbleEnvironments.owner}/${environment.botsRepository}/commits/HEAD", null)
        if (answer.status != 200) throw IOException("GitHub gave an unexpected answer (HTTP ${answer.status}) about the Rumble's bot repository.")
        val sha = try {
            ((Json.parseToJsonElement(answer.body) as? JsonObject)?.get("sha") as? JsonPrimitive)?.contentOrNull
        } catch (e: IllegalArgumentException) {
            null
        }
        if (sha == null || !SHA.matches(sha)) throw IOException("GitHub's answer about the Rumble's bot repository could not be understood.")
        return sha
    }
}
