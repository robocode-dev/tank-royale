package dev.robocode.tankroyale.server.mappers;

import net.robocode2.schema.*;
import net.robocode2.schema.Message.Type;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public final class EventsToEventsMapper {

	private EventsToEventsMapper() {}

	public static List<Event> map(Set<dev.robocode.tankroyale.server.events.Event> events) {
		List<net.robocode2.schema.Event> mappedEvents = new ArrayList<>();
		for (dev.robocode.tankroyale.server.events.Event event : events) {
			mappedEvents.add(map(event));
		}
		return mappedEvents;
	}

	private static Event map(dev.robocode.tankroyale.server.events.Event event) {

		if (event instanceof dev.robocode.tankroyale.server.events.BotDeathEvent) {
			return map((dev.robocode.tankroyale.server.events.BotDeathEvent) event);
		}
		if (event instanceof dev.robocode.tankroyale.server.events.BotHitBotEvent) {
			return map((dev.robocode.tankroyale.server.events.BotHitBotEvent) event);
		}
		if (event instanceof dev.robocode.tankroyale.server.events.BotHitWallEvent) {
			return map((dev.robocode.tankroyale.server.events.BotHitWallEvent) event);
		}
		if (event instanceof dev.robocode.tankroyale.server.events.BulletFiredEvent) {
			return map((dev.robocode.tankroyale.server.events.BulletFiredEvent) event);
		}
		if (event instanceof dev.robocode.tankroyale.server.events.BulletHitBotEvent) {
			return map((dev.robocode.tankroyale.server.events.BulletHitBotEvent) event);
		}
		if (event instanceof dev.robocode.tankroyale.server.events.BulletHitBulletEvent) {
			return map((dev.robocode.tankroyale.server.events.BulletHitBulletEvent) event);
		}
		if (event instanceof dev.robocode.tankroyale.server.events.BulletHitWallEvent) {
			return map((dev.robocode.tankroyale.server.events.BulletHitWallEvent) event);
		}
		if (event instanceof dev.robocode.tankroyale.server.events.ScannedBotEvent) {
			return map((dev.robocode.tankroyale.server.events.ScannedBotEvent) event);
		}
		if (event instanceof dev.robocode.tankroyale.server.events.SkippedTurnEvent) {
			return map((dev.robocode.tankroyale.server.events.SkippedTurnEvent) event);
		}
		throw new IllegalStateException("Event type not handled: " + event.getClass().getCanonicalName());
	}

	private static BotDeathEvent map(dev.robocode.tankroyale.server.events.BotDeathEvent botDeathEvent) {
		BotDeathEvent event = new BotDeathEvent();
		event.setType(Type.BOT_DEATH_EVENT);
		event.setTurnNumber(botDeathEvent.getTurnNumber());
		event.setVictimId(botDeathEvent.getVictimId());
		return event;
	}

	private static BotHitBotEvent map(dev.robocode.tankroyale.server.events.BotHitBotEvent botHitBotEvent) {
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

	private static BotHitWallEvent map(dev.robocode.tankroyale.server.events.BotHitWallEvent botHitWallEvent) {
		BotHitWallEvent event = new BotHitWallEvent();
		event.setType(Type.BOT_HIT_WALL_EVENT);
		event.setTurnNumber(botHitWallEvent.getTurnNumber());
		event.setVictimId(botHitWallEvent.getVictimId());
		return event;
	}

	private static BulletFiredEvent map(dev.robocode.tankroyale.server.events.BulletFiredEvent bulletFiredEvent) {
		BulletFiredEvent event = new BulletFiredEvent();
		event.setType(Type.BULLET_FIRED_EVENT);
		event.setTurnNumber(bulletFiredEvent.getTurnNumber());
		event.setBullet(BulletToBulletStateMapper.map(bulletFiredEvent.getBullet()));
		return event;
	}

	private static BulletHitBotEvent map(dev.robocode.tankroyale.server.events.BulletHitBotEvent bulletHitBotEvent) {
		BulletHitBotEvent event = new BulletHitBotEvent();
		event.setType(Type.BULLET_HIT_BOT_EVENT);
		event.setTurnNumber(bulletHitBotEvent.getTurnNumber());
		event.setBullet(BulletToBulletStateMapper.map(bulletHitBotEvent.getBullet()));
		event.setVictimId(bulletHitBotEvent.getVictimId());
		event.setDamage(bulletHitBotEvent.getDamage());
		event.setEnergy(bulletHitBotEvent.getEnergy());
		return event;
	}

	private static BulletHitBulletEvent map(dev.robocode.tankroyale.server.events.BulletHitBulletEvent bulletHitBulletEvent) {
		BulletHitBulletEvent event = new BulletHitBulletEvent();
		event.setType(Type.BULLET_HIT_BULLET_EVENT);
		event.setTurnNumber(bulletHitBulletEvent.getTurnNumber());
		event.setBullet(BulletToBulletStateMapper.map(bulletHitBulletEvent.getBullet()));
		event.setHitBullet(BulletToBulletStateMapper.map(bulletHitBulletEvent.getHitBullet()));
		return event;
	}

	private static BulletHitWallEvent map(dev.robocode.tankroyale.server.events.BulletHitWallEvent bulletHitWallEvent) {
		BulletHitWallEvent event = new BulletHitWallEvent();
		event.setType(Type.BULLET_HIT_WALL_EVENT);
		event.setTurnNumber(bulletHitWallEvent.getTurnNumber());
		event.setBullet(BulletToBulletStateMapper.map(bulletHitWallEvent.getBullet()));
		return event;
	}

	private static ScannedBotEvent map(dev.robocode.tankroyale.server.events.ScannedBotEvent scannedBotEvent) {
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

	private static SkippedTurnEvent map(dev.robocode.tankroyale.server.events.SkippedTurnEvent skippedTurnEvent) {
		SkippedTurnEvent event = new SkippedTurnEvent();
		event.setType(Type.SKIPPED_TURN_EVENT);
		event.setTurnNumber(skippedTurnEvent.getTurnNumber());
		return event;
	}
}