package dev.robocode.tankroyale.ui.desktop.model

import kotlinx.serialization.Serializable

@Serializable
data class Participant(
    val id: Int,
    val name: String,
    val version: String,
    val author: String,
    val description: String? = null,
    val url: String? = null,
    val countryCode: String? = null,
    val gameTypes: Set<String>? = HashSet(),
    val platform: String? = null,
    val programmingLang: String? = null
)