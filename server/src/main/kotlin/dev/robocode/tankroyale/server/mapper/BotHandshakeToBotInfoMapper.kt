package dev.robocode.tankroyale.server.mapper

import dev.robocode.tankroyale.schema.game.BotHandshake
import dev.robocode.tankroyale.schema.game.BotInfo

object BotHandshakeToBotInfoMapper {
    fun map(botHandshake: BotHandshake, hostName: String, port: Int): BotInfo {
        val botInfo = BotInfo()
        botHandshake.apply {
            botInfo.sessionId = sessionId
            botInfo.name = name
            botInfo.version = version
            botInfo.authors = authors.toList()
            botInfo.description = description
            botInfo.homepage = homepage
            botInfo.countryCodes = countryCodes.toList()
            botInfo.gameTypes = gameTypes.toList()
            botInfo.platform = platform
            botInfo.programmingLang = programmingLang
            botInfo.initialPosition = initialPosition
            botInfo.teamId = teamId
            botInfo.teamName = teamName
            botInfo.teamVersion = teamVersion
            botInfo.isDroid = isDroid
            botInfo.host = hostName
            botInfo.port = port
        }
        return botInfo
    }
}