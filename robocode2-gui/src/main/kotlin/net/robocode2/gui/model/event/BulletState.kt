package net.robocode2.gui.model.event

import net.robocode2.gui.model.types.Point

data class BulletState(
        val bulletId: Int,
        val ownerId: Int,
        val power: Double,
        val position: Point,
        val direction: Double,
        val speed: Double
)