package net.robocode2.gui.bootstrap.model

import kotlinx.serialization.Serializable
import net.robocode2.gui.model.BotInfo

@Serializable
data class BotEntry (
    val filename: String,
    val info: BotInfo
)
