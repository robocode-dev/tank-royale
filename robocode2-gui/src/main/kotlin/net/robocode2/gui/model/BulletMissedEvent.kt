package net.robocode2.gui.model

class BulletMissedEvent(
        val bullet: BulletState
) : Content(type = ContentType.BULLET_MISSED_EVENT.type)
