package dev.robocode.tankroyale.gui.settings

object ConfigSettings : PropertiesStore("Robocode Misc Settings", "config.properties") {

    private const val BOT_DIRECTORIES = "bot-directories"
    private const val TPS = "tps"
    private const val DEFAULT_TPS = 30

    private const val BOT_DIRS_SEPARATOR = ","

    var botDirectories: List<String>
        get() {
            load()
            return properties.getProperty(BOT_DIRECTORIES, "")
                .split(BOT_DIRS_SEPARATOR)
                .filter { it.isNotBlank() }
        }
        set(value) {
            properties.setProperty(BOT_DIRECTORIES, value
                .filter { it.isNotBlank() }
                .joinToString(separator = BOT_DIRS_SEPARATOR))
            save()
        }

    var tps: Int
        get() {
            load()
            var tps = try {
                properties.getProperty(TPS, DEFAULT_TPS.toString()).toInt()
            } catch (e: NumberFormatException) {
                DEFAULT_TPS
            }
            if (tps < 0) tps = DEFAULT_TPS
            return tps
        }
        set(value) {
            properties.setProperty(TPS, value.toString())
            save()
        }
}