package net.robocode2.gui.model.event

import net.robocode2.gui.model.comm.Content
import net.robocode2.gui.model.comm.ContentType

class BotDeathEvent(
        val victimId: Int
) : Content(type = ContentType.BOT_DEATH_EVENT.type)
