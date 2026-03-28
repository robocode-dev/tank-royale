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
 * @param debugGraphics Debug graphics as an SVG string. If set to `null`, no debug graphics will be displayed.
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
    override var debugGraphics: String? = null,

    ) : IBotIntent {
    /**
     * Updates this [BotIntent] with new updated values from another [BotIntent].
     * @param update contains the fields that must be updated. Fields that are `null` are being ignored, meaning
     * that the corresponding fields on this intent are left unchanged.
     */
    fun update(update: IBotIntent) {
        update.targetSpeed?.let { targetSpeed = it }
        update.turnRate?.let { turnRate = it }
        update.gunTurnRate?.let { gunTurnRate = it }
        update.radarTurnRate?.let { radarTurnRate = it }
        update.firepower?.let { firepower = it }
        update.adjustGunForBodyTurn?.let { adjustGunForBodyTurn = it }
        update.adjustRadarForBodyTurn?.let { adjustRadarForBodyTurn = it }
        update.adjustRadarForGunTurn?.let { adjustRadarForGunTurn = it }
        update.rescan?.let { rescan = it }
        update.fireAssist?.let { fireAssist = it }
        update.bodyColor?.let { bodyColor = it.ifBlank { null } }
        update.turretColor?.let { turretColor = it.ifBlank { null } }
        update.radarColor?.let { radarColor = it.ifBlank { null } }
        update.bulletColor?.let { bulletColor = it.ifBlank { null } }
        update.scanColor?.let { scanColor = it.ifBlank { null } }
        update.tracksColor?.let { tracksColor = it.ifBlank { null } }
        update.gunColor?.let { gunColor = it.ifBlank { null } }
        update.stdOut?.let { stdOut = it.ifBlank { null } }
        update.stdErr?.let { stdErr = it.ifBlank { null } }
        update.teamMessages?.let { teamMessages = it }
        update.debugGraphics?.let { debugGraphics = it.ifBlank { null } }
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
