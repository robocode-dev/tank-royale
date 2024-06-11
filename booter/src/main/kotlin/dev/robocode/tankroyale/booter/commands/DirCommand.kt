package dev.robocode.tankroyale.booter.commands

import dev.robocode.tankroyale.booter.model.DirBootEntry
import dev.robocode.tankroyale.booter.model.BootEntry
import dev.robocode.tankroyale.booter.util.Log
import java.nio.file.Files.exists
import java.nio.file.Files.list
import java.nio.file.Path
import java.nio.file.Paths
import java.util.*
import kotlin.io.path.isDirectory

class DirCommand(private val botRootPaths: List<Path>) : Command() {

    fun listBootEntries(
        gameTypesCsv: String?,
        botsOnly: Boolean,
        teamsOnly: Boolean
    ): List<DirBootEntry> {
        val gameTypes = gameTypesCsv
            ?.split(",")
            ?.map { it.trim().lowercase(Locale.getDefault()) }
            ?.filter { it.isNotBlank() }
            ?: emptyList()

        val bootEntries = mutableSetOf<DirBootEntry>()

        listBotDirectories().forEach { directoryPath ->
            try {
                val bootEntry = getBootEntry(directoryPath)
                if (bootEntry != null) {
                    if (shouldSkipBootEntry(bootEntry, botsOnly, teamsOnly)) return@forEach
                    if (isBootEntryContainingGameTypes(bootEntry, gameTypes)) {
                        bootEntries.add(bootEntry.dirBootEntry(directoryPath, gameTypes))
                    }
                }
            } catch (ex: Exception) {
                Log.error(ex)
            }
        }
        return bootEntries.toList()
    }

    private fun shouldSkipBootEntry(
        bootEntry: BootEntry,
        botsOnly: Boolean,
        teamsOnly: Boolean
    ): Boolean {
        val isTeam = bootEntry.teamMembers?.isNotEmpty() == true
        return if (botsOnly) isTeam else if (teamsOnly) !isTeam else false
    }

    private fun BootEntry.dirBootEntry(dirPath: Path, gameTypes: List<String>) =
        DirBootEntry(
            dirPath.toAbsolutePath().toString(),
            name,
            version,
            authors,
            description,
            homepage,
            countryCodes,
            gameTypes,
            platform,
            programmingLang,
            initialPosition,
            teamMembers,
        )

    fun listBotDirectories(gameTypesCSV: String?, botsOnly: Boolean, teamsOnly: Boolean): List<Path> =
        listBootEntries(gameTypesCSV, botsOnly, teamsOnly)
            .map { entry -> Paths.get(entry.dir) }
            .toSet().toList()
            .sorted()

    private fun listBotDirectories(): Set<Path> {
        val dirs = HashSet<Path>()

        botRootPaths.forEach { rootPath ->
            list(rootPath).forEach { dirPath ->
                if (dirPath.isDirectory()) {
                    val botName = dirPath.fileName.toString()
                    val jsonPath = dirPath.resolve("$botName.json")
                    if (exists(jsonPath)) {
                        dirs.add(dirPath)
                    }
                }
            }
        }
        return dirs
    }

    private fun isBootEntryContainingGameTypes(
        bootEntry: BootEntry,
        gameTypes: List<String>
    ): Boolean {
        return if (gameTypes.isEmpty() || gameTypes.contains("custom")) {
            true
        } else {
            val botGameTypes = bootEntry.gameTypes?.filter { it.isNotEmpty() }?.toMutableList()
            botGameTypes?.forEachIndexed { index, gameType -> botGameTypes[index] = gameType.lowercase() }
            if (botGameTypes.isNullOrEmpty()) {
                true
            } else {
                botGameTypes.containsAll(gameTypes)
            }
        }
    }
}
