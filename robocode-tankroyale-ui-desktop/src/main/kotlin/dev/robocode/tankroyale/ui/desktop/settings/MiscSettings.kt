package dev.robocode.tankroyale.ui.desktop.settings

import java.nio.file.Path

object MiscSettings : PropertiesStore("Robocode Misc Config", "misc.properties") {

    private const val BOT_DIRECTORIES_PROPERTY = "bot.directories"
    private const val DEFAULT_BOT_DIRECTORIES = "bots"

    var botsDirectories: List<String>
        get() =
            properties.getProperty(BOT_DIRECTORIES_PROPERTY, DEFAULT_BOT_DIRECTORIES).split(";")
        set(value) {
            properties.setProperty(BOT_DIRECTORIES_PROPERTY, value.joinToString(separator = ";"))
        }
}