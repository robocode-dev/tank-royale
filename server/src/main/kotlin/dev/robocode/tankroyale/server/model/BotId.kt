package dev.robocode.tankroyale.server.model

/**
 * BotId contains the id of a bot.
 * @param id the id of the bot.
 */
@JvmInline
value class BotId(val id: Int) {
    override fun toString() = "$id"
}
