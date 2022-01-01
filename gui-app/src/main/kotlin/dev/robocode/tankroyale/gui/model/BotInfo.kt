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
    val host: String, // bot directory name, when running locally
    val port: Int = -1,
    var pid: Long? = null

) : Comparable<BotInfo> {

    val botAddress: BotAddress
        get() = BotAddress(host, port)

    val displayText: String
        get() = "$name $version"

    override fun compareTo(other: BotInfo): Int {
        val cmp = "$host$port".compareTo("${other.host}${other.port}")
        return if (cmp != 0) cmp else displayText.compareTo(other.displayText)
    }
}