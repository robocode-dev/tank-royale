package dev.robocode.tankroyale.booter.process

import dev.robocode.tankroyale.booter.model.BootEntry
import dev.robocode.tankroyale.booter.util.Env
import dev.robocode.tankroyale.booter.util.Log
import dev.robocode.tankroyale.booter.util.OperatingSystemCheck
import dev.robocode.tankroyale.booter.util.OperatingSystemCheck.OperatingSystemType.Mac
import dev.robocode.tankroyale.booter.util.OperatingSystemCheck.OperatingSystemType.Windows
import java.nio.file.Files
import java.nio.file.Files.list
import java.nio.file.Path
import java.util.concurrent.ConcurrentSkipListMap
import java.util.function.Predicate
import java.util.stream.Collectors.toList
import kotlin.io.path.absolutePathString
import kotlin.io.path.exists

/**
 * Manages processes for running bots.
 */
class ProcessManager {

    private val processes = ConcurrentSkipListMap<Pid, Process>()
    private var teamId: TeamId = 1

    /**
     * Register shutdown hook to clean up processes when JVM exits.
     */
    fun registerShutdownHook() {
        Runtime.getRuntime().addShutdownHook(Thread { killAllProcesses() })
    }

    /**
     * Create processes for a bot or team located at the specified path.
     */
    fun createBotProcess(bootDir: Path, getBootEntry: (Path) -> BootEntry?) {
        if (!bootDir.exists()) {
            Log.error("Bot directory not found", bootDir)
            return
        }
        if (!Files.isDirectory(bootDir)) {
            Log.error("Path is not a valid bot directory", bootDir)
            return
        }
        boot(bootDir, getBootEntry).forEach { process ->
            processes[process.pid()] = process
        }
    }

    /**
     * Stop a bot process by its process ID.
     */
    fun stopBotProcess(pid: Pid): Boolean {
        val process = processes[pid]
        return if (process == null) {
            println("lost: $pid")
            false
        } else {
            stopProcess(process)
            true
        }
    }

    /**
     * Kill all running processes.
     */
    fun killAllProcesses() {
        processes.values.parallelStream().forEach { stopProcess(it) }
    }

    /**
     * Stop a specific process and remove it from the process map.
     */
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

    // BOT BOOTING

    /**
     * Boot either a single bot or a team from the specified directory.
     * Returns a set of started processes.
     */
    private fun boot(bootDir: Path, getBootEntry: (Path) -> BootEntry?): Set<Process> {
        try {
            val bootEntry = getBootEntry(bootDir)
            if (bootEntry == null) {
                Log.error("No valid boot entry found", bootDir)
                return emptySet()
            }

            // Check if this is a team entry
            if (bootEntry.teamMembers?.isNotEmpty() == true) {
                return bootTeam(
                    bootDir,
                    Team(teamId, bootEntry.name, bootEntry.version, bootEntry.teamMembers),
                    getBootEntry
                )
            }

            // Otherwise boot as single bot
            bootBot(bootDir, null, getBootEntry)?.let { return setOf(it) }

            // If we got here, the bot couldn't be booted but no exception was thrown
            Log.error("Failed to boot bot - no suitable boot method found", bootDir)
            return emptySet()
        } catch (ex: Exception) {
            Log.error(ex, bootDir)
            return emptySet()
        }
    }

    /**
     * Boot a team of bots with the specified team information.
     * Returns a set of started processes.
     */
    private fun bootTeam(bootDir: Path, team: Team, getBootEntry: (Path) -> BootEntry?): Set<Process> {
        val parentPath = bootDir.parent
        val botProcesses = HashSet<Process>()

        try {
            team.members.forEach { botName ->
                try {
                    val botDir = parentPath.resolve(botName)
                    if (!botDir.exists()) {
                        Log.error("Team member bot directory not found", botDir)
                        return@forEach
                    }
                    if (!Files.isDirectory(botDir)) {
                        Log.error("Team member path is not a valid directory", botDir)
                        return@forEach
                    }
                    findBootScriptOrNull(botDir)?.let { scriptPath ->
                        bootBot(botDir, team, getBootEntry)?.let { process ->
                            botProcesses.add(process)
                        } ?: run {
                            Log.error("Failed to boot team member bot", botDir)
                        }
                    }
                } catch (ex: Exception) {
                    val botDir = parentPath.resolve(botName)
                    Log.error(ex, botDir)
                }
            }

            teamId++
            return botProcesses
        } catch (ex: Exception) {
            Log.error(ex, bootDir)
            return botProcesses
        }
    }

    /**
     * Boot a single bot with optional team information.
     * Returns the started process or null if booting failed.
     */
    private fun bootBot(botDir: Path, team: Team? = null, getBootEntry: (Path) -> BootEntry?): Process? {
        try {
            val botEntry = getBootEntry(botDir)
            if (botEntry == null) {
                Log.error("Failed to get boot entry for bot", botDir)
                return null
            }

            val scriptPath = findBootScriptOrNull(botDir)
            if (scriptPath == null) {
                // Error already logged in findBootScriptOrNull
                return null
            }

            try {
                val processBuilder = createProcessBuilder(scriptPath.toString())
                processBuilder.directory(scriptPath.parent.toFile())

                // Set up environment variables
                val envMap = processBuilder.environment()
                setupBotEnvironment(envMap, botEntry, team)

                // Start the process
                return startProcess(processBuilder, botDir)
            } catch (ex: Exception) {
                Log.error(ex, botDir)
                return null
            }
        } catch (ex: Exception) {
            Log.error(ex, botDir)
            return null
        }
    }

    /**
     * Start a process from the given process builder and register it.
     * Returns the started process or throws an exception with formatted error message.
     */
    private fun startProcess(processBuilder: ProcessBuilder, botDir: Path): Process {
        try {
            return processBuilder.start().also { process ->
                println("${process.pid()};${botDir.absolutePathString()}")
                processes[process.pid()] = process
            }
        } catch (ex: Exception) {
            Log.error(ex, botDir)
            throw ex
        }
    }

    /**
     * Set up environment variables for a bot process.
     */
    private fun setupBotEnvironment(envMap: MutableMap<String, String?>, bootEntry: BootEntry, team: Team? = null) {
        // Set server properties
        setEnvVars(envMap, bootEntry)

        // Set team-specific properties if this is part of a team
        team?.let {
            envMap["TEAM_ID"] = team.id.toString()
            envMap["TEAM_NAME"] = team.name
            envMap["TEAM_VERSION"] = team.version
        }
    }

    // SCRIPT FINDING

    /**
     * Find the boot script for a bot based on the operating system.
     * Logs an error if no script is found using a simple line format.
     */
    private fun findBootScriptOrNull(botDir: Path): Path? {
        if (!botDir.exists()) {
            Log.error("Bot directory not found", botDir)
            return null
        }
        if (!Files.isDirectory(botDir)) {
            Log.error("Path is not a valid bot directory", botDir)
            return null
        }
        val scriptPath = findOsScript(botDir)
        if (scriptPath == null) {
            Log.error("No script found within the bot directory", botDir)
        }
        return scriptPath
    }

    /**
     * Find the appropriate boot script based on the current operating system.
     */
    private fun findOsScript(botDir: Path): Path? = when (OperatingSystemCheck.getOperatingSystemType()) {
        Windows -> findWindowsScript(botDir)
        Mac -> findMacOsScript(botDir)
        else -> findFirstUnixScript(botDir)
    }

    /**
     * Find a Windows-specific script (.bat or .cmd) for the bot.
     */
    private fun findWindowsScript(botDir: Path): Path? {
        val botName = botDir.fileName.toString()

        var path = botDir.resolve("$botName.bat")
        if (path.exists()) return path

        path = botDir.resolve("$botName.cmd")
        if (path.exists()) return path

        return findFirstUnixScript(botDir)
    }

    /**
     * Find a macOS-specific script (.command) for the bot.
     */
    private fun findMacOsScript(botDir: Path): Path? {
        val botName = botDir.fileName.toString()

        val path = botDir.resolve("$botName.command")
        if (path.exists()) return path

        return findFirstUnixScript(botDir)
    }

    /**
     * Find a Unix-compatible script (.sh or script with shebang) for the bot.
     */
    private fun findFirstUnixScript(botDir: Path): Path? {
        val botName = botDir.fileName.toString()

        val path = botDir.resolve("$botName.sh")
        if (path.exists()) return path

        // Look for any file with no file extension or containing the '#!' (shebang) characters
        return list(botDir)
            .filter(IsBotFile(botName))
            .collect(toList())
            .firstOrNull { filePath ->
                filePath.fileName.toString().equals(botName, ignoreCase = true) ||
                        readFirstLine(botDir.resolve(filePath)).trim().startsWith("#!")
            }
    }

    // PROCESS CREATION

    /**
     * Create a process builder for the given command, handling different script types.
     */
    private fun createProcessBuilder(command: String): ProcessBuilder {
        val cmd = command.lowercase()
        return when {
            cmd.endsWith(".bat") -> ProcessBuilder("cmd.exe", "/c \"$command\"")
            cmd.endsWith(".sh") -> ProcessBuilder("bash", "-c", "\"$command\"")
            else -> ProcessBuilder(command)
        }
    }

    companion object {
        /**
         * Read the first line of a file.
         */
        private fun readFirstLine(path: Path): String {
            return Files.newInputStream(path).bufferedReader().readLine() ?: ""
        }

        /**
         * Set environment variables based on bot entry information.
         */
        private fun setEnvVars(envMap: MutableMap<String, String?>, bootEntry: BootEntry) {
            // Set server connection properties
            System.getProperty("server.url")?.let { envMap[Env.SERVER_URL.name] = it }
            System.getProperty("server.secret")?.let { envMap[Env.SERVER_SECRET.name] = it }

            // Set bot-specific properties
            envMap[Env.BOT_BOOTED.name] = "true"
            envMap[Env.BOT_NAME.name] = bootEntry.name
            envMap[Env.BOT_VERSION.name] = bootEntry.version
            envMap[Env.BOT_AUTHORS.name] = bootEntry.authors.joinToString()

            // Set optional properties if available
            bootEntry.gameTypes?.let { envMap[Env.BOT_GAME_TYPES.name] = it.joinToString() }
            bootEntry.description?.let { envMap[Env.BOT_DESCRIPTION.name] = it }
            bootEntry.homepage?.let { envMap[Env.BOT_HOMEPAGE.name] = it }
            bootEntry.countryCodes?.let { envMap[Env.BOT_COUNTRY_CODES.name] = it.joinToString() }
            bootEntry.platform?.let { envMap[Env.BOT_PLATFORM.name] = it }
            bootEntry.programmingLang?.let { envMap[Env.BOT_PROG_LANG.name] = it }
            bootEntry.initialPosition?.let { envMap[Env.BOT_INITIAL_POS.name] = it }
        }
    }
}

/**
 * Predicate for filtering files that match the bot name pattern.
 */
internal class IsBotFile(private val botName: String) : Predicate<Path> {
    override fun test(path: Path): Boolean {
        val filename = path.fileName.toString().lowercase()
        val botName = botName.lowercase()

        return filename == botName || filename.startsWith("$botName.")
    }
}

/**
 * Data class representing a team of bots.
 */
internal class Team(val id: TeamId, val name: String, val version: String, val members: List<String>)

/**
 * Type aliases
 */
typealias Pid = Long
typealias TeamId = Long