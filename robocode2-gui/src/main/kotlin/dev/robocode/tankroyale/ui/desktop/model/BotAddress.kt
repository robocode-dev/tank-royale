package dev.robocode.tankroyale.ui.desktop.model

import kotlinx.serialization.Serializable

@Serializable
data class BotAddress(
    val host: String,
    val port: Int
)