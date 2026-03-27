package dev.robocode.tankroyale.booter.model

/**
 * Common contract for both bot and team boot entries parsed from their respective JSON files.
 * Optional fields are null when the entry omits them; callers should treat null as "unspecified".
 */
interface IBootEntry {
    val name: String
    val version: String
    val authors: List<String>
    val description: String?
    val homepage: String?
    val countryCodes: List<String>?
    val gameTypes: List<String>?
    val platform: String?
    val programmingLang: String?
    val initialPosition: String?
    val teamMembers: List<String>?
}