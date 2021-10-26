package dev.robocode.tankroyale.gui.booter

import kotlinx.serialization.Serializable

@Serializable
data class BotEntry(
    val filename: String,
    val info: Info
)

@Serializable
data class Info(
    val name: String,
    val version: String,
    val authors: String,
    val description: String? = null,
    val homepage: String? = null,
    val countryCodes: String,
    val gameTypes: String,
    val programmingLang: String? = null,
    val platform: String? = null
)