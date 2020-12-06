package dev.robocode.tankroyale.server.model

/** Defines a score record to keep track of a bot's score. */
data class Score(
    /** Bot ID */
    var botId: Int,

    /** Survival score gained whenever another bot is defeated */
    var survival: Double = 0.0,

    /** Last survivor score as last survivor in a round */
    var lastSurvivorBonus: Double = 0.0,

    /** Bullet damage given */
    var bulletDamage: Double = 0.0,

    /** Bullet kill bonus */
    var bulletKillBonus: Double = 0.0,

    /** Ram damage given */
    var ramDamage: Double = 0.0,

    /** Ram kill bonus */
    var ramKillBonus: Double = 0.0,

    /** Number of 1st places  */
    var firstPlaces: Int = 0,

    /** Number of 2nd places  */
    var secondPlaces: Int = 0,

    /** Number of 3rd places  */
    var thirdPlaces: Int = 0
) {
    /** Total score */
    val totalScore: Double
        get() = survival + lastSurvivorBonus + bulletDamage + bulletKillBonus + ramDamage + ramKillBonus
}