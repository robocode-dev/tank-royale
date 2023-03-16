package dev.robocode.tankroyale.server.event

import dev.robocode.tankroyale.server.model.BotId

class TeamMessageEvent(
    /** Turn number when event occurred */
    override val turnNumber: Int,

    /** Message that was received */
    var message: String,

    /** The type of message that was received */
    var messageType: String,

    /** ID of the teammate that sent the message */
    val senderId: BotId,
) : Event()