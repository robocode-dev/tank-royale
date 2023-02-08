package dev.robocode.tankroyale.booter.model

import kotlinx.serialization.Serializable

@Serializable
data class BootDirEntry(
    val dir: String, // extra field compared to BootEntry

    override val name: String,
    override val version: String,
    override val authors: List<String>,
    override val description: String? = null,
    override val homepage: String? = null,
    override val countryCodes: List<String>? = null,
    override val gameTypes: List<String>? = null,

    override val platform: String? = null,
    override val programmingLang: String? = null,
    override val initialPosition: String? = null,
) : AbstractBootEntry()