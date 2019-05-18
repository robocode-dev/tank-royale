package net.robocode2.gui.model

data class BulletHitBotEvent(
        override val turnNumber: Int,
        val victimId: Int,
        val bullet: BulletState,
        val damage: Double,
        val energy: Double
) : Event(MessageType.BOT_HIT_BOT_EVENT.type, turnNumber)
