package dev.robocode.tankroyale.common.util

import dev.robocode.tankroyale.common.APPLICATION_NAME
import java.io.File

/**
 * Utility for obtaining platform-specific user data directories.
 *
 * This follows platform conventions for storing application data:
 * - Windows: %LOCALAPPDATA%\Robocode Tank Royale or %APPDATA%\Robocode Tank Royale
 * - macOS: ~/Library/Application Support/Robocode Tank Royale
 * - Linux: $XDG_CONFIG_HOME/robocode-tank-royale or ~/.config/robocode-tank-royale
 *
 * These directories are writable by standard users, unlike system installation directories
 * (e.g., Program Files on Windows, /Applications on macOS, /usr on Linux).
 */
object UserDataDirectory {

    /**
     * Get the platform-specific user data directory for the application.
     *
     * The directory is automatically created if it doesn't exist.
     *
     * @param appName The application name to use as the subdirectory. Defaults to "Robocode Tank Royale".
     * @return A File representing the user data directory
     *
     * Examples:
     * - Windows: C:\Users\{user}\AppData\Local\Robocode Tank Royale
     * - macOS: /Users/{user}/Library/Application Support/Robocode Tank Royale
     * - Linux: /home/{user}/.config/robocode-tank-royale
     */
    fun get(appName: String = APPLICATION_NAME): File {
        val dir = when (Platform.operatingSystemType) {
            Platform.PlatformType.Windows -> getWindowsUserDataDir(appName)
            Platform.PlatformType.Mac -> getMacUserDataDir(appName)
            else -> getLinuxUserDataDir(appName)
        }
        // Ensure directory exists
        dir.mkdirs()
        return dir
    }

    /**
     * Get a subdirectory within the user data directory.
     *
     * The directory and all parent directories are automatically created if they don't exist.
     *
     * @param subdir The subdirectory name (e.g., "recordings")
     * @param appName The application name to use as the base directory. Defaults to "Robocode Tank Royale".
     * @return A File representing the subdirectory within the user data directory
     *
     * Examples:
     * - Windows: C:\Users\{user}\AppData\Local\Robocode Tank Royale\recordings
     * - macOS: /Users/{user}/Library/Application Support/Robocode Tank Royale/recordings
     * - Linux: /home/{user}/.config/robocode-tank-royale/recordings
     */
    fun getSubdir(subdir: String, appName: String = APPLICATION_NAME): File {
        val userDir = get(appName)
        val subdirFile = File(userDir, subdir)
        subdirFile.mkdirs()
        return subdirFile
    }

    private fun getWindowsUserDataDir(appName: String): File {
        val localAppData = System.getenv("LOCALAPPDATA")
            ?: System.getenv("APPDATA")
            ?: File(System.getProperty("user.home"), "AppData${File.separator}Local").absolutePath
        return File(localAppData, appName)
    }

    private fun getMacUserDataDir(appName: String): File {
        return File(
            System.getProperty("user.home"),
            "Library${File.separator}Application Support${File.separator}$appName"
        )
    }

    private fun getLinuxUserDataDir(appName: String): File {
        // Linux convention: use lowercase with hyphens for directory names
        val normalizedAppName = appName.lowercase().replace(" ", "-")
        val xdgConfigHome = System.getenv("XDG_CONFIG_HOME")
        val baseDir = if (xdgConfigHome.isNullOrBlank()) {
            File(System.getProperty("user.home"), ".config")
        } else {
            File(xdgConfigHome)
        }
        return File(baseDir, normalizedAppName)
    }
}
