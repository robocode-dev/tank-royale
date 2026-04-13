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
        val botIntent = dev.robocode.tankroyale.server.model.BotIntent()

        intent.apply {
            // Apply values with defaults for nulls
            botIntent.targetSpeed = targetSpeed ?: 0.0
            botIntent.turnRate = turnRate ?: 0.0
            botIntent.gunTurnRate = gunTurnRate ?: 0.0
            botIntent.radarTurnRate = radarTurnRate ?: 0.0
            botIntent.firepower = firepower ?: 0.0
            botIntent.adjustGunForBodyTurn = adjustGunForBodyTurn ?: false
            botIntent.adjustRadarForBodyTurn = adjustRadarForBodyTurn ?: false
            botIntent.adjustRadarForGunTurn = adjustRadarForGunTurn ?: false
            botIntent.rescan = rescan ?: false
            botIntent.fireAssist = fireAssist ?: true
            botIntent.bodyColor = bodyColor
            botIntent.turretColor = turretColor
            botIntent.radarColor = radarColor
            botIntent.bulletColor = bulletColor
            botIntent.scanColor = scanColor
            botIntent.tracksColor = tracksColor
            botIntent.gunColor = gunColor
            botIntent.stdOut = stdOut
            botIntent.stdErr = stdErr
            botIntent.teamMessages = TeamMessageMapper.map(teamMessages)
            botIntent.debugGraphics = debugGraphics
        }

        return botIntent
    }

    /**
     * Maps a schema BotIntent to a server model BotIntent preserving nulls for unset fields.
     * Used for merging into an existing intent — null fields will be skipped by [dev.robocode.tankroyale.server.model.BotIntent.update].
     */
    fun mapForMerge(intent: BotIntent): dev.robocode.tankroyale.server.model.BotIntent =
        dev.robocode.tankroyale.server.model.BotIntent(
            targetSpeed = intent.targetSpeed,
            turnRate = intent.turnRate,
            gunTurnRate = intent.gunTurnRate,
            radarTurnRate = intent.radarTurnRate,
            firepower = intent.firepower,
            adjustGunForBodyTurn = intent.adjustGunForBodyTurn,
            adjustRadarForBodyTurn = intent.adjustRadarForBodyTurn,
            adjustRadarForGunTurn = intent.adjustRadarForGunTurn,
            rescan = intent.rescan,
            fireAssist = intent.fireAssist,
            bodyColor = intent.bodyColor,
            turretColor = intent.turretColor,
            radarColor = intent.radarColor,
            bulletColor = intent.bulletColor,
            scanColor = intent.scanColor,
            tracksColor = intent.tracksColor,
            gunColor = intent.gunColor,
            stdOut = intent.stdOut,
            stdErr = intent.stdErr,
            teamMessages = intent.teamMessages?.let { TeamMessageMapper.map(it) },
            debugGraphics = intent.debugGraphics,
        )
}