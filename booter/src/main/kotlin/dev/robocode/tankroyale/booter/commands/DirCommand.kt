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

/**
 * Command for managing bot directories and their boot entries.
 */
class DirCommand(private val botRootPaths: List<Path>) : Command() {

    /**
     * Lists boot entries with optional filtering by game types and entry type.
     *
     * @param gameTypesCsv Comma-separated game types to filter by
     * @param botsOnly True to include only bots (not teams)
     * @param teamsOnly True to include only teams (not individual bots)
     * @return List of boot entries matching the criteria
     */
    fun listBootEntries(
        gameTypesCsv: String?,
        botsOnly: Boolean,
        teamsOnly: Boolean
    ): List<DirBootEntry> {
        val gameTypes = parseGameTypes(gameTypesCsv)
        val bootEntries = mutableSetOf<DirBootEntry>()

        listBotDirectories().forEach { directoryPath ->
            try {
                processDirectory(directoryPath, gameTypes, botsOnly, teamsOnly)?.let {
                    bootEntries.add(it)
                }
            } catch (ex: Exception) {
                Log.error(ex)
            }
        }

        return bootEntries.toList()
    }

    /**
     * Process a single bot directory to extract its boot entry.
     */
    private fun processDirectory(
        directoryPath: Path,
        gameTypes: List<String>,
        botsOnly: Boolean,
        teamsOnly: Boolean
    ): DirBootEntry? {
        val bootEntry = getBootEntry(directoryPath) ?: return null

        if (shouldSkipBootEntry(bootEntry, botsOnly, teamsOnly)) {
            return null
        }

        if (!isBootEntryContainingGameTypes(bootEntry, gameTypes)) {
            return null
        }

        return bootEntry.dirBootEntry(directoryPath, gameTypes)
    }

    /**
     * Parses game types from comma-separated string.
     */
    private fun parseGameTypes(gameTypesCsv: String?): List<String> {
        return gameTypesCsv
            ?.split(",")
            ?.map { it.trim().lowercase(Locale.getDefault()) }
            ?.filter { it.isNotBlank() }
            ?: emptyList()
    }

    /**
     * Determines if a boot entry should be skipped based on filter criteria.
     */
    private fun shouldSkipBootEntry(
        bootEntry: BootEntry,
        botsOnly: Boolean,
        teamsOnly: Boolean
    ): Boolean {
        val isTeam = bootEntry.teamMembers?.isNotEmpty() == true
        return when {
            botsOnly -> isTeam
            teamsOnly -> !isTeam
            else -> false
        }
    }

    /**
     * Converts a BootEntry to a DirBootEntry with directory information.
     */
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

    /**
     * Lists bot directories with optional filtering.
     */
    fun listBotDirectories(gameTypesCSV: String?, botsOnly: Boolean, teamsOnly: Boolean): List<Path> =
        listBootEntries(gameTypesCSV, botsOnly, teamsOnly)
            .map { entry -> Paths.get(entry.dir) }
            .toSet().toList()
            .sorted()

    /**
     * Lists all bot directories in the configured root paths.
     */
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

    /**
     * Checks if a boot entry contains all the specified game types.
     */
    private fun isBootEntryContainingGameTypes(
        bootEntry: BootEntry,
        gameTypes: List<String>
    ): Boolean {
        // If no game types specified or "custom" is included, accept all entries
        if (gameTypes.isEmpty() || gameTypes.contains("custom")) {
            return true
        }

        // Convert bot game types to lowercase for case-insensitive comparison
        val botGameTypes = bootEntry.gameTypes
            ?.filter { it.isNotEmpty() }
            ?.map { it.lowercase() }

        // If bot has no game types, accept it
        if (botGameTypes.isNullOrEmpty()) {
            return true
        }

        // Check if bot supports all requested game types
        return botGameTypes.containsAll(gameTypes)
    }
}