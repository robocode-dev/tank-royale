package net.robocode2.gui.model.comm

import com.beust.klaxon.TypeFor

@TypeFor(field = "type", adapter = ContentAdapter::class)
open class Content(
        val type: String
)