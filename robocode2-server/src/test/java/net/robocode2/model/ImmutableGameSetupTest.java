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
		setup.setArenaHeightFixed(true);
		setup.setArenaHeight(3456);
		setup.setArenaHeightFixed(true);
		setup.setMinNumberOfParticipants(7);
		setup.setMinNumberOfParticipantsFixed(true);
		setup.setMaxNumberOfParticipants(53);
		setup.setMaxNumberOfParticipantsFixed(true);
		setup.setNumberOfRounds(78);
		setup.setNumberOfRoundsFixed(true);
		setup.setGunCoolingRate(0.465);
		setup.setGunCoolingRateFixed(true);
		setup.setInactivityTurns(451);
		setup.setInactiveTurnsFixed(true);
		setup.setTurnTimeout(3517);
		setup.setTurnTimeoutFixed(true);
		setup.setReadyTimeout(8462);
		setup.setReadyTimeoutFixed(true);
		setup.setDelayedObserverTurns(56);
		setup.setDelayedObserverTurnsFixed(true);

		initializedGameSetup = setup;
	}

	@Test
	public void constructorIGameSetup() {
		ImmutableGameSetup setup = new ImmutableGameSetup(initializedGameSetup);

		assertEquals(initializedGameSetup.getGameType(), setup.getGameType());
		assertEquals(initializedGameSetup.getArenaWidth(), setup.getArenaWidth());
		assertEquals(initializedGameSetup.isArenaWidthFixed(), setup.isArenaWidthFixed());
		assertEquals(initializedGameSetup.getArenaHeight(), setup.getArenaHeight());
		assertEquals(initializedGameSetup.isArenaHeightFixed(), setup.isArenaHeightFixed());
		assertEquals(initializedGameSetup.getMinNumberOfParticipants(), setup.getMinNumberOfParticipants());
		assertEquals(initializedGameSetup.isMinNumberOfParticipantsFixed(), setup.isMinNumberOfParticipantsFixed());
		assertEquals(initializedGameSetup.getMaxNumberOfParticipants(), setup.getMaxNumberOfParticipants());
		assertEquals(initializedGameSetup.isMaxNumberOfParticipantsFixed(), setup.isMaxNumberOfParticipantsFixed());
		assertEquals(initializedGameSetup.getNumberOfRounds(), setup.getNumberOfRounds());
		assertEquals(initializedGameSetup.isNumberOfRoundsFixed(), setup.isNumberOfRoundsFixed());
		assertEquals(initializedGameSetup.getGunCoolingRate(), setup.getGunCoolingRate());
		assertEquals(initializedGameSetup.isGunCoolingRateFixed(), setup.isGunCoolingRateFixed());
		assertEquals(initializedGameSetup.getInactivityTurns(), setup.getInactivityTurns());
		assertEquals(initializedGameSetup.isInactiveTurnsFixed(), setup.isInactiveTurnsFixed());
		assertEquals(initializedGameSetup.getTurnTimeout(), setup.getTurnTimeout());
		assertEquals(initializedGameSetup.isTurnTimeoutFixed(), setup.isTurnTimeoutFixed());
		assertEquals(initializedGameSetup.getReadyTimeout(), setup.getReadyTimeout());
		assertEquals(initializedGameSetup.isReadyTimeoutFixed(), setup.isReadyTimeoutFixed());
		assertEquals(initializedGameSetup.getDelayedObserverTurns(), setup.getDelayedObserverTurns());
		assertEquals(initializedGameSetup.isDelayedObserverTurnsFixed(), setup.isDelayedObserverTurnsFixed());
	}
}
