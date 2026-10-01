package dev.robocode.tankroyale.server.model

/**
 * ParticipantId is a hybrid id to represent either a team or bot participating in a battle.
 * A bot can either be a member of a team, or no team. If a bot is not a member of a team, the bot is considered to
 * be a team of its own.
 *
 * If a bot is a member of a team, the team id will take precedence to present the id of this participant id.
 * If the bot is not a member of any team, the bot id will be used instead to present the id of this participant id.
 *
 * When a bot id takes precedence, its negated value is used as the compact integer id. That integer can collide
 * with a negative team id, so callers must use the typed team and bot ids when group identity matters.
 *
 * @param botId is the bot id of the participant bot.
 * @param teamId is the team id of the participant bot, or `null` if the bot is not a member of a team.
 */
data class ParticipantId(val botId: BotId, val teamId: TeamId? = null) {
    val id: Int = teamId?.id ?: -botId.value

    override fun toString() = "id:$id, BotId.id:$botId, TeamId.id:$teamId"
}
