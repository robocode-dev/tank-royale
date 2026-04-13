package dev.robocode.tankroyale.intent

import kotlinx.serialization.Serializable

/**
 * A team message sent between teammates, as captured from the `bot-intent` message.
 */
@Serializable
data class TeamMessage(
    val receiverId: Int? = null,
    val message: String? = null,
    val messageType: String? = null,
)
