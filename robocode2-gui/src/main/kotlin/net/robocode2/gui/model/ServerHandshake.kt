package net.robocode2.gui.model

data class ServerHandshake(
        override val clientKey: String,
        val variant: String,
        val version: String,
        val games: Set<GameSetup>
) : ClientMessage(MessageType.SERVER_HANDSHAKE.type, clientKey)
