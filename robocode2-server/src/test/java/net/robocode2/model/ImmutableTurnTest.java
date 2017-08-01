package net.robocode2.model;

import static org.junit.Assert.assertEquals;
import static org.unitils.reflectionassert.ReflectionAssert.assertReflectionEquals;
import static org.unitils.reflectionassert.ReflectionComparatorMode.LENIENT_ORDER;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.junit.BeforeClass;
import org.junit.Test;

import net.robocode2.model.events.BotDeathEvent;
import net.robocode2.model.events.BotHitBotEvent;
import net.robocode2.model.events.BotHitWallEvent;
import net.robocode2.model.events.IEvent;

public class ImmutableTurnTest {

	static Turn initializedTurn;

	static IEvent initializedEvent1;
	static IEvent initializedEvent2;
	static IEvent initializedEvent3;

	@BeforeClass
	public static void initialize() {
		Turn turn = new Turn();

		turn.setTurnNumber(7913);

		Bot bot1 = new Bot();
		Bot bot2 = new Bot();
		Bot bot3 = new Bot();
		bot1.setId(1);
		bot2.setId(2);
		bot3.setId(3);
		turn.setBots(Arrays.asList(bot1.toImmutableBot(), bot2.toImmutableBot(), bot3.toImmutableBot()));

		Bullet bullet1 = new Bullet();
		Bullet bullet2 = new Bullet();
		Bullet bullet3 = new Bullet();
		bullet1.setBotId(1);
		bullet2.setBotId(2);
		bullet3.setBotId(3);

		turn.setBullets(
				Arrays.asList(bullet1.toImmutableBullet(), bullet2.toImmutableBullet(), bullet3.toImmutableBullet()));

		int victimId = 456;
		BotDeathEvent event1 = new BotDeathEvent(victimId);

		initializedEvent1 = event1;

		int botId = 789;
		victimId = 261;
		double victimEnergy = 34.56;
		Point victimPosition = new Point(12.34, 56.78);
		boolean rammed = true;
		BotHitBotEvent event2 = new BotHitBotEvent(botId, victimId, victimEnergy, victimPosition, rammed);

		initializedEvent2 = event2;

		victimId = 344;
		BotHitWallEvent event3 = new BotHitWallEvent(victimId);

		initializedEvent3 = event3;

		turn.addObserverEvent(event1);
		turn.addObserverEvent(event2);
		turn.addObserverEvent(event3);

		turn.addPublicBotEvent(event1);

		turn.addPrivateBotEvent(2, event2);
		turn.addPrivateBotEvent(3, event3);

		initializedTurn = turn;
	}

	@Test
	public void constructorITurn() {
		ImmutableTurn turn = new ImmutableTurn(initializedTurn);

		assertEquals(initializedTurn.getTurnNumber(), turn.getTurnNumber());
		assertReflectionEquals(initializedTurn.getBots(), turn.getBots(), LENIENT_ORDER);
		assertReflectionEquals(initializedTurn.getBullets(), turn.getBullets(), LENIENT_ORDER);
		assertReflectionEquals(initializedTurn.getObserverEvents(), turn.getObserverEvents());

		Map<Integer, Set<IEvent>> botEventMap = initializedTurn.getBotEventsMap();

		assertReflectionEquals(botEventMap.get(1), new HashSet<>(Arrays.asList(initializedEvent1)));
		assertReflectionEquals(botEventMap.get(2), new HashSet<>(Arrays.asList(initializedEvent1, initializedEvent2)));
		assertReflectionEquals(botEventMap.get(3), new HashSet<>(Arrays.asList(initializedEvent1, initializedEvent3)));
	}
}
