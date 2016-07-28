package net.robocode2.model;

public final class Score {

	private final int rank;
	private final double survival;
	private final double lastSurvivorBonus;
	private final double bulletDamage;
	private final double bulletKillBonus;
	private final double ramDamage;
	private final double ramKillBonus;
	private final int firstPlaces;
	private final int secondPlaces;
	private final int thirdPlaces;

	public Score(int rank, double survival, double lastSurvivorBonus, double bulletDamage, double bulletKillBonus,
			double ramDamage, double ramKillBonus, int firstPlaces, int secondPlaces, int thirdPlaces) {

		this.rank = rank;
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
		return survival + lastSurvivorBonus + bulletDamage + bulletKillBonus + ramDamage + ramKillBonus;
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

	private Score(ScoreBuilder builder) {
		this.rank = builder.rank;
		this.survival = builder.survival;
		this.lastSurvivorBonus = builder.lastSurvivorBonus;
		this.bulletDamage = builder.bulletDamage;
		this.bulletKillBonus = builder.bulletKillBonus;
		this.ramDamage = builder.ramDamage;
		this.ramKillBonus = builder.ramKillBonus;
		this.firstPlaces = builder.firstPlaces;
		this.secondPlaces = builder.secondPlaces;
		this.thirdPlaces = builder.thirdPlaces;
	}

	public final static class ScoreBuilder {
		private int rank;
		private double survival;
		private double lastSurvivorBonus;
		private double bulletDamage;
		private double bulletKillBonus;
		private double ramDamage;
		private double ramKillBonus;
		private int firstPlaces;
		private int secondPlaces;
		private int thirdPlaces;

		public Score build() {
			return new Score(this);
		}

		public ScoreBuilder setRank(int rank) {
			this.rank = rank;
			return this;
		}

		public ScoreBuilder setSurvival(double survival) {
			this.survival = survival;
			return this;
		}

		public ScoreBuilder setLastSurvivorBonus(double lastSurvivorBonus) {
			this.lastSurvivorBonus = lastSurvivorBonus;
			return this;
		}

		public ScoreBuilder setBulletDamage(double bulletDamage) {
			this.bulletDamage = bulletDamage;
			return this;
		}

		public ScoreBuilder setRamDamage(double ramDamage) {
			this.ramDamage = ramDamage;
			return this;
		}

		public ScoreBuilder setRamKillBonus(double ramKillBonus) {
			this.ramKillBonus = ramKillBonus;
			return this;
		}

		public ScoreBuilder setFirstPlaces(int firstPlaces) {
			this.firstPlaces = firstPlaces;
			return this;
		}

		public ScoreBuilder setSecondPlaces(int secondPlaces) {
			this.secondPlaces = secondPlaces;
			return this;
		}

		public ScoreBuilder setThirdPlaces(int thirdPlaces) {
			this.thirdPlaces = thirdPlaces;
			return this;
		}
	}
}
