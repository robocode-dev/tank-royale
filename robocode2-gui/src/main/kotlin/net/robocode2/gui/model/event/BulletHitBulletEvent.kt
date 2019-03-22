package net.robocode2.gui.model.event

import net.robocode2.gui.model.comm.Content
import net.robocode2.gui.model.comm.ContentType

class BulletHitBulletEvent(
        val bullet: BulletState,
        val hitBullet: BulletState
) : Content(type = ContentType.BULLET_HIT_BULLET_EVENT.type)
