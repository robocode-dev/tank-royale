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

	private final Boolean arenaWidthFixed;
	private final Boolean arenaHeightFixed;
	private final Boolean minNumberOfParticipantsFixed;
	private final Boolean maxNumberOfParticipantsFixed;
	private final Boolean numberOfRoundsFixed;
	private final Boolean gunCoolingRateFixed;
	private final Boolean inactiveTurnsFixed;
	private final Boolean turnTimeoutFixed;
	private final Boolean readyTimeoutFixed;
	private final Boolean delayedObserverTurnsFixed;

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
	public Boolean isArenaWidthFixed() {
		return arenaWidthFixed;
	}

	@Override
	public Boolean isArenaHeightFixed() {
		return arenaHeightFixed;
	}

	@Override
	public Boolean isMinNumberOfParticipantsFixed() {
		return minNumberOfParticipantsFixed;
	}

	@Override
	public Boolean isMaxNumberOfParticipantsFixed() {
		return maxNumberOfParticipantsFixed;
	}

	@Override
	public Boolean isNumberOfRoundsFixed() {
		return numberOfRoundsFixed;
	}

	@Override
	public Boolean isGunCoolingRateFixed() {
		return gunCoolingRateFixed;
	}

	@Override
	public Boolean isInactiveTurnsFixed() {
		return inactiveTurnsFixed;
	}

	@Override
	public Boolean isTurnTimeoutFixed() {
		return turnTimeoutFixed;
	}

	@Override
	public Boolean isReadyTimeoutFixed() {
		return readyTimeoutFixed;
	}

	@Override
	public Boolean isDelayedObserverTurnsFixed() {
		return delayedObserverTurnsFixed;
	}
}