package dev.robocode.tankroyale.booter.commands

import dev.robocode.tankroyale.booter.model.BootEntry
import kotlinx.serialization.json.Json
import java.nio.file.Path
import kotlin.io.path.exists

internal abstract class Command {

    companion object {
        private val json = Json {
            ignoreUnknownKeys = true
        }
    }

    protected fun getBootEntry(botDirPath: Path): BootEntry? {
        val botDirName = botDirPath.fileName.toString()
        val bootEntryJsonPath = botDirPath.resolve("$botDirName.json")
        if (!bootEntryJsonPath.exists()) return null

        val bootEntryJsonContent = bootEntryJsonPath.toFile().readText(Charsets.UTF_8)
        val bootEntry = json.decodeFromString<BootEntry>(bootEntryJsonContent)

        return if (bootEntry.base == null && bootEntry.teamMembers == null) {
            bootEntry.copy(base = botDirName)
        } else {
            bootEntry
        }
    }
}