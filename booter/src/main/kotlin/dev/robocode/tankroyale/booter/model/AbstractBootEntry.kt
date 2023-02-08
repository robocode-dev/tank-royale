package dev.robocode.tankroyale.booter.model

abstract class AbstractBootEntry {
    abstract val name: String
    abstract val version: String
    abstract val authors: List<String>
    abstract val description: String?
    abstract val homepage: String?
    abstract val countryCodes: List<String>?
    abstract val gameTypes: List<String>?
    abstract val platform: String?
    abstract val programmingLang: String?
    abstract val initialPosition: String?
}