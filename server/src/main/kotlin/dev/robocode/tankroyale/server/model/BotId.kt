package dev.robocode.tankroyale.server.model

/**
 * BotId contains the id of a bot.
 * @param value the id of the bot.
 */
@JvmInline
value class BotId(val value: Int) {
    override fun toString() = "$value"
}
