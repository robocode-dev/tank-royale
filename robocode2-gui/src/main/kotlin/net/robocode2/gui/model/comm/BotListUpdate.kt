package net.robocode2.gui.model.comm

import net.robocode2.gui.model.Content
import net.robocode2.gui.model.ContentType

data class BotListUpdate(
        val bots: Set<BotInfo>
) : Content(type = ContentType.BOT_LIST_UPDATE.type)