package net.robocode2.gui.model.event

import net.robocode2.gui.model.comm.Content
import net.robocode2.gui.model.comm.ContentType
import net.robocode2.gui.model.types.Point

class ScannedBotEvent(
        val scannedByBotId: Int,
        val scannedBotId: Int,
        val energy: Double,
        val position: Point,
        val direction: Double,
        val speed: Double
) : Content(type = ContentType.SCANNED_BOT_EVENT.type)
