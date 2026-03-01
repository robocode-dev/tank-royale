package dev.robocode.tankroyale.intent

/**
 * A bot intent captured by the diagnostics proxy, annotated with metadata
 * about which bot sent it and during which turn.
 *
 * @property botName name of the bot (from `bot-handshake`)
 * @property botVersion version of the bot (from `bot-handshake`)
 * @property roundNumber the round in which this intent was sent (1-based)
 * @property turnNumber the turn in which this intent was sent (1-based)
 * @property intent the deserialized bot intent data
 */
data class CapturedIntent(
    val botName: String,
    val botVersion: String,
    val roundNumber: Int,
    val turnNumber: Int,
    val intent: BotIntent,
)
