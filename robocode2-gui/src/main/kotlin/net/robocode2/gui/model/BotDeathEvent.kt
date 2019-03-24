package net.robocode2.gui.model

class BotDeathEvent(
        val victimId: Int
) : Content(type = ContentType.BOT_DEATH_EVENT.type)
