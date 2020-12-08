package dev.robocode.tankroyale.server.mapper

import dev.robocode.tankroyale.schema.BotHandshake
import dev.robocode.tankroyale.schema.BotInfo
import java.util.*

object BotHandshakeToBotInfoMapper {
    fun map(botHandshake: BotHandshake, hostName: String, port: Int): BotInfo {
        val botInfo = BotInfo()
        botHandshake.apply {
            botInfo.name = name
            botInfo.version = version
            botInfo.author = author
            botInfo.description = description
            botInfo.url = url
            botInfo.countryCode = countryCode
            botInfo.gameTypes = Collections.unmodifiableList(gameTypes)
            botInfo.platform = platform
            botInfo.programmingLang = programmingLang
            botInfo.host = hostName
            botInfo.port = port
        }
        return botInfo
    }
}