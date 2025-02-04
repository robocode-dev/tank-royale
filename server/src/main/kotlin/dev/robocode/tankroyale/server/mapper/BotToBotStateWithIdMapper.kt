package dev.robocode.tankroyale.server.mapper

import dev.robocode.tankroyale.schema.game.BotStateWithId
import dev.robocode.tankroyale.server.model.normalizeAbsoluteDegrees
import dev.robocode.tankroyale.server.model.IBot

object BotToBotStateWithIdMapper {
    fun map(bot: IBot, sessionId: String, enemyCount: Int, isDebugGraphicsEnabled: Boolean): BotStateWithId {
        val botState = BotStateWithId()
        bot.apply {
            botState.isDroid = isDroid
            botState.id = id.value
            botState.sessionId = sessionId
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
            botState.enemyCount = enemyCount
            botState.bodyColor = bodyColor?.value
            botState.turretColor = turretColor?.value
            botState.radarColor = radarColor?.value
            botState.bulletColor = bulletColor?.value
            botState.scanColor = scanColor?.value
            botState.tracksColor = tracksColor?.value
            botState.gunColor = gunColor?.value
            botState.stdOut = stdOut
            botState.stdErr = stdErr
            botState.isDebuggingEnabled = isDebugGraphicsEnabled
            // avoid sending SVG string if debug graphics is disabled (waste of network traffic)
            botState.debugGraphics = if (isDebugGraphicsEnabled) debugGraphics else null
            return botState
        }
    }
}