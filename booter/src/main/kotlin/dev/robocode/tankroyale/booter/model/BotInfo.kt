package dev.robocode.tankroyale.booter.model

import kotlinx.serialization.Serializable

@Serializable
data class BotInfo(
    val name: String,
    val version: String,
    val authors: List<String>,
    val description: String? = null,
    val homepage: String? = null,
    val countryCodes: List<String>? = null,
    val platform: String? = null,
    val programmingLang: String? = null,
    val gameTypes: List<String>? = null,
    val initialPosition: String? = null,
)