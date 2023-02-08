package dev.robocode.tankroyale.booter.commands

import dev.robocode.tankroyale.booter.model.BootEntry
import dev.robocode.tankroyale.booter.model.BotInfo
import java.nio.file.Files.exists
import java.nio.file.Files.list
import java.nio.file.Path
import java.nio.file.Paths
import java.util.*
import kotlin.io.path.isDirectory

class DirCommand(private val botRootPaths: List<Path>) : Command() {

    fun listBootEntries(gameTypesCSV: String?): List<BootEntry> {
        val gameTypes: List<String> = gameTypesCSV?.split(",")?.map {
            it.trim().lowercase(Locale.getDefault())
        }?.filter { it.isNotBlank() } ?: emptyList()

        val bootEntries = HashSet<BootEntry>()
        listBotDirectories().forEach { dirPath ->
            try {
                getBotInfo(dirPath)?.let { botInfo ->
                    if (isBotInfoContainingGameTypes(botInfo, gameTypes)) {
                        bootEntries += BootEntry(dirPath.toAbsolutePath().toString(), botInfo)
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

    private fun isBotInfoContainingGameTypes(botInfo: BotInfo?, gameTypes: List<String>): Boolean {
        if (gameTypes.isEmpty() || gameTypes.contains("custom")) {
            return true
        } else {
            if (botInfo != null) {
                val botGameTypes = botInfo.gameTypes?.filter { it.isNotEmpty() }?.toMutableList()
                botGameTypes?.forEachIndexed { index, gameType -> botGameTypes[index] = gameType.lowercase() }
                return if (botGameTypes.isNullOrEmpty()) {
                    true
                } else {
                    botGameTypes.containsAll(gameTypes)
                }
            }
            return false
        }
    }
}
