package dev.robocode.tankroyale.gui.model

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
    val turnRate: Double,
    val gunTurnRate: Double,
    val radarTurnRate: Double,
    val gunHeat: Double,
    val bodyColor: String? = null,
    val turretColor: String? = null,
    val radarColor: String? = null,
    val bulletColor: String? = null,
    val scanColor: String? = null,
    val tracksColor: String? = null,
    val gunColor: String? = null
)