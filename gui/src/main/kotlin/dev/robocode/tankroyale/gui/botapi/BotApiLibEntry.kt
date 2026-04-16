package dev.robocode.tankroyale.gui.botapi

import java.nio.file.Path

data class BotApiLibEntry(
    val botRootDir: Path,
    val platform: BotApiPlatform,
    val installedVersion: String?,
)
