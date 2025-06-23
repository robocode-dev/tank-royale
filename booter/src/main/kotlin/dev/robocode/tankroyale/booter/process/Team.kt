package dev.robocode.tankroyale.booter.process

/**
 * Data class representing a team of bots.
 * @param id The unique team ID
 * @param name The team name
 * @param version The team version
 * @param members List of bot names that are part of this team
 */
internal data class Team(val id: TeamId, val name: String, val version: String, val members: List<String>)
