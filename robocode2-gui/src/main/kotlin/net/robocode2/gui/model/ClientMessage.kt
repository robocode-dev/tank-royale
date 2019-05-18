package net.robocode2.gui.model

open class ClientMessage(
        override val type: String,
        open val clientKey: String
) : Message(type)