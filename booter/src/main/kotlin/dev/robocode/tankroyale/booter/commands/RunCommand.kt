package dev.robocode.tankroyale.booter.commands

import dev.robocode.tankroyale.booter.model.AbstractBotEntry
import dev.robocode.tankroyale.booter.util.Env
import dev.robocode.tankroyale.booter.util.OSCheck
import dev.robocode.tankroyale.booter.util.OSCheck.OSType.MacOS
import dev.robocode.tankroyale.booter.util.OSCheck.OSType.Windows
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Files.list
import java.nio.file.Path
import java.util.*
import java.util.concurrent.ConcurrentSkipListMap
import java.util.function.Predicate
import java.util.stream.Collectors.toList
import kotlin.io.path.Path
import kotlin.io.path.absolutePathString
import kotlin.io.path.exists

class RunCommand : Command() {

    private val processes = ConcurrentSkipListMap<Long, Process>() // pid, process

    fun runBots(botPaths: Array<String>) {
        Runtime.getRuntime().addShutdownHook(Thread {
            killAllProcesses() // Kill all running processes before terminating
        })

        // Start up the bots provided with the input list
        botPaths.forEach { createBotProcess(Path(it)) }

        // Add new bots from the std-in or terminate if blank line is provided
        while (true) {
            val line = readlnOrNull()?.trim()
            val cmdAndArgs = line?.split("\\s+".toRegex(), limit = 2)
            if (cmdAndArgs?.isNotEmpty() == true) {
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
        }

        killAllProcesses() // Kill all running processes before terminating
    }

    private fun createBotProcess(botDir: Path) {
        startBotProcess(botDir)?.let { processes[it.pid()] = it }
    }

    private fun stopBotProcess(pid: Long) {
        processes[pid]?.let { stopProcess(it) }
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

            var process: Process? = null

            getBootEntry(botDir)?.let { bootEntry ->

                // important to transfer env. variables for bot to the process
                setEnvVars(processBuilder.environment(), bootEntry)

                process = processBuilder.start().also {
                    println("${it.pid()};${botDir.absolutePathString()}")
                }
                process?.let {
                    processes[it.pid()] = it
                }
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
        process.apply {
            val pid = pid()
            onExit().thenAccept {
                processes.remove(pid)
                println("stopped $pid")
            }
            descendants().forEach { it.destroyForcibly() }
            destroyForcibly()
        }
    }

    private fun createProcessBuilder(command: String): ProcessBuilder {
        val cmd = command.lowercase()
        return when {
            cmd.endsWith(".bat") -> // handle Batch script
                ProcessBuilder("cmd.exe", "/c \"$command\"")
            cmd.endsWith(".sh") -> // handle Bash Shell script
                ProcessBuilder("bash", "-c", "\"$command\"")
            else -> // handle regular command
                ProcessBuilder(command)
        }
    }

    private fun findOsScript(botDir: Path): Path? = when (OSCheck.getOsType()) {
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

        private fun setEnvVars(envMap: MutableMap<String, String?>, bootEntry: AbstractBotEntry) {
            System.getProperty("server.url")?.let {
                envMap[Env.SERVER_URL.name] = it
            }
            System.getProperty("server.secret")?.let {
                envMap[Env.SERVER_SECRET.name] = it
            }

            envMap[Env.BOT_BOOTED.name] = "true"

            envMap[Env.BOT_NAME.name] = bootEntry.name
            envMap[Env.BOT_VERSION.name] = bootEntry.version
            envMap[Env.BOT_AUTHORS.name] = bootEntry.authors.joinToString()

            bootEntry.gameTypes?.let {
                envMap[Env.BOT_GAME_TYPES.name] = bootEntry.gameTypes?.joinToString()
            }
            bootEntry.description?.let {
                envMap[Env.BOT_DESCRIPTION.name] = it
            }
            bootEntry.homepage?.let {
                envMap[Env.BOT_HOMEPAGE.name] = it
            }
            bootEntry.countryCodes?.let {
                envMap[Env.BOT_COUNTRY_CODES.name] = it.joinToString()
            }
            bootEntry.platform?.let {
                envMap[Env.BOT_PLATFORM.name] = it
            }
            bootEntry.programmingLang?.let {
                envMap[Env.BOT_PROG_LANG.name] = it
            }
            bootEntry.initialPosition?.let {
                envMap[Env.BOT_INITIAL_POS.name] = it
            }
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