package model.factory

import dev.robocode.tankroyale.server.model.IBotIntent

/**
 * BotUpdate is a bot intent sent from a bot between turns.
 * A bot intent reflects the bot's orders for setting a new target speed, turn rates, bullet power etc.
 * @property targetSpeed New target speed.
 * @property turnRate New driving turn rate.
 * @property gunTurnRate New gun turn rate.
 * @property radarTurnRate New radar turn rate.
 * @property firepower New firepower.
 * @property adjustGunForBodyTurn Flag set to adjusting gun for body turn.
 * @property adjustRadarForGunTurn Flag set to adjusting radar for gun turn.
 * @property scan Flag set to perform rescan (reusing last scan direction and scan spread angle)
 * @property bodyColor New body color.
 * @property turretColor New gun turret color.
 * @property radarColor New radar color.
 * @property bulletColor New bullet color.
 * @property scanColor New scan color.
 * @property tracksColor New tracks color.
 * @property gunColor New gun color.
 */
data class BotUpdate(
    override var targetSpeed: Double? = null,
    override var turnRate: Double? = null,
    override var gunTurnRate: Double? = null,
    override var radarTurnRate: Double? = null,
    override var firepower: Double? = null,
    override var adjustGunForBodyTurn: Boolean? = null,
    override var adjustRadarForGunTurn: Boolean? = null,
    override var scan: Boolean? = null,
    override var bodyColor: String? = null,
    override var turretColor: String? = null,
    override var radarColor: String? = null,
    override var bulletColor: String? = null,
    override var scanColor: String? = null,
    override var tracksColor: String? = null,
    override var gunColor: String? = null,

    ) : IBotIntent