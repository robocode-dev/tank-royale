package dev.robocode.tankroyale.ui.desktop.bootstrap

import kotlinx.serialization.Serializable

@Serializable
data class BotEntry(
    val filename: String,
    val info: Info
) {
    val displayText: String
        get() {
            return info.displayText
        }
}

@Serializable
data class Info(
    val name: String,
    val version: String,
    val author: String,
    val description: String? = null,
    val countryCode: String? = null,
    val programmingLang: String? = null,
    val gameTypes: Set<String>
) {
    val displayText: String
        get() {
            return if (author.isBlank()) "$name $version" else "$author: $name $version"
        }
}