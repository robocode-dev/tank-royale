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

	/** Flag specifying if the arena width is fixed */
	private final Boolean arenaWidthFixed;
	/** Flag specifying if the arena height is fixed */
	private final Boolean arenaHeightFixed;
	/** Flag specifying if the minimum number of bot participants is fixed */
	private final Boolean minNumberOfParticipantsFixed;
	/** Flag specifying if the maximum number of bot participants is fixed */
	private final Boolean maxNumberOfParticipantsFixed;
	/** Flag specifying if the number of rounds is fixed */
	private final Boolean numberOfRoundsFixed;
	/** Flag specifying if the gun cooling rate is fixed */
	private final Boolean gunCoolingRateFixed;
	/** Flag specifying if the number of allowed inactivity turns is fixed */
	private final Boolean inactivityTurnsFixed;
	/** Flag specifying if the turn timeout is fixed */
	private final Boolean turnTimeoutFixed;
	/** Flag specifying if the ready timeout is fixed */
	private final Boolean readyTimeoutFixed;
	/** Flag specifying if the number of delayed turns for observers is fixed */
	private final Boolean delayedObserverTurnsFixed;

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
}