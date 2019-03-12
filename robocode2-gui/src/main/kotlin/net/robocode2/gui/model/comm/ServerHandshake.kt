package net.robocode2.gui.model.comm

import net.robocode2.gui.model.Content
import net.robocode2.gui.model.ContentType
import net.robocode2.gui.model.GameSetup

data class ServerHandshake(
        val clientKey: String,
        val protocolVersion: String,
        val games: Set<GameSetup>
) : Content(type = ContentType.SERVER_HANDSHAKE.type)
