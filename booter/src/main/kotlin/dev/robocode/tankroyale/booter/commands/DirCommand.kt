package dev.robocode.tankroyale.booter.commands

import dev.robocode.tankroyale.booter.model.BotEntry
import dev.robocode.tankroyale.booter.model.BotInfo
import java.nio.file.Files.exists
import java.nio.file.Files.list
import java.nio.file.Path
import java.nio.file.Paths
import kotlin.collections.ArrayList
import kotlin.collections.HashSet
import kotlin.io.path.isDirectory

class DirCommand(private val botRootPaths: List<Path>) : Command() {

    fun listBotEntries(gameTypesCSV: String?): List<BotEntry> {
        val gameTypes: List<String> = gameTypesCSV?.split(",")?.map { it.trim() } ?: emptyList()

        val dirs = listBotDirectories()
        val botEntries = ArrayList<BotEntry>()
        dirs.forEach { dirPath ->
            try {
                val botInfo = getBotInfo(dirPath)
                if (botInfoContainsGameTypes(botInfo, gameTypes)) {
                    botEntries.add(BotEntry(dirPath.toAbsolutePath().toString(), botInfo!!))
                }
            } catch (ex: Exception) {
                System.err.println("ERROR: ${ex.message}")
            }
        }
        return botEntries
    }

    fun listBotDirectories(gameTypesCSV: String?): List<Path> =
        listBotEntries(gameTypesCSV).map { entry -> Paths.get(entry.dir) }.toSet().toList().sorted()

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

    private fun botInfoContainsGameTypes(botInfo: BotInfo?, gameTypes: List<String>) =
        botInfo != null && (gameTypes.isEmpty() || botInfo.gameTypes.split(",").containsAll(gameTypes))
}
