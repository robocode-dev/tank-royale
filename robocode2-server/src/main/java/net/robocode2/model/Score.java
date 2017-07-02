package net.robocode2.model;

/**
 * Defines a score record to keep track of a bot's score
 * 
 * @author Flemming N. Larsen
 */
public class Score implements IScore {

	/** Survival score gained whenever another bot is defeated */
	private double survival;
	/** Last survivor score as last survivor in a round */
	private double lastSurvivorBonus;
	/** Bullet damage given */
	private double bulletDamage;
	/** Bullet kill bonus */
	private double bulletKillBonus;
	/** Ram damage given */
	private double ramDamage;
	/** Ram kill bonus */
	private double ramKillBonus;

	/**
	 * Creates a immutable score record that is a deep copy of this score record.
	 * 
	 * @return a immutable score record
	 */
	public ImmutableScore toImmutableScore() {
		return new ImmutableScore(this);
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

	/**
	 * Sets the survival score gained whenever another bot is defeated
	 * 
	 * @param survival
	 *            is the survival score
	 */
	public void setSurvival(double survival) {
		this.survival = survival;
	}

	/**
	 * Sets the last survivor score as last survivor in a round
	 * 
	 * @param lastSurvivorBonus
	 *            is the last survivor score
	 */
	public void setLastSurvivorBonus(double lastSurvivorBonus) {
		this.lastSurvivorBonus = lastSurvivorBonus;
	}

	/**
	 * Sets the bullet damage given
	 * 
	 * @param bulletDamage
	 *            is the bullet damage
	 */
	public void setBulletDamage(double bulletDamage) {
		this.bulletDamage = bulletDamage;
	}

	/**
	 * Sets the bullet kill bonus
	 * 
	 * @param bulletKillBonus
	 *            is the bullet kill bonus
	 */
	public void setBulletKillBonus(double bulletKillBonus) {
		this.bulletKillBonus = bulletKillBonus;
	}

	/**
	 * Sets the ram damage given
	 * 
	 * @param ramDamage
	 *            is the ram damage
	 */
	public void setRamDamage(double ramDamage) {
		this.ramDamage = ramDamage;
	}

	/**
	 * Sets the ram kill bonus
	 * 
	 * @param ramKillBonus
	 *            is the ram kill bonus
	 */
	public void setRamKillBonus(double ramKillBonus) {
		this.ramKillBonus = ramKillBonus;
	}
}
