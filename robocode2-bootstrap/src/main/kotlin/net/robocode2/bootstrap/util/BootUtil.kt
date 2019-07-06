package net.robocode2.bootstrap.util

import net.robocode2.bootstrap.BootstrapException
import net.robocode2.bootstrap.model.BotEntry
import net.robocode2.bootstrap.model.BotInfo
import net.robocode2.bootstrap.util.OSUtil.OSType.MacOS
import net.robocode2.bootstrap.util.OSUtil.OSType.Windows
import kotlinx.serialization.ImplicitReflectionSerializer
import kotlinx.serialization.MissingFieldException
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonParsingException
import kotlinx.serialization.parse
import java.io.IOException
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Files.list
import java.nio.file.Path
import java.util.function.Predicate
import java.util.stream.Collectors.toList


class BootUtil(private val bootstrapPath: Path) {

    @ImplicitReflectionSerializer
    fun findBotEntries(): List<BotEntry> {
        val botNames = findBotNames()
        val botEntries = ArrayList<BotEntry>()
        botNames.forEach { botName ->
            try {
                botEntries.add(BotEntry(botName, getBotInfo(botName)))
            } catch (ex: Exception) {
                System.err.println("ERROR: ${ex.message}")
            }
        }
        return botEntries
    }

    @ImplicitReflectionSerializer
    fun startBots(filenames: Array<String>): List<Process> {
        val processes = ArrayList<Process>()
        filenames.forEach { filename ->
            run {
                val process = startBot(filename)
                if (process != null) processes.add(process)
            }
        }
        return processes
    }

    @ImplicitReflectionSerializer
    private fun startBot(filename: String): Process? {
        try {
            val scriptPath = findOsScript(filename)
            if (scriptPath == null) {
                System.err.println("ERROR: No script found for the bot: $filename")
                return null
            }

            val command = scriptPath.toString()
            val commandLC = command.toLowerCase()

            val processBuilder = when {
                commandLC.endsWith(".bat") -> // handle Batch script
                    ProcessBuilder("cmd.exe", "/c \"$command\"")
                commandLC.endsWith(".ps1") -> // handle PowerShell script
                    ProcessBuilder("powershell.exe", "-ExecutionPolicy ByPass", "-File \"$command\"")
                commandLC.endsWith(".sh") -> // handle Bash Shell script
                    ProcessBuilder("bash.exe", "-c \"$command\"")
                else -> // handle regular command
                    ProcessBuilder(command)
            }

//            processBuilder.redirectErrorStream(true)

            val botInfo = getBotInfo(filename)

            val env = processBuilder.environment()

            env[Env.BOT_NAME.name] = botInfo.name
            env[Env.BOT_VERSION.name] = botInfo.version
            env[Env.BOT_AUTHOR.name] = botInfo.author
            if (botInfo.description != null)
                env[Env.BOT_DESCRIPTION.name] = botInfo.description
            if (botInfo.countryCode != null)
                env[Env.BOT_COUNTRY_CODE.name] = botInfo.countryCode
            env[Env.BOT_GAME_TYPES.name] = botInfo.gameTypes.joinToString()
            if (botInfo.programmingLang != null)
                env[Env.BOT_PROG_LANG.name] = botInfo.programmingLang

            val process = processBuilder.start()

//            val reader = BufferedReader(InputStreamReader(process.inputStream))

//            Thread.sleep(2000)

//            var line: String? = reader.readLine()
//            do {
//                println(line)
//                line = reader.readLine()
//            } while (line != null)

            println("$filename started")
            return process

        } catch (ex: IOException) {
            System.err.println("ERROR: ${ex.message}")
            return null
        }
    }

    private fun findOsScript(botName: String): Path? = when (OSUtil.getOsType()) {
        Windows -> findWindowsScript(botName)
        MacOS -> findMacOsScript(botName)
        else -> findFirstUnixScript(botName)
    }

    private fun findWindowsScript(botName: String): Path? {
        var path = bootstrapPath.resolve("$botName.bat")
        if (Files.exists(path)) return path

        path = bootstrapPath.resolve("$botName.cmd")
        if (Files.exists(path)) return path

        path = bootstrapPath.resolve("$botName.ps1")
        if (Files.exists(path)) return path

        return findFirstUnixScript(botName)
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