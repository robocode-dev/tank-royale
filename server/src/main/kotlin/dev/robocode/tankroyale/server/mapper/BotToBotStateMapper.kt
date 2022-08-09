package dev.robocode.tankroyale.server.mapper

import dev.robocode.tankroyale.schema.BotState
import dev.robocode.tankroyale.server.math.normalizeAbsoluteDegrees
import dev.robocode.tankroyale.server.model.IBot

object BotToBotStateMapper {
    fun map(bot: IBot): BotState {
        val botState = BotState()
        bot.apply {
            botState.energy = energy
            botState.x = x
            botState.y = y
            botState.speed = speed
            botState.turnRate = turnRate
            botState.gunTurnRate = gunTurnRate
            botState.radarTurnRate = radarTurnRate
            botState.direction = normalizeAbsoluteDegrees(direction)
            botState.gunDirection = normalizeAbsoluteDegrees(gunDirection)
            botState.radarDirection = normalizeAbsoluteDegrees(radarDirection)
            botState.radarSweep = radarSpreadAngle
            botState.gunHeat = gunHeat
            botState.bodyColor = bodyColor?.value
            botState.turretColor = turretColor?.value
            botState.radarColor = radarColor?.value
            botState.bulletColor = bulletColor?.value
            botState.scanColor = scanColor?.value
            botState.tracksColor = tracksColor?.value
            botState.gunColor = gunColor?.value
        }
        return botState
    }
}