package dev.robocode.tankroyale.server.dev.robocode.tankroyale.server.model

import dev.robocode.tankroyale.server.model.BotId
import dev.robocode.tankroyale.server.model.TeamId

/**
 * BotOrTeamId is a hybrid id to represent a team, or bot, if the bot is not participating in a team.
 * If the bot is participating in a team, the team id has precedence and must be used as the id of this class.
 * Otherwise, the negated bot id is being used for the id of this class.
 *
 * @param botId is the fallback id, which is the id of the bot. The negated bot id will be used as "team id" if the
 * @param teamId is the id of the team, if the bot is participating in a team.
 * teamId is not provided.
 */
class BotOrTeamId(val botId: BotId, val teamId: TeamId? = null) {
    val id: Int = teamId?.id ?: -botId.id

    override fun toString() = "id:$id, botId:$botId, teamId:$teamId"
}
