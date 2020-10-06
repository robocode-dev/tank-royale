package dev.robocode.tankroyale.ui.desktop.model

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
    val bodyColor: Int? = null,
    val turretColor: Int? = null,
    val radarColor: Int? = null,
    val bulletColor: Int? = null,
    val scanColor: Int? = null,
    val tracksColor: Int? = null,
    val gunColor: Int? = null
)