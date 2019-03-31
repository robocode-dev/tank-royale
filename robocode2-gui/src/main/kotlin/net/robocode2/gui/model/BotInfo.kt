package net.robocode2.gui.model

data class BotInfo(
        val name: String,
        val version: String,
        val author: String?,
        val countryCode: String?,
        val gameTypes: Set<String>,
        val programmingLanguage: String?,
        val host: String,
        val port: Int) {

    val botAddress: BotAddress
        get() = BotAddress(host, port)

    val displayText: String
        get() {
            var text = "$name $version"
            if (author != null) {
                text = "$author: $text"
            }
            return text
        }
}

