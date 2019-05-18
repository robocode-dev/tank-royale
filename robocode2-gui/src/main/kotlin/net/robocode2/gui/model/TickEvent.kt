package net.robocode2.gui.model

open class TickEvent(
        override val turnNumber: Int,
        val roundNumber: Int,
        val botStates: Set<BotState>,
        val bulletStates: Set<BulletState>,
        val events: Set<Message>
) : Event(MessageType.TICK_EVENT.type, turnNumber)
