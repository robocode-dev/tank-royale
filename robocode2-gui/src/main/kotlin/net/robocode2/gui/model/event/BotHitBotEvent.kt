package net.robocode2.gui.model.event

import net.robocode2.gui.model.comm.Content
import net.robocode2.gui.model.comm.ContentType
import net.robocode2.gui.model.types.Point

class BotHitBotEvent(
        val botId: Int,
        val victimId: Int,
        val energy: Double,
        val position: Point,
        val rammed: Boolean
) : Content(type = ContentType.BOT_HIT_BOT_EVENT.type)
