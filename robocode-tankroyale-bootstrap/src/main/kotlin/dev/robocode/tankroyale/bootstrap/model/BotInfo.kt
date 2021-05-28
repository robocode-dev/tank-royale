package dev.robocode.tankroyale.bootstrap.model

import kotlinx.serialization.Serializable

@Serializable
data class BotInfo(
        val name: String,
        val version: String,
        val authors: List<String>,
        val description: String?,
        val url: String? = null,
        val countryCodes: List<String>,
        val platform: String? = null,
        val programmingLang: String? = null,
        val gameTypes: List<String>
)