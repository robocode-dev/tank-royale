package io.robocode2.bootstrap.util

import io.robocode2.bootstrap.BootstrapException
import io.robocode2.bootstrap.model.BotEntry
import io.robocode2.bootstrap.model.BotInfo
import io.robocode2.bootstrap.util.OSUtil.OSType.MacOS
import io.robocode2.bootstrap.util.OSUtil.OSType.Windows
import kotlinx.serialization.ImplicitReflectionSerializer
import kotlinx.serialization.MissingFieldException
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonParsingException
import kotlinx.serialization.parse
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Files.list
import java.nio.file.Path
import java.util.function.Predicate
import java.util.stream.Collectors.toList


class BotFinder(private val bootstrapPath: Path) {

    @ImplicitReflectionSerializer
    fun findBotEntries(): List<BotEntry> {
        val botNames = findBotNames()
        val botEntries = ArrayList<BotEntry>()
        botNames.forEach { botName ->
            try {
                botEntries += BotEntry(botName, getBotInfo(botName))
            } catch (ex: Exception) {
                System.err.println(ex.message)
            }
        }
        return botEntries
    }

    fun findOsScript(botName: String): Path? = when (OSUtil.getOsType()) {
        Windows -> findWindowsScript(botName)
        MacOS -> findMacOsScript(botName)
        else -> findFirstUnixScript(botName)
    }

    private fun findWindowsScript(botName: String): Path? {
        var path = bootstrapPath.resolve("$botName.bat")
        if (Files.exists(path)) return path

        path = path.resolve("$botName.cmd")
        if (Files.exists(path)) return path

        path = path.resolve("$botName.ps1")
        if (Files.exists(path)) return path

        return null
    }

    private fun findMacOsScript(botName: String): Path? {
        val commandFile = bootstrapPath.resolve("$botName.command")
        if (Files.exists(commandFile)) return commandFile
        return findFirstUnixScript(botName)
    }

    private fun findFirstUnixScript(botName: String): Path? {
        val shFile = bootstrapPath.resolve("$botName.sh")
        if (Files.exists(shFile)) return shFile

        list(bootstrapPath).filter(IsBotFile(botName)).collect(toList()).forEach { path ->
            if (path.fileName.toString().toLowerCase() == botName.toLowerCase() ||
                    readFirstLine(path).trim().startsWith("#!")) {
                return path
            }
        }
        return null
    }

    private fun readFirstLine(filePath: Path): String {
        return Files.newInputStream(bootstrapPath.resolve(filePath)).bufferedReader().readLine() ?: ""
    }

    private fun findBotNames(): Set<String> {
        val files = list(bootstrapPath).filter(HasFileExtensions(arrayOf("json")))

        val names = HashSet<String>()
        files?.forEach { path -> names += path.toFile().nameWithoutExtension }
        return names
    }

    @ImplicitReflectionSerializer
    private fun getBotInfo(botName: String): BotInfo {
        var filePath: Path? = null
        try {
            filePath = bootstrapPath.resolve("$botName.json").toAbsolutePath()
            val content = readContent(filePath)
            return Json.parse(content)
        } catch (ex: JsonParsingException) {
            throw BootstrapException("Could not parse JSON file: $filePath")
        } catch (ex: MissingFieldException) {
            throw BootstrapException("${ex.message}. File: $filePath")
        }
    }

    private fun readContent(filePath: Path): String {
        val contentBuilder = StringBuilder()
        Files.lines(filePath, StandardCharsets.UTF_8).use { stream ->
            stream.forEach { s -> contentBuilder.append(s).append('\n') }
        }
        return contentBuilder.toString()
    }
}

internal class HasFileExtensions(private val fileExtensions: Array<String>) : Predicate<Path> {

    override fun test(path: Path): Boolean {
        if (Files.isDirectory(path)) return false
        fileExtensions.forEach { ext ->
            if (path.toString().toLowerCase().endsWith(".${ext.toLowerCase()}")) return true
        }
        return false
    }
}

internal class IsBotFile(private val botName: String) : Predicate<Path> {

    override fun test(path: Path): Boolean {
        val filenameLC = path.fileName.toString().toLowerCase()
        val botNameLC = botName.toLowerCase()

        return filenameLC == botNameLC || filenameLC.startsWith("$botNameLC.")
    }
}