package net.robocode2.gui.model

class ControllerHandshake(
        clientKey: String,
        val name: String,
        val version: String,
        val author: String?
) : ClientContent(type = ContentType.CONTROLLER_HANDSHAKE.type, clientKey = clientKey)
