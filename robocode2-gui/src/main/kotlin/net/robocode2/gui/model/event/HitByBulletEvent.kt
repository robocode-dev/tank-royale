package net.robocode2.gui.model.event

import net.robocode2.gui.model.comm.Content
import net.robocode2.gui.model.comm.ContentType

class HitByBulletEvent(
        val bullet: BulletState,
        val damage: Double,
        val energy: Double
) : Content(type = ContentType.HIT_BY_BULLET_EVENT.type)
