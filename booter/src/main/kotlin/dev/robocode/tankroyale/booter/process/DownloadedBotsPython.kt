package dev.robocode.tankroyale.booter.process

import dev.robocode.tankroyale.common.RumblePythonEnvironment
import dev.robocode.tankroyale.common.util.Platform
import java.nio.file.Path

/**
 * Bots downloaded from the Rumble can share a private Python environment that the desktop program's Download bots
 * screen makes in their folder. A bot that lies in a folder holding a finished environment starts with it: its scripts
 * folder comes first on the bot's path and `RUMBLE_PYTHON` names its Python, so launchers that call `python` and
 * launchers that use `RUMBLE_PYTHON` both use it. Every other bot starts with its environment unchanged.
 */
internal object DownloadedBotsPython {

    /** [windows] is a parameter, and so is the path separator it decides, so both kinds of computer can be tested anywhere. */
    fun apply(environment: MutableMap<String, String?>, botDir: Path, windows: Boolean = Platform.isWindows) {
        val folder = botDir.toAbsolutePath().parent ?: return
        if (!RumblePythonEnvironment.isReady(folder, windows)) return
        val scripts = RumblePythonEnvironment.scripts(folder, windows)

        // The process environment is case-insensitive on Windows, but a plain map is not, so the existing name is kept.
        val key = environment.keys.firstOrNull { it.equals("PATH", ignoreCase = true) } ?: "PATH"
        val path = environment[key]
        environment[key] = if (path.isNullOrEmpty()) scripts.toString() else scripts.toString() + (if (windows) ";" else ":") + path
        environment["RUMBLE_PYTHON"] = RumblePythonEnvironment.python(folder, windows).toString()
        environment["VIRTUAL_ENV"] = RumblePythonEnvironment.home(folder).toString()
    }
}
