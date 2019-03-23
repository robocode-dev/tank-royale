package net.robocode2.gui.model

import net.robocode2.gui.model.Content
import net.robocode2.gui.model.ContentType

class BotHitWallEvent(
        val victimId: Int
) : Content(type = ContentType.BOT_HIT_WALL_EVENT.type)
