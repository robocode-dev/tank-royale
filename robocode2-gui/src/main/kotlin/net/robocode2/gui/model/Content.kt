package net.robocode2.gui.model

import com.beust.klaxon.TypeFor

@TypeFor(field = "type", adapter = ContentAdapter::class)
open class Content(open val type: String)