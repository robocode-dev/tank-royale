package net.robocode2.gui.model

data class BotListUpdate(val bots: Set<BotInfo>)
    : Content(type = ContentType.BOT_LIST_UPDATE.type)