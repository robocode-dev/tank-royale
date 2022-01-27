package dev.robocode.tankroyale.booter.commands

import dev.robocode.tankroyale.booter.model.BotInfo
import dev.robocode.tankroyale.booter.util.Env
import dev.robocode.tankroyale.booter.util.OSUtil
import dev.robocode.tankroyale.booter.util.OSUtil.OSType.MacOS
import dev.robocode.tankroyale.booter.util.OSUtil.OSType.Windows
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Files.list
import java.nio.file.Path
import java.util.*
import java.util.function.Predicate
import java.util.stream.Collectors.toList
import kotlin.io.path.Path
import kotlin.io.path.absolutePathString
import kotlin.io.path.exists

class RunCommand : Command() {

    private val processes = HashMap<Long, Process>() // pid, process

    fun runBots(botPaths: Array<String>?) {
        Runtime.getRuntime().addShutdownHook(Thread {
            killAllProcesses() // Kill all running processes before terminating
        })

        // Start up the bots provided with the input list
        botPaths?.forEach {
            createBotProcess(Path(it))
        }

        // Add new bots from the std-in or terminate if blank line is provided
        do {
            val line = readLine()?.trim()
            val cmdAndArgs = line?.split("\\s+".toRegex(), limit = 2)
            if (cmdAndArgs != null && cmdAndArgs.isNotEmpty()) {
                val command = cmdAndArgs[0].lowercase(Locale.getDefault()).trim()
                if (command == "quit") {
                    break // terminate running bots
                }
                if (cmdAndArgs.size >= 2) {
                    val arg = cmdAndArgs[1]

                    when (command) {
                        "run" -> {
                            val dir = Path(arg)
                            createBotProcess(dir)
                        }
                        "stop" -> {
                            val pid = arg.toLong()
                            stopBotProcess(pid)
                        }
                    }
                }
            }
        } while (true)

        killAllProcesses() // Kill all running processes before terminating
    }

    private fun createBotProcess(botDir: Path) {
        val process = startBotProcess(botDir)
        if (process != null) {
            processes[process.pid()] = process
        }
    }

    private fun stopBotProcess(pid: Long) {
        val process = processes[pid]
        if (process != null) {
            stopProcess(process)
            processes.remove(pid)
        }
    }

    private fun startBotProcess(botDir: Path): Process? {
        try {
            val scriptPath = findOsScript(botDir)
            if (scriptPath == null) {
                System.err.println("ERROR: No script found for the bot: $botDir")
                return null
            }

            val processBuilder = createProcessBuilder(scriptPath.toString())
            processBuilder.directory(scriptPath.parent.toFile()) // set working directory
//            processBuilder.inheritIO()

            val process = processBuilder.start()
            val env = processBuilder.environment()

            val botInfo = getBotInfo(botDir)
            if (botInfo != null) {
                setEnvVars(env, botInfo) // important to transfer env. variables for bot to the process

                println("${process.pid()};${botDir.absolutePathString()}")
            }
            return process

        } catch (ex: IOException) {
            System.err.println("ERROR: ${ex.message}")
            return null
        }
    }

    private fun killAllProcesses() {
        processes.values.parallelStream().forEach { stopProcess(it) }
    }

    private fun stopProcess(process: Process) {
        val pid = process.pid()

        process.onExit().thenAccept {
            println("stopped $pid")
        }
        process.descendants().forEach { it.destroyForcibly() }
        process.destroyForcibly().waitFor()
    }

    private fun createProcessBuilder(command: String): ProcessBuilder {
        val cmd = command.lowercase()
        return when {
            cmd.endsWith(".bat") -> // handle Batch script
                ProcessBuilder("cmd.exe", "/c \"$command\"")
            cmd.endsWith(".sh") -> // handle Bash Shell script
                ProcessBuilder("bash", "-c", command)
            else -> // handle regular command
                ProcessBuilder(command)
        }
    }

    private fun findOsScript(botDir: Path): Path? = when (OSUtil.getOsType()) {
        Windows -> findWindowsScript(botDir)
        MacOS -> findMacOsScript(botDir)
        else -> findFirstUnixScript(botDir)
    }

    private fun findWindowsScript(botDir: Path): Path? {
        val botName = botDir.fileName.toString()

        var path = botDir.resolve("$botName.bat")
        if (path.exists()) return path

        path = botDir.resolve("$botName.cmd")
        if (path.exists()) return path

        return findFirstUnixScript(botDir)
    }

    private fun findMacOsScript(botDir: Path): Path? {
        val botName = botDir.fileName.toString()

        val path = botDir.resolve("$botName.command")
        if (path.exists()) return path

        return findFirstUnixScript(botDir)
    }

    private fun findFirstUnixScript(botDir: Path): Path? {
        val botName = botDir.fileName.toString()

        val path = botDir.resolve("$botName.sh")
        if (path.exists()) return path

        // Look for any file with no file extension or where the file containing the '#!' characters, i.e. a script

        list(botDir).filter(IsBotFile(botName)).collect(toList()).forEach { filePath ->
            if (filePath.fileName.toString().equals(botName, ignoreCase = true))
                return filePath
            if (readFirstLine(botDir.resolve(filePath)).trim().startsWith("#!"))
                return filePath
        }
        return null // No path found
    }

    companion object {
        private fun readFirstLine(path: Path): String {
            return Files.newInputStream(path).bufferedReader().readLine() ?: ""
        }

        private fun setEnvVars(envMap: MutableMap<String, String>, botInfo: BotInfo) {
            setEnvVar(envMap, Env.SERVER_URL, System.getProperty("server.url"))
            setEnvVar(envMap, Env.BOT_NAME, botInfo.name)
            setEnvVar(envMap, Env.BOT_VERSION, botInfo.version)
            setEnvVar(envMap, Env.BOT_AUTHORS, botInfo.authors)
            setEnvVar(envMap, Env.BOT_DESCRIPTION, botInfo.description)
            setEnvVar(envMap, Env.BOT_HOMEPAGE, botInfo.homepage)
            setEnvVar(envMap, Env.BOT_COUNTRY_CODES, botInfo.countryCodes)
            setEnvVar(envMap, Env.BOT_GAME_TYPES, botInfo.gameTypes)
            setEnvVar(envMap, Env.BOT_PLATFORM, botInfo.platform)
            setEnvVar(envMap, Env.BOT_PROG_LANG, botInfo.programmingLang)
        }

        private fun setEnvVar(envMap: MutableMap<String, String>, env: Env, value: Any?) {
            if (value != null) envMap[env.name] = value.toString()
        }
    }
}

internal class IsBotFile(private val botName: String) : Predicate<Path> {

    override fun test(path: Path): Boolean {
        val filename = path.fileName.toString().lowercase()
        val botName = botName.lowercase()

        return filename == botName || filename.startsWith("$botName.")
    }
}