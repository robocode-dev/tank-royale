package net.robocode2.gui.model.comm

import net.robocode2.gui.model.ClientContent
import net.robocode2.gui.model.ContentType

class ControllerHandshake(
        clientKey: String,
        val name: String,
        val version: String,
        val author: String?
) : ClientContent(type = ContentType.CONTROLLER_HANDSHAKE.type, clientKey = clientKey)
