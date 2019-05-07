package net.robocode2.gui.model

data class BulletHitWallEvent(val bullet: BulletState)
    : Content(type = ContentType.BULLET_HIT_WALL_EVENT.type)
