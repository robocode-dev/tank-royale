package net.robocode2.gui.model

data class BotHitBotEvent(
        override val turnNumber: Int,
        val victimId: Int,
        val botId: Int,
        val energy: Double,
        val x: Double,
        val y: Double,
        val rammed: Boolean
): Event(MessageType.BOT_HIT_BOT_EVENT.type, turnNumber)
