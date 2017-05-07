package net.robocode2.model;

public interface IGameSetup {

	static String DEFAULT_GAME_TYPE = "melee";
	static int DEFAULT_ARENA_WIDTH = 800;
	static int DEFAULT_ARENA_HEIGHT = 600;
	static int DEFAULT_MIN_NUMBER_OF_PARTICIPANTS = 2;
	static Integer DEFAULT_MAX_NUMBER_OF_PARTICIPANTS = null;
	static int DEFAULT_NUMBER_OF_ROUNDS = 10;
	static double DEFAULT_GUN_COOLING_RATE = 0.1;
	static int DEFAULT_INACTIVITY_TURNS = 450;
	static int DEFAULT_TURN_TIMEOUT = 100;
	static int DEFAULT_READY_TIMEOUT = 10_000;
	static int DEFAULT_DELAYED_OBSERVER_TURNS = 10;

	default String getGameType() {
		return DEFAULT_GAME_TYPE;
	}

	default Integer getArenaWidth() {
		return DEFAULT_ARENA_WIDTH;
	}

	default Integer getArenaHeight() {
		return DEFAULT_ARENA_HEIGHT;
	}

	default Integer getMinNumberOfParticipants() {
		return DEFAULT_MIN_NUMBER_OF_PARTICIPANTS;
	}

	default Integer getMaxNumberOfParticipants() {
		return DEFAULT_MAX_NUMBER_OF_PARTICIPANTS;
	}

	default Integer getNumberOfRounds() {
		return DEFAULT_NUMBER_OF_ROUNDS;
	}

	default Double getGunCoolingRate() {
		return DEFAULT_GUN_COOLING_RATE;
	}

	default Integer getInactiveTurns() {
		return DEFAULT_INACTIVITY_TURNS;
	}

	default Integer getTurnTimeout() {
		return DEFAULT_TURN_TIMEOUT;
	}

	default Integer getReadyTimeout() {
		return DEFAULT_READY_TIMEOUT;
	}

	default Integer getDelayedObserverTurns() {
		return DEFAULT_DELAYED_OBSERVER_TURNS;
	}

	boolean isGameTypeFixed();

	boolean isArenaWidthFixed();

	boolean isArenaHeightFixed();

	boolean isMinNumberOfParticipantsFixed();

	boolean isMaxNumberOfParticipantsFixed();

	boolean isNumberOfRoundsFixed();

	boolean isGunCoolingRateFixed();

	boolean isInactiveTurnsFixed();

	boolean isTurnTimeoutFixed();

	boolean isReadyTimeoutFixed();

	boolean isDelayedObserverTurnsFixed();
}