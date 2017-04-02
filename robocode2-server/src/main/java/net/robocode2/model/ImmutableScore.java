package net.robocode2.model;

public class ImmutableScore implements IScore {

	private final double survival;
	private final double lastSurvivorBonus;
	private final double bulletDamage;
	private final double bulletKillBonus;
	private final double ramDamage;
	private final double ramKillBonus;

	public ImmutableScore(double survival, double lastSurvivorBonus, double bulletDamage, double bulletKillBonus,
			double ramDamage, double ramKillBonus) {

		this.survival = survival;
		this.lastSurvivorBonus = lastSurvivorBonus;
		this.bulletDamage = bulletDamage;
		this.bulletKillBonus = bulletKillBonus;
		this.ramDamage = ramDamage;
		this.ramKillBonus = ramKillBonus;
	}

	public ImmutableScore(IScore score) {
		this(score.getSurvival(), score.getLastSurvivorBonus(), score.getBulletDamage(), score.getBulletKillBonus(),
				score.getRamDamage(), score.getRamKillBonus());
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
