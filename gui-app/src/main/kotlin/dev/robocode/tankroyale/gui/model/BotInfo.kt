package dev.robocode.tankroyale.gui.model

import kotlinx.serialization.Serializable

@Serializable
data class BotInfo(
    val name: String,
    val version: String,
    val authors: List<String>,
    val description: String? = null,
    val homepage: String? = null,
    val countryCodes: List<String>,
    val gameTypes: Set<String>,
    val platform: String? = null,
    val programmingLang: String? = null,
    val host: String,
    val port: Int
): Comparable<BotInfo> {

    val botAddress: BotAddress
        get() = BotAddress(host, port)

    val displayText: String
        get() = "$name $version"

    override fun compareTo(other: BotInfo): Int = "$displayText $port".compareTo("${other.displayText} ${other.port}")
}