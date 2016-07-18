package net.robocode2.model;

public final class Score {

	private final int rank;
	private final double totalScore;
	private final double survival;
	private final double lastSurvivorBonus;
	private final double bulletDamage;
	private final double bulletKillBonus;
	private final double ramDamage;
	private final double ramKillBonus;
	private final int firstPlaces;
	private final int secondPlaces;
	private final int thirdPlaces;

	public Score(int rank, double totalScore, double survival, double lastSurvivorBonus, double bulletDamage,
			double bulletKillBonus, double ramDamage, double ramKillBonus, int firstPlaces, int secondPlaces,
			int thirdPlaces) {

		this.rank = rank;
		this.totalScore = totalScore;
		this.survival = survival;
		this.lastSurvivorBonus = lastSurvivorBonus;
		this.bulletDamage = bulletDamage;
		this.bulletKillBonus = bulletKillBonus;
		this.ramDamage = ramDamage;
		this.ramKillBonus = ramKillBonus;
		this.firstPlaces = firstPlaces;
		this.secondPlaces = secondPlaces;
		this.thirdPlaces = thirdPlaces;
	}

	public int getRank() {
		return rank;
	}

	public double getTotalScore() {
		return totalScore;
	}

	public double getSurvival() {
		return survival;
	}

	public double getLastSurvivorBonus() {
		return lastSurvivorBonus;
	}

	public double getBulletDamage() {
		return bulletDamage;
	}

	public double getBulletKillBonus() {
		return bulletKillBonus;
	}

	public double getRamDamage() {
		return ramDamage;
	}

	public double getRamKillBonus() {
		return ramKillBonus;
	}

	public int getFirstPlaces() {
		return firstPlaces;
	}

	public int getSecondPlaces() {
		return secondPlaces;
	}

	public int getThirdPlaces() {
		return thirdPlaces;
	}
}
