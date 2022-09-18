package dev.robocode.tankroyale.gui.settings

import java.util.*

object ConfigSettings : PropertiesStore("Robocode Misc Settings", "config.properties") {

    const val DEFAULT_TPS = 30

    private const val BOT_DIRECTORIES = "bot-directories"
    private const val TPS = "tps"

    private const val ENABLE_SOUNDS = "enable-sounds"
    private const val ENABLE_GUNSHOT_SOUND = "enable-gunshot-sound"
    private const val ENABLE_BULLET_HIT_SOUND = "enable-bullet-hit-sound"
    private const val ENABLE_WALL_COLLISION_SOUND = "enable-wall-collision-sound"
    private const val ENABLE_BOT_COLLISION_SOUND = "enable-bot-collision-sound"
    private const val ENABLE_BULLET_COLLISION_SOUND = "enable-bullet-collision-sound"
    private const val ENABLE_DEATH_EXPLOSION_SOUND = "enable-death-explosion-sound"

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

            val tpsStr = properties.getProperty(TPS)?.lowercase(Locale.getDefault())
            if (tpsStr in listOf("m", "ma", "max")) {
                return -1 // infinite tps
            }
            return try {
                tpsStr?.toInt() ?: DEFAULT_TPS
            } catch (e: NumberFormatException) {
                DEFAULT_TPS
            }
        }
        set(value) {
            properties.setProperty(TPS, value.toString())
            save()
        }

    var enableSounds: Boolean
        get() {
            load()
            return properties.getProperty(ENABLE_SOUNDS)?.lowercase() != "false"
        }
        set(value) {
            properties.setProperty(ENABLE_SOUNDS, if (value) "true" else "false")
            save()
        }

    var enableGunshotSound: Boolean
        get() {
            load()
            return properties.getProperty(ENABLE_GUNSHOT_SOUND)?.lowercase() != "false"
        }
        set(value) {
            properties.setProperty(ENABLE_GUNSHOT_SOUND, if (value) "true" else "false")
            save()
        }

    var enableBulletHitSound: Boolean
        get() {
            load()
            return properties.getProperty(ENABLE_BULLET_HIT_SOUND)?.lowercase() != "false"
        }
        set(value) {
            properties.setProperty(ENABLE_BULLET_HIT_SOUND, if (value) "true" else "false")
            save()
        }

    var enableWallCollisionSound: Boolean
        get() {
            load()
            return properties.getProperty(ENABLE_WALL_COLLISION_SOUND)?.lowercase() != "false"
        }
        set(value) {
            properties.setProperty(ENABLE_WALL_COLLISION_SOUND, if (value) "true" else "false")
            save()
        }

    var enableBotCollisionSound: Boolean
        get() {
            load()
            return properties.getProperty(ENABLE_BOT_COLLISION_SOUND)?.lowercase() != "false"
        }
        set(value) {
            properties.setProperty(ENABLE_BOT_COLLISION_SOUND, if (value) "true" else "false")
            save()
        }

    var enableBulletCollisionSound: Boolean
        get() {
            load()
            return properties.getProperty(ENABLE_BULLET_COLLISION_SOUND)?.lowercase() != "false"
        }
        set(value) {
            properties.setProperty(ENABLE_BULLET_COLLISION_SOUND, if (value) "true" else "false")
            save()
        }

    var enableDeathExplosionSound: Boolean
        get() {
            load()
            return properties.getProperty(ENABLE_DEATH_EXPLOSION_SOUND)?.lowercase() != "false"
        }
        set(value) {
            properties.setProperty(ENABLE_DEATH_EXPLOSION_SOUND, if (value) "true" else "false")
            save()
        }
}