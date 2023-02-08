package dev.robocode.tankroyale.booter.commands

import dev.robocode.tankroyale.booter.model.AbstractBootEntry
import dev.robocode.tankroyale.booter.model.BootDirEntry
import java.nio.file.Files.exists
import java.nio.file.Files.list
import java.nio.file.Path
import java.nio.file.Paths
import java.util.*
import kotlin.io.path.isDirectory

class DirCommand(private val botRootPaths: List<Path>) : Command() {

    fun listBootEntries(gameTypesCSV: String?): List<BootDirEntry> {
        val gameTypes: List<String> = gameTypesCSV?.split(",")?.map {
            it.trim().lowercase(Locale.getDefault())
        }?.filter { it.isNotBlank() } ?: emptyList()

        val bootEntries = HashSet<BootDirEntry>()
        listBotDirectories().forEach { dirPath ->
            try {
                getBootEntry(dirPath)?.let { bootEntry ->
                    if (isBootEntryContainingGameTypes(bootEntry, gameTypes)) {
                        bootEntry.apply {
                            bootEntries += BootDirEntry(
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
                                initialPosition
                            )
                        }
                    }
                }
            } catch (ex: Exception) {
                System.err.println("ERROR: ${ex.message}")
            }
        }
        return bootEntries.toList()
    }

    fun listBotDirectories(gameTypesCSV: String?): List<Path> =
        listBootEntries(gameTypesCSV).map { entry -> Paths.get(entry.dir) }.toSet().toList().sorted()

    private fun listBotDirectories(): Set<Path> {
        val dirs = HashSet<Path>()

        botRootPaths.forEach { rootPath ->
            list(rootPath).forEach { dirPath ->
                run {
                    if (dirPath.isDirectory()) {
                        val botName = dirPath.fileName.toString()
                        val jsonPath = dirPath.resolve("$botName.json")
                        if (exists(jsonPath)) {
                            dirs.add(dirPath)
                        }
                    }
                }
            }
        }
        return dirs
    }

    private fun isBootEntryContainingGameTypes(bootEntry: AbstractBootEntry, gameTypes: List<String>): Boolean {
        if (gameTypes.isEmpty() || gameTypes.contains("custom")) {
            return true
        } else {
            val botGameTypes = bootEntry.gameTypes?.filter { it.isNotEmpty() }?.toMutableList()
            botGameTypes?.forEachIndexed { index, gameType -> botGameTypes[index] = gameType.lowercase() }
            return if (botGameTypes.isNullOrEmpty()) {
                true
            } else {
                botGameTypes.containsAll(gameTypes)
            }
        }
    }
}
