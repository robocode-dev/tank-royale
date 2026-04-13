package dev.robocode.tankroyale.intent

import kotlinx.serialization.Serializable

/**
 * Typed representation of a bot's intent (control request) for a single turn.
 *
 * Fields that are `null` were not set by the bot in that particular intent message,
 * meaning the server retains the previous value for that field.
 *
 * This matches the `bot-intent` schema definition.
 */
@Serializable
data class BotIntent(
    val turnRate: Double? = null,
    val gunTurnRate: Double? = null,
    val radarTurnRate: Double? = null,
    val targetSpeed: Double? = null,
    val firepower: Double? = null,
    val adjustGunForBodyTurn: Boolean? = null,
    val adjustRadarForBodyTurn: Boolean? = null,
    val adjustRadarForGunTurn: Boolean? = null,
    val rescan: Boolean? = null,
    val fireAssist: Boolean? = null,
    val bodyColor: String? = null,
    val turretColor: String? = null,
    val radarColor: String? = null,
    val bulletColor: String? = null,
    val scanColor: String? = null,
    val tracksColor: String? = null,
    val gunColor: String? = null,
    val stdOut: String? = null,
    val stdErr: String? = null,
    val teamMessages: List<TeamMessage>? = null,
    val debugGraphics: String? = null,
)
