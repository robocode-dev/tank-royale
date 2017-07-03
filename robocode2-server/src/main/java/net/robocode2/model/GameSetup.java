package net.robocode2.model;

/**
 * Mutable game setup.
 * 
 * @author Flemming N. Larsen
 */
public class GameSetup implements IGameSetup {

	/** Game type */
	private String gameType = DEFAULT_GAME_TYPE;
	/** Arena width */
	private int arenaWidth = DEFAULT_ARENA_WIDTH;
	/** Arena hieght */
	private int arenaHeight = DEFAULT_ARENA_HEIGHT;
	/** Minimum number of bot participants */
	private int minNumberOfParticipants = DEFAULT_MIN_NUMBER_OF_PARTICIPANTS;
	/** Maximum number of bot participants */
	private Integer maxNumberOfParticipants = DEFAULT_MAX_NUMBER_OF_PARTICIPANTS;
	/** Number of rounds */
	private int numberOfRounds = DEFAULT_NUMBER_OF_ROUNDS;
	/** Gun cooling rate */
	private double gunCoolingRate = DEFAULT_GUN_COOLING_RATE;
	/** Number of allowed inactivity turns */
	private int inactivityTurns = DEFAULT_INACTIVITY_TURNS;
	/** Turn timeout in milliseconds */
	private int turnTimeout = DEFAULT_TURN_TIMEOUT;
	/** Ready timeout in milliseconds */
	private int readyTimeout = DEFAULT_READY_TIMEOUT;
	/** Number of delayed turns for observers */
	private int delayedObserverTurns = DEFAULT_DELAYED_OBSERVER_TURNS;

	/** Flag specifying if the arena width is fixed */
	private Boolean arenaWidthFixed;
	/** Flag specifying if the arena height is fixed */
	private Boolean arenaHeightFixed;
	/** Flag specifying if the minimum number of bot participants is fixed */
	private Boolean minNumberOfParticipantsFixed;
	/** Flag specifying if the maximum number of bot participants is fixed */
	private Boolean maxNumberOfParticipantsFixed;
	/** Flag specifying if the number of rounds is fixed */
	private Boolean numberOfRoundsFixed;
	/** Flag specifying if the gun cooling rate is fixed */
	private Boolean gunCoolingRateFixed;
	/** Flag specifying if the number of allowed inactivity turns is fixed */
	private Boolean inactivityTurnsFixed;
	/** Flag specifying if the turn timeout is fixed */
	private Boolean turnTimeoutFixed;
	/** Flag specifying if the ready timeout is fixed */
	private Boolean readyTimeoutFixed;
	/** Flag specifying if the number of delayed turns for observers is fixed */
	private Boolean delayedObserverTurnsFixed;

	/**
	 * Creates a mutable game setup that needs to be initialized
	 */
	public GameSetup() {
	}

	/**
	 * Creates a mutable game setup that is initialized by another game setup.
	 * 
	 * @param gameSetup
	 *            is the other game setup, which is deep copied into this game setup.
	 */
	public GameSetup(IGameSetup gameSetup) {
		gameType = gameSetup.getGameType();
		arenaWidth = gameSetup.getArenaWidth();
		arenaHeight = gameSetup.getArenaHeight();
		minNumberOfParticipants = gameSetup.getMinNumberOfParticipants();
		maxNumberOfParticipants = gameSetup.getMaxNumberOfParticipants();
		numberOfRounds = gameSetup.getNumberOfRounds();
		gunCoolingRate = gameSetup.getGunCoolingRate();
		inactivityTurns = gameSetup.getInactivityTurns();
		turnTimeout = gameSetup.getTurnTimeout();
		readyTimeout = gameSetup.getReadyTimeout();
		delayedObserverTurns = gameSetup.getDelayedObserverTurns();

		arenaWidthFixed = gameSetup.isArenaWidthFixed();
		arenaHeightFixed = gameSetup.isArenaHeightFixed();
		minNumberOfParticipantsFixed = gameSetup.isMinNumberOfParticipantsFixed();
		maxNumberOfParticipantsFixed = gameSetup.isMaxNumberOfParticipantsFixed();
		numberOfRoundsFixed = gameSetup.isNumberOfRoundsFixed();
		gunCoolingRateFixed = gameSetup.isGunCoolingRateFixed();
		inactivityTurnsFixed = gameSetup.isInactiveTurnsFixed();
		turnTimeoutFixed = gameSetup.isTurnTimeoutFixed();
		readyTimeoutFixed = gameSetup.isReadyTimeoutFixed();
		delayedObserverTurnsFixed = gameSetup.isDelayedObserverTurnsFixed();
	}

	/**
	 * Creates an immutable game setup that is a copy of this game setup.
	 * 
	 * @return an immutable game setup
	 */
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
	public Integer getInactivityTurns() {
		return inactivityTurns;
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

	/**
	 * Sets the game type.
	 * 
	 * @param gameType
	 *            is the game type
	 */
	public void setGameType(String gameType) {
		if (gameType == null) {
			gameType = DEFAULT_GAME_TYPE;
		} else if (gameType.trim().length() == 0) {
			throw new IllegalArgumentException("gameType cannot be empty");
		}
		this.gameType = gameType;
	}

	/**
	 * Sets the arena width
	 * 
	 * @param arenaWidth
	 *            is the arena width
	 */
	public void setArenaWidth(Integer arenaWidth) {
		if (arenaWidth == null) {
			arenaWidth = DEFAULT_ARENA_WIDTH;
		} else {
			if (arenaWidth < IRuleConstants.ARENA_MIN_WIDTH) {
				throw new IllegalArgumentException("arenaWidth cannot be less than " + IRuleConstants.ARENA_MIN_WIDTH);
			}
			if (arenaWidth > IRuleConstants.ARENA_MAX_WIDTH) {
				throw new IllegalArgumentException(
						"arenaWidth cannot be greater than " + IRuleConstants.ARENA_MAX_WIDTH);
			}
		}
		this.arenaWidth = arenaWidth;
	}

	/**
	 * Sets the arena height
	 * 
	 * @param arenaHeight
	 *            is the arena height
	 */
	public void setArenaHeight(Integer arenaHeight) {
		if (arenaHeight == null) {
			arenaHeight = DEFAULT_ARENA_HEIGHT;
		} else {
			if (arenaHeight < IRuleConstants.ARENA_MIN_HEIGHT) {
				throw new IllegalArgumentException(
						"arenaHeight cannot be less than " + IRuleConstants.ARENA_MIN_HEIGHT);
			}
			if (arenaHeight > IRuleConstants.ARENA_MAX_HEIGHT) {
				throw new IllegalArgumentException(
						"arenaHeight cannot be greater than " + IRuleConstants.ARENA_MAX_HEIGHT);
			}
		}
		this.arenaHeight = arenaHeight;
	}

	/**
	 * Sets the minimum number of participant bots.
	 * 
	 * @param minNumberOfParticipants
	 *            is the minimum number of participant bots.
	 */
	public void setMinNumberOfParticipants(Integer minNumberOfParticipants) {
		if (minNumberOfParticipants == null) {
			minNumberOfParticipants = DEFAULT_MIN_NUMBER_OF_PARTICIPANTS;
		} else if (minNumberOfParticipants < 1) {
			throw new IllegalArgumentException("minNumberOfParticipants cannot be less than 1");
		}
		this.minNumberOfParticipants = minNumberOfParticipants;
	}

	/**
	 * Sets the maximum number of participant bots.
	 * 
	 * @param maxNumberOfParticipants
	 *            is the maximum number of participant bots.
	 */
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

	/**
	 * Sets the number of rounds.
	 * 
	 * @param numberOfRounds
	 *            is the number of rounds
	 */
	public void setNumberOfRounds(Integer numberOfRounds) {
		if (numberOfRounds == null) {
			numberOfRounds = DEFAULT_NUMBER_OF_ROUNDS;
		} else if (numberOfRounds < 1) {
			throw new IllegalArgumentException("numberOfRounds cannot be less than 1");
		}
		this.numberOfRounds = numberOfRounds;
	}

	/**
	 * Sets the gun cooling rate.
	 * 
	 * @param gunCoolingRate
	 *            is the gun cooling rate.
	 */
	public void setGunCoolingRate(Double gunCoolingRate) {
		if (gunCoolingRate == null) {
			gunCoolingRate = DEFAULT_GUN_COOLING_RATE;
		} else {
			if (gunCoolingRate < IRuleConstants.MIN_GUN_COOLING_RATE) {
				throw new IllegalArgumentException(
						"gunCoolingRate cannot be less than " + IRuleConstants.MIN_GUN_COOLING_RATE);
			}
			if (gunCoolingRate > IRuleConstants.MAX_GUN_COOLING_RATE) {
				throw new IllegalArgumentException(
						"gunCoolingRate cannot be greater than " + IRuleConstants.MAX_GUN_COOLING_RATE);
			}
		}
		this.gunCoolingRate = gunCoolingRate;
	}

	/**
	 * Sets the number of allowed inactivity turns.
	 * 
	 * @param inactivityTurns
	 *            is the number of allowed inactivity turns.
	 */
	public void setInactivityTurns(Integer inactivityTurns) {
		if (inactivityTurns == null) {
			inactivityTurns = DEFAULT_INACTIVITY_TURNS;
		} else if (inactivityTurns < 0) {
			throw new IllegalArgumentException("inactivityTurns cannot be less than 0");
		}
		this.inactivityTurns = inactivityTurns;
	}

	/**
	 * Sets the turn timeout.
	 * 
	 * @param turnTimeout
	 *            is the turn timeout in milliseconds.
	 */
	public void setTurnTimeout(Integer turnTimeout) {
		if (turnTimeout == null) {
			turnTimeout = DEFAULT_TURN_TIMEOUT;
		} else if (turnTimeout < 0) {
			throw new IllegalArgumentException("turnTimeout cannot be less than 0");
		}
		this.turnTimeout = turnTimeout;
	}

	/**
	 * Sets the ready timeout.
	 * 
	 * @param readyTimeout
	 *            is the ready timeout in milliseconds.
	 */
	public void setReadyTimeout(Integer readyTimeout) {
		if (readyTimeout == null) {
			readyTimeout = DEFAULT_READY_TIMEOUT;
		} else if (readyTimeout < 0) {
			throw new IllegalArgumentException("readyTimeout cannot be less than 0");
		}
		this.readyTimeout = readyTimeout;
	}

	/**
	 * Sets the number of delayed turns for observers
	 * 
	 * @param delayedObserverTurns
	 *            is the number of delayed turns for observers
	 */
	public void setDelayedObserverTurns(Integer delayedObserverTurns) {
		if (delayedObserverTurns == null) {
			delayedObserverTurns = DEFAULT_DELAYED_OBSERVER_TURNS;
		} else if (delayedObserverTurns < 0) {
			throw new IllegalArgumentException("delayedObserverTurns cannot be less than 0");
		}
		this.delayedObserverTurns = delayedObserverTurns;
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
		return inactivityTurnsFixed;
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

	/**
	 * Sets the flag that the arena width is fixed.
	 * 
	 * @param fixed
	 *            is a flag specifying if the arena width is fixed
	 */
	public void setArenaWidthFixed(Boolean fixed) {
		this.arenaWidthFixed = fixed;
	}

	/**
	 * Sets the flag that the arena height is fixed.
	 * 
	 * @param fixed
	 *            is a flag specifying if the arena height is fixed
	 */
	public void setArenaHeightFixed(Boolean fixed) {
		this.arenaHeightFixed = fixed;
	}

	/**
	 * Sets the flag that the minimum number of participant bots is fixed.
	 * 
	 * @param fixed
	 *            is a flag specifying if the minimum number of participant bots is fixed.
	 */
	public void setMinNumberOfParticipantsFixed(Boolean fixed) {
		this.minNumberOfParticipantsFixed = fixed;
	}

	/**
	 * Sets the flag that the maximum number of participant bots is fixed.
	 * 
	 * @param fixed
	 *            is a flag specifying if the maximum number of participant bots is fixed.
	 */
	public void setMaxNumberOfParticipantsFixed(Boolean fixed) {
		this.maxNumberOfParticipantsFixed = fixed;
	}

	/**
	 * Sets the flag that the number of rounds is fixed.
	 * 
	 * @param fixed
	 *            is a flag specifying if the number of rounds is fixed.
	 */
	public void setNumberOfRoundsFixed(Boolean fixed) {
		this.numberOfRoundsFixed = fixed;
	}

	/**
	 * Sets the flag that the gun cooling rate is fixed.
	 * 
	 * @param fixed
	 *            is a flag specifying if the gun cooling rate is fixed.
	 */
	public void setGunCoolingRateFixed(Boolean fixed) {
		this.gunCoolingRateFixed = fixed;
	}

	/**
	 * Sets the flag that the number of allowed inactivity turns is fixed.
	 * 
	 * @param fixed
	 *            is a flag specifying if the number of allowed inactivity turns is fixed.
	 */
	public void setInactiveTurnsFixed(Boolean fixed) {
		this.inactivityTurnsFixed = fixed;
	}

	/**
	 * Sets the flag that the turn timeout is fixed.
	 * 
	 * @param fixed
	 *            is a flag specifying if the turn timeout is fixed.
	 */
	public void setTurnTimeoutFixed(Boolean fixed) {
		this.turnTimeoutFixed = fixed;
	}

	/**
	 * Sets the flag that the ready timeout is fixed.
	 * 
	 * @param fixed
	 *            is a flag specifying if the ready timeout is fixed.
	 */
	public void setReadyTimeoutFixed(Boolean fixed) {
		this.readyTimeoutFixed = fixed;
	}

	/**
	 * Sets the flag that the number of allowed inactivity turns is fixed.
	 * 
	 * @param fixed
	 *            is a flag specifying if the number of allowed inactivity turns is fixed.
	 */
	public void setDelayedObserverTurnsFixed(Boolean fixed) {
		this.delayedObserverTurnsFixed = fixed;
	}
}