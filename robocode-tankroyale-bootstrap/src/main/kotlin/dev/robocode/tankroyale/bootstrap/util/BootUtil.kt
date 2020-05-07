package dev.robocode.tankroyale.bootstrap.util

import dev.robocode.tankroyale.bootstrap.BootstrapException
import dev.robocode.tankroyale.bootstrap.model.BotEntry
import dev.robocode.tankroyale.bootstrap.model.BotInfo
import dev.robocode.tankroyale.bootstrap.util.OSUtil.OSType.MacOS
import dev.robocode.tankroyale.bootstrap.util.OSUtil.OSType.Windows
import kotlinx.serialization.ImplicitReflectionSerializer
import kotlinx.serialization.MissingFieldException
import kotlinx.serialization.UnstableDefault
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonDecodingException
import kotlinx.serialization.parse
import java.io.IOException
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Files.list
import java.nio.file.Path
import java.util.function.Predicate
import java.util.stream.Collectors.toList

@UnstableDefault
@ImplicitReflectionSerializer
class BootUtil(private val botPaths: List<Path>) {

    fun findBotEntries(gameTypesCSV: String?): List<BotEntry> {
        val gameTypes: List<String>? = gameTypesCSV?.split(",")?.map { it.trim() }

        val botNames = findBotNames()
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

            val process = processBuilder.start()
            val env = processBuilder.environment()

            setEnvVars(env, getBotInfo(filename)!!);

            println("$filename started")
            return process

        } catch (ex: IOException) {
            System.err.println("ERROR: ${ex.message}")
            return null
        }
    }

    private fun setEnvVars(envMap: MutableMap<String,String>, botInfo: BotInfo) {
        setEnvVar(envMap, Env.SERVER_URL, System.getProperty("server.url"))
        setEnvVar(envMap, Env.BOT_NAME, botInfo.name)
        setEnvVar(envMap, Env.BOT_VERSION, botInfo.version)
        setEnvVar(envMap, Env.BOT_AUTHOR, botInfo.author)
        setEnvVar(envMap, Env.BOT_DESCRIPTION, botInfo.description)
        setEnvVar(envMap, Env.BOT_URL, botInfo.url)
        setEnvVar(envMap, Env.BOT_COUNTRY_CODE, botInfo.countryCode)
        setEnvVar(envMap, Env.BOT_GAME_TYPES, botInfo.gameTypes.joinToString())
        setEnvVar(envMap, Env.BOT_PLATFORM, botInfo.platform)
        setEnvVar(envMap, Env.BOT_PROG_LANG, botInfo.programmingLang)
    }

    private fun setEnvVar(envMap: MutableMap<String,String>, env: Env, value: Any?) {
        if (value != null) envMap[env.name] = value.toString()
    }

    private fun findOsScript(botName: String): Path? = when (OSUtil.getOsType()) {
        Windows -> findWindowsScript(botName)
        MacOS -> findMacOsScript(botName)
        else -> findFirstUnixScript(botName)
    }

    private fun findWindowsScript(botName: String): Path? {
        var path: Path?
        botPaths.forEach { dirPath ->
            run {
                path = resolveFullBotPath(dirPath, "$botName.bat")
                if (path != null) return path

                path = resolveFullBotPath(dirPath, "$botName.cmd")
                if (path != null) return path

                path = resolveFullBotPath(dirPath, "$botName.psi")
                if (path != null) return path
            }
        }
        return findFirstUnixScript(botName)
    }

    private fun findMacOsScript(botName: String): Path? {
        var path: Path?
        botPaths.forEach { dirPath ->
            run {
                path = resolveFullBotPath(dirPath, "$botName.command")
                if (path != null) return path
            }
        }
        return findFirstUnixScript(botName)
    }

    private fun findFirstUnixScript(botName: String): Path? {
        var path: Path?
        botPaths.forEach { dirPath ->
            run {
                path = resolveFullBotPath(dirPath, "$botName.sh")
                if (path != null) return path
            }
        }

        // Look for any file with no file extension or where the file containing the '#!' characters, i.e. a script
        botPaths.forEach { dirPath ->
            run {
                list(dirPath).filter(IsBotFile(botName)).collect(toList()).forEach { path ->
                    if (path.fileName.toString().toLowerCase() == botName.toLowerCase())
                        return path
                    if (readFirstLine(dirPath.resolve(path)).trim().startsWith("#!"))
                        return path
                }
            }
        }

        return null // No path found
    }

    private fun readFirstLine(path: Path): String {
        return Files.newInputStream(path).bufferedReader().readLine() ?: ""
    }

    private fun findBotNames(): Set<String> {
        val names = HashSet<String>()
        botPaths.forEach { dirPath ->
            val files = list(dirPath).filter(HasFileExtensions(arrayOf("json")))
            files?.forEach { path -> names += path.toFile().nameWithoutExtension }
        }
        return names
    }

    private fun resolveFullBotPath(botDirPath: Path, botPath: String): Path? {
        val path = botDirPath.resolve(botPath)
        return if (Files.exists(path)) path else null
    }

    private fun getBotInfo(botName: String): BotInfo? {
        var path: Path? = null
        botPaths.forEach { dirPath ->
            run {
                try {
                    path = resolveFullBotPath(dirPath, "$botName.json")?.toAbsolutePath()
                    if (path != null) {
                        val content = readContent(path!!)
                        return Json.parse(content)
                    }
                } catch (ex: JsonDecodingException) {
                    throw BootstrapException("Could not parse JSON file: $path")
                } catch (ex: MissingFieldException) {
                    throw BootstrapException("${ex.message}. File: $path")
                }
            }
        }
        return null // not found
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