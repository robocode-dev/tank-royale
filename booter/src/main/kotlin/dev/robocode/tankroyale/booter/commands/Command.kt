package dev.robocode.tankroyale.booter.commands

import dev.robocode.tankroyale.booter.model.BootEntry
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import java.nio.file.Path
import kotlin.io.path.exists

typealias Pid = Long

abstract class Command {

    companion object {
        val json = Json() {
            ignoreUnknownKeys = true
        }
    }

    protected fun getBootEntry(botDirPath: Path): BootEntry? {
        val jsonPath = botDirPath.resolve("${botDirPath.fileName}.json")
        return if (jsonPath.exists()) {
            val content = jsonPath.toFile().readText(Charsets.UTF_8)
            json.decodeFromString(content)
        } else null
    }
}