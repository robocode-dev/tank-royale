package net.robocode2.gui.model

data class ScannedBotEvent(
        override val turnNumber: Int,
        val scannedByBotId: Int,
        val scannedBotId: Int,
        val energy: Double,
        val x: Double,
        val y: Double,
        val direction: Double,
        val speed: Double
) : Event(MessageType.SCANNED_BOT_EVENT.type, turnNumber)
