package dev.robocode.tankroyale.runner

/**
 * Identifies a bot by its name and version as declared in its `bot.json` configuration file.
 *
 * @property name the bot's display name
 * @property version the bot's version string
 */
data class BotIdentity(val name: String, val version: String) {
    override fun toString(): String = "$name $version"
}
