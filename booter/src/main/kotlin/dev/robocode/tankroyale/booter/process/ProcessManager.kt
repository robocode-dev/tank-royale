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
     * Register a shutdown hook to clean up processes when JVM exits.
     */
    fun registerShutdownHook() {
        Runtime.getRuntime().addShutdownHook(Thread { killAllProcesses() })
    }

    /**
     * Create processes for a bot or team located at the specified path.
     */
    fun createBotProcess(bootDir: Path, getBootEntry: (Path) -> BootEntry?) {
        if (!isValidBotDirectory(bootDir)) {
            return
        }
        boot(bootDir, getBootEntry).forEach { process ->
            processes[process.pid()] = process
        }
    }

    /**
     * Validates if a path is a valid bot directory.
     * @return true if the directory exists and is a directory, false otherwise
     */
    private fun isValidBotDirectory(botDir: Path): Boolean {
        if (!botDir.exists()) {
            Log.error("Bot directory not found", botDir)
            return false
        }
        if (!Files.isDirectory(botDir)) {
            Log.error("Path is not a valid bot directory", botDir)
            return false
        }
        return true
    }

    /**
     * Stop a bot process by its process ID.
     * @return true if the process was found and stopped, false otherwise
     */
    fun stopBotProcess(pid: Pid): Boolean {
        val process = processes[pid] ?: run {
            println("lost: $pid")
            return false
        }

        stopProcess(process)
        return true
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
            val bootEntry = getBootEntry(bootDir) ?: run {
                Log.error("No valid boot entry found", bootDir)
                return emptySet()
            }

            // If this is a team entry, boot as a team
            if (isTeamEntry(bootEntry)) {
                return bootTeam(
                    bootDir,
                    createTeam(bootEntry),
                    getBootEntry
                )
            }

            // Otherwise boot as a single bot
            return bootSingleBot(bootDir, getBootEntry)
        } catch (ex: Exception) {
            Log.error(ex, bootDir)
            return emptySet()
        }
    }

    /**
     * Checks if a boot entry represents a team
     */
    private fun isTeamEntry(bootEntry: BootEntry): Boolean {
        return bootEntry.teamMembers?.isNotEmpty() == true
    }

    /**
     * Creates a Team object from a boot entry
     */
    private fun createTeam(bootEntry: BootEntry): Team {
        return Team(teamId, bootEntry.name, bootEntry.version, bootEntry.teamMembers!!)
    }

    /**
     * Boots a single bot (not part of a team)
     */
    private fun bootSingleBot(bootDir: Path, getBootEntry: (Path) -> BootEntry?): Set<Process> {
        bootBot(bootDir, null, getBootEntry)?.let { return setOf(it) }

        // If we got here, the bot couldn't be booted, but no exception was thrown
        Log.error("Failed to boot bot - no suitable boot method found", bootDir)
        return emptySet()
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
                bootTeamMember(parentPath, botName, team, getBootEntry, botProcesses)
            }

            teamId++
            return botProcesses
        } catch (ex: Exception) {
            Log.error(ex, bootDir)
            return botProcesses
        }
    }

    /**
     * Boot an individual team member
     */
    private fun bootTeamMember(
        parentPath: Path,
        botName: String,
        team: Team,
        getBootEntry: (Path) -> BootEntry?,
        botProcesses: MutableSet<Process>
    ) {
        try {
            val botDir = parentPath.resolve(botName)

            if (!isValidBotDirectory(botDir)) {
                return
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

    /**
     * Boot a single bot with optional team information.
     * Returns the started process or null if booting failed.
     */
    private fun bootBot(botDir: Path, team: Team? = null, getBootEntry: (Path) -> BootEntry?): Process? {
        try {
            // Get the boot entry
            val botEntry = getBootEntry(botDir) ?: run {
                Log.error("Failed to get boot entry for bot", botDir)
                return null
            }

            // Find the boot script
            val scriptPath = findBootScriptOrNull(botDir) ?: return null

            return createAndStartBotProcess(scriptPath, botDir, botEntry, team)
        } catch (ex: Exception) {
            Log.error(ex, botDir)
            return null
        }
    }

    /**
     * Creates and starts a bot process
     */
    private fun createAndStartBotProcess(
        scriptPath: Path,
        botDir: Path,
        botEntry: BootEntry,
        team: Team?
    ): Process? {
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
    }

    /**
     * Start a process from the given process builder and register it.
     * Returns the started process or throws an exception with a formatted error message.
     */
    /**
     * Start a process from the given process builder and register it.
     * Captures stderr output from the process and logs it.
     * 
     * @param processBuilder The ProcessBuilder to start the process from
     * @param botDir The directory of the bot being started
     * @return The started process
     * @throws Exception if the process could not be started
     */
    private fun startProcess(processBuilder: ProcessBuilder, botDir: Path): Process {
        try {
            val process = processBuilder.start()
            registerProcess(process, botDir)
            captureProcessErrorOutput(process, botDir)
            return process
        } catch (ex: Exception) {
            Log.error(ex, botDir)
            throw ex
        }
    }

    /**
     * Registers a started process in the process map and logs its PID.
     */
    private fun registerProcess(process: Process, botDir: Path) {
        val pid = process.pid()
        println("$pid;${botDir.absolutePathString()}")
        processes[pid] = process
    }

    /**
     * Captures stderr output from a process and logs it via Log.error().
     * Creates a dedicated thread for reading the error stream.
     */
    private fun captureProcessErrorOutput(process: Process, botDir: Path) {
        Thread {
            process.errorStream.bufferedReader().use { reader ->
                val lines = reader.lineSequence().toList()
                if (lines.isNotEmpty()) {
                    Log.error(lines.joinToString("\n"), botDir)
                }
            }
        }.apply {
            name = "ErrorCapture-${process.pid()}"
            isDaemon = true
            start()
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
        if (!isValidBotDirectory(botDir)) {
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
     * Falls back to a Unix script if not found.
     */
    private fun findWindowsScript(botDir: Path): Path? {
        val botName = botDir.fileName.toString()
        val extensions = listOf("bat", "cmd")

        for (extension in extensions) {
            val path = botDir.resolve("$botName.$extension")
            if (path.exists()) return path
        }

        return findFirstUnixScript(botDir)
    }

    /**
     * Find a macOS-specific script (.command) for the bot.
     * Falls back to a Unix script if not found.
     */
    private fun findMacOsScript(botDir: Path): Path? {
        val botName = botDir.fileName.toString()
        val path = botDir.resolve("$botName.command")

        return if (path.exists()) path else findFirstUnixScript(botDir)
    }

    /**
     * Find a Unix-compatible script (.sh or script with shebang) for the bot.
     */
    private fun findFirstUnixScript(botDir: Path): Path? {
        val botName = botDir.fileName.toString()

        // First check for the.sh file as it is the most common
        val shScript = botDir.resolve("$botName.sh")
        if (shScript.exists()) return shScript

        // Look for any file with no file extension or containing the '#!' (shebang) characters
        return findScriptWithShebangOrMatchingName(botDir, botName)
    }

    /**
     * Find a script file that either has a shebang or matches the bot name
     */
    private fun findScriptWithShebangOrMatchingName(botDir: Path, botName: String): Path? {
        return list(botDir)
            .filter(IsBotFile(botName))
            .collect(toList())
            .firstOrNull { filePath ->
                isExactNameMatch(filePath, botName) || hasShebang(botDir, filePath)
            }
    }

    /**
     * Checks if the file name exactly matches the bot name (case-insensitive)
     */
    private fun isExactNameMatch(filePath: Path, botName: String): Boolean {
        return filePath.fileName.toString().equals(botName, ignoreCase = true)
    }

    /**
     * Checks if the file has a shebang (#!) at the beginning
     */
    private fun hasShebang(botDir: Path, filePath: Path): Boolean {
        return readFirstLine(botDir.resolve(filePath)).trim().startsWith("#!")
    }

    // PROCESS CREATION

    /**
     * Create a process builder for the given command, handling different script types.
     */
    private fun createProcessBuilder(command: String): ProcessBuilder {
        return when (getScriptType(command)) {
            ScriptType.WINDOWS_BATCH -> ProcessBuilder("cmd.exe", "/c \"$command\"")
            ScriptType.SHELL_SCRIPT -> ProcessBuilder("bash", "-c", "\"$command\"")
            ScriptType.OTHER -> ProcessBuilder(command)
        }
    }

    /**
     * Enum defining different types of scripts
     */
    private enum class ScriptType {
        WINDOWS_BATCH,
        SHELL_SCRIPT,
        OTHER
    }

    /**
     * Determines the script type based on file extension
     */
    private fun getScriptType(command: String): ScriptType {
        val cmd = command.lowercase()
        return when {
            cmd.endsWith(".bat") -> ScriptType.WINDOWS_BATCH
            cmd.endsWith(".sh") -> ScriptType.SHELL_SCRIPT
            else -> ScriptType.OTHER
        }
    }

    companion object {
        /**
         * Read the first line of a file.
         */
        private fun readFirstLine(path: Path): String {
            return Files.newInputStream(path).bufferedReader().use { it.readLine() ?: "" }
        }

        /**
         * Set environment variables based on bot entry information.
         */
        private fun setEnvVars(envMap: MutableMap<String, String?>, bootEntry: BootEntry) {
            setServerProperties(envMap)
            setBotProperties(envMap, bootEntry)
            setOptionalBotProperties(envMap, bootEntry)
        }

        /**
         * Set server connection properties
         */
        private fun setServerProperties(envMap: MutableMap<String, String?>) {
            System.getProperty("server.url")?.let { envMap[Env.SERVER_URL.name] = it }
            System.getProperty("server.secret")?.let { envMap[Env.SERVER_SECRET.name] = it }
        }

        /**
         * Set required bot properties
         */
        private fun setBotProperties(envMap: MutableMap<String, String?>, bootEntry: BootEntry) {
            envMap[Env.BOT_BOOTED.name] = "true"
            envMap[Env.BOT_NAME.name] = bootEntry.name
            envMap[Env.BOT_VERSION.name] = bootEntry.version
            envMap[Env.BOT_AUTHORS.name] = bootEntry.authors.joinToString()
        }

        /**
         * Set optional bot properties if available
         */
        private fun setOptionalBotProperties(envMap: MutableMap<String, String?>, bootEntry: BootEntry) {
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
 * Type aliases
 */
typealias Pid = Long
typealias TeamId = Long