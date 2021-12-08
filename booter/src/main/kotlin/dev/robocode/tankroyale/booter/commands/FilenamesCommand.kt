package dev.robocode.tankroyale.booter.commands

import dev.robocode.tankroyale.booter.model.BotEntry
import java.nio.file.Files.exists
import java.nio.file.Files.list
import java.nio.file.Path
import kotlin.collections.ArrayList
import kotlin.collections.HashSet
import kotlin.io.path.isDirectory

class FilenamesCommand(private val botsDirPaths: List<Path>) : Command(botsDirPaths) {

    fun listBotEntries(gameTypesCSV: String?): List<BotEntry> {
        val gameTypes: List<String>? = gameTypesCSV?.split(",")?.map { it.trim() }

        val botNames = listBotNames()
        val botEntries = ArrayList<BotEntry>()
        botNames.forEach { botName ->
            try {
                getBotInfoList(botName).forEach { botInfo ->
                    if (gameTypes == null || botInfo.gameTypes.split(",").containsAll(gameTypes)) {
                        botEntries.add(BotEntry(botName, botInfo))
                    }
                }
            } catch (ex: Exception) {
                System.err.println("ERROR: ${ex.message}")
            }
        }
        return botEntries
    }

    fun listBotNames(gameTypesCSV: String?): Set<String> {
        val botNames = HashSet<String>()
        listBotEntries(gameTypesCSV).forEach { entry -> botNames.add(entry.filename) }
        return botNames
    }

    private fun listBotNames(): Set<String> {
        val botNames = HashSet<String>()

        botsDirPaths.forEach { dirPath ->
            list(dirPath).forEach { botDirPath ->
                run {
                    if (botDirPath.isDirectory()) {
                        val botName = botDirPath.fileName.toString()
                        if (exists(botDirPath.resolve("$botName.json"))) {
                            botNames += botName
                        }
                    }
                }
            }
        }
        return botNames
    }
}
