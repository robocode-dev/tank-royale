package dev.robocode.tankroyale.runner

/**
 * Structured result returned after a battle completes.
 *
 * @property numberOfRounds the number of rounds that were completed
 * @property results per-bot results ordered by final ranking (rank 1 first)
 */
data class BattleResults(
    val numberOfRounds: Int,
    val results: List<BotResult>,
)

/**
 * Score and ranking data for a single bot participant.
 *
 * @property id the bot's session identifier assigned by the server
 * @property name the bot's name as declared in its configuration
 * @property version the bot's version string
 * @property isTeam whether this entry represents a team
 * @property rank final placement (1 = winner)
 * @property totalScore sum of all scoring components
 * @property survival score from surviving rounds
 * @property lastSurvivorBonus bonus awarded to the last surviving bot each round
 * @property bulletDamage score from bullet damage dealt
 * @property bulletKillBonus bonus awarded for killing an opponent with a bullet
 * @property ramDamage score from ramming damage dealt
 * @property ramKillBonus bonus awarded for killing an opponent by ramming
 * @property firstPlaces number of rounds won (1st place)
 * @property secondPlaces number of rounds finished in 2nd place
 * @property thirdPlaces number of rounds finished in 3rd place
 */
data class BotResult(
    val id: Int,
    val name: String,
    val version: String,
    val isTeam: Boolean,
    val rank: Int,
    val totalScore: Int,
    val survival: Int,
    val lastSurvivorBonus: Int,
    val bulletDamage: Int,
    val bulletKillBonus: Int,
    val ramDamage: Int,
    val ramKillBonus: Int,
    val firstPlaces: Int,
    val secondPlaces: Int,
    val thirdPlaces: Int,
)
