package net.robocode2.gui.model

import com.beust.klaxon.TypeFor

@TypeFor(field = "type", adapter = MessageAdapter::class)
open class Message(open val type: String)