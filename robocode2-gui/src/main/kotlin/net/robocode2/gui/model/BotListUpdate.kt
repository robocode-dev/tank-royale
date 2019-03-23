package net.robocode2.gui.model

class BotListUpdate(
        val bots: Set<BotInfo>
) : Content(type = ContentType.BOT_LIST_UPDATE.type)