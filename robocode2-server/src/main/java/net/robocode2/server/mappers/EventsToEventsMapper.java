package net.robocode2.server.mappers;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import net.robocode2.json_schema.events.BotDeathEvent;
import net.robocode2.json_schema.events.BotHitBotEvent;
import net.robocode2.json_schema.events.BotHitWallEvent;
import net.robocode2.json_schema.events.BulletFiredEvent;
import net.robocode2.json_schema.events.BulletHitBotEvent;
import net.robocode2.json_schema.events.BulletHitBulletEvent;
import net.robocode2.json_schema.events.BulletMissedEvent;
import net.robocode2.json_schema.events.Event;
import net.robocode2.json_schema.events.Event.Type;
import net.robocode2.json_schema.events.ScannedBotEvent;
import net.robocode2.json_schema.events.SkippedTurnEvent;

public final class EventsToEventsMapper {

	public static List<Event> map(Set<net.robocode2.model.events.Event> events) {
		List<net.robocode2.json_schema.events.Event> mappedEvents = new ArrayList<net.robocode2.json_schema.events.Event>();
		for (net.robocode2.model.events.Event event : events) {
			mappedEvents.add(map(event));
		}
		return mappedEvents;
	}

	private static Event map(net.robocode2.model.events.Event event) {

		if (event instanceof net.robocode2.model.events.BotDeathEvent) {
			return map((net.robocode2.model.events.BotDeathEvent) event);
		}
		if (event instanceof net.robocode2.model.events.BotHitBotEvent) {
			return map((net.robocode2.model.events.BotHitBotEvent) event);
		}
		if (event instanceof net.robocode2.model.events.BotHitWallEvent) {
			return map((net.robocode2.model.events.BotHitWallEvent) event);
		}
		if (event instanceof net.robocode2.model.events.BulletFiredEvent) {
			return map((net.robocode2.model.events.BulletFiredEvent) event);
		}
		if (event instanceof net.robocode2.model.events.BulletHitBotEvent) {
			return map((net.robocode2.model.events.BulletHitBotEvent) event);
		}
		if (event instanceof net.robocode2.model.events.BulletHitBulletEvent) {
			return map((net.robocode2.model.events.BulletHitBulletEvent) event);
		}
		if (event instanceof net.robocode2.model.events.BulletMissedEvent) {
			return map((net.robocode2.model.events.BulletMissedEvent) event);
		}
		if (event instanceof net.robocode2.model.events.ScannedBotEvent) {
			return map((net.robocode2.model.events.ScannedBotEvent) event);
		}
		if (event instanceof net.robocode2.model.events.SkippedTurnEvent) {
			return map((net.robocode2.model.events.SkippedTurnEvent) event);
		}
		throw new IllegalStateException("Event type not handled: " + event.getClass().getCanonicalName());
	}

	private static BotDeathEvent map(net.robocode2.model.events.BotDeathEvent botDeathEvent) {
		BotDeathEvent event = new BotDeathEvent();
		event.setType(Type.BOT_DEATH_EVENT);
		event.setVictimId(botDeathEvent.getVictimId());
		return event;
	}

	private static BotHitBotEvent map(net.robocode2.model.events.BotHitBotEvent botHitBotEvent) {
		BotHitBotEvent event = new BotHitBotEvent();
		event.setType(Type.BOT_HIT_BOT_EVENT);
		event.setBotId(botHitBotEvent.getBotId());
		event.setVictimId(botHitBotEvent.getVictimId());
		event.setEnergy(botHitBotEvent.getEnergy());
		event.setPosition(PositionMapper.map(botHitBotEvent.getPosition()));
		event.setRammed(botHitBotEvent.isRammed());
		return event;
	}

	private static BotHitWallEvent map(net.robocode2.model.events.BotHitWallEvent botHitWallEvent) {
		BotHitWallEvent event = new BotHitWallEvent();
		event.setType(Type.BOT_HIT_WALL_EVENT);
		event.setVictimId(botHitWallEvent.getVictimId());
		return event;
	}

	private static BulletFiredEvent map(net.robocode2.model.events.BulletFiredEvent bulletFiredEvent) {
		BulletFiredEvent event = new BulletFiredEvent();
		event.setType(Type.BULLET_FIRED_EVENT);
		event.setBullet(BulletToBulletStateMapper.map(bulletFiredEvent.getBullet()));
		return event;
	}

	private static BulletHitBotEvent map(net.robocode2.model.events.BulletHitBotEvent bulletHitBotEvent) {
		BulletHitBotEvent event = new BulletHitBotEvent();
		event.setType(Type.BULLET_HIT_BOT_EVENT);
		event.setBullet(BulletToBulletStateMapper.map(bulletHitBotEvent.getBullet()));
		event.setVictimId(bulletHitBotEvent.getVictimId());
		event.setDamage(bulletHitBotEvent.getDamage());
		event.setEnergy(bulletHitBotEvent.getEnergy());
		return event;
	}

	private static BulletHitBulletEvent map(net.robocode2.model.events.BulletHitBulletEvent bulletHitBulletEvent) {
		BulletHitBulletEvent event = new BulletHitBulletEvent();
		event.setType(Type.BULLET_HIT_BULLET_EVENT);
		event.setBullet(BulletToBulletStateMapper.map(bulletHitBulletEvent.getBullet()));
		event.setHitBullet(BulletToBulletStateMapper.map(bulletHitBulletEvent.getHitBullet()));
		return event;
	}

	private static BulletMissedEvent map(net.robocode2.model.events.BulletMissedEvent bulletMissedEvent) {
		BulletMissedEvent event = new BulletMissedEvent();
		event.setType(Type.BULLET_MISSED_EVENT);
		event.setBullet(BulletToBulletStateMapper.map(bulletMissedEvent.getBullet()));
		return event;
	}

	private static ScannedBotEvent map(net.robocode2.model.events.ScannedBotEvent scannedBotEvent) {
		ScannedBotEvent event = new ScannedBotEvent();
		event.setType(Type.SCANNED_BOT_EVENT);
		event.setScannedByBotId(scannedBotEvent.getScannedByBotId());
		event.setScannedBotId(scannedBotEvent.getScannedBotId());
		event.setEnergy(scannedBotEvent.getEnergy());
		event.setPosition(PositionMapper.map(scannedBotEvent.getPosition()));
		event.setDirection(scannedBotEvent.getDirection());
		event.setSpeed(scannedBotEvent.getSpeed());
		return event;
	}

	private static SkippedTurnEvent map(net.robocode2.model.events.SkippedTurnEvent skippedTurnEvent) {
		SkippedTurnEvent event = new SkippedTurnEvent();
		event.setType(Type.SKIPPED_TURN_EVENT);
		event.setSkippedTurn(skippedTurnEvent.getSkippedTurn());
		return event;
	}
}