package net.robocode2.gui.model

import net.robocode2.gui.model.Content
import net.robocode2.gui.model.ContentType

class BotDeathEvent(
        val victimId: Int
) : Content(type = ContentType.BOT_DEATH_EVENT.type)
