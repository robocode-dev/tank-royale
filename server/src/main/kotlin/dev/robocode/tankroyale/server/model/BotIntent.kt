package dev.robocode.tankroyale.server.model

/**
 * BotIntent is used to accumulate all bot orders sent to the server over time.
 * Hence, the BotIntent contains the current state of the bot intent state.
 * @param targetSpeed Target speed.
 * @param turnRate Driving turn rate.
 * @param gunTurnRate Gun turn rate.
 * @param radarTurnRate Radar turn rate.
 * @param firepower Firepower.
 * @param adjustGunForBodyTurn Flag set for adjusting gun for body turn.
 * @param adjustRadarForGunTurn Flag set for adjusting radar for gun turn.
 * @param rescan Flag set for performing rescan (reusing last scan direction and scan spread angle).
 * @param fireAssist Flag set for enabling fire assistance.
 * @param bodyColor Body color string. If set to `null` the default body color will be used.
 * @param turretColor Gun turret color string. If set to `null` the default body color will be used.
 * @param radarColor Radar color string. If set to `null` the default body color will be used.
 * @param bulletColor Bullet color string. If set to `null` the default body color will be used.
 * @param scanColor Scan color string. If set to `null` the default body color will be used.
 * @param tracksColor Tracks color string. If set to `null` the default body color will be used.
 * @param gunColor Gun color string. If set to `null` the default body color will be used.
 */
data class BotIntent(
    override var targetSpeed: Double? = 0.0,
    override var turnRate: Double? = 0.0,
    override var gunTurnRate: Double? = 0.0,
    override var radarTurnRate: Double? = 0.0,
    override var firepower: Double? = 0.0,
    override var adjustGunForBodyTurn: Boolean? = false,
    override var adjustRadarForBodyTurn: Boolean? = false,
    override var adjustRadarForGunTurn: Boolean? = false,
    override var rescan: Boolean? = false,
    override var fireAssist: Boolean? = true, // fire assistant is per default on
    override var bodyColor: String? = null,
    override var turretColor: String? = null,
    override var radarColor: String? = null,
    override var bulletColor: String? = null,
    override var scanColor: String? = null,
    override var tracksColor: String? = null,
    override var gunColor: String? = null,
    override var stdOut: String? = null,
    override var stdErr: String? = null,
    override var teamMessages: List<TeamMessage>? = null,

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
        if (update.adjustRadarForBodyTurn != null) {
            adjustRadarForBodyTurn = update.adjustRadarForBodyTurn
        }
        if (update.adjustRadarForGunTurn != null) {
            adjustRadarForGunTurn = update.adjustRadarForGunTurn
        }
        if (update.rescan != null) {
            rescan = update.rescan
        }
        if (update.fireAssist != null) {
            fireAssist = update.fireAssist
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
        if (update.stdOut != null) {
            stdOut = if (update.stdOut!!.isBlank()) null else update.stdOut
        }
        if (update.stdErr != null) {
            stdErr = if (update.stdErr!!.isBlank()) null else update.stdErr
        }
        if (update.teamMessages != null) {
            teamMessages = update.teamMessages
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
