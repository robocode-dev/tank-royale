package net.robocode2.gui.model.comm

import net.robocode2.gui.model.ClientContent
import net.robocode2.gui.model.ContentType
import net.robocode2.gui.model.GameSetup

class ServerHandshake(
        clientKey: String,
        val protocolVersion: String,
        val games: Set<GameSetup>
) : ClientContent(type = ContentType.SERVER_HANDSHAKE.type, clientKey = clientKey)
