package dev.robocode.tankroyale.runner

/**
 * Identifies a bot by its name, version, and authors as declared in its `bot.json` configuration file.
 *
 * @property name the bot's display name
 * @property version the bot's version string
 * @property authors the bot's authors list (comma-separated or single)
 */
data class BotIdentity(val name: String, val version: String, val authors: String) {
    override fun toString(): String = "$name $version ($authors)"
}
