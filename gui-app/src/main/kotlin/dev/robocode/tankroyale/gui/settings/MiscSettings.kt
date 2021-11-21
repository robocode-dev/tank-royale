package dev.robocode.tankroyale.gui.settings

object MiscSettings : PropertiesStore("Robocode Misc Config", "misc.properties") {

    private const val BOT_DIRECTORIES_PROPERTY = "bot.directories"

    const val BOT_DIRS_SEPARATOR = ","

    fun getBotDirectories(): List<String> {
        load()
        return properties.getProperty(BOT_DIRECTORIES_PROPERTY, "").split(BOT_DIRS_SEPARATOR)
    }

    fun setBotDirectories(botDirs: List<String>) {
        properties.setProperty(BOT_DIRECTORIES_PROPERTY, botDirs.joinToString(separator = BOT_DIRS_SEPARATOR))
        save()
    }
}