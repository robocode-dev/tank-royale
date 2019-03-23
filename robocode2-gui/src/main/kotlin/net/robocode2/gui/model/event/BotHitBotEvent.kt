package net.robocode2.gui.model.event

import net.robocode2.gui.model.comm.Content
import net.robocode2.gui.model.comm.ContentType

class BotHitBotEvent(
        val botId: Int,
        val victimId: Int,
        val energy: Double,
        val x: Double,
        val y: Double,
        val rammed: Boolean
) : Content(type = ContentType.BOT_HIT_BOT_EVENT.type)
