package net.robocode2.gui.model

data class BotDeathEvent(val victimId: Int)
    : Content(type = ContentType.BOT_DEATH_EVENT.type)
