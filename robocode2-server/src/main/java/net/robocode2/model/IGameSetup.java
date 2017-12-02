package net.robocode2.model;

/**
 * Game setup interface.
 * 
 * @author Flemming N. Larsen
 */
public interface IGameSetup {

	/** Default game type */
	static String DEFAULT_GAME_TYPE = "melee";
	/** Default arena width */
	static int DEFAULT_ARENA_WIDTH = 800;
	/** Default arena height */
	static int DEFAULT_ARENA_HEIGHT = 600;
	/** Default minimum number of bot participants */
	static int DEFAULT_MIN_NUMBER_OF_PARTICIPANTS = 2;
	/** Default maximum number of bot participants */
	static Integer DEFAULT_MAX_NUMBER_OF_PARTICIPANTS = null;
	/** Default number of rounds */
	static int DEFAULT_NUMBER_OF_ROUNDS = 10;
	/** Default gun cooling rate */
	static double DEFAULT_GUN_COOLING_RATE = 0.1;
	/** Default number of allowed inactivity turns */
	static int DEFAULT_INACTIVITY_TURNS = 450;
	/** Default turn timeout in milliseconds */
	static int DEFAULT_TURN_TIMEOUT = 100;
	/** Default ready timeout in milliseconds */
	static int DEFAULT_READY_TIMEOUT = 10_000;
	/** Default number of delayed turns for observers */
	static int DEFAULT_DELAYED_OBSERVER_TURNS = 10;

	/** Returns the game type */
	String getGameType();

	/** Returns the arena width */
	Integer getArenaWidth();

	/** Returns the arena height */
	Integer getArenaHeight();

	/** Returns the minimum number of bot participants */
	Integer getMinNumberOfParticipants();

	/** Returns the maximum number of bot participants */
	Integer getMaxNumberOfParticipants();

	/** Returns the number of rounds */
	Integer getNumberOfRounds();

	/** Returns the gun cooling rate */
	Double getGunCoolingRate();

	/** Returns the number of allowed inactivity turns */
	Integer getInactivityTurns();

	/** Returns the turn timeout in milliseconds */
	Integer getTurnTimeout();

	/** Returns the ready timeout in milliseconds */
	Integer getReadyTimeout();

	/** Returns the number of delayed turns for observers */
	Integer getDelayedObserverTurns();

	/**
	 * Checks if the arena width is locked.
	 * 
	 * @return true if the arena width is locked; false otherwise
	 */
	Boolean isArenaWidthLocked();

	/**
	 * Checks if the arena height is locked.
	 * 
	 * @return true if the arena height is locked; false otherwise
	 */
	Boolean isArenaHeightLocked();

	/**
	 * Checks if the minimum number of bot participant is locked.
	 * 
	 * @return true if the minimum number of bot participant is locked.; false otherwise
	 */
	Boolean isMinNumberOfParticipantsLocked();

	/**
	 * Checks if the maximum number of bot participant is locked.
	 * 
	 * @return true if the maximum number of bot participant is locked.; false otherwise
	 */
	Boolean isMaxNumberOfParticipantsLocked();

	/**
	 * Checks if the number of rounds is locked.
	 * 
	 * @return true if the number of rounds is locked; false otherwise
	 */
	Boolean isNumberOfRoundsLocked();

	/**
	 * Checks if the gun cooling rate is locked.
	 * 
	 * @return true if the the gun cooling rate is locked; false otherwise
	 */
	Boolean isGunCoolingRateLocked();

	/**
	 * Check if the number of allowed inactivity turns is locked.
	 * 
	 * @return true if the number of allowed inactivity turns is locked; false otherwise
	 */
	Boolean isInactiveTurnsLocked();

	/**
	 * Checks if the turn timeout is locked.
	 * 
	 * @return true if the the turn timeout is locked; false otherwise
	 */
	Boolean isTurnTimeoutLocked();

	/**
	 * Checks if the ready timeout is locked.
	 * 
	 * @return true if the the ready timeout is locked; false otherwise
	 */
	Boolean isReadyTimeoutLocked();

	/**
	 * Checks if the number of delayed turns for observers is locked.
	 * 
	 * @return true if the number of delayed turns for observers is locked; false otherwise
	 */
	Boolean isDelayedObserverTurnsLocked();
}