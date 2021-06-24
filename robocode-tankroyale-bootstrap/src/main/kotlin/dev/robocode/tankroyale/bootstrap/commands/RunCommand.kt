package dev.robocode.tankroyale.bootstrap.commands

import dev.robocode.tankroyale.bootstrap.model.BotInfo
import dev.robocode.tankroyale.bootstrap.util.Env
import dev.robocode.tankroyale.bootstrap.util.OSUtil
import dev.robocode.tankroyale.bootstrap.util.OSUtil.OSType.MacOS
import dev.robocode.tankroyale.bootstrap.util.OSUtil.OSType.Windows
import java.io.IOException
import java.lang.Thread.sleep
import java.nio.file.Files
import java.nio.file.Files.list
import java.nio.file.Path
import java.util.function.Predicate
import java.util.stream.Collectors.toList
import kotlin.collections.ArrayList

class RunCommand(private val botPaths: List<Path>): Command(botPaths) {

    private val processes = ArrayList<Process>()

    fun runBots(filenames: Array<String>) {
        Runtime.getRuntime().addShutdownHook(Thread {
            killProcesses(processes) // Kill all running processes before terminating
        })

        // Start up the bots provided with the input list
        filenames.forEach { filename -> addBotProcess(filename, processes) }

        // Add new bots from the std-in or terminate if blank line is provided
        do {
            val filename = readLine()?.trim()
            if (filename != null && filename.isBlank()) {
                break
            }
            if (filename != null) {
                addBotProcess(filename, processes)
            } else {
                sleep(500)
            }
        } while (true)

        killProcesses(processes) // Kill all running processes before terminating
    }

    private fun addBotProcess(filename: String, processes: MutableList<Process>) {
        val process = startBotProcess(filename)
        if (process != null) {
            processes.add(process)
        }
    }

    private fun startBotProcess(filename: String): Process? {
        try {
            val scriptPath = findOsScript(filename)
            if (scriptPath == null) {
                System.err.println("ERROR: No script found for the bot: $filename")
                return null
            }

            val command = scriptPath.toString()
            val processBuilder = createProcessBuilder(command)
            val process = processBuilder.start()
            val env = processBuilder.environment()

            setEnvVars(env, getBotInfo(filename)!!)

            println("${process.pid()}:$filename")
            return process

        } catch (ex: IOException) {
            System.err.println("ERROR: ${ex.message}")
            return null
        }
    }

    private fun createProcessBuilder(command: String): ProcessBuilder {
        val cmd = command.lowercase()
        return when {
            cmd.endsWith(".bat") -> // handle Batch script
                ProcessBuilder("cmd.exe", "/c \"$command\"")
            cmd.endsWith(".ps1") -> // handle PowerShell script
                ProcessBuilder("powershell.exe", "-ExecutionPolicy ByPass", "-File \"$command\"")
            cmd.endsWith(".sh") -> // handle Bash Shell script
                ProcessBuilder("bash.exe", "-c \"$command\"")
            else -> // handle regular command
                ProcessBuilder(command)
        }
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
                    if (path.fileName.toString().equals(botName, ignoreCase = true))
                        return path
                    if (readFirstLine(dirPath.resolve(path)).trim().startsWith("#!"))
                        return path
                }
            }
        }
        return null // No path found
    }

    companion object {
        private fun readFirstLine(path: Path): String {
            return Files.newInputStream(path).bufferedReader().readLine() ?: ""
        }

        private fun setEnvVars(envMap: MutableMap<String,String>, botInfo: BotInfo) {
            setEnvVar(envMap, Env.SERVER_URL, System.getProperty("server.url"))
            setEnvVar(envMap, Env.BOT_NAME, botInfo.name)
            setEnvVar(envMap, Env.BOT_VERSION, botInfo.version)
            setEnvVar(envMap, Env.BOT_AUTHORS, botInfo.authors)
            setEnvVar(envMap, Env.BOT_DESCRIPTION, botInfo.description)
            setEnvVar(envMap, Env.BOT_URL, botInfo.url)
            setEnvVar(envMap, Env.BOT_COUNTRY_CODES, botInfo.countryCodes)
            setEnvVar(envMap, Env.BOT_GAME_TYPES, botInfo.gameTypes)
            setEnvVar(envMap, Env.BOT_PLATFORM, botInfo.platform)
            setEnvVar(envMap, Env.BOT_PROG_LANG, botInfo.programmingLang)
        }

        private fun setEnvVar(envMap: MutableMap<String,String>, env: Env, value: Any?) {
            if (value != null) envMap[env.name] = value.toString()
        }

        private fun killProcesses(processes: List<Process>) {
            processes.parallelStream().forEach { p ->
                p.descendants().forEach { d -> d.destroyForcibly() }
                p.destroyForcibly().waitFor()
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