package net.robocode2.gui.model

class BulletHitBulletEvent(
        val bullet: BulletState,
        val hitBullet: BulletState
) : Content(type = ContentType.BULLET_HIT_BULLET_EVENT.type)
