package net.robocode2.model;

/**
 * Immutable game setup.
 * 
 * @author Flemming N. Larsen
 */
public final class ImmutableGameSetup implements IGameSetup {

	/** Game type */
	private final String gameType;
	/** Arena width */
	private final int arenaWidth;
	/** Arena hieght */
	private final int arenaHeight;
	/** Minimum number of bot participants */
	private final int minNumberOfParticipants;
	/** Maximum number of bot participants */
	private final Integer maxNumberOfParticipants;
	/** Number of rounds */
	private final int numberOfRounds;
	/** Gun cooling rate */
	private final double gunCoolingRate;
	/** Number of allowed inactivity turns */
	private final int inactivityTurns;
	/** Turn timeout in milliseconds */
	private final int turnTimeout;
	/** Ready timeout in milliseconds */
	private final int readyTimeout;
	/** Number of delayed turns for observers */
	private final int delayedObserverTurns;

	/** Flag specifying if the arena width is locked */
	private final Boolean arenaWidthLocked;
	/** Flag specifying if the arena height is locked */
	private final Boolean arenaHeightLocked;
	/** Flag specifying if the minimum number of bot participants is locked */
	private final Boolean minNumberOfParticipantsLocked;
	/** Flag specifying if the maximum number of bot participants is locked */
	private final Boolean maxNumberOfParticipantsLocked;
	/** Flag specifying if the number of rounds is locked */
	private final Boolean numberOfRoundsLocked;
	/** Flag specifying if the gun cooling rate is locked */
	private final Boolean gunCoolingRateLocked;
	/** Flag specifying if the number of allowed inactivity turns is locked */
	private final Boolean inactivityTurnsLocked;
	/** Flag specifying if the turn timeout is locked */
	private final Boolean turnTimeoutLocked;
	/** Flag specifying if the ready timeout is locked */
	private final Boolean readyTimeoutLocked;
	/** Flag specifying if the number of delayed turns for observers is locked */
	private final Boolean delayedObserverTurnsLocked;

	/**
	 * Creates a immutable game setup based on another game setup.
	 * 
	 * @param bot
	 *            is the game setup that is deep copied into this game setup.
	 */
	public ImmutableGameSetup(IGameSetup gameSetup) {
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
}