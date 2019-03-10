package net.robocode2.gui.model

data class ServerHandshake(
        val clientKey: String,
        val protocolVersion: String,
        val games: Set<GameSetup>
) : Content(type = ContentType.SERVER_HANDSHAKE.type)
