package net.robocode2.gui.model

data class Participant(
    val id: Int,
    val name: String,
    val version: String,
    val gameTypes: Set<String>?,
    val author: String?,
    val countryCode: String?,
    val programmingLang: String?
) : Content(type = ContentType.PARTICIPANT.type)