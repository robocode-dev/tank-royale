package dev.robocode.tankroyale.botproject

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import java.nio.file.Files
import java.nio.file.Path

/** A bot or team folder, as read from its configuration file. Reading changes nothing on disk. */
data class BotProject(
    val directory: Path,
    /** The parsed configuration file, `<folder name>.json`. */
    val config: JsonObject,
) {
    val folderName: String get() = directory.fileName.toString()
    val configFile: String get() = "$folderName.json"

    val name: String? get() = text("name")
    val version: String? get() = text("version")
    val license: String? get() = text("license")
    val description: String? get() = text("description")
    val platform: String? get() = text("platform")

    /** The authors when `authors` is a list of names, otherwise empty. */
    val authors: List<String> get() = (config["authors"] as? JsonArray)?.mapNotNull { (it as? JsonPrimitive)?.takeIf { p -> p.isString }?.content }.orEmpty()

    /** True for a team folder, which lists its members and carries no source. */
    val isTeam: Boolean get() = "teamMembers" in config

    val teamMembers: List<String> get() = (config["teamMembers"] as? JsonArray)?.mapNotNull { (it as? JsonPrimitive)?.takeIf { p -> p.isString }?.content }.orEmpty()

    /** The language key (`java`, `csharp`, `python` or `typescript`) the declared platform names, or null. */
    val language: String?
        get() = platform?.let { declared ->
            ValidationRules.current.platforms.entries.firstOrNull { it.value.platform == declared }?.key
        }

    private fun text(key: String): String? = (config[key] as? JsonPrimitive)?.takeIf { it.isString }?.content
}

/** The result of looking at a folder. */
sealed interface Inspection {
    data class Found(val project: BotProject) : Inspection

    /** The folder is not a bot project; [code] is stable and [reason] says what to do about it. */
    data class NotAProject(val code: String, val file: String, val reason: String) : Inspection
}

object BotProjects {
    /** Looks at [directory] and reads its configuration file `<folder name>.json`. */
    fun inspect(directory: Path): Inspection {
        if (!Files.isDirectory(directory)) {
            return Inspection.NotAProject("NOT_A_FOLDER", directory.toString(), "$directory is not a folder. Choose the folder of your bot.")
        }
        val name = directory.fileName.toString()
        val file = directory.resolve("$name.json")
        if (!Files.isRegularFile(file)) {
            return Inspection.NotAProject(
                "MISSING_CONFIG", "$name.json",
                "Add a configuration file named $name.json to the folder $name, or choose the folder that holds it.",
            )
        }
        val parsed: JsonElement = try {
            Json.parseToJsonElement(Files.readString(file))
        } catch (e: Exception) {
            return Inspection.NotAProject("INVALID_CONFIG", "$name.json", "$name.json is not valid JSON. Fix its syntax.")
        }
        if (parsed !is JsonObject) {
            return Inspection.NotAProject("INVALID_CONFIG", "$name.json", "$name.json must hold one JSON object with the bot's details.")
        }
        return Inspection.Found(BotProject(directory, parsed))
    }
}
