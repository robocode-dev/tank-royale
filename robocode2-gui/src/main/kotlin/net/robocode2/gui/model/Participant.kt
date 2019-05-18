package net.robocode2.gui.model

data class Participant(
    val id: Int,
    val name: String,
    val version: String,
    val author: String,
    val countryCode: String? = null,
    val gameTypes: Set<String>? = HashSet(),
    val programmingLang: String? = null
) : Message(MessageType.PARTICIPANT.type)