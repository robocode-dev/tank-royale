package net.robocode2.gui.model.comm

data class BotInfo(
        val name: String,
        val version: String,
        val author: String?,
        val countryCode: String?,
        val gameTypes: Set<String>,
        val programmingLanguage: String?,
        val host: String,
        val port: Int
)
