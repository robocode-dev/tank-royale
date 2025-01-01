package model.factory

import dev.robocode.tankroyale.server.model.IBotIntent
import dev.robocode.tankroyale.server.model.TeamMessage

/**
 * BotUpdate is a bot intent sent from a bot between turns.
 * A bot intent reflects the bot´s orders for setting a new target speed, turn rates, bullet power etc.
 * @param targetSpeed New target speed.
 * @param turnRate New driving turn rate.
 * @param gunTurnRate New gun turn rate.
 * @param radarTurnRate New radar turn rate.
 * @param firepower New firepower.
 * @param adjustGunForBodyTurn Flag set to adjusting gun for the body turn.
 * @param adjustRadarForBodyTurn Flag set to adjusting radar for the body turn.
 * @param adjustRadarForGunTurn Flag set to adjusting radar for the gun turn.
 * @param rescan Flag set to perform rescan (reusing last scan direction and scan spread angle)
 * @param fireAssist Flag set to enable fire assistance.
 * @param bodyColor New body color.
 * @param turretColor New gun turret color.
 * @param radarColor New radar color.
 * @param bulletColor New bullet color.
 * @param scanColor New scan color.
 * @param tracksColor New tracks color.
 * @param gunColor New gun color.
 */
data class BotUpdate(
    override var targetSpeed: Double? = null,
    override var turnRate: Double? = null,
    override var gunTurnRate: Double? = null,
    override var radarTurnRate: Double? = null,
    override var firepower: Double? = null,
    override var adjustGunForBodyTurn: Boolean? = null,
    override var adjustRadarForBodyTurn: Boolean? = null,
    override var adjustRadarForGunTurn: Boolean? = null,
    override var rescan: Boolean? = null,
    override var fireAssist: Boolean? = null,
    override var bodyColor: String? = null,
    override var turretColor: String? = null,
    override var radarColor: String? = null,
    override var bulletColor: String? = null,
    override var scanColor: String? = null,
    override var tracksColor: String? = null,
    override var gunColor: String? = null,
    override var stdOut: String? = null,
    override var stdErr: String? = null,
    override var debugGraphics: String? = null,
    override var teamMessages: List<TeamMessage>? = null,
) : IBotIntent