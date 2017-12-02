package net.robocode2.model;

import static org.junit.Assert.assertEquals;

import org.junit.BeforeClass;
import org.junit.Test;

public class ImmutableGameSetupTest {

	static GameSetup initializedGameSetup;

	@BeforeClass
	public static void initialize() {
		GameSetup setup = new GameSetup();

		setup.setGameType("game type");
		setup.setArenaWidth(1234);
		setup.setArenaHeightLocked(true);
		setup.setArenaHeight(3456);
		setup.setArenaHeightLocked(true);
		setup.setMinNumberOfParticipants(7);
		setup.setMinNumberOfParticipantsLocked(true);
		setup.setMaxNumberOfParticipants(53);
		setup.setMaxNumberOfParticipantsLocked(true);
		setup.setNumberOfRounds(78);
		setup.setNumberOfRoundsLocked(true);
		setup.setGunCoolingRate(0.465);
		setup.setGunCoolingRateLocked(true);
		setup.setInactivityTurns(451);
		setup.setInactiveTurnsLocked(true);
		setup.setTurnTimeout(3517);
		setup.setTurnTimeoutLocked(true);
		setup.setReadyTimeout(8462);
		setup.setReadyTimeoutLocked(true);
		setup.setDelayedObserverTurns(56);
		setup.setDelayedObserverTurnsLocked(true);

		initializedGameSetup = setup;
	}

	@Test
	public void constructorIGameSetup() {
		ImmutableGameSetup setup = new ImmutableGameSetup(initializedGameSetup);

		assertEquals(initializedGameSetup.getGameType(), setup.getGameType());
		assertEquals(initializedGameSetup.getArenaWidth(), setup.getArenaWidth());
		assertEquals(initializedGameSetup.isArenaWidthLocked(), setup.isArenaWidthLocked());
		assertEquals(initializedGameSetup.getArenaHeight(), setup.getArenaHeight());
		assertEquals(initializedGameSetup.isArenaHeightLocked(), setup.isArenaHeightLocked());
		assertEquals(initializedGameSetup.getMinNumberOfParticipants(), setup.getMinNumberOfParticipants());
		assertEquals(initializedGameSetup.isMinNumberOfParticipantsLocked(), setup.isMinNumberOfParticipantsLocked());
		assertEquals(initializedGameSetup.getMaxNumberOfParticipants(), setup.getMaxNumberOfParticipants());
		assertEquals(initializedGameSetup.isMaxNumberOfParticipantsLocked(), setup.isMaxNumberOfParticipantsLocked());
		assertEquals(initializedGameSetup.getNumberOfRounds(), setup.getNumberOfRounds());
		assertEquals(initializedGameSetup.isNumberOfRoundsLocked(), setup.isNumberOfRoundsLocked());
		assertEquals(initializedGameSetup.getGunCoolingRate(), setup.getGunCoolingRate());
		assertEquals(initializedGameSetup.isGunCoolingRateLocked(), setup.isGunCoolingRateLocked());
		assertEquals(initializedGameSetup.getInactivityTurns(), setup.getInactivityTurns());
		assertEquals(initializedGameSetup.isInactiveTurnsLocked(), setup.isInactiveTurnsLocked());
		assertEquals(initializedGameSetup.getTurnTimeout(), setup.getTurnTimeout());
		assertEquals(initializedGameSetup.isTurnTimeoutLocked(), setup.isTurnTimeoutLocked());
		assertEquals(initializedGameSetup.getReadyTimeout(), setup.getReadyTimeout());
		assertEquals(initializedGameSetup.isReadyTimeoutLocked(), setup.isReadyTimeoutLocked());
		assertEquals(initializedGameSetup.getDelayedObserverTurns(), setup.getDelayedObserverTurns());
		assertEquals(initializedGameSetup.isDelayedObserverTurnsLocked(), setup.isDelayedObserverTurnsLocked());
	}
}
