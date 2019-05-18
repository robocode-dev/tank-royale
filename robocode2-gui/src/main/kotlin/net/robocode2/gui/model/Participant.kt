package net.robocode2.gui.model

data class Participant(
    val id: Int,
    val name: String,
    val version: String,
    val author: String,
    val countryCode: String?,
    val gameTypes: Set<String>?,
    val programmingLang: String?
) : Message(type = MessageType.PARTICIPANT.type)