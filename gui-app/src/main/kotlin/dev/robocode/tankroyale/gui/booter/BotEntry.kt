package dev.robocode.tankroyale.gui.booter

import kotlinx.serialization.Serializable

@Serializable
data class BotEntry(
    val dir: String,
    val info: Info
)

@Serializable
data class Info(
    val name: String,
    val version: String,
    val authors: String,
    val description: String? = null,
    val homepage: String? = null,
    val countryCodes: String? = null,
    val programmingLang: String? = null,
    val platform: String? = null,
    val gameTypes: String? = null,
    val initialPosition: String? = null
)