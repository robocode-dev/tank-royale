package net.robocode2.model;

/**
 * Score record interface
 *
 * @author Flemming N. Larsen
 */
public interface IScore {

	/** Returns the survival score gained whenever another bot is defeated */
	double getSurvival();

	/** Returns the last survivor score as last survivor in a round */
	double getLastSurvivorBonus();

	/** Returns the bullet damage given */
	double getBulletDamage();

	/** Returns the bullet kill bonus */
	double getBulletKillBonus();

	/** Returns the ram damage given */
	double getRamDamage();

	/** Returns the ram kill bonus */
	double getRamKillBonus();

	/** Returns the total score */
	default double getTotalScore() {
		return getSurvival() + getLastSurvivorBonus() + getBulletDamage() + getBulletKillBonus() + getRamDamage()
				+ getRamKillBonus();
	}
}
