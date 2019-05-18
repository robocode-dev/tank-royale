package net.robocode2.gui.model

data class BotHitWallEvent(
        override val turnNumber: Int,
        val victimId: Int
) : Event(MessageType.BOT_HIT_WALL_EVENT.type, turnNumber)
