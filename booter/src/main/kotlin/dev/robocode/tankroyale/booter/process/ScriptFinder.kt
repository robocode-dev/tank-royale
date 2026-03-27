package dev.robocode.tankroyale.booter.process

import dev.robocode.tankroyale.common.util.Platform
import dev.robocode.tankroyale.common.util.Platform.PlatformType.Mac
import dev.robocode.tankroyale.common.util.Platform.PlatformType.Windows
import java.nio.file.Files
import java.nio.file.Files.list
import java.nio.file.Path
import java.util.stream.Collectors.toList
import kotlin.io.path.exists

/**
 * Discovers executable bot script files for a given bot directory.
 * Handles OS-specific naming conventions (Windows .bat/.cmd, macOS .command, Unix .sh/.py/shebang).
 */
internal object ScriptFinder {

    /**
     * Finds the appropriate boot script for the bot directory based on the current OS.
     * Returns null if no suitable script is found.
     */
    fun findScript(botDir: Path): Path? = when (Platform.operatingSystemType) {
        Windows -> findWindowsScript(botDir)
        Mac -> findMacOsScript(botDir)
        else -> findFirstUnixScript(botDir)
    }

    private fun findWindowsScript(botDir: Path): Path? {
        val botName = botDir.fileName.toString()
        for (extension in listOf("bat", "cmd")) {
            val path = botDir.resolve("$botName.$extension")
            if (path.exists()) return path
        }
        return findFirstUnixScript(botDir)
    }

    private fun findMacOsScript(botDir: Path): Path? {
        val botName = botDir.fileName.toString()
        val path = botDir.resolve("$botName.command")
        return if (path.exists()) path else findFirstUnixScript(botDir)
    }

    private fun findFirstUnixScript(botDir: Path): Path? {
        val botName = botDir.fileName.toString()

        val shScript = botDir.resolve("$botName.sh")
        if (shScript.exists()) return shScript

        val pyScript = botDir.resolve("$botName.py")
        if (pyScript.exists()) return pyScript

        return findScriptWithShebangOrMatchingName(botDir, botName)
    }

    private fun findScriptWithShebangOrMatchingName(botDir: Path, botName: String): Path? {
        val lowerBotName = botName.lowercase()
        list(botDir).use { stream ->
            return stream.filter { path ->
                val filename = path.fileName.toString().lowercase()
                filename == lowerBotName || filename.startsWith("$lowerBotName.")
            }
                .collect(toList())
                .firstOrNull { filePath ->
                    isExactNameMatch(filePath, botName) || hasShebang(botDir, filePath)
                }
        }
    }

    private fun isExactNameMatch(filePath: Path, botName: String): Boolean =
        filePath.fileName.toString().equals(botName, ignoreCase = true)

    private fun hasShebang(botDir: Path, filePath: Path): Boolean =
        readFirstLine(botDir.resolve(filePath)).trim().startsWith("#!")

    private fun readFirstLine(path: Path): String =
        Files.newInputStream(path).bufferedReader().use { it.readLine() ?: "" }
}
