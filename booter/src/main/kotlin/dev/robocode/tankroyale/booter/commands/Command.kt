package dev.robocode.tankroyale.booter.commands

import dev.robocode.tankroyale.booter.model.BootEntry
import kotlinx.serialization.json.Json
import java.nio.file.Path
import kotlin.io.path.exists

typealias Pid = Long
typealias TeamId = Long

abstract class Command {

    companion object {
        val json = Json {
            ignoreUnknownKeys = true
        }
    }

    protected fun getBootEntry(botDirPath: Path): BootEntry? {
        val bootEntryJsonPath = botDirPath.resolve("${botDirPath.fileName}.json")
        if (!bootEntryJsonPath.exists()) return null

        val bootEntryJsonContent = bootEntryJsonPath.toFile().readText(Charsets.UTF_8)
        return json.decodeFromString(bootEntryJsonContent)
    }
}