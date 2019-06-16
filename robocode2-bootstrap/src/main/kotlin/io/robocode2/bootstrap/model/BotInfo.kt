package io.robocode2.bootstrap.model

import kotlinx.serialization.Serializable

@Serializable
data class BotInfo(
        val name: String,
        val version: String,
        val author: String,
        val countryCode: String? = null,
        val programmingLang: String? = null,
        val gameTypes: Set<String>
)