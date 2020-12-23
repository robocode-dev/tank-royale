package dev.robocode.tankroyale.server.model

/**
 * Interface for a bot intent. Bot intents are sent from bots between turns.
 * A bot intent reflects the bot's orders for setting a new target speed, turn rates, bullet power etc.
 */
interface IBotIntent {
    /** New target speed. */
    var targetSpeed: Double?

    /** New driving turn rate. */
    var turnRate: Double?

    /** New gun turn rate. */
    var gunTurnRate: Double?

    /** New radar turn rate. */
    var radarTurnRate: Double?

    /** New bullet power. */
    var bulletPower: Double?

    /** Flag set to adjusting gun for body turn. */
    var adjustGunForBodyTurn: Boolean?

    /** Flag set to adjusting radar for gun turn. */
    var adjustRadarForGunTurn: Boolean?

    /** Flag set to perform rescan (reusing last scan direction and scan spread angle) */
    var scan: Boolean?

    /** New body color. */
    var bodyColor: String?

    /** New gun turret color. */
    var turretColor: String?

    /** New radar color. */
    var radarColor: String?

    /** New bullet color. */
    var bulletColor: String?

    /** New scan color. */
    var scanColor: String?

    /** New tracks color. */
    var tracksColor: String?

    /** New gun color. */
    var gunColor: String?
}
