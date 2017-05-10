package net.robocode2.model;

public class ImmutableScore implements IScore {

	private final double survival;
	private final double lastSurvivorBonus;
	private final double bulletDamage;
	private final double bulletKillBonus;
	private final double ramDamage;
	private final double ramKillBonus;

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
