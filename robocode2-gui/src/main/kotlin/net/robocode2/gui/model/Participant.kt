package net.robocode2.gui.model

import net.robocode2.gui.model.Content
import net.robocode2.gui.model.ContentType

class Participant(
    val id: Int,
    val name: String,
    val version: String,
    val gameTypes: Set<String>?,
    val author: String?,
    val countryCode: String?,
    val programmingLanguage: String?
) : Content(type = ContentType.PARTICIPANT.type)