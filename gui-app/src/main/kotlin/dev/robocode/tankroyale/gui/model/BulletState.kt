package dev.robocode.tankroyale.gui.model

import kotlinx.serialization.Serializable

@Serializable
data class BulletState(
    val bulletId: Int,
    val ownerId: Int,
    val power: Double,
    val x: Double,
    val y: Double,
    val direction: Double,
    val color: String? = null
)