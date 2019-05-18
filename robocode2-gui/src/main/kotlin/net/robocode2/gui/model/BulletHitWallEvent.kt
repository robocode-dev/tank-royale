package net.robocode2.gui.model

data class BulletHitWallEvent(
        override val turnNumber: Int,
        val bullet: BulletState
) : Event(MessageType.BULLET_HIT_WALL_EVENT.type, turnNumber)
