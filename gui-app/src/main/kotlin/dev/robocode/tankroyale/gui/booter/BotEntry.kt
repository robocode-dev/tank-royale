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
    val authors: List<String>,
    val description: String? = null,
    val url: String? = null,
    val countryCodes: List<String>,
    val gameTypes: Set<String>,
    val programmingLang: String? = null,
    val platform: String? = null
)