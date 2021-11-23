package dev.robocode.tankroyale.gui.settings

object MiscSettings : PropertiesStore("Robocode Misc Settings", "misc.properties") {

    private const val BOT_DIRECTORIES = "bot-directories"
    private const val TPS = "tps"

    const val BOT_DIRS_SEPARATOR = ","

    private const val DEFAULT_TPS = 30

    fun getBotDirectories(): List<String> {
        load()
        return properties.getProperty(BOT_DIRECTORIES, "")
            .split(BOT_DIRS_SEPARATOR)
            .filter { it.isNotBlank() }
    }

    fun setBotDirectories(botDirs: List<String>) {
        properties.setProperty(BOT_DIRECTORIES, botDirs
            .filter { it.isNotBlank() }
            .joinToString(separator = BOT_DIRS_SEPARATOR))
        save()
    }

    fun getTps(): Int {
        load()
        var tps = try {
            properties.getProperty(TPS, DEFAULT_TPS.toString()).toInt()
        } catch (e: NumberFormatException) {
            DEFAULT_TPS
        }
        if (tps < 0) tps = DEFAULT_TPS
        return tps
    }

    fun setTps(tps: Int) {
        properties.setProperty(TPS, tps.toString())
        save()
    }
}