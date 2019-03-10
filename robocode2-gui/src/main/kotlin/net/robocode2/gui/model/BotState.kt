package net.robocode2.gui.model

data class BotState(
        val id: Int,
        val entergy: Double,
        val position: Point,
        val direction: Double,
        val gunDirection: Double,
        val radarDirection: Double,
        val radarSweep: Double,
        val speed: Double
)