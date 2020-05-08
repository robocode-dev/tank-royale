package dev.robocode.tankroyale.bootstrap.commands

import dev.robocode.tankroyale.bootstrap.model.BotEntry
import kotlinx.serialization.ImplicitReflectionSerializer
import kotlinx.serialization.UnstableDefault
import java.nio.file.Files
import java.nio.file.Files.list
import java.nio.file.Path
import java.util.function.Predicate

@UnstableDefault
@ImplicitReflectionSerializer
class FilenamesCommand(private val botPaths: List<Path>): Command(botPaths) {

    fun listBotEntries(gameTypesCSV: String?): List<BotEntry> {
        val gameTypes: List<String>? = gameTypesCSV?.split(",")?.map { it.trim() }

        val botNames = listBotNames()
        val botEntries = ArrayList<BotEntry>()
        botNames.forEach { botName ->
            try {
                val botInfo = getBotInfo(botName)
                if (botInfo != null && (gameTypes == null || botInfo.gameTypes.containsAll(gameTypes)))
                    botEntries.add(BotEntry(botName, botInfo))
            } catch (ex: Exception) {
                System.err.println("ERROR: ${ex.message}")
            }
        }
        return botEntries
    }

    private fun listBotNames(): Set<String> {
        val names = HashSet<String>()
        botPaths.forEach { dirPath ->
            val files = list(dirPath).filter(HasFileExtensions(arrayOf("json")))
            files?.forEach { path -> names += path.toFile().nameWithoutExtension }
        }
        return names
    }
}

private class HasFileExtensions(private val fileExtensions: Array<String>) : Predicate<Path> {

    override fun test(path: Path): Boolean {
        if (Files.isDirectory(path)) return false
        fileExtensions.forEach { ext ->
            if (path.toString().toLowerCase().endsWith(".${ext.toLowerCase()}")) return true
        }
        return false
    }
}