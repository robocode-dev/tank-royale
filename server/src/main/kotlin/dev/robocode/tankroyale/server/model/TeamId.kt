package dev.robocode.tankroyale.server.model

/**
 * BotId contains the id of a team.
 * @param id the id of the team.
 */
@JvmInline
value class TeamId(val id: Int) {
    override fun toString() = "$id"
}
