package dev.robocode.tankroyale.server.mapper

import dev.robocode.tankroyale.schema.BotIntent

object BotIntentToBotIntentMapper {
    fun map(intent: BotIntent): dev.robocode.tankroyale.server.model.BotIntent {
        intent.apply {
            return dev.robocode.tankroyale.server.model.BotIntent(
                targetSpeed = targetSpeed,
                turnRate = turnRate,
                gunTurnRate = gunTurnRate,
                radarTurnRate = radarTurnRate,
                firepower = firepower,
                adjustGunForBodyTurn = adjustGunForBodyTurn,
                adjustRadarForGunTurn = adjustRadarForGunTurn,
                scan = scan,
                bodyColor = bodyColor,
                turretColor = turretColor,
                radarColor = radarColor,
                bulletColor = bulletColor,
                scanColor = scanColor,
                tracksColor = tracksColor,
                gunColor = gunColor,
            )
        }
    }
}