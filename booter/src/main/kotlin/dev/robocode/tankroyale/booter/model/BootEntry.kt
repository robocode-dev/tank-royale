package dev.robocode.tankroyale.booter.model

import kotlinx.serialization.Serializable

@Serializable
data class BootEntry(
    // Shared bot and team fields
    override val name: String,
    override val version: String,
    override val authors: List<String>,
    override val description: String? = null,
    override val homepage: String? = null,
    override val countryCodes: List<String>? = null,
    override val gameTypes: List<String>? = null,

    // Bot fields only
    override val platform: String? = null,
    override val programmingLang: String? = null,
    override val initialPosition: String? = null,

    // Team fields only
    override val teamMembers: List<String>? = null,
) : IBootEntry