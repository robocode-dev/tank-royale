package net.robocode2.gui.model.event

import net.robocode2.gui.model.types.Point

data class BotState(
        val id: Int,
        val energy: Double,
        val position: Point,
        val direction: Double,
        val gunDirection: Double,
        val radarDirection: Double,
        val radarSweep: Double,
        val speed: Double
)