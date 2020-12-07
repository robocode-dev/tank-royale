package dev.robocode.tankroyale.server.model

/**
 * BotIntent is the intent sent from a bot between turns. The bot intent reflects the
 * bot's wishes/orders for new target speed, turn rates, bullet power etc.
 */
data class BotIntent(
    /** Desired speed */
    var targetSpeed: Double? = 0.0,

    /** Desired driving turn rate */
    var turnRate: Double? = 0.0,

    /** Desired gun turn rate */
    var gunTurnRate: Double? = 0.0,

    /** Desired radar turn rate */
    var radarTurnRate: Double? = 0.0,

    /** Desired bullet power */
    var bulletPower: Double? = 0.0,

    /** Adjust gun for body turn */
    var adjustGunForBodyTurn: Boolean? = false,

    /** Adjust radar for gun turn */
    var adjustRadarForGunTurn: Boolean? = false,

    /** Perform rescan */
    var scan: Boolean? = false,

    /** Body color */
    var bodyColor: String? = null,

    /** Gun turret color */
    var turretColor: String? = null,

    /** Radar color */
    var radarColor: String? = null,

    /** Bullet color */
    var bulletColor: String? = null,

    /** Scan color */
    var scanColor: String? = null,

    /** Tracks color */
    var tracksColor: String? = null,

    /** Gun color */
    var gunColor: String? = null,
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

    /** Resets the target speed, turn rates, and bullet power. */
    fun resetMovement() {
        targetSpeed = 0.0
        turnRate = 0.0
        gunTurnRate = 0.0
        radarTurnRate = 0.0
        bulletPower = 0.0
    }
}