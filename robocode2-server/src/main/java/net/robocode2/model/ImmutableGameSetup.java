package net.robocode2.model;

public final class ImmutableGameSetup implements IGameSetup {

	private final String gameType;
	private final Integer arenaWidth;
	private final Integer arenaHeight;
	private final Integer minNumberOfParticipants;
	private final Integer maxNumberOfParticipants;
	private final Integer numberOfRounds;
	private final Double gunCoolingRate;
	private final Integer inactiveTurns;
	private final Integer turnTimeout;
	private final Integer readyTimeout;
	private final Integer delayedObserverTurns;

	private final boolean gameTypeFixed;
	private final boolean arenaWidthFixed;
	private final boolean arenaHeightFixed;
	private final boolean minNumberOfParticipantsFixed;
	private final boolean maxNumberOfParticipantsFixed;
	private final boolean numberOfRoundsFixed;
	private final boolean gunCoolingRateFixed;
	private final boolean inactiveTurnsFixed;
	private final boolean turnTimeoutFixed;
	private final boolean readyTimeoutFixed;
	private final boolean delayedObserverTurnsFixed;

	public ImmutableGameSetup(IGameSetup gameSetup) {
		gameType = gameSetup.getGameType();
		arenaWidth = gameSetup.getArenaWidth();
		arenaHeight = gameSetup.getArenaHeight();
		minNumberOfParticipants = gameSetup.getMinNumberOfParticipants();
		maxNumberOfParticipants = gameSetup.getMaxNumberOfParticipants();
		numberOfRounds = gameSetup.getNumberOfRounds();
		gunCoolingRate = gameSetup.getGunCoolingRate();
		inactiveTurns = gameSetup.getInactiveTurns();
		turnTimeout = gameSetup.getTurnTimeout();
		readyTimeout = gameSetup.getReadyTimeout();
		delayedObserverTurns = gameSetup.getDelayedObserverTurns();

		gameTypeFixed = gameSetup.isGameTypeFixed();
		arenaWidthFixed = gameSetup.isArenaWidthFixed();
		arenaHeightFixed = gameSetup.isArenaHeightFixed();
		minNumberOfParticipantsFixed = gameSetup.isMinNumberOfParticipantsFixed();
		maxNumberOfParticipantsFixed = gameSetup.isMaxNumberOfParticipantsFixed();
		numberOfRoundsFixed = gameSetup.isNumberOfRoundsFixed();
		gunCoolingRateFixed = gameSetup.isGunCoolingRateFixed();
		inactiveTurnsFixed = gameSetup.isInactiveTurnsFixed();
		turnTimeoutFixed = gameSetup.isTurnTimeoutFixed();
		readyTimeoutFixed = gameSetup.isReadyTimeoutFixed();
		delayedObserverTurnsFixed = gameSetup.isDelayedObserverTurnsFixed();
	}

	@Override
	public String getGameType() {
		return gameType;
	}

	@Override
	public Integer getArenaWidth() {
		return arenaWidth;
	}

	@Override
	public Integer getArenaHeight() {
		return arenaHeight;
	}

	@Override
	public Integer getMinNumberOfParticipants() {
		return minNumberOfParticipants;
	}

	@Override
	public Integer getMaxNumberOfParticipants() {
		return maxNumberOfParticipants;
	}

	@Override
	public Integer getNumberOfRounds() {
		return numberOfRounds;
	}

	@Override
	public Double getGunCoolingRate() {
		return gunCoolingRate;
	}

	@Override
	public Integer getInactiveTurns() {
		return inactiveTurns;
	}

	@Override
	public Integer getTurnTimeout() {
		return turnTimeout;
	}

	@Override
	public Integer getReadyTimeout() {
		return readyTimeout;
	}

	@Override
	public Integer getDelayedObserverTurns() {
		return delayedObserverTurns;
	}

	@Override
	public boolean isGameTypeFixed() {
		return gameTypeFixed;
	}

	@Override
	public boolean isArenaWidthFixed() {
		return arenaWidthFixed;
	}

	@Override
	public boolean isArenaHeightFixed() {
		return arenaHeightFixed;
	}

	@Override
	public boolean isMinNumberOfParticipantsFixed() {
		return minNumberOfParticipantsFixed;
	}

	@Override
	public boolean isMaxNumberOfParticipantsFixed() {
		return maxNumberOfParticipantsFixed;
	}

	@Override
	public boolean isNumberOfRoundsFixed() {
		return numberOfRoundsFixed;
	}

	@Override
	public boolean isGunCoolingRateFixed() {
		return gunCoolingRateFixed;
	}

	@Override
	public boolean isInactiveTurnsFixed() {
		return inactiveTurnsFixed;
	}

	@Override
	public boolean isTurnTimeoutFixed() {
		return turnTimeoutFixed;
	}

	@Override
	public boolean isReadyTimeoutFixed() {
		return readyTimeoutFixed;
	}

	@Override
	public boolean isDelayedObserverTurnsFixed() {
		return delayedObserverTurnsFixed;
	}
}