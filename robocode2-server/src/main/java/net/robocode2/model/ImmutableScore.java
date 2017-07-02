package net.robocode2.model;

/**
 * Immutable score record
 *
 * @author Flemming N. Larsen
 */
public class ImmutableScore implements IScore {

	/** Survival score gained whenever another bot is defeated */
	private final double survival;
	/** Last survivor score as last survivor in a round */
	private final double lastSurvivorBonus;
	/** Bullet damage given */
	private final double bulletDamage;
	/** Bullet kill bonus */
	private final double bulletKillBonus;
	/** Ram damage given */
	private final double ramDamage;
	/** Ram kill bonus */
	private final double ramKillBonus;

	/**
	 * Creates a immutable score record based on another score record.
	 * 
	 * @param bot
	 *            is the score record that is deep copied into this score record.
	 */
	public ImmutableScore(IScore score) {
		survival = score.getSurvival();
		lastSurvivorBonus = score.getLastSurvivorBonus();
		bulletDamage = score.getBulletDamage();
		bulletKillBonus = score.getBulletKillBonus();
		ramDamage = score.getRamDamage();
		ramKillBonus = score.getRamKillBonus();
	}

	@Override
	public double getSurvival() {
		return survival;
	}

	@Override
	public double getLastSurvivorBonus() {
		return lastSurvivorBonus;
	}

	@Override
	public double getBulletDamage() {
		return bulletDamage;
	}

	@Override
	public double getBulletKillBonus() {
		return bulletKillBonus;
	}

	@Override
	public double getRamDamage() {
		return ramDamage;
	}

	@Override
	public double getRamKillBonus() {
		return ramKillBonus;
	}
}
