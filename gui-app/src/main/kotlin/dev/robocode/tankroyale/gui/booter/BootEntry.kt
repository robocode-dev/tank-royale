package dev.robocode.tankroyale.gui.booter

import kotlinx.serialization.Serializable

@Serializable
data class BootEntry(
    val dir: String,
    val name: String,
    val version: String,
    val authors: List<String>,
    val description: String? = null,
    val homepage: String? = null,
    val countryCodes: List<String>? = null,
    val gameTypes: List<String>? = null,
    val platform: String? = null,
    val programmingLang: String? = null,
    val initialPosition: String? = null,
    val teamMembers: List<String>? = null,
)