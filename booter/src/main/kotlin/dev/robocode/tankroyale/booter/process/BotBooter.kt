package dev.robocode.tankroyale.booter.process

import dev.robocode.tankroyale.booter.model.BootEntry
import dev.robocode.tankroyale.booter.util.Log
import java.nio.file.Files
import java.nio.file.Path
import java.util.concurrent.atomic.AtomicLong
import kotlin.io.path.exists

/**
 * Handles bot and team boot orchestration: locates scripts, configures process builders,
 * and starts bot processes. Returns started processes to [ProcessManager] for lifecycle tracking.
 *
 * Thread-safety: [teamId] is an [AtomicLong], so concurrent calls to [boot] are safe.
 *
 * @param launchProcess Creates an OS-appropriate [ProcessBuilder] for a script path.
 *   Defaults to [ProcessLauncher.createProcessBuilder].
 * @param setupEnvironment Configures bot environment variables on a process environment map.
 *   Defaults to [BotEnvironment.setup].
 * @param findScript Locates the executable boot script for a bot directory.
 *   Defaults to [ScriptFinder.findScript].
 */
internal class BotBooter(
    private val launchProcess: (String) -> ProcessBuilder = ProcessLauncher::createProcessBuilder,
    private val setupEnvironment: (MutableMap<String, String?>, BootEntry, Team?) -> Unit = BotEnvironment::setup,
    private val findScript: (Path) -> Path? = ScriptFinder::findScript,
    private val templateBooter: TemplateBooter = TemplateBooter(setupEnvironment)
) {

    private val teamId = AtomicLong(1)

    /**
     * Boots a bot or team from [bootDir].
     * @return a map of each started [Process] to its bot directory so that the caller can
     * register and monitor each process.
     */
    fun boot(bootDir: Path, getBootEntry: (Path) -> BootEntry?): Map<Process, Path> {
        if (!isValidBotDirectory(bootDir)) return emptyMap()
        return try {
            val bootEntry = getBootEntry(bootDir) ?: run {
                Log.error("No valid boot entry found", bootDir)
                return emptyMap()
            }
            if (isTeamEntry(bootEntry)) {
                val team = createTeam(bootEntry) ?: run {
                    Log.error("Team entry has no members", bootDir)
                    return emptyMap()
                }
                bootTeam(bootDir, team, getBootEntry)
            } else {
                bootSingleBot(bootDir, getBootEntry)
            }
        } catch (ex: Exception) {
            Log.error(ex, bootDir)
            emptyMap()
        }
    }

    private fun isTeamEntry(bootEntry: BootEntry): Boolean =
        bootEntry.teamMembers?.isNotEmpty() == true

    private fun createTeam(bootEntry: BootEntry): Team? {
        val members = bootEntry.teamMembers ?: return null
        return Team(teamId.getAndIncrement(), bootEntry.name, bootEntry.version, members)
    }

    private fun bootSingleBot(bootDir: Path, getBootEntry: (Path) -> BootEntry?): Map<Process, Path> {
        val process = bootBot(bootDir, null, getBootEntry) ?: run {
            Log.error("Failed to boot bot - no suitable boot method found", bootDir)
            return emptyMap()
        }
        return mapOf(process to bootDir)
    }

    /**
     * Boot a team of bots with the specified team information.
     * @return a map of started processes to their bot directories.
     */
    private fun bootTeam(bootDir: Path, team: Team, getBootEntry: (Path) -> BootEntry?): Map<Process, Path> {
        val parentPath = bootDir.parent
        val result = HashMap<Process, Path>()
        try {
            team.members.forEach { botName ->
                bootTeamMember(parentPath, botName, team, getBootEntry, result)
            }
        } catch (ex: Exception) {
            Log.error(ex, bootDir)
        }
        return result
    }

    private fun bootTeamMember(
        parentPath: Path,
        botName: String,
        team: Team,
        getBootEntry: (Path) -> BootEntry?,
        result: MutableMap<Process, Path>
    ) {
        val botDir = parentPath.resolve(botName)
        try {
            val process = bootBot(botDir, team, getBootEntry) ?: run {
                Log.error("Failed to boot team member bot", botDir)
                return
            }
            result[process] = botDir
        } catch (ex: Exception) {
            Log.error(ex, botDir)
        }
    }

    /**
     * Boot a single bot with optional team information.
     * @return the started process, or null if booting failed.
     */
    private fun bootBot(botDir: Path, team: Team?, getBootEntry: (Path) -> BootEntry?): Process? {
        return try {
            val botEntry = getBootEntry(botDir) ?: run {
                Log.error("Failed to get boot entry for bot", botDir)
                return null
            }
            val scriptPath = findBootScriptOrNull(botDir)
            if (scriptPath != null) {
                createAndStartProcess(scriptPath, botDir, botEntry, team)
            } else {
                templateBooter.boot(botDir, botEntry, team)
            }
        } catch (ex: Exception) {
            Log.error(ex, botDir)
            null
        }
    }

    private fun createAndStartProcess(
        scriptPath: Path,
        botDir: Path,
        botEntry: BootEntry,
        team: Team?
    ): Process? {
        return try {
            val processBuilder = launchProcess(scriptPath.toString())
            processBuilder.directory(scriptPath.parent.toFile())
            // Bug fix #188:
            // Discard stdout from bot processes. Bot APIs (Java, .NET, Python) write to the OS stdout pipe
            // AND capture to an in-memory buffer for delivery via WebSocket. If nothing reads the OS pipe,
            // the pipe buffer fills up (~4 KB) and writes block, freezing the bot thread.
            // The GUI already receives bot stdout via WebSocket (BotIntent.stdOut), so the raw pipe is redundant.
            processBuilder.redirectOutput(ProcessBuilder.Redirect.DISCARD)
            setupEnvironment(processBuilder.environment(), botEntry, team)
            processBuilder.start()
        } catch (ex: Exception) {
            Log.error(ex, botDir)
            null
        }
    }

    /**
     * Validates the bot directory and finds its boot script.
     * Logs an error if no script is found.
     */
    private fun findBootScriptOrNull(botDir: Path): Path? {
        if (!isValidBotDirectory(botDir)) return null
        return findScript(botDir)
    }

    /**
     * Validates if a path is a valid bot directory.
     * @return true if the directory exists and is a directory, false otherwise
     */
    private fun isValidBotDirectory(botDir: Path): Boolean {
        if (!botDir.exists()) {
            Log.error(Exception("Bot directory not found"), botDir)
            return false
        }
        if (!Files.isDirectory(botDir)) {
            Log.error("Path is not a valid bot directory", botDir)
            return false
        }
        return true
    }
}
