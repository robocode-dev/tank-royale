package net.robocode2.gui.model

open class TickEvent(
        val roundNumber: Int,
        val turnNumber: Int,
        val botStates: Set<BotState>,
        val bulletStates: Set<BulletState>,
        val events: Set<Content>
) : Content(type = ContentType.TICK_EVENT.type)
