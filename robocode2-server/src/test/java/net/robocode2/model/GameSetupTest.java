package net.robocode2.model;

import static net.robocode2.model.IGameSetup.DEFAULT_ARENA_HEIGHT;
import static net.robocode2.model.IGameSetup.DEFAULT_ARENA_WIDTH;
import static net.robocode2.model.IGameSetup.DEFAULT_DELAYED_OBSERVER_TURNS;
import static net.robocode2.model.IGameSetup.DEFAULT_GAME_TYPE;
import static net.robocode2.model.IGameSetup.DEFAULT_GUN_COOLING_RATE;
import static net.robocode2.model.IGameSetup.DEFAULT_INACTIVITY_TURNS;
import static net.robocode2.model.IGameSetup.DEFAULT_MAX_NUMBER_OF_PARTICIPANTS;
import static net.robocode2.model.IGameSetup.DEFAULT_MIN_NUMBER_OF_PARTICIPANTS;
import static net.robocode2.model.IGameSetup.DEFAULT_NUMBER_OF_ROUNDS;
import static net.robocode2.model.IGameSetup.DEFAULT_READY_TIMEOUT;
import static net.robocode2.model.IGameSetup.DEFAULT_TURN_TIMEOUT;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;
import static org.unitils.reflectionassert.ReflectionAssert.assertReflectionEquals;

import org.junit.BeforeClass;
import org.junit.Test;

public class GameSetupTest {

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
	public void constructorEmpty() {
		GameSetup setup = new GameSetup();

		assertEquals(DEFAULT_GAME_TYPE, setup.getGameType());
		assertEquals(DEFAULT_ARENA_WIDTH, setup.getArenaWidth(), 0.00001);
		assertEquals(DEFAULT_ARENA_HEIGHT, setup.getArenaHeight(), 0.00001);
		assertEquals(DEFAULT_MIN_NUMBER_OF_PARTICIPANTS, setup.getMinNumberOfParticipants(), 0.00001);
		assertEquals(DEFAULT_MAX_NUMBER_OF_PARTICIPANTS, setup.getMaxNumberOfParticipants());
		assertEquals(DEFAULT_NUMBER_OF_ROUNDS, setup.getNumberOfRounds(), 0.00001);
		assertEquals(DEFAULT_GUN_COOLING_RATE, setup.getGunCoolingRate(), 0.00001);
		assertEquals(DEFAULT_INACTIVITY_TURNS, setup.getInactivityTurns(), 0.00001);
		assertEquals(DEFAULT_TURN_TIMEOUT, setup.getTurnTimeout(), 0.00001);
		assertEquals(DEFAULT_READY_TIMEOUT, setup.getReadyTimeout(), 0.00001);
		assertEquals(DEFAULT_DELAYED_OBSERVER_TURNS, setup.getDelayedObserverTurns(), 0.00001);

		assertNull(setup.isArenaWidthLocked());
		assertNull(setup.isArenaHeightLocked());
		assertNull(setup.isMinNumberOfParticipantsLocked());
		assertNull(setup.isMaxNumberOfParticipantsLocked());
		assertNull(setup.isNumberOfRoundsLocked());
		assertNull(setup.isGunCoolingRateLocked());
		assertNull(setup.isInactiveTurnsLocked());
		assertNull(setup.isTurnTimeoutLocked());
		assertNull(setup.isReadyTimeoutLocked());
		assertNull(setup.isDelayedObserverTurnsLocked());
	}

	@Test
	public void constructorIGameSetup() {
		assertReflectionEquals(initializedGameSetup, new GameSetup(initializedGameSetup));
	}

	@Test
	public void toImmutableGameSetup() {
		assertReflectionEquals(initializedGameSetup.toImmutableGameSetup(),
				new GameSetup(initializedGameSetup).toImmutableGameSetup());
	}

	@Test
	public void setGameType() {
		GameSetup setup = new GameSetup();

		setup.setGameType(null);
		assertEquals(DEFAULT_GAME_TYPE, setup.getGameType());

		setup.setGameType(" test ");
		assertEquals(" test ", setup.getGameType());

		try {
			setup.setGameType("");
			fail();
		} catch (IllegalArgumentException e) {
		}

		try {
			setup.setGameType("\t ");
			fail();
		} catch (IllegalArgumentException e) {
		}
	}

	@Test
	public void setArenaWidth() {
		GameSetup setup = new GameSetup();

		setup.setArenaWidth(null);
		assertEquals(DEFAULT_ARENA_WIDTH, (int) setup.getArenaWidth());

		setup.setArenaWidth(IRuleConstants.ARENA_MIN_SIZE);
		assertEquals(IRuleConstants.ARENA_MIN_SIZE, (int) setup.getArenaWidth());

		setup.setArenaWidth(IRuleConstants.ARENA_MAX_SIZE);
		assertEquals(IRuleConstants.ARENA_MAX_SIZE, (int) setup.getArenaWidth());

		try {
			setup.setArenaWidth(-400);
			fail();
		} catch (IllegalArgumentException e) {
		}

		try {
			setup.setArenaWidth(0);
			fail();
		} catch (IllegalArgumentException e) {
		}

		try {
			setup.setArenaWidth(IRuleConstants.ARENA_MIN_SIZE - 1);
			fail();
		} catch (IllegalArgumentException e) {
		}

		try {
			setup.setArenaWidth(IRuleConstants.ARENA_MAX_SIZE + 1);
			fail();
		} catch (IllegalArgumentException e) {
		}
	}

	@Test
	public void setArenaHeight() {
		GameSetup setup = new GameSetup();

		setup.setArenaHeight(null);
		assertEquals(DEFAULT_ARENA_HEIGHT, (int) setup.getArenaHeight());

		setup.setArenaHeight(IRuleConstants.ARENA_MIN_SIZE);
		assertEquals(IRuleConstants.ARENA_MIN_SIZE, (int) setup.getArenaHeight());

		setup.setArenaHeight(IRuleConstants.ARENA_MAX_SIZE);
		assertEquals(IRuleConstants.ARENA_MAX_SIZE, (int) setup.getArenaHeight());

		try {
			setup.setArenaHeight(-400);
			fail();
		} catch (IllegalArgumentException e) {
		}

		try {
			setup.setArenaHeight(0);
			fail();
		} catch (IllegalArgumentException e) {
		}

		try {
			setup.setArenaHeight(IRuleConstants.ARENA_MIN_SIZE - 1);
			fail();
		} catch (IllegalArgumentException e) {
		}

		try {
			setup.setArenaHeight(IRuleConstants.ARENA_MAX_SIZE + 1);
			fail();
		} catch (IllegalArgumentException e) {
		}
	}

	@Test
	public void setMinNumberOfParticipants() {
		GameSetup setup = new GameSetup();

		setup.setMinNumberOfParticipants(null);
		assertEquals(DEFAULT_MIN_NUMBER_OF_PARTICIPANTS, (int) setup.getMinNumberOfParticipants());

		setup.setMinNumberOfParticipants(1);
		assertEquals(1, (int) setup.getMinNumberOfParticipants());

		setup.setMinNumberOfParticipants(50);
		assertEquals(50, (int) setup.getMinNumberOfParticipants());

		try {
			setup.setMinNumberOfParticipants(-10);
			fail();
		} catch (IllegalArgumentException e) {
		}

		try {
			setup.setMinNumberOfParticipants(0);
			fail();
		} catch (IllegalArgumentException e) {
		}
	}

	@Test
	public void setMaxNumberOfParticipants() {
		GameSetup setup = new GameSetup();

		setup.setMaxNumberOfParticipants(null);
		assertEquals(DEFAULT_MAX_NUMBER_OF_PARTICIPANTS, setup.getMaxNumberOfParticipants());

		setup.setMinNumberOfParticipants(1);
		setup.setMaxNumberOfParticipants(1);
		assertEquals(1, (int) setup.getMaxNumberOfParticipants());

		setup.setMaxNumberOfParticipants(50);
		assertEquals(50, (int) setup.getMaxNumberOfParticipants());

		try {
			setup.setMaxNumberOfParticipants(-10);
			fail();
		} catch (IllegalArgumentException e) {
		}

		try {
			setup.setMaxNumberOfParticipants(0);
			fail();
		} catch (IllegalArgumentException e) {
		}

		try {
			setup.setMinNumberOfParticipants(2);
			setup.setMaxNumberOfParticipants(1);
			fail();
		} catch (IllegalArgumentException e) {
		}
	}

	@Test
	public void setNumberOfRounds() {
		GameSetup setup = new GameSetup();

		setup.setNumberOfRounds(null);
		assertEquals(DEFAULT_NUMBER_OF_ROUNDS, (int) setup.getNumberOfRounds());

		setup.setNumberOfRounds(1);
		assertEquals(1, (int) setup.getNumberOfRounds());

		setup.setNumberOfRounds(50);
		assertEquals(50, (int) setup.getNumberOfRounds());

		try {
			setup.setNumberOfRounds(0);
			fail();
		} catch (IllegalArgumentException e) {
		}
	}

	@Test
	public void setGunCoolingRate() {
		GameSetup setup = new GameSetup();

		setup.setGunCoolingRate(null);
		assertEquals(DEFAULT_GUN_COOLING_RATE, setup.getGunCoolingRate(), 0.0001);

		setup.setGunCoolingRate(IRuleConstants.MIN_GUN_COOLING_RATE);
		assertEquals(IRuleConstants.MIN_GUN_COOLING_RATE, setup.getGunCoolingRate(), 0.0001);

		setup.setGunCoolingRate(IRuleConstants.MAX_GUN_COOLING_RATE);
		assertEquals(IRuleConstants.MAX_GUN_COOLING_RATE, setup.getGunCoolingRate(), 0.0001);

		try {
			setup.setGunCoolingRate(IRuleConstants.MIN_GUN_COOLING_RATE - 0.1);
			fail();
		} catch (IllegalArgumentException e) {
		}

		try {
			setup.setGunCoolingRate(IRuleConstants.MAX_GUN_COOLING_RATE + 0.1);
			fail();
		} catch (IllegalArgumentException e) {
		}
	}

	@Test
	public void setInactivityTurns() {
		GameSetup setup = new GameSetup();

		setup.setInactivityTurns(null);
		assertEquals(DEFAULT_INACTIVITY_TURNS, (int) setup.getInactivityTurns());

		setup.setInactivityTurns(0);
		assertEquals(0, (int) setup.getInactivityTurns());

		setup.setInactivityTurns(50000);
		assertEquals(50000, (int) setup.getInactivityTurns());

		try {
			setup.setInactivityTurns(-10);
			fail();
		} catch (IllegalArgumentException e) {
		}
	}

	@Test
	public void setTurnTimeout() {
		GameSetup setup = new GameSetup();

		setup.setTurnTimeout(null);
		assertEquals(DEFAULT_TURN_TIMEOUT, (int) setup.getTurnTimeout());

		setup.setTurnTimeout(0);
		assertEquals(0, (int) setup.getTurnTimeout());

		setup.setTurnTimeout(50000);
		assertEquals(50000, (int) setup.getTurnTimeout());

		try {
			setup.setTurnTimeout(-10);
			fail();
		} catch (IllegalArgumentException e) {
		}
	}

	@Test
	public void setReadyTimeout() {
		GameSetup setup = new GameSetup();

		setup.setReadyTimeout(null);
		assertEquals(DEFAULT_READY_TIMEOUT, (int) setup.getReadyTimeout());

		setup.setReadyTimeout(0);
		assertEquals(0, (int) setup.getReadyTimeout());

		setup.setReadyTimeout(50000);
		assertEquals(50000, (int) setup.getReadyTimeout());

		try {
			setup.setReadyTimeout(-10);
			fail();
		} catch (IllegalArgumentException e) {
		}
	}

	@Test
	public void setDelayedObserverTurns() {
		GameSetup setup = new GameSetup();

		setup.setDelayedObserverTurns(null);
		assertEquals(DEFAULT_DELAYED_OBSERVER_TURNS, (int) setup.getDelayedObserverTurns());

		setup.setDelayedObserverTurns(0);
		assertEquals(0, (int) setup.getDelayedObserverTurns());

		setup.setDelayedObserverTurns(50000);
		assertEquals(50000, (int) setup.getDelayedObserverTurns());

		try {
			setup.setDelayedObserverTurns(-10);
			fail();
		} catch (IllegalArgumentException e) {
		}
	}

	@Test
	public void setArenaWidthLocked() {
		GameSetup setup = new GameSetup();

		setup.setArenaWidthLocked(null);
		assertNull(setup.isArenaWidthLocked());

		setup.setArenaWidthLocked(true);
		assertEquals(true, setup.isArenaWidthLocked());

		setup.setArenaWidthLocked(false);
		assertEquals(false, setup.isArenaWidthLocked());
	}

	@Test
	public void setArenaHeightLocked() {
		GameSetup setup = new GameSetup();

		setup.setArenaHeightLocked(null);
		assertNull(setup.isArenaHeightLocked());

		setup.setArenaHeightLocked(true);
		assertEquals(true, setup.isArenaHeightLocked());

		setup.setArenaHeightLocked(false);
		assertEquals(false, setup.isArenaHeightLocked());
	}

	@Test
	public void setMinNumberOfParticipantsLocked() {
		GameSetup setup = new GameSetup();

		setup.setMinNumberOfParticipantsLocked(null);
		assertNull(setup.isMinNumberOfParticipantsLocked());

		setup.setMinNumberOfParticipantsLocked(true);
		assertEquals(true, setup.isMinNumberOfParticipantsLocked());

		setup.setMinNumberOfParticipantsLocked(false);
		assertEquals(false, setup.isMinNumberOfParticipantsLocked());
	}

	@Test
	public void setMaxNumberOfParticipantsLocked() {
		GameSetup setup = new GameSetup();

		setup.setMaxNumberOfParticipantsLocked(null);
		assertNull(setup.isMaxNumberOfParticipantsLocked());

		setup.setMaxNumberOfParticipantsLocked(true);
		assertEquals(true, setup.isMaxNumberOfParticipantsLocked());

		setup.setMaxNumberOfParticipantsLocked(false);
		assertEquals(false, setup.isMaxNumberOfParticipantsLocked());
	}

	@Test
	public void setNumberOfRoundsLocked() {
		GameSetup setup = new GameSetup();

		setup.setNumberOfRoundsLocked(null);
		assertNull(setup.isNumberOfRoundsLocked());

		setup.setNumberOfRoundsLocked(true);
		assertEquals(true, setup.isNumberOfRoundsLocked());

		setup.setNumberOfRoundsLocked(false);
		assertEquals(false, setup.isNumberOfRoundsLocked());
	}

	@Test
	public void setGunCoolingRateLocked() {
		GameSetup setup = new GameSetup();

		setup.setGunCoolingRateLocked(null);
		assertNull(setup.isGunCoolingRateLocked());

		setup.setGunCoolingRateLocked(true);
		assertEquals(true, setup.isGunCoolingRateLocked());

		setup.setGunCoolingRateLocked(false);
		assertEquals(false, setup.isGunCoolingRateLocked());
	}

	@Test
	public void setInactiveTurnsLocked() {
		GameSetup setup = new GameSetup();

		setup.setInactiveTurnsLocked(null);
		assertNull(setup.isInactiveTurnsLocked());

		setup.setInactiveTurnsLocked(true);
		assertEquals(true, setup.isInactiveTurnsLocked());

		setup.setInactiveTurnsLocked(false);
		assertEquals(false, setup.isInactiveTurnsLocked());
	}

	@Test
	public void setTurnTimeoutLocked() {
		GameSetup setup = new GameSetup();

		setup.setTurnTimeoutLocked(null);
		assertNull(setup.isTurnTimeoutLocked());

		setup.setTurnTimeoutLocked(true);
		assertEquals(true, setup.isTurnTimeoutLocked());

		setup.setTurnTimeoutLocked(false);
		assertEquals(false, setup.isTurnTimeoutLocked());
	}

	@Test
	public void setReadyTimeoutLocked() {
		GameSetup setup = new GameSetup();

		setup.setReadyTimeoutLocked(null);
		assertNull(setup.isReadyTimeoutLocked());

		setup.setReadyTimeoutLocked(true);
		assertEquals(true, setup.isReadyTimeoutLocked());

		setup.setReadyTimeoutLocked(false);
		assertEquals(false, setup.isReadyTimeoutLocked());
	}

	@Test
	public void setDelayedObserverTurnsLocked() {
		GameSetup setup = new GameSetup();

		setup.setDelayedObserverTurnsLocked(null);
		assertNull(setup.isDelayedObserverTurnsLocked());

		setup.setDelayedObserverTurnsLocked(true);
		assertEquals(true, setup.isDelayedObserverTurnsLocked());

		setup.setDelayedObserverTurnsLocked(false);
		assertEquals(false, setup.isDelayedObserverTurnsLocked());
	}
}
