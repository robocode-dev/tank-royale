package net.robocode2.gui.model

data class BulletFiredEvent(
        override val turnNumber: Int,
        val bullet: BulletState
) : Event(MessageType.BULLET_FIRED_EVENT.type, turnNumber)
