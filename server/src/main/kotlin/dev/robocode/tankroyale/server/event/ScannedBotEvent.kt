package dev.robocode.tankroyale.server.event

import dev.robocode.tankroyale.server.model.BotId

/** Event sent when a bot got scanned. */
class ScannedBotEvent(
    /** Turn number when event occurred */
    override val turnNumber: Int,

    /** Bot id of the bot that scanned the bot */
    val scannedByBotId: BotId,

    /** Bot id of the bot that got scanned */
    val scannedBotId: BotId,

    /** Energy level of the scanned bot */
    val energy: Double,

    /** X coordinate of the scanned bot */
    val x: Double,

    /** Y coordinate of the scanned bot */
    val y: Double,

    /** Driving direction of the scanned bot */
    val direction: Double,

    /** Speed of the scanned bot */
    val speed: Double,
) : Event()