package dev.robocode.tankroyale.client.model

import kotlinx.serialization.Serializable

@Serializable
data class InitialPosition(val x: Double?, val y: Double?, val direction: Double?)
