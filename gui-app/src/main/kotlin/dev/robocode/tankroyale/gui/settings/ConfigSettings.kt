package dev.robocode.tankroyale.gui.settings

import java.util.*

object ConfigSettings : PropertiesStore("Robocode Misc Settings", "config.properties") {

    const val DEFAULT_TPS = 30

    private const val BOT_DIRECTORIES = "bot-directories"
    private const val TPS = "tps"

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

            val tpsStr = properties.getProperty(TPS).lowercase(Locale.getDefault())
            if (tpsStr in listOf("m", "ma", "max")) {
                return -1 // infinite tps
            }
            return try {
                tpsStr.toInt()
            } catch (e: NumberFormatException) {
                DEFAULT_TPS
            }
        }
        set(value) {
            properties.setProperty(TPS, value.toString())
            save()
        }
}