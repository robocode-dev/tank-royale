package dev.robocode.tankroyale.booter.commands

import dev.robocode.tankroyale.booter.model.BootEntry
import dev.robocode.tankroyale.booter.process.PlatformDetector
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

        val bootEntry = if (bootEntryJsonPath.exists()) {
            val bootEntryJsonContent = bootEntryJsonPath.toFile().readText(Charsets.UTF_8)
            json.decodeFromString<BootEntry>(bootEntryJsonContent)
        } else {
            // Fallback: Create a default BootEntry if no JSON file is found
            BootEntry(
                name = botDirName,
                version = "0.0.1",
                authors = listOf("unknown"),
                platform = PlatformDetector.detectPlatform(botDirPath)
            )
        }

        return if (bootEntry.base == null && bootEntry.teamMembers == null) {
            bootEntry.copy(base = botDirName)
        } else {
            bootEntry
        }
    }
}