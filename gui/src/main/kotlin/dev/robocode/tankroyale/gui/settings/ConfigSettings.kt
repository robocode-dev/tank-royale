package dev.robocode.tankroyale.gui.settings

import java.util.*

object ConfigSettings : PropertiesStore("Robocode Misc Settings", "gui.properties") {

    const val DEFAULT_TPS = 30
    const val SOUNDS_DIR = "sounds/"

    private const val BOT_DIRECTORIES = "bot-directories"
    private const val GAME_TYPE = "game-type"
    private const val TPS = "tps"
    private const val UI_SCALE = "ui-scale"
    private const val LANGUAGE = "language"
    private const val CONSOLE_MAX_CHARACTERS = "console-max-characters"

    private const val DEFAULT_CONSOLE_MAX_CHARACTERS = 10000

    private const val ENABLE_SOUNDS = "enable-sounds"
    private const val ENABLE_GUNSHOT_SOUND = "enable-gunshot-sound"
    private const val ENABLE_BULLET_HIT_SOUND = "enable-bullet-hit-sound"
    private const val ENABLE_WALL_COLLISION_SOUND = "enable-wall-collision-sound"
    private const val ENABLE_BOT_COLLISION_SOUND = "enable-bot-collision-sound"
    private const val ENABLE_BULLET_COLLISION_SOUND = "enable-bullet-collision-sound"
    private const val ENABLE_DEATH_EXPLOSION_SOUND = "enable-death-explosion-sound"
    private const val SOUND_VOLUME = "sound-volume"

    private const val ENABLE_AUTO_RECORDING = "enable-auto-recording"

    private const val BOT_DIRS_SEPARATOR = ','

    var enableAutoRecording: Boolean
        get() = load(ENABLE_AUTO_RECORDING)?.lowercase() == "true"
        set(value) {
            save(ENABLE_AUTO_RECORDING, if (value) "true" else "false")
        }

    var botDirectories: List<BotDirectoryConfig>
        get() = getBotDirectoryConfigs()
        set(value) {
            setBotDirectoryConfigs(value)
        }

    var gameType: GameType
        get() {
            val displayName = load(GAME_TYPE, GameType.CLASSIC.displayName)
            return GameType.from(displayName)
        }
        set(value) {
            save(GAME_TYPE, value.displayName)
        }

    var tps: Int
        get() {
            load()

            val tpsStr = load(TPS)?.lowercase(Locale.getDefault())
            if (tpsStr in listOf("m", "ma", "max")) {
                return -1 // infinite tps
            }
            return try {
                tpsStr?.toInt() ?: DEFAULT_TPS
            } catch (_: NumberFormatException) {
                DEFAULT_TPS
            }
        }
        set(value) {
            save(TPS, value.toString())
        }

    var enableSounds: Boolean
        get() = load(ENABLE_SOUNDS)?.lowercase() != "false"
        set(value) {
            save(ENABLE_SOUNDS, if (value) "true" else "false")
        }

    var soundVolume: Int
        get() = try {
            load(SOUND_VOLUME, "50").toInt().coerceIn(0, 100)
        } catch (_: NumberFormatException) {
            50
        }
        set(value) {
            val clamped = value.coerceIn(0, 100)
            save(SOUND_VOLUME, clamped.toString())
        }

    var uiScale: Int
        get() = try {
            load(UI_SCALE, "100").toInt()
        } catch (_: NumberFormatException) {
            100
        }
        set(value) {
            val clamped = value.coerceIn(50, 400)
            save(UI_SCALE, clamped.toString())
        }

    var language: String
        get() {
            val lang = load(LANGUAGE, "en").lowercase(Locale.getDefault())
            return when (lang) {
                "es" -> "es"
                "da" -> "da"
                "ca" -> "ca"
                else -> "en"
            }
        }
        set(value) {
            val v = when (value.lowercase(Locale.getDefault())) {
                "es" -> "es"
                "da" -> "da"
                "ca" -> "ca"
                else -> "en"
            }
            save(LANGUAGE, v)
        }

    var consoleMaxCharacters: Int
        get() = try {
            load(CONSOLE_MAX_CHARACTERS, DEFAULT_CONSOLE_MAX_CHARACTERS.toString()).toInt()
        } catch (_: NumberFormatException) {
            DEFAULT_CONSOLE_MAX_CHARACTERS
        }
        set(value) {
            save(CONSOLE_MAX_CHARACTERS, value.toString())
        }

    var enableGunshotSound: Boolean
        get() = load(ENABLE_GUNSHOT_SOUND)?.lowercase() != "false"
        set(value) {
            save(ENABLE_GUNSHOT_SOUND, if (value) "true" else "false")
        }

    var enableBulletHitSound: Boolean
        get() = load(ENABLE_BULLET_HIT_SOUND)?.lowercase() != "false"
        set(value) {
            save(ENABLE_BULLET_HIT_SOUND, if (value) "true" else "false")
        }

    var enableWallCollisionSound: Boolean
        get() = load(ENABLE_WALL_COLLISION_SOUND)?.lowercase() != "false"
        set(value) {
            save(ENABLE_WALL_COLLISION_SOUND, if (value) "true" else "false")
        }

    var enableBotCollisionSound: Boolean
        get() = load(ENABLE_BOT_COLLISION_SOUND)?.lowercase() != "false"
        set(value) {
            save(ENABLE_BOT_COLLISION_SOUND, if (value) "true" else "false")
        }

    var enableBulletCollisionSound: Boolean
        get() = load(ENABLE_BULLET_COLLISION_SOUND)?.lowercase() != "false"
        set(value) {
            save(ENABLE_BULLET_COLLISION_SOUND, if (value) "true" else "false")
        }

    var enableDeathExplosionSound: Boolean
        get() = load(ENABLE_DEATH_EXPLOSION_SOUND)?.lowercase() != "false"
        set(value) {
            save(ENABLE_DEATH_EXPLOSION_SOUND, if (value) "true" else "false")
        }

    private fun getBotDirectoryConfigs(): List<BotDirectoryConfig> {
        val botDirectoryConfigs = mutableListOf<BotDirectoryConfig>()

        var lastPath: String? = null
        load(BOT_DIRECTORIES, "")
            .split(BOT_DIRS_SEPARATOR)
            .filter { it.isNotBlank() }
            .forEach { path ->
                if ("true".equals(path, ignoreCase = true)) {
                    botDirectoryConfigs.add(BotDirectoryConfig(lastPath!!, true))
                } else if ("false".equals(path, ignoreCase = true)) {
                    botDirectoryConfigs.add(BotDirectoryConfig(lastPath!!, false))
                } else {
                    lastPath = path
                }
            }
        return botDirectoryConfigs
    }

    private fun setBotDirectoryConfigs(botDirectoryConfigs: List<BotDirectoryConfig>) {
        val stringBuffer = StringBuilder()
        botDirectoryConfigs.filter { it.path.isNotBlank() }.forEach { botDirectoryConfig ->
            // Normalize path separators to forward slashes to avoid escape sequence issues in properties files
            val normalizedPath = botDirectoryConfig.path.replace('\\', '/')
            stringBuffer
                .append(normalizedPath).append(BOT_DIRS_SEPARATOR)
                .append(if (botDirectoryConfig.enabled) "true" else "false").append(BOT_DIRS_SEPARATOR)
        }
        save(BOT_DIRECTORIES, stringBuffer.toString().trimEnd(BOT_DIRS_SEPARATOR))
    }
}
