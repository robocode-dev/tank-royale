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

	String getGameType();

	Integer getArenaWidth();

	Integer getArenaHeight();

	Integer getMinNumberOfParticipants();

	Integer getMaxNumberOfParticipants();

	Integer getNumberOfRounds();

	Double getGunCoolingRate();

	Integer getInactiveTurns();

	Integer getTurnTimeout();

	Integer getReadyTimeout();

	Integer getDelayedObserverTurns();

	Boolean isArenaWidthFixed();

	Boolean isArenaHeightFixed();

	Boolean isMinNumberOfParticipantsFixed();

	Boolean isMaxNumberOfParticipantsFixed();

	Boolean isNumberOfRoundsFixed();

	Boolean isGunCoolingRateFixed();

	Boolean isInactiveTurnsFixed();

	Boolean isTurnTimeoutFixed();

	Boolean isReadyTimeoutFixed();

	Boolean isDelayedObserverTurnsFixed();
}