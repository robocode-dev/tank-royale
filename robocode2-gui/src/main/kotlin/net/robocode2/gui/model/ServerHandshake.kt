package net.robocode2.gui.model

data class ServerHandshake(
        override val clientKey: String,
        val protocolVersion: String,
        val games: Set<GameSetup>
) : ClientContent(type = ContentType.SERVER_HANDSHAKE.type, clientKey = clientKey)
