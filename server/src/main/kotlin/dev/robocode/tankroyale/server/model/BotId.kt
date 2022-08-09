package dev.robocode.tankroyale.server.model

/**
 * BotId contains the id of a bot. It is a inline class used to make it easy to differ between an Int and a BotId.
 * @param value ID value of the bot.
 */
@JvmInline
value class BotId(val value: Int) {
    override fun toString() = "$value"
}
