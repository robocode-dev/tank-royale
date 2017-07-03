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

import org.junit.Test;

public class GameSetupTest {

	private IGameSetup initializedGameSetup() {
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

		return setup.toImmutableGameSetup();
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

		assertNull(setup.isArenaWidthFixed());
		assertNull(setup.isArenaHeightFixed());
		assertNull(setup.isMinNumberOfParticipantsFixed());
		assertNull(setup.isMaxNumberOfParticipantsFixed());
		assertNull(setup.isNumberOfRoundsFixed());
		assertNull(setup.isGunCoolingRateFixed());
		assertNull(setup.isInactiveTurnsFixed());
		assertNull(setup.isTurnTimeoutFixed());
		assertNull(setup.isReadyTimeoutFixed());
		assertNull(setup.isDelayedObserverTurnsFixed());
	}

	@Test
	public void constructorIGameSetup() {
		IGameSetup isetup = initializedGameSetup();
		assertReflectionEquals(isetup, new GameSetup(isetup).toImmutableGameSetup());
	}

	@Test
	public void toImmutableGameSetup() {
		IGameSetup isetup = initializedGameSetup();
		GameSetup setup = new GameSetup(isetup);
		ImmutableGameSetup immutableSetup = setup.toImmutableGameSetup();

		assertReflectionEquals(isetup, immutableSetup);
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

		setup.setArenaWidth(IRuleConstants.ARENA_MIN_WIDTH);
		assertEquals(IRuleConstants.ARENA_MIN_WIDTH, (int) setup.getArenaWidth());

		setup.setArenaWidth(IRuleConstants.ARENA_MAX_WIDTH);
		assertEquals(IRuleConstants.ARENA_MAX_WIDTH, (int) setup.getArenaWidth());

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
			setup.setArenaWidth(IRuleConstants.ARENA_MIN_WIDTH - 1);
			fail();
		} catch (IllegalArgumentException e) {
		}

		try {
			setup.setArenaWidth(IRuleConstants.ARENA_MAX_WIDTH + 1);
			fail();
		} catch (IllegalArgumentException e) {
		}
	}

	@Test
	public void setArenaHeight() {
		GameSetup setup = new GameSetup();

		setup.setArenaHeight(null);
		assertEquals(DEFAULT_ARENA_HEIGHT, (int) setup.getArenaHeight());

		setup.setArenaHeight(IRuleConstants.ARENA_MIN_HEIGHT);
		assertEquals(IRuleConstants.ARENA_MIN_HEIGHT, (int) setup.getArenaHeight());

		setup.setArenaHeight(IRuleConstants.ARENA_MAX_HEIGHT);
		assertEquals(IRuleConstants.ARENA_MAX_HEIGHT, (int) setup.getArenaHeight());

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
			setup.setArenaHeight(IRuleConstants.ARENA_MIN_HEIGHT - 1);
			fail();
		} catch (IllegalArgumentException e) {
		}

		try {
			setup.setArenaHeight(IRuleConstants.ARENA_MAX_HEIGHT + 1);
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
	public void setArenaWidthFixed() {
		GameSetup setup = new GameSetup();

		setup.setArenaWidthFixed(null);
		assertNull(setup.isArenaWidthFixed());

		setup.setArenaWidthFixed(true);
		assertEquals(true, setup.isArenaWidthFixed());

		setup.setArenaWidthFixed(false);
		assertEquals(false, setup.isArenaWidthFixed());
	}

	@Test
	public void setArenaHeightFixed() {
		GameSetup setup = new GameSetup();

		setup.setArenaHeightFixed(null);
		assertNull(setup.isArenaHeightFixed());

		setup.setArenaHeightFixed(true);
		assertEquals(true, setup.isArenaHeightFixed());

		setup.setArenaHeightFixed(false);
		assertEquals(false, setup.isArenaHeightFixed());
	}

	@Test
	public void setMinNumberOfParticipantsFixed() {
		GameSetup setup = new GameSetup();

		setup.setMinNumberOfParticipantsFixed(null);
		assertNull(setup.isMinNumberOfParticipantsFixed());

		setup.setMinNumberOfParticipantsFixed(true);
		assertEquals(true, setup.isMinNumberOfParticipantsFixed());

		setup.setMinNumberOfParticipantsFixed(false);
		assertEquals(false, setup.isMinNumberOfParticipantsFixed());
	}

	@Test
	public void setMaxNumberOfParticipantsFixed() {
		GameSetup setup = new GameSetup();

		setup.setMaxNumberOfParticipantsFixed(null);
		assertNull(setup.isMaxNumberOfParticipantsFixed());

		setup.setMaxNumberOfParticipantsFixed(true);
		assertEquals(true, setup.isMaxNumberOfParticipantsFixed());

		setup.setMaxNumberOfParticipantsFixed(false);
		assertEquals(false, setup.isMaxNumberOfParticipantsFixed());
	}

	@Test
	public void setNumberOfRoundsFixed() {
		GameSetup setup = new GameSetup();

		setup.setNumberOfRoundsFixed(null);
		assertNull(setup.isNumberOfRoundsFixed());

		setup.setNumberOfRoundsFixed(true);
		assertEquals(true, setup.isNumberOfRoundsFixed());

		setup.setNumberOfRoundsFixed(false);
		assertEquals(false, setup.isNumberOfRoundsFixed());
	}

	@Test
	public void setGunCoolingRateFixed() {
		GameSetup setup = new GameSetup();

		setup.setGunCoolingRateFixed(null);
		assertNull(setup.isGunCoolingRateFixed());

		setup.setGunCoolingRateFixed(true);
		assertEquals(true, setup.isGunCoolingRateFixed());

		setup.setGunCoolingRateFixed(false);
		assertEquals(false, setup.isGunCoolingRateFixed());
	}

	@Test
	public void setInactiveTurnsFixed() {
		GameSetup setup = new GameSetup();

		setup.setInactiveTurnsFixed(null);
		assertNull(setup.isInactiveTurnsFixed());

		setup.setInactiveTurnsFixed(true);
		assertEquals(true, setup.isInactiveTurnsFixed());

		setup.setInactiveTurnsFixed(false);
		assertEquals(false, setup.isInactiveTurnsFixed());
	}

	@Test
	public void setTurnTimeoutFixed() {
		GameSetup setup = new GameSetup();

		setup.setTurnTimeoutFixed(null);
		assertNull(setup.isTurnTimeoutFixed());

		setup.setTurnTimeoutFixed(true);
		assertEquals(true, setup.isTurnTimeoutFixed());

		setup.setTurnTimeoutFixed(false);
		assertEquals(false, setup.isTurnTimeoutFixed());
	}

	@Test
	public void setReadyTimeoutFixed() {
		GameSetup setup = new GameSetup();

		setup.setReadyTimeoutFixed(null);
		assertNull(setup.isReadyTimeoutFixed());

		setup.setReadyTimeoutFixed(true);
		assertEquals(true, setup.isReadyTimeoutFixed());

		setup.setReadyTimeoutFixed(false);
		assertEquals(false, setup.isReadyTimeoutFixed());
	}

	@Test
	public void setDelayedObserverTurnsFixed() {
		GameSetup setup = new GameSetup();

		setup.setDelayedObserverTurnsFixed(null);
		assertNull(setup.isDelayedObserverTurnsFixed());

		setup.setDelayedObserverTurnsFixed(true);
		assertEquals(true, setup.isDelayedObserverTurnsFixed());

		setup.setDelayedObserverTurnsFixed(false);
		assertEquals(false, setup.isDelayedObserverTurnsFixed());
	}
}
