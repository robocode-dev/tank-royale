package dev.robocode.tankroyale.booter.process

import dev.robocode.tankroyale.common.RUMBLE_PYTHON_ENVIRONMENT
import dev.robocode.tankroyale.common.util.Platform
import java.nio.file.Files
import java.nio.file.Path

/**
 * Bots downloaded from the Rumble can share a private Python environment that the desktop program's Download bots
 * screen makes in their folder. A bot that lies in a folder holding that environment starts with it: its scripts folder
 * comes first on the bot's path and `RUMBLE_PYTHON` names its Python, so launchers that call `python` and launchers that
 * use `RUMBLE_PYTHON` both use it. Every other bot starts with its environment unchanged.
 */
internal object DownloadedBotsPython {

    fun apply(environment: MutableMap<String, String?>, botDir: Path, windows: Boolean = Platform.isWindows) {
        val folder = botDir.toAbsolutePath().parent ?: return
        val home = folder.resolve(RUMBLE_PYTHON_ENVIRONMENT)
        val scripts = home.resolve(if (windows) "Scripts" else "bin")
        val python = scripts.resolve(if (windows) "python.exe" else "python")
        if (!Files.isRegularFile(python)) return

        // The process environment is case-insensitive on Windows, but a plain map is not, so the existing name is kept.
        val key = environment.keys.firstOrNull { it.equals("PATH", ignoreCase = true) } ?: "PATH"
        val path = environment[key]
        environment[key] = if (path.isNullOrEmpty()) scripts.toString() else scripts.toString() + (if (windows) ";" else ":") + path
        environment["RUMBLE_PYTHON"] = python.toString()
        environment["VIRTUAL_ENV"] = home.toString()
    }

}
