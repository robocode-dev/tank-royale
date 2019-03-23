package net.robocode2.gui.model

data class BulletState(
        val bulletId: Int,
        val ownerId: Int,
        val power: Double,
        val x: Double,
        val y: Double,
        val direction: Double,
        val speed: Double
)