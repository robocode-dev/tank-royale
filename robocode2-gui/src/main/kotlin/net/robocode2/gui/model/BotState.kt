package net.robocode2.gui.model

import kotlinx.serialization.Serializable

@Serializable
data class BotState(
    val id: Int,
    val energy: Double,
    val x: Double,
    val y: Double,
    val direction: Double,
    val gunDirection: Double,
    val radarDirection: Double,
    val radarSweep: Double,
    val speed: Double,
    val gunHeat: Double
)