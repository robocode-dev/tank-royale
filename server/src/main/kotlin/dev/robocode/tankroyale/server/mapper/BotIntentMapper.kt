package dev.robocode.tankroyale.server.mapper

import dev.robocode.tankroyale.schema.game.BotIntent
import dev.robocode.tankroyale.server.dev.robocode.tankroyale.server.mapper.TeamMessageMapper

object BotIntentMapper {
    fun map(intent: BotIntent): dev.robocode.tankroyale.server.model.BotIntent {
        intent.apply {
            return dev.robocode.tankroyale.server.model.BotIntent(
                targetSpeed = targetSpeed,
                turnRate = turnRate,
                gunTurnRate = gunTurnRate,
                radarTurnRate = radarTurnRate,
                firepower = firepower,
                adjustGunForBodyTurn = adjustGunForBodyTurn,
                adjustRadarForBodyTurn = adjustRadarForBodyTurn,
                adjustRadarForGunTurn = adjustRadarForGunTurn,
                rescan = rescan,
                fireAssist = fireAssist,
                bodyColor = bodyColor,
                turretColor = turretColor,
                radarColor = radarColor,
                bulletColor = bulletColor,
                scanColor = scanColor,
                tracksColor = tracksColor,
                gunColor = gunColor,
                stdOut = stdOut,
                stdErr = stdErr,
                teamMessages = TeamMessageMapper.map(teamMessages),
                debugGraphics = debugGraphics,
            )
        }
    }
}