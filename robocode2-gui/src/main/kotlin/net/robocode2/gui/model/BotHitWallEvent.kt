package net.robocode2.gui.model

data class BotHitWallEvent(
        val victimId: Int
) : Content(type = ContentType.BOT_HIT_WALL_EVENT.type)
