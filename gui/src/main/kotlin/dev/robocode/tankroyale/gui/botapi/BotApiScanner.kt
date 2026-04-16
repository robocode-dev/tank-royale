package dev.robocode.tankroyale.gui.botapi

import dev.robocode.tankroyale.common.util.Version
import dev.robocode.tankroyale.gui.settings.ConfigSettings
import java.nio.file.Files
import java.nio.file.Path

object BotApiScanner {

    fun scan(): List<BotApiLibEntry> {
        val entries = mutableListOf<BotApiLibEntry>()
        val currentVersion = Version.version

        ConfigSettings.botDirectories.forEach { dirConfig ->
            val rootDir = Path.of(dirConfig.path)
            if (!Files.isDirectory(rootDir)) return@forEach

            BotApiPlatform.entries.forEach { platform ->
                val subDir = rootDir.resolve(platform.subDir)
                if (!Files.isDirectory(subDir)) return@forEach

                val installedVersion = findInstalledVersion(subDir, platform)
                when {
                    installedVersion != null && installedVersion != currentVersion ->
                        entries.add(BotApiLibEntry(rootDir, platform, installedVersion))
                    installedVersion == null && hasPlatformMarker(rootDir, platform) ->
                        entries.add(BotApiLibEntry(rootDir, platform, null))
                }
            }
        }
        return entries
    }

    private fun findInstalledVersion(subDir: Path, platform: BotApiPlatform): String? {
        val fileNames = mutableListOf<String>()
        Files.list(subDir).use { stream -> stream.forEach { fileNames.add(it.fileName.toString()) } }
        return fileNames.mapNotNull { platform.extractVersion(it) }.firstOrNull()
    }

    /** Returns true if the bot root dir contains platform-specific source files (repair use case). */
    private fun hasPlatformMarker(botRootDir: Path, platform: BotApiPlatform): Boolean =
        when (platform) {
            BotApiPlatform.JAVA -> hasExtensionInSubdirs(botRootDir, "java")
            BotApiPlatform.DOTNET -> hasExtensionInSubdirs(botRootDir, "csproj")
            BotApiPlatform.PYTHON -> hasExtensionInSubdirs(botRootDir, "py") ||
                    Files.exists(botRootDir.resolve("deps").resolve("requirements.txt"))
            BotApiPlatform.TYPESCRIPT -> Files.exists(botRootDir.resolve("package.json"))
        }

    private fun hasExtensionInSubdirs(botRootDir: Path, extension: String): Boolean {
        val subDirs = mutableListOf<Path>()
        Files.list(botRootDir).use { stream ->
            stream.filter { Files.isDirectory(it) }.forEach { subDirs.add(it) }
        }
        return subDirs.any { subDir ->
            val names = mutableListOf<String>()
            Files.list(subDir).use { stream -> stream.forEach { names.add(it.fileName.toString()) } }
            names.any { it.endsWith(".$extension") }
        }
    }
}

