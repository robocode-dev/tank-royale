package net.robocode2.gui.model

data class ControllerHandshake(
        override val clientKey: String,
        val name: String,
        val version: String,
        val author: String?
) : ClientMessage(type = MessageType.CONTROLLER_HANDSHAKE.type, clientKey = clientKey)
