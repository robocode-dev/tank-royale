package net.robocode2.gui.model.event

import net.robocode2.gui.model.comm.Content
import net.robocode2.gui.model.comm.ContentType

class Participant(
    val id: Int,
    val name: String,
    val version: String,
    val gameTypes: Set<String>?,
    val author: String?,
    val countryCode: String?,
    val programmingLanguage: String?
) : Content(type = ContentType.PARTICIPANT.type)