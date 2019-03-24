package net.robocode2.gui.model

class BotHitBotEvent(
        val botId: Int,
        val victimId: Int,
        val energy: Double,
        val x: Double,
        val y: Double,
        val rammed: Boolean
) : Content(type = ContentType.BOT_HIT_BOT_EVENT.type)
