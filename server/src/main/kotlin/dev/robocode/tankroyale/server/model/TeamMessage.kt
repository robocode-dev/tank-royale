package dev.robocode.tankroyale.server.model

data class TeamMessage(
    /** Message that was received */
    val message: String,

    /** The type of message that was received */
    var messageType: String,

    /** Id of the teammate that must receive the message. If null, the message must be broadcast to all teammates */
    val receiverId: BotId?,
)