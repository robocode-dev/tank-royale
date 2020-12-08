package dev.robocode.tankroyale.server.mapper

import dev.robocode.tankroyale.schema.BotStateWithId
import dev.robocode.tankroyale.server.math.normalAbsoluteDegrees
import dev.robocode.tankroyale.server.model.Bot

object BotToBotStateWithIdMapper {
    fun map(bot: Bot): BotStateWithId {
        val botState = BotStateWithId()
        bot.apply {
            botState.id = id.value
            botState.energy = energy
            botState.x = x
            botState.y = y
            botState.speed = speed
            botState.turnRate = turnRate
            botState.gunTurnRate = gunTurnRate
            botState.radarTurnRate = radarTurnRate
            botState.direction = normalAbsoluteDegrees(direction)
            botState.gunDirection = normalAbsoluteDegrees(gunDirection)
            botState.radarDirection = normalAbsoluteDegrees(radarDirection)
            botState.radarSweep = radarSpreadAngle
            botState.gunHeat = gunHeat
            botState.bodyColor = bodyColor
            botState.turretColor = turretColor
            botState.radarColor = radarColor
            botState.bulletColor = bulletColor
            botState.scanColor = scanColor
            botState.tracksColor = tracksColor
            botState.gunColor = gunColor
            return botState
        }
    }
}