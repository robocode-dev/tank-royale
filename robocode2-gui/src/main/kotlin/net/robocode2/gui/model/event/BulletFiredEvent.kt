package net.robocode2.gui.model.event

import net.robocode2.gui.model.comm.Content
import net.robocode2.gui.model.comm.ContentType

class BulletFiredEvent(
        val bullet: BulletState
) : Content(type = ContentType.BULLET_FIRED_EVENT.type)
