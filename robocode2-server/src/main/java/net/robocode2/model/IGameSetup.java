package net.robocode2.model;

public interface IGameSetup {

	String getGameType();

	int getArenaWidth();

	int getArenaHeight();

	int getMinNumberOfParticipants();

	Integer getMaxNumberOfParticipants();

	int getNumberOfRounds();

	double getGunCoolingRate();

	int getInactiveTurns();

	int getTurnTimeout();

	int getReadyTimeout();

	int getNumberOfDelayedTurnsForObservers();
}