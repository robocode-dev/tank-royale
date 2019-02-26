package net.robocode2.gui.model

import com.beust.klaxon.TypeFor

@TypeFor(field = "type", adapter = MessageAdapter::class)
open class Message(
        val type: String
)