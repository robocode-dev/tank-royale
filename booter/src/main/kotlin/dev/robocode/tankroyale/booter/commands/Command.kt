package dev.robocode.tankroyale.booter.commands

import dev.robocode.tankroyale.booter.model.BotInfo
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.exists

abstract class Command {

    protected fun getBotInfo(botDirPath: Path): BotInfo? {
        val jsonPath = botDirPath.resolve("${botDirPath.fileName}.json")
        return if (jsonPath.exists()) {
            val content = jsonPath.toFile().readText(Charsets.UTF_8)
            Json.decodeFromString(content)
        } else null
    }
}