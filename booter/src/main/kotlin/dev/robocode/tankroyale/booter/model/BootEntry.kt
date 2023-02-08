package dev.robocode.tankroyale.booter.model

import kotlinx.serialization.Serializable

@Serializable
data class BootEntry(
    /** Absolute file path used for identifying all booter files for the bot */
    val dir: String,
    /** Information about the bot used for displaying the bot info prior to starting it */
    val info: BotInfo
)