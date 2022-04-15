package dev.robocode.tankroyale.server.model

/**
 * BotIntent is used to accumulate all bot orders sent to the server over time.
 * Hence, the BotIntent contains the current state of the bot intent state.
 * @param targetSpeed Current target speed.
 * @param turnRate Current driving turn rate.
 * @param gunTurnRate Current gun turn rate.
 * @param radarTurnRate Current radar turn rate.
 * @param firepower Current firepower.
 * @param adjustGunForBodyTurn Current flag set for adjusting gun for body turn.
 * @param adjustRadarForGunTurn Current flag set for adjusting radar for gun turn.
 * @param scan Current flag set for performing rescan (reusing last scan direction and scan spread angle)
 * @param bodyColor Current body color string. If set to `null` the default body color will be used.
 * @param turretColor Current gun turret color string. If set to `null` the default body color will be used.
 * @param radarColor Current radar color string. If set to `null` the default body color will be used.
 * @param bulletColor Current bullet color string. If set to `null` the default body color will be used.
 * @param scanColor Current scan color string. If set to `null` the default body color will be used.
 * @param tracksColor Current tracks color string. If set to `null` the default body color will be used.
 * @param gunColor Current gun color string. If set to `null` the default body color will be used.
 */
data class BotIntent(
    override var targetSpeed: Double? = 0.0,
    override var turnRate: Double? = 0.0,
    override var gunTurnRate: Double? = 0.0,
    override var radarTurnRate: Double? = 0.0,
    override var firepower: Double? = 0.0,
    override var adjustGunForBodyTurn: Boolean? = false,
    override var adjustRadarForGunTurn: Boolean? = false,
    override var scan: Boolean? = false,
    override var bodyColor: String? = null,
    override var turretColor: String? = null,
    override var radarColor: String? = null,
    override var bulletColor: String? = null,
    override var scanColor: String? = null,
    override var tracksColor: String? = null,
    override var gunColor: String? = null,

    ) : IBotIntent {
    /**
     * Updates this [BotIntent] with new updated values from another [BotIntent].
     * @param update contains the fields that must be updated. Fields that are `null` are being ignored, meaning
     * that the corresponding fields on this intent are left unchanged.
     */
    fun update(update: IBotIntent) {
        if (update.targetSpeed != null) {
            targetSpeed = update.targetSpeed
        }
        if (update.turnRate != null) {
            turnRate = update.turnRate
        }
        if (update.gunTurnRate != null) {
            gunTurnRate = update.gunTurnRate
        }
        if (update.radarTurnRate != null) {
            radarTurnRate = update.radarTurnRate
        }
        if (update.firepower != null) {
            firepower = update.firepower
        }
        if (update.adjustGunForBodyTurn != null) {
            adjustGunForBodyTurn = update.adjustGunForBodyTurn
        }
        if (update.adjustRadarForGunTurn != null) {
            adjustRadarForGunTurn = update.adjustRadarForGunTurn
        }
        if (update.scan != null) {
            scan = update.scan
        }
        if (update.bodyColor != null) {
            bodyColor = if (update.bodyColor!!.isBlank()) null else update.bodyColor
        }
        if (update.turretColor != null) {
            turretColor = if (update.turretColor!!.isBlank()) null else update.turretColor
        }
        if (update.radarColor != null) {
            radarColor = if (update.radarColor!!.isBlank()) null else update.radarColor
        }
        if (update.bulletColor != null) {
            bulletColor = if (update.bulletColor!!.isBlank()) null else update.bulletColor
        }
        if (update.scanColor != null) {
            scanColor = if (update.scanColor!!.isBlank()) null else update.scanColor
        }
        if (update.tracksColor != null) {
            tracksColor = if (update.tracksColor!!.isBlank()) null else update.tracksColor
        }
        if (update.gunColor != null) {
            gunColor = if (update.gunColor!!.isBlank()) null else update.gunColor
        }
    }

    /**
     * Disables movement by setting these values to zero:
     *  * targetSpeed
     *  * turnRates
     *  * gunTurnRate
     *  * radarTurnRate
     *  * bulletPower
     */
    fun disableMovement() {
        targetSpeed = 0.0
        turnRate = 0.0
        gunTurnRate = 0.0
        radarTurnRate = 0.0
        firepower = 0.0
    }
}
