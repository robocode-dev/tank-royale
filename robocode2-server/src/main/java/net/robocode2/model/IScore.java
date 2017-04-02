package net.robocode2.model;

public interface IScore {

	double getSurvival();

	double getLastSurvivorBonus();

	double getBulletDamage();

	double getBulletKillBonus();

	double getRamDamage();

	double getRamKillBonus();

	default double getTotalScore() {
		return getSurvival() + getLastSurvivorBonus() + getBulletDamage() + getBulletKillBonus() + getRamDamage()
				+ getRamKillBonus();
	}
}
