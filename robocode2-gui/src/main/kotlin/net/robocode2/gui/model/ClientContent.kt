package net.robocode2.gui.model

import com.beust.klaxon.TypeFor

@TypeFor(field = "type", adapter = ContentAdapter::class)
open class ClientContent(
        type: String,
        val clientKey: String
) : Content(type)