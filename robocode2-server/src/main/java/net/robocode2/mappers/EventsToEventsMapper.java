package net.robocode2.mappers;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import net.robocode2.schema.BotDeathEvent;
import net.robocode2.schema.BotHitBotEvent;
import net.robocode2.schema.BotHitWallEvent;
import net.robocode2.schema.BulletFiredEvent;
import net.robocode2.schema.BulletHitBotEvent;
import net.robocode2.schema.BulletHitBulletEvent;
import net.robocode2.schema.BulletHitWallEvent;
import net.robocode2.schema.Event;
import net.robocode2.schema.Event.Type;
import net.robocode2.schema.HitByBulletEvent;
import net.robocode2.schema.ScannedBotEvent;
import net.robocode2.schema.SkippedTurnEvent;

public final class EventsToEventsMapper {

	private EventsToEventsMapper() {}

	public static List<Event> map(Set<net.robocode2.events.Event> events) {
		List<net.robocode2.schema.Event> mappedEvents = new ArrayList<>();
		for (net.robocode2.events.Event event : events) {
			mappedEvents.add(map(event));
		}
		return mappedEvents;
	}

	private static Event map(net.robocode2.events.Event event) {

		if (event instanceof net.robocode2.events.BotDeathEvent) {
			return map((net.robocode2.events.BotDeathEvent) event);
		}
		if (event instanceof net.robocode2.events.BotHitBotEvent) {
			return map((net.robocode2.events.BotHitBotEvent) event);
		}
		if (event instanceof net.robocode2.events.BotHitWallEvent) {
			return map((net.robocode2.events.BotHitWallEvent) event);
		}
		if (event instanceof net.robocode2.events.BulletFiredEvent) {
			return map((net.robocode2.events.BulletFiredEvent) event);
		}
		if (event instanceof net.robocode2.events.BulletHitBotEvent) {
			return map((net.robocode2.events.BulletHitBotEvent) event);
		}
		if (event instanceof net.robocode2.events.BulletHitBulletEvent) {
			return map((net.robocode2.events.BulletHitBulletEvent) event);
		}
		if (event instanceof net.robocode2.events.BulletHitWallEvent) {
			return map((net.robocode2.events.BulletHitWallEvent) event);
		}
		if (event instanceof net.robocode2.events.HitByBulletEvent) {
			return map((net.robocode2.events.HitByBulletEvent) event);
		}
		if (event instanceof net.robocode2.events.ScannedBotEvent) {
			return map((net.robocode2.events.ScannedBotEvent) event);
		}
		if (event instanceof net.robocode2.events.SkippedTurnEvent) {
			return map((net.robocode2.events.SkippedTurnEvent) event);
		}
		throw new IllegalStateException("Event type not handled: " + event.getClass().getCanonicalName());
	}

	private static BotDeathEvent map(net.robocode2.events.BotDeathEvent botDeathEvent) {
		BotDeathEvent event = new BotDeathEvent();
		event.setType(Type.BOT_DEATH_EVENT);
		event.setTurnNumber(botDeathEvent.getTurnNumber());
		event.setVictimId(botDeathEvent.getVictimId());
		return event;
	}

	private static BotHitBotEvent map(net.robocode2.events.BotHitBotEvent botHitBotEvent) {
		BotHitBotEvent event = new BotHitBotEvent();
		event.setType(Type.BOT_HIT_BOT_EVENT);
		event.setTurnNumber(botHitBotEvent.getTurnNumber());
		event.setBotId(botHitBotEvent.getBotId());
		event.setVictimId(botHitBotEvent.getVictimId());
		event.setEnergy(botHitBotEvent.getEnergy());
		event.setX(botHitBotEvent.getX());
		event.setY(botHitBotEvent.getY());
		event.setRammed(botHitBotEvent.isRammed());
		return event;
	}

	private static BotHitWallEvent map(net.robocode2.events.BotHitWallEvent botHitWallEvent) {
		BotHitWallEvent event = new BotHitWallEvent();
		event.setType(Type.BOT_HIT_WALL_EVENT);
		event.setTurnNumber(botHitWallEvent.getTurnNumber());
		event.setVictimId(botHitWallEvent.getVictimId());
		return event;
	}

	private static BulletFiredEvent map(net.robocode2.events.BulletFiredEvent bulletFiredEvent) {
		BulletFiredEvent event = new BulletFiredEvent();
		event.setType(Type.BULLET_FIRED_EVENT);
		event.setTurnNumber(bulletFiredEvent.getTurnNumber());
		event.setBullet(BulletToBulletStateMapper.map(bulletFiredEvent.getBullet()));
		return event;
	}

	private static BulletHitBotEvent map(net.robocode2.events.BulletHitBotEvent bulletHitBotEvent) {
		BulletHitBotEvent event = new BulletHitBotEvent();
		event.setType(Type.BULLET_HIT_BOT_EVENT);
		event.setTurnNumber(bulletHitBotEvent.getTurnNumber());
		event.setBullet(BulletToBulletStateMapper.map(bulletHitBotEvent.getBullet()));
		event.setVictimId(bulletHitBotEvent.getVictimId());
		event.setDamage(bulletHitBotEvent.getDamage());
		event.setEnergy(bulletHitBotEvent.getEnergy());
		return event;
	}

	private static BulletHitBulletEvent map(net.robocode2.events.BulletHitBulletEvent bulletHitBulletEvent) {
		BulletHitBulletEvent event = new BulletHitBulletEvent();
		event.setType(Type.BULLET_HIT_BULLET_EVENT);
		event.setTurnNumber(bulletHitBulletEvent.getTurnNumber());
		event.setBullet(BulletToBulletStateMapper.map(bulletHitBulletEvent.getBullet()));
		event.setHitBullet(BulletToBulletStateMapper.map(bulletHitBulletEvent.getHitBullet()));
		return event;
	}

	private static BulletHitWallEvent map(net.robocode2.events.BulletHitWallEvent bulletHitWallEvent) {
		BulletHitWallEvent event = new BulletHitWallEvent();
		event.setType(Type.BULLET_HIT_WALL_EVENT);
		event.setTurnNumber(bulletHitWallEvent.getTurnNumber());
		event.setBullet(BulletToBulletStateMapper.map(bulletHitWallEvent.getBullet()));
		return event;
	}

	private static HitByBulletEvent map(net.robocode2.events.HitByBulletEvent hitByBulletEvent) {
		HitByBulletEvent event = new HitByBulletEvent();
		event.setType(Type.HIT_BY_BULLET_EVENT);
		event.setTurnNumber(hitByBulletEvent.getTurnNumber());
		event.setBullet(BulletToBulletStateMapper.map(hitByBulletEvent.getBullet()));
		event.setDamage(hitByBulletEvent.getDamage());
		event.setEnergy(hitByBulletEvent.getEnergy());
		return event;
	}

	private static ScannedBotEvent map(net.robocode2.events.ScannedBotEvent scannedBotEvent) {
		ScannedBotEvent event = new ScannedBotEvent();
		event.setType(Type.SCANNED_BOT_EVENT);
		event.setTurnNumber(scannedBotEvent.getTurnNumber());
		event.setScannedByBotId(scannedBotEvent.getScannedByBotId());
		event.setScannedBotId(scannedBotEvent.getScannedBotId());
		event.setEnergy(scannedBotEvent.getEnergy());
		event.setX(scannedBotEvent.getX());
		event.setY(scannedBotEvent.getY());
		event.setDirection(scannedBotEvent.getDirection());
		event.setSpeed(scannedBotEvent.getSpeed());
		return event;
	}

	private static SkippedTurnEvent map(net.robocode2.events.SkippedTurnEvent skippedTurnEvent) {
		SkippedTurnEvent event = new SkippedTurnEvent();
		event.setType(Type.SKIPPED_TURN_EVENT);
		event.setTurnNumber(skippedTurnEvent.getTurnNumber());
		return event;
	}
}