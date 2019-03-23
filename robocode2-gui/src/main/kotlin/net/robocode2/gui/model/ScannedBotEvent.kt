package net.robocode2.gui.model

import net.robocode2.gui.model.Content
import net.robocode2.gui.model.ContentType

class ScannedBotEvent(
        val scannedByBotId: Int,
        val scannedBotId: Int,
        val energy: Double,
        val x: Double,
        val y: Double,
        val direction: Double,
        val speed: Double
) : Content(type = ContentType.SCANNED_BOT_EVENT.type)
