package dev.robocode.tankroyale.server.dev.robocode.tankroyale.server.model

import dev.robocode.tankroyale.server.model.BotId
import dev.robocode.tankroyale.server.model.TeamId

/**
 * TeamOrBotId is a hybrid id to represent a team or bot, if the bot is not participating in a team.
 * If the bot is participating in a team, the team id is used as the id if this class. Otherwise, the negated bot id
 * is being used for the id of this class.
 *
 * @param teamId is the id of the team, if the bot is participating in a team.
 * @param botId is the fallback id, which is the id of the bot. The negated bot id will be used as "team id" if the
 * teamId is not provided.
 */
class TeamOrBotId(val teamId: TeamId?, val botId: BotId) {
    val id: Int = teamId?.id ?: -botId.id

    override fun toString() = "id:$id, botId:$botId, teamId:$teamId"
}
