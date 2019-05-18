package net.robocode2.gui.model

abstract class Event(
        override val type: String,
        open val turnNumber: Int
) : Message(type)