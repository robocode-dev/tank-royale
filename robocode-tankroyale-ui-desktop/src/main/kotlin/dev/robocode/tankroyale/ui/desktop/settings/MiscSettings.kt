package dev.robocode.tankroyale.ui.desktop.settings

object MiscSettings : PropertiesStore("Robocode Misc Config", "misc.properties") {

    private const val BOT_DIRECTORIES_PROPERTY = "bot.directories"
    private const val DEFAULT_BOT_DIRECTORIES = "bots"

    const val BOT_DIRS_SEPARATOR = ","

    init {
        load()
    }

    var botsDirectories: List<String>
        get() =
            properties.getProperty(BOT_DIRECTORIES_PROPERTY, DEFAULT_BOT_DIRECTORIES).split(BOT_DIRS_SEPARATOR)
        set(value) {
            properties.setProperty(BOT_DIRECTORIES_PROPERTY, value.joinToString(separator = BOT_DIRS_SEPARATOR))
        }
}