package net.robocode2.gui.bootstrap

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
        val author: String,
        val description: String? = null,
        val countryCode: String? = null,
        val programmingLang: String? = null,
        val gameTypes: Set<String>
)