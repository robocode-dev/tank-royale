package dev.robocode.tankroyale.booter.model

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