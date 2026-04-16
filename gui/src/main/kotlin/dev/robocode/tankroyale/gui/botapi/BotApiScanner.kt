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
                if (installedVersion == null || installedVersion != currentVersion) {
                    entries.add(BotApiLibEntry(rootDir, platform, installedVersion))
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
}
