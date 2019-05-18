package net.robocode2.gui.model

import com.beust.klaxon.TypeFor

@TypeFor(field = "type", adapter = MessageAdapter::class)
open class ClientMessage(
        override val type: String,
        open val clientKey: String
) : Message(type)