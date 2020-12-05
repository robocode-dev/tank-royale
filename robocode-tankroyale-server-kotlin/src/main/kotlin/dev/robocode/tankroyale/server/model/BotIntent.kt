package dev.robocode.tankroyale.server.model

/**
 * BotIntent is the intent sent from a bot between turns. The bot intent reflects the
 * bot's wishes/orders for new target speed, turn rates, bullet power etc.
 */
data class BotIntent(
    /** Desired speed */
    var targetSpeed: Double?,

    /** Desired driving turn rate */
    var turnRate: Double?,

    /** Desired gun turn rate */
    var gunTurnRate: Double?,

    /** Desired radar turn rate */
    var radarTurnRate: Double?,

    /** Desired bullet power */
    var bulletPower: Double?,

    /** Adjust gun for body turn */
    var adjustGunForBodyTurn: Boolean?,

    /** Adjust radar for gun turn */
    var adjustRadarForGunTurn: Boolean?,

    /** Perform rescan */
    var scan: Boolean?,

    /** Body color */
    var bodyColor: String?,

    /** Gun turret color */
    var turretColor: String?,

    /** Radar color */
    var radarColor: String?,

    /** Bullet color */
    var bulletColor: String?,

    /** Scan color */
    var scanColor: String?,

    /** Tracks color */
    var tracksColor: String?,

    /** Gun color */
    var gunColor: String?,
) {
    /**
     * Updates this BotIntent with adjustment values from another BotIntent.
     * @param botIntent is the adjustments for this intent. Fields that are null are being ignored, meaning
     * that the corresponding fields on this intent are left unchanged.
     */
    fun update(botIntent: BotIntent) {
        if (botIntent.targetSpeed != null) {
            targetSpeed = botIntent.targetSpeed
        }
        if (botIntent.turnRate != null) {
            turnRate = botIntent.turnRate
        }
        if (botIntent.gunTurnRate != null) {
            gunTurnRate = botIntent.gunTurnRate
        }
        if (botIntent.radarTurnRate != null) {
            radarTurnRate = botIntent.radarTurnRate
        }
        if (botIntent.bulletPower != null) {
            bulletPower = botIntent.bulletPower
        }
        if (botIntent.adjustGunForBodyTurn != null) {
            adjustGunForBodyTurn = botIntent.adjustGunForBodyTurn
        }
        if (botIntent.adjustRadarForGunTurn != null) {
            adjustRadarForGunTurn = botIntent.adjustRadarForGunTurn
        }
        if (botIntent.scan != null) {
            scan = botIntent.scan
        }
        if (botIntent.bodyColor != null) {
            bodyColor = botIntent.bodyColor
        }
        if (botIntent.turretColor != null) {
            turretColor = botIntent.turretColor
        }
        if (botIntent.radarColor != null) {
            radarColor = botIntent.radarColor
        }
        if (botIntent.bulletColor != null) {
            bulletColor = botIntent.bulletColor
        }
        if (botIntent.scanColor != null) {
            scanColor = botIntent.scanColor
        }
        if (botIntent.tracksColor != null) {
            tracksColor = botIntent.tracksColor
        }
        if (botIntent.gunColor != null) {
            gunColor = botIntent.gunColor
        }
    }

    /** Sets all null values of target speed, turn rates, and bullet power to 0. */
    fun nullsToZeros() {
        targetSpeed = targetSpeed ?: 0.0
        turnRate = turnRate ?: 0.0
        gunTurnRate = gunTurnRate ?: 0.0
        radarTurnRate = radarTurnRate ?: 0.0
        bulletPower = bulletPower ?: 0.0
    }
}