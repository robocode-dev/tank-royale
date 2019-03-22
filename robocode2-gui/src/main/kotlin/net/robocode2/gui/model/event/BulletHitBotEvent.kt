package net.robocode2.gui.model.event

import net.robocode2.gui.model.comm.Content
import net.robocode2.gui.model.comm.ContentType

class BulletHitBotEvent(
        val bullet: BulletState,
        val victimId: Int,
        val damage: Double,
        val energy: Double
) : Content(type = ContentType.BOT_HIT_BOT_EVENT.type)
