package dev.robocode.tankroyale.server.dev.robocode.tankroyale.server.mapper

import dev.robocode.tankroyale.server.model.BotId
import dev.robocode.tankroyale.server.model.TeamMessage

object TeamMessageMapper {

    fun map(teamMessages: List<dev.robocode.tankroyale.schema.game.TeamMessage>): List<TeamMessage> {
        val list = ArrayList<TeamMessage>()
        teamMessages.forEach { list.add(map(it)) }
        return list
    }

    fun map(teamMessage: dev.robocode.tankroyale.schema.game.TeamMessage): TeamMessage {
        teamMessage.apply {
            return TeamMessage(message, messageType, if (receiverId != null) BotId(receiverId) else null)
        }
    }
}