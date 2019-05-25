package net.robocode2.gui.model

import kotlinx.serialization.Serializable

@Serializable
data class BotAddress(
        val host: String,
        val port: Int
)