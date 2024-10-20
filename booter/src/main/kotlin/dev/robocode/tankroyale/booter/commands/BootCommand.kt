package dev.robocode.tankroyale.booter.commands

import dev.robocode.tankroyale.booter.model.BootEntry
import dev.robocode.tankroyale.booter.util.Env
import dev.robocode.tankroyale.booter.util.OperatingSystemCheck
import dev.robocode.tankroyale.booter.util.OperatingSystemCheck.OperatingSystemType.Mac
import dev.robocode.tankroyale.booter.util.OperatingSystemCheck.OperatingSystemType.Windows
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

    private val processes = ConcurrentSkipListMap<Pid, Process>()

    private var teamId: TeamId = 1

    fun boot(bootPaths: Array<String>?) {
        // Kill all running processes before terminating
        Runtime.getRuntime().addShutdownHook(Thread { killAllProcesses() })

        // Start up the bots provided with the input list
        bootPaths?.forEach { createBotProcess(Path(it)) }

        processCommandLineInput()
    }

    private fun processCommandLineInput() {
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
                        "boot" -> {
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
    }

    private fun createBotProcess(bootDir: Path) {
        boot(bootDir).forEach {
            processes[it.pid()] = it
        }
    }

    private fun stopBotProcess(pid: Pid) {
        val process = processes[pid]
        if (process == null) {
            println("lost: $pid")
        } else {
            stopProcess(process)
        }
    }

    private fun boot(bootDir: Path): Set<Process> {
        getBootEntry(bootDir)?.let { bootEntry ->
            bootEntry.apply {
                if (teamMembers?.isNotEmpty() == true) {
                    return bootTeam(bootDir, Team(teamId, bootEntry.name, bootEntry.version, teamMembers))
                }
            }
            bootBot(bootDir)?.let { return setOf(it) }
        }
        return emptySet()
    }

    private fun findBootScriptOrNull(botDir: Path): Path? {
        val scriptPath = findOsScript(botDir)
        if (scriptPath == null) {
            System.err.println("ERROR: No script found within the bot directory: $botDir")
        }
        return scriptPath
    }

    private fun bootTeam(bootDir: Path, team: Team): Set<Process> {
        val parentPath = bootDir.parent

        val botProcesses = HashSet<Process>()

        team.members.forEach { botName ->
            val botDir = parentPath.resolve(botName)
            findBootScriptOrNull(botDir)?.let {
                bootBot(botDir, team)?.let {
                    botProcesses.add(it)
                }
            }
        }

        teamId++

        return botProcesses
    }

    private fun bootBot(botDir: Path, team: Team? = null): Process? {
        getBootEntry(botDir)?.let { botEntry ->
            findBootScriptOrNull(botDir)?.let { scriptPath ->

                val processBuilder = createProcessBuilder(scriptPath.toString())
                processBuilder.directory(scriptPath.parent.toFile()) // set working directory

                // important to transfer env. variables for bot to the process
                val envMap = processBuilder.environment()
                setEnvVars(envMap, botEntry)
                team?.let {
                    envMap["TEAM_ID"] = team.id.toString()
                    envMap["TEAM_NAME"] = team.name
                    envMap["TEAM_VERSION"] = team.version
                }
                val process = processBuilder.start().also {
                    println("${it.pid()};${botDir.absolutePathString()}")
                    processes[it.pid()] = it
                }
                return process
            }
        }
        return null
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

    private fun findOsScript(botDir: Path): Path? = when (OperatingSystemCheck.getOperatingSystemType()) {
        Windows -> findWindowsScript(botDir)
        Mac -> findMacOsScript(botDir)
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

        private fun setEnvVars(envMap: MutableMap<String, String?>, bootEntry: BootEntry) {
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
                envMap[Env.BOT_GAME_TYPES.name] = bootEntry.gameTypes.joinToString()
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

internal class Team(val id: TeamId, val name: String, val version: String, val members: List<String>)
