package dev.robocode.tankroyale.booter.model

import kotlinx.serialization.Serializable

@Serializable
data class BotEntry(
        /** Filename used for identifying all booter files for the bot (each file with different file extension) */
        val filename: String,
        /** Information about the bot used for displaying the bot info prior to starting it */
        val info: BotInfo
)