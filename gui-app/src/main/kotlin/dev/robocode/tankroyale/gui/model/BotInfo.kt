package dev.robocode.tankroyale.gui.model

import kotlinx.serialization.Serializable
import java.util.Objects

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
    val initialPosition: InitialPosition? = null,
    val teamId: Int? = null,
    val teamName: String? = null,
    val teamVersion: String? = null,
    val host: String, // bot directory name, when running locally
    val port: Int = -1,
    val sessionId: String? = null,
) : Comparable<BotInfo> {

    val botAddress: BotAddress = BotAddress(host, port)

    val displayText: String = "$name $version"

    override fun compareTo(other: BotInfo): Int {
        val cmp = "$host$port".compareTo("${other.host}${other.port}")
        return if (cmp != 0) cmp else displayText.compareTo(other.displayText)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as BotInfo

        if (host != other.host) return false
        if (port != other.port) return false

        return true
    }

    override fun hashCode(): Int {
        return Objects.hash(host, port)
    }
}