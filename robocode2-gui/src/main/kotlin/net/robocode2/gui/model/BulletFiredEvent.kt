package net.robocode2.gui.model

data class BulletFiredEvent(val bullet: BulletState)
    : Content(type = ContentType.BULLET_FIRED_EVENT.type)
