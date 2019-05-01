package net.robocode2.model;

import lombok.Builder;
import lombok.Value;

/**
 * Game setup.
 * 
 * @author Flemming N. Larsen
 */
@Value
@Builder
public class GameSetup {

	/** Default game type */
	public static final String DEFAULT_GAME_TYPE = "melee";

	/** Default arena width */
	public static final int DEFAULT_ARENA_WIDTH = 800;

	/** Default arena height */
	public static final int DEFAULT_ARENA_HEIGHT = 600;

	/** Default minimum number of bot participants */
	public static final int DEFAULT_MIN_NUMBER_OF_PARTICIPANTS = 2;

	/** Default maximum number of bot participants */
	public static final Integer DEFAULT_MAX_NUMBER_OF_PARTICIPANTS = null;

	/** Default number of rounds */
	public static final int DEFAULT_NUMBER_OF_ROUNDS = 2; //35

	/** Default gun cooling rate */
	public static final double DEFAULT_GUN_COOLING_RATE = 0.1;

	/** Default number of allowed inactivity turns */
	public static final int DEFAULT_INACTIVITY_TURNS = 450;

	/** Default turn timeout in milliseconds */
	public static final int DEFAULT_TURN_TIMEOUT = 1; // 50

	/** Default ready timeout in milliseconds */
	public static final int DEFAULT_READY_TIMEOUT = 10_000;

	
	/** Game type */
	@Builder.Default String gameType = DEFAULT_GAME_TYPE;

	/** Arena width */
	@Builder.Default int arenaWidth = DEFAULT_ARENA_WIDTH;

	/** Arena height */
	@Builder.Default int arenaHeight = DEFAULT_ARENA_HEIGHT;

	/** Minimum number of bot participants */
	@Builder.Default int minNumberOfParticipants = DEFAULT_MIN_NUMBER_OF_PARTICIPANTS;

	/** Maximum number of bot participants */
	@Builder.Default Integer maxNumberOfParticipants = DEFAULT_MAX_NUMBER_OF_PARTICIPANTS;

	/** Number of rounds */
	@Builder.Default int numberOfRounds = DEFAULT_NUMBER_OF_ROUNDS;

	/** Gun cooling rate */
	@Builder.Default double gunCoolingRate = DEFAULT_GUN_COOLING_RATE;

	/** Number of allowed inactivity turns */
	@Builder.Default int inactivityTurns = DEFAULT_INACTIVITY_TURNS;

	/** Turn timeout in milliseconds */
	@Builder.Default int turnTimeout = DEFAULT_TURN_TIMEOUT;

	/** Ready timeout in milliseconds */
	@Builder.Default int readyTimeout = DEFAULT_READY_TIMEOUT;


	/** Flag specifying if the arena width is locked */
	boolean arenaWidthLocked;

	/** Flag specifying if the arena height is locked */
	boolean arenaHeightLocked;

	/** Flag specifying if the minimum number of bot participants is locked */
	boolean minNumberOfParticipantsLocked;

	/** Flag specifying if the maximum number of bot participants is locked */
	boolean maxNumberOfParticipantsLocked;

	/** Flag specifying if the number of rounds is locked */
	boolean numberOfRoundsLocked;

	/** Flag specifying if the gun cooling rate is locked */
	boolean gunCoolingRateLocked;

	/** Flag specifying if the number of allowed inactivity turns is locked */
	boolean inactivityTurnsLocked;

	/** Flag specifying if the turn timeout is locked */
	boolean turnTimeoutLocked;

	/** Flag specifying if the ready timeout is locked */
	boolean readyTimeoutLocked;
}