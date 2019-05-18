package net.robocode2.gui.model

data class HitByBulletEvent(
        override val turnNumber: Int,
        val bullet: BulletState,
        val damage: Double,
        val energy: Double
) : Event(MessageType.HIT_BY_BULLET_EVENT.type, turnNumber)
