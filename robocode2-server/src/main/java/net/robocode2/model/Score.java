package net.robocode2.model;

public class Score implements IScore {

	private double survival;
	private double lastSurvivorBonus;
	private double bulletDamage;
	private double bulletKillBonus;
	private double ramDamage;
	private double ramKillBonus;

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

	public void setSurvival(double survival) {
		this.survival = survival;
	}

	public void setLastSurvivorBonus(double lastSurvivorBonus) {
		this.lastSurvivorBonus = lastSurvivorBonus;
	}

	public void setBulletDamage(double bulletDamage) {
		this.bulletDamage = bulletDamage;
	}

	public void setBulletKillBonus(double bulletKillBonus) {
		this.bulletKillBonus = bulletKillBonus;
	}

	public void setRamDamage(double ramDamage) {
		this.ramDamage = ramDamage;
	}

	public void setRamKillBonus(double ramKillBonus) {
		this.ramKillBonus = ramKillBonus;
	}
}
