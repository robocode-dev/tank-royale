package dev.robocode.tankroyale.server.mapper

import dev.robocode.tankroyale.schema.BotIntent

object BotIntentMapper {
    /**
     * Maps a schema BotIntent to a server model BotIntent.
     * Null values in the schema intent are interpreted as "no change" and those fields are left unchanged.
     * When no existing intent is available, default values are used for null fields.
     */
    fun map(intent: BotIntent): dev.robocode.tankroyale.server.model.BotIntent {
        // Create a new BotIntent with default values
        val result = dev.robocode.tankroyale.server.model.BotIntent()
        
        intent.apply {
            // Apply values with defaults for nulls
            result.targetSpeed = targetSpeed ?: 0.0
            result.turnRate = turnRate ?: 0.0
            result.gunTurnRate = gunTurnRate ?: 0.0
            result.radarTurnRate = radarTurnRate ?: 0.0
            result.firepower = firepower ?: 0.0
            result.adjustGunForBodyTurn = adjustGunForBodyTurn ?: false
            result.adjustRadarForBodyTurn = adjustRadarForBodyTurn ?: false
            result.adjustRadarForGunTurn = adjustRadarForGunTurn ?: false
            result.rescan = rescan ?: false 
            result.fireAssist = fireAssist ?: true
            result.bodyColor = bodyColor 
            result.turretColor = turretColor
            result.radarColor = radarColor
            result.bulletColor = bulletColor
            result.scanColor = scanColor
            result.tracksColor = tracksColor
            result.gunColor = gunColor
            result.stdOut = stdOut
            result.stdErr = stdErr
            result.teamMessages = TeamMessageMapper.map(teamMessages)
            result.debugGraphics = debugGraphics
        }
        
        return result
    }
}