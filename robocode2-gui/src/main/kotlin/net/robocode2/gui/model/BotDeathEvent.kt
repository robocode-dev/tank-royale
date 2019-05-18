package net.robocode2.gui.model

open class BotDeathEvent(
        override val turnNumber: Int,
        val victimId: Int
) : Event(MessageType.BOT_DEATH_EVENT.type, turnNumber)
