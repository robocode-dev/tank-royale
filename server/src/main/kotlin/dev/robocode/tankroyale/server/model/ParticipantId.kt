package dev.robocode.tankroyale.server.dev.robocode.tankroyale.server.model

import dev.robocode.tankroyale.server.model.BotId
import dev.robocode.tankroyale.server.model.TeamId

/**
 * ParticipantId is a hybrid id to represent either a team or bot participating in a battle.
 * A bot can either be a member of a team, or no team. If a bot is not a member of a team, the bot is considered to
 * be a team of its own.
 *
 * If a bot is a member of a team, the team id will take precedence to present the id of this participant id.
 * If the bot is not a member of any team, the bot id will be used instead to present the id of this participant id.
 *
 * Note that when a bot id takes precedence, the negated id of the bot will be used as participant id.
 * This is done to ensure that the participant id is unique, as a bot id and team id might otherwise collide
 * (be the same).
 *
 * @param botId is the bot id of the participant bot.
 * @param teamId is the team id of the participant bot, or `null` if the bot is not a member of a team.
 */
data class ParticipantId(val botId: BotId, val teamId: TeamId? = null) {
    val id: Int = teamId?.id ?: -botId.value

    override fun toString() = "id:$id, BotId.id:$botId, TeamId.id:$teamId"
}
