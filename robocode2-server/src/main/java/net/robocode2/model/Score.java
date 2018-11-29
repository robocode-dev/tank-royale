package net.robocode2.model;

import lombok.Builder;
import lombok.Value;

/**
 * Defines a score record to keep track of a bot's score
 * 
 * @author Flemming N. Larsen
 */
@Value
@Builder(toBuilder=true)
public class Score {

	/** Bot ID */
	int botId;

	/** Survival score gained whenever another bot is defeated */
	double survival;

	/** Last survivor score as last survivor in a round */
	double lastSurvivorBonus;

	/** Bullet damage given */
	double bulletDamage;

	/** Bullet kill bonus */
	double bulletKillBonus;

	/** Ram damage given */
	double ramDamage;

	/** Ram kill bonus */
	double ramKillBonus;

	/** Total score */
	public double getTotalScore() {
		return survival + lastSurvivorBonus + bulletDamage + bulletKillBonus + ramDamage + ramKillBonus;
	}

	/** Number of 1st places */
	int firstPlaces;

	/** Number of 2nd places */
	int secondPlaces;

	/** Number of 3rd places */
	int thirdPlaces;
}