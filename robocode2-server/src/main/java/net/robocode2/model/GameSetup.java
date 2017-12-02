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

	/** Flag specifying if the arena width is locked */
	private Boolean arenaWidthLocked;
	/** Flag specifying if the arena height is locked */
	private Boolean arenaHeightLocked;
	/** Flag specifying if the minimum number of bot participants is locked */
	private Boolean minNumberOfParticipantsLocked;
	/** Flag specifying if the maximum number of bot participants is locked */
	private Boolean maxNumberOfParticipantsLocked;
	/** Flag specifying if the number of rounds is locked */
	private Boolean numberOfRoundsLocked;
	/** Flag specifying if the gun cooling rate is locked */
	private Boolean gunCoolingRateLocked;
	/** Flag specifying if the number of allowed inactivity turns is locked */
	private Boolean inactivityTurnsLocked;
	/** Flag specifying if the turn timeout is locked */
	private Boolean turnTimeoutLocked;
	/** Flag specifying if the ready timeout is locked */
	private Boolean readyTimeoutLocked;
	/** Flag specifying if the number of delayed turns for observers is locked */
	private Boolean delayedObserverTurnsLocked;

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

		arenaWidthLocked = gameSetup.isArenaWidthLocked();
		arenaHeightLocked = gameSetup.isArenaHeightLocked();
		minNumberOfParticipantsLocked = gameSetup.isMinNumberOfParticipantsLocked();
		maxNumberOfParticipantsLocked = gameSetup.isMaxNumberOfParticipantsLocked();
		numberOfRoundsLocked = gameSetup.isNumberOfRoundsLocked();
		gunCoolingRateLocked = gameSetup.isGunCoolingRateLocked();
		inactivityTurnsLocked = gameSetup.isInactiveTurnsLocked();
		turnTimeoutLocked = gameSetup.isTurnTimeoutLocked();
		readyTimeoutLocked = gameSetup.isReadyTimeoutLocked();
		delayedObserverTurnsLocked = gameSetup.isDelayedObserverTurnsLocked();
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
			if (arenaWidth < IRuleConstants.ARENA_MIN_SIZE) {
				throw new IllegalArgumentException("arenaWidth cannot be less than " + IRuleConstants.ARENA_MIN_SIZE);
			}
			if (arenaWidth > IRuleConstants.ARENA_MAX_SIZE) {
				throw new IllegalArgumentException(
						"arenaWidth cannot be greater than " + IRuleConstants.ARENA_MAX_SIZE);
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
			if (arenaHeight < IRuleConstants.ARENA_MIN_SIZE) {
				throw new IllegalArgumentException("arenaHeight cannot be less than " + IRuleConstants.ARENA_MIN_SIZE);
			}
			if (arenaHeight > IRuleConstants.ARENA_MAX_SIZE) {
				throw new IllegalArgumentException(
						"arenaHeight cannot be greater than " + IRuleConstants.ARENA_MAX_SIZE);
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
	public Boolean isArenaWidthLocked() {
		return arenaWidthLocked;
	}

	@Override
	public Boolean isArenaHeightLocked() {
		return arenaHeightLocked;
	}

	@Override
	public Boolean isMinNumberOfParticipantsLocked() {
		return minNumberOfParticipantsLocked;
	}

	@Override
	public Boolean isMaxNumberOfParticipantsLocked() {
		return maxNumberOfParticipantsLocked;
	}

	@Override
	public Boolean isNumberOfRoundsLocked() {
		return numberOfRoundsLocked;
	}

	@Override
	public Boolean isGunCoolingRateLocked() {
		return gunCoolingRateLocked;
	}

	@Override
	public Boolean isInactiveTurnsLocked() {
		return inactivityTurnsLocked;
	}

	@Override
	public Boolean isTurnTimeoutLocked() {
		return turnTimeoutLocked;
	}

	@Override
	public Boolean isReadyTimeoutLocked() {
		return readyTimeoutLocked;
	}

	@Override
	public Boolean isDelayedObserverTurnsLocked() {
		return delayedObserverTurnsLocked;
	}

	/**
	 * Sets the flag that the arena width is locked.
	 * 
	 * @param locked
	 *            is a flag specifying if the arena width is locked
	 */
	public void setArenaWidthLocked(Boolean locked) {
		this.arenaWidthLocked = locked;
	}

	/**
	 * Sets the flag that the arena height is locked.
	 * 
	 * @param locked
	 *            is a flag specifying if the arena height is locked
	 */
	public void setArenaHeightLocked(Boolean locked) {
		this.arenaHeightLocked = locked;
	}

	/**
	 * Sets the flag that the minimum number of participant bots is locked.
	 * 
	 * @param locked
	 *            is a flag specifying if the minimum number of participant bots is locked.
	 */
	public void setMinNumberOfParticipantsLocked(Boolean locked) {
		this.minNumberOfParticipantsLocked = locked;
	}

	/**
	 * Sets the flag that the maximum number of participant bots is locked.
	 * 
	 * @param locked
	 *            is a flag specifying if the maximum number of participant bots is locked.
	 */
	public void setMaxNumberOfParticipantsLocked(Boolean locked) {
		this.maxNumberOfParticipantsLocked = locked;
	}

	/**
	 * Sets the flag that the number of rounds is locked.
	 * 
	 * @param locked
	 *            is a flag specifying if the number of rounds is locked.
	 */
	public void setNumberOfRoundsLocked(Boolean locked) {
		this.numberOfRoundsLocked = locked;
	}

	/**
	 * Sets the flag that the gun cooling rate is locked.
	 * 
	 * @param locked
	 *            is a flag specifying if the gun cooling rate is locked.
	 */
	public void setGunCoolingRateLocked(Boolean locked) {
		this.gunCoolingRateLocked = locked;
	}

	/**
	 * Sets the flag that the number of allowed inactivity turns is locked.
	 * 
	 * @param locked
	 *            is a flag specifying if the number of allowed inactivity turns is locked.
	 */
	public void setInactiveTurnsLocked(Boolean locked) {
		this.inactivityTurnsLocked = locked;
	}

	/**
	 * Sets the flag that the turn timeout is locked.
	 * 
	 * @param locked
	 *            is a flag specifying if the turn timeout is locked.
	 */
	public void setTurnTimeoutLocked(Boolean locked) {
		this.turnTimeoutLocked = locked;
	}

	/**
	 * Sets the flag that the ready timeout is locked.
	 * 
	 * @param locked
	 *            is a flag specifying if the ready timeout is locked.
	 */
	public void setReadyTimeoutLocked(Boolean locked) {
		this.readyTimeoutLocked = locked;
	}

	/**
	 * Sets the flag that the number of allowed inactivity turns is locked.
	 * 
	 * @param locked
	 *            is a flag specifying if the number of allowed inactivity turns is locked.
	 */
	public void setDelayedObserverTurnsLocked(Boolean locked) {
		this.delayedObserverTurnsLocked = locked;
	}
}