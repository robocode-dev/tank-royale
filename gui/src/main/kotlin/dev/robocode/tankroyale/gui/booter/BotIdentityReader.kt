package dev.robocode.tankroyale.gui.booter

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.nio.file.Files
import java.nio.file.Path

data class BotIdentity(val name: String, val version: String)

@Serializable
private data class BotJson(
    val name: String? = null,
    val version: String? = null,
    val teamMembers: List<String>? = null,
)

object BotIdentityReader {

    private val json = Json { ignoreUnknownKeys = true }

    /**
     * Reads bot identities from a bot directory.
     * For regular bots, returns a single [BotIdentity].
     * For team directories (containing `teamMembers`), expands to member identities
     * by reading each member's `bot.json` from sibling directories.
     *
     * @param botDir the path to the bot or team directory
     * @return list of [BotIdentity] instances (may contain duplicates for teams)
     * @throws IllegalArgumentException if the JSON is missing, malformed, or required fields are absent
     */
    fun readIdentities(botDir: Path): List<BotIdentity> {
        val botJson = readBotJson(botDir)

        return if (botJson.teamMembers != null) {
            // Team directory: expand to member identities
            botJson.teamMembers.map { memberDirName ->
                val memberDir = botDir.parent?.resolve(memberDirName)
                    ?: throw IllegalArgumentException(
                        "Cannot resolve member directory '$memberDirName' relative to '$botDir'"
                    )
                val memberJson = readBotJson(memberDir)
                BotIdentity(
                    name = memberJson.name
                        ?: throw IllegalArgumentException("Missing 'name' in bot.json for member '$memberDirName'"),
                    version = memberJson.version
                        ?: throw IllegalArgumentException("Missing 'version' in bot.json for member '$memberDirName'")
                )
            }
        } else {
            listOf(
                BotIdentity(
                    name = botJson.name
                        ?: throw IllegalArgumentException("Missing 'name' in bot.json at '$botDir'"),
                    version = botJson.version
                        ?: throw IllegalArgumentException("Missing 'version' in bot.json at '$botDir'")
                )
            )
        }
    }

    private fun readBotJson(dir: Path): BotJson {
        val dirName = dir.fileName?.toString()
            ?: throw IllegalArgumentException("Cannot determine directory name from '$dir'")
        val jsonFile = dir.resolve("$dirName.json")
        if (!Files.exists(jsonFile)) {
            throw IllegalArgumentException("Missing $dirName.json in directory '$dir'")
        }
        val content = try {
            Files.readString(jsonFile)
        } catch (e: Exception) {
            throw IllegalArgumentException("Cannot read $dirName.json at '$jsonFile': ${e.message}", e)
        }
        return try {
            json.decodeFromString<BotJson>(content)
        } catch (e: Exception) {
            throw IllegalArgumentException("Malformed $dirName.json at '$jsonFile': ${e.message}", e)
        }
    }
}
