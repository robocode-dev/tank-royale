package net.robocode2.model;

public class GameSetup implements IGameSetup {

	private String gameType = DEFAULT_GAME_TYPE;
	private int arenaWidth = DEFAULT_ARENA_WIDTH;
	private int arenaHeight = DEFAULT_ARENA_HEIGHT;
	private int minNumberOfParticipants = DEFAULT_MIN_NUMBER_OF_PARTICIPANTS;
	private Integer maxNumberOfParticipants = DEFAULT_MAX_NUMBER_OF_PARTICIPANTS;
	private int numberOfRounds = DEFAULT_NUMBER_OF_ROUNDS;
	private double gunCoolingRate = DEFAULT_GUN_COOLING_RATE;
	private int inactiveTurns = DEFAULT_INACTIVITY_TURNS;
	private int turnTimeout = DEFAULT_TURN_TIMEOUT;
	private int readyTimeout = DEFAULT_READY_TIMEOUT;
	private int delayedObserverTurns = DEFAULT_DELAYED_OBSERVER_TURNS;

	private boolean gameTypeFixed;
	private boolean arenaWidthFixed;
	private boolean arenaHeightFixed;
	private boolean minNumberOfParticipantsFixed;
	private boolean maxNumberOfParticipantsFixed;
	private boolean numberOfRoundsFixed;
	private boolean gunCoolingRateFixed;
	private boolean inactiveTurnsFixed;
	private boolean turnTimeoutFixed;
	private boolean readyTimeoutFixed;
	private boolean delayedObserverTurnsFixed;

	public GameSetup() {
	}

	public GameSetup(IGameSetup gameSetup) {
		gameType = gameSetup.getGameType();
		arenaWidth = gameSetup.getArenaWidth();
		arenaHeight = gameSetup.getArenaWidth();
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

	public ImmutableGameSetup toImmutableGameSetup() {
		return new ImmutableGameSetup(this);
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

	public void setGameType(String gameType) {
		if (gameType == null) {
			gameType = DEFAULT_GAME_TYPE;
		} else if (gameType.trim().length() == 0) {
			throw new IllegalArgumentException("gameType cannot be empty");
		}
		this.gameType = gameType;
	}

	public void setArenaWidth(Integer arenaWidth) {
		if (arenaWidth == null) {
			arenaWidth = DEFAULT_ARENA_WIDTH;
		} else if (arenaWidth < 400) {
			throw new IllegalArgumentException("arenaWidth cannot be less than 400");
		}
		this.arenaWidth = arenaWidth;
	}

	public void setArenaHeight(Integer arenaHeight) {
		if (arenaHeight == null) {
			arenaHeight = DEFAULT_ARENA_HEIGHT;
		} else if (arenaHeight < 400) {
			throw new IllegalArgumentException("arenaHeight cannot be less than 400");
		}
		this.arenaHeight = arenaHeight;
	}

	public void setMinNumberOfParticipants(Integer minNumberOfParticipants) {
		if (minNumberOfParticipants == null) {
			minNumberOfParticipants = DEFAULT_MIN_NUMBER_OF_PARTICIPANTS;
		} else if (minNumberOfParticipants < 1) {
			throw new IllegalArgumentException("minNumberOfParticipants cannot be less than 1");
		}
		this.minNumberOfParticipants = minNumberOfParticipants;
	}

	public void setMaxNumberOfParticipants(Integer maxNumberOfParticipants) {
		if (maxNumberOfParticipants == null) {
			maxNumberOfParticipants = DEFAULT_MAX_NUMBER_OF_PARTICIPANTS;
		} else if (maxNumberOfParticipants < 1) {
			throw new IllegalArgumentException("maxNumberOfParticipants cannot be less than 1");
		} else if (maxNumberOfParticipants < minNumberOfParticipants) {
			throw new IllegalArgumentException("maxNumberOfParticipants cannot be less than minNumberOfParticipants");
		}
		this.maxNumberOfParticipants = maxNumberOfParticipants;
	}

	public void setNumberOfRounds(Integer numberOfRounds) {
		if (numberOfRounds == null) {
			numberOfRounds = DEFAULT_NUMBER_OF_ROUNDS;
		} else if (numberOfRounds < 1) {
			throw new IllegalArgumentException("numberOfRounds cannot be less than 1");
		}
		this.numberOfRounds = numberOfRounds;
	}

	public void setGunCoolingRate(Double gunCoolingRate) {
		if (gunCoolingRate == null) {
			gunCoolingRate = DEFAULT_GUN_COOLING_RATE;
		} else if (gunCoolingRate < 0.1) {
			throw new IllegalArgumentException("gunCoolingRate cannot be less than 0.1");
		} else if (gunCoolingRate > 0.7) {
			throw new IllegalArgumentException("gunCoolingRate cannot be greater than 0.7");
		}
		this.gunCoolingRate = gunCoolingRate;
	}

	public void setInactiveTurns(Integer inactiveTurns) {
		if (inactiveTurns == null) {
			inactiveTurns = DEFAULT_INACTIVITY_TURNS;
		} else if (inactiveTurns < 100) {
			throw new IllegalArgumentException("inactiveTurns cannot be less than 100");
		}
		this.inactiveTurns = inactiveTurns;
	}

	public void setTurnTimeout(Integer turnTimeout) {
		if (turnTimeout == null) {
			turnTimeout = DEFAULT_TURN_TIMEOUT;
		}
		this.turnTimeout = turnTimeout;
	}

	public void setReadyTimeout(Integer readyTimeout) {
		if (readyTimeout == null) {
			readyTimeout = DEFAULT_READY_TIMEOUT;
		}
		this.readyTimeout = readyTimeout;
	}

	public void setDelayedObserverTurns(Integer delayedObserverTurns) {
		if (delayedObserverTurns == null) {
			delayedObserverTurns = DEFAULT_DELAYED_OBSERVER_TURNS;
		}
		this.delayedObserverTurns = delayedObserverTurns;
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

	public void setGameTypeFixed(boolean gameTypeFixed) {
		this.gameTypeFixed = gameTypeFixed;
	}

	public void setArenaWidthFixed(boolean arenaWidthFixed) {
		this.arenaWidthFixed = arenaWidthFixed;
	}

	public void setArenaHeightFixed(boolean arenaHeightFixed) {
		this.arenaHeightFixed = arenaHeightFixed;
	}

	public void setMinNumberOfParticipantsFixed(boolean minNumberOfParticipantsFixed) {
		this.minNumberOfParticipantsFixed = minNumberOfParticipantsFixed;
	}

	public void setMaxNumberOfParticipantsFixed(boolean maxNumberOfParticipantsFixed) {
		this.maxNumberOfParticipantsFixed = maxNumberOfParticipantsFixed;
	}

	public void setNumberOfRoundsFixed(boolean numberOfRoundsFixed) {
		this.numberOfRoundsFixed = numberOfRoundsFixed;
	}

	public void setGunCoolingRateFixed(boolean gunCoolingRateFixed) {
		this.gunCoolingRateFixed = gunCoolingRateFixed;
	}

	public void setInactiveTurnsFixed(boolean inactiveTurnsFixed) {
		this.inactiveTurnsFixed = inactiveTurnsFixed;
	}

	public void setTurnTimeoutFixed(boolean turnTimeoutFixed) {
		this.turnTimeoutFixed = turnTimeoutFixed;
	}

	public void setReadyTimeoutFixed(boolean readyTimeoutFixed) {
		this.readyTimeoutFixed = readyTimeoutFixed;
	}

	public void setDelayedObserverTurnsFixed(boolean delayedObserverTurnsFixed) {
		this.delayedObserverTurnsFixed = delayedObserverTurnsFixed;
	}
}