package net.robocode2.gui.model

data class BulletHitBulletEvent(
        override val turnNumber: Int,
        val bullet: BulletState,
        val hitBullet: BulletState
) : Event(MessageType.BULLET_HIT_BULLET_EVENT.type, turnNumber)
