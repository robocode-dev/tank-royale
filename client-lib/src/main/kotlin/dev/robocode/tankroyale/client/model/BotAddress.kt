package dev.robocode.tankroyale.client.model

import kotlinx.serialization.Serializable

@Serializable
data class BotAddress(
    val host: String,
    val port: Int
)