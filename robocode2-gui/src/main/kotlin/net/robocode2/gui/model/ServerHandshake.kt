package net.robocode2.gui.model

data class ServerHandshake(
        val clientKey: String,
        val protocolVersion: String,
        val games: Set<GameSetup>
) : Message(type = MessageType.SERVER_HANDSHAKE.type)
