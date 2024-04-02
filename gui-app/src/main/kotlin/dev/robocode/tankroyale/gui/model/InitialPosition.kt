package dev.robocode.tankroyale.gui.model

import kotlinx.serialization.Serializable

@Serializable
data class InitialPosition(val x: Double?, val y: Double?, val direction: Double?)
