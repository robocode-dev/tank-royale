package dev.robocode.tankroyale.gui.model

import kotlinx.serialization.Serializable

@Serializable
data class Participant(
    val id: Int,
    val sessionId: String,
    val team: String? = null,
    val name: String,
    val version: String,
    val authors: List<String>,
    val description: String? = null,
    val homepage: String? = null,
    val countryCodes: List<String>,
    val gameTypes: Set<String> = HashSet(),
    val platform: String? = null,
    val programmingLang: String? = null,
    val initialPosition: InitialPosition? = null
) {
    val displayName =
        if (team != null) {
            "$id: $name $version / $team"
        } else {
            "$id: $name $version"
        }
}