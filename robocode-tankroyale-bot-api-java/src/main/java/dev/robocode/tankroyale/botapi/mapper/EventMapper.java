package dev.robocode.tankroyale.botapi.mapper;

import dev.robocode.tankroyale.botapi.BotException;
import dev.robocode.tankroyale.botapi.events.*;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/** Utility class for mapping events */
public final class EventMapper {

  public static TickEvent map(final dev.robocode.tankroyale.schema.TickEventForBot source) {
    return new TickEvent(
        source.getTurnNumber(),
        source.getRoundNumber(),
        BotStateMapper.map(source.getBotState()),
        BulletStateMapper.map(source.getBulletStates()),
        map(source.getEvents()));
  }

  private static Set<Event> map(final Collection<? extends dev.robocode.tankroyale.schema.Event> source) {
    Set<Event> gameEvents = new HashSet<>();
    source.forEach(event -> gameEvents.add(map(event)));
    return gameEvents;
  }

  public static Event map(final dev.robocode.tankroyale.schema.Event source) {
    if (source instanceof dev.robocode.tankroyale.schema.BotDeathEvent) {
      return map((dev.robocode.tankroyale.schema.BotDeathEvent) source);
    }
    if (source instanceof dev.robocode.tankroyale.schema.BotHitBotEvent) {
      return map((dev.robocode.tankroyale.schema.BotHitBotEvent) source);
    }
    if (source instanceof dev.robocode.tankroyale.schema.BotHitWallEvent) {
      return map((dev.robocode.tankroyale.schema.BotHitWallEvent) source);
    }
    if (source instanceof dev.robocode.tankroyale.schema.BulletFiredEvent) {
      return map((dev.robocode.tankroyale.schema.BulletFiredEvent) source);
    }
    if (source instanceof dev.robocode.tankroyale.schema.BulletHitBotEvent) {
      return map((dev.robocode.tankroyale.schema.BulletHitBotEvent) source);
    }
    if (source instanceof dev.robocode.tankroyale.schema.BulletHitBulletEvent) {
      return map((dev.robocode.tankroyale.schema.BulletHitBulletEvent) source);
    }
    if (source instanceof dev.robocode.tankroyale.schema.BulletHitWallEvent) {
      return map((dev.robocode.tankroyale.schema.BulletHitWallEvent) source);
    }
    if (source instanceof dev.robocode.tankroyale.schema.ScannedBotEvent) {
      return map((dev.robocode.tankroyale.schema.ScannedBotEvent) source);
    }
    if (source instanceof dev.robocode.tankroyale.schema.SkippedTurnEvent) {
      return map((dev.robocode.tankroyale.schema.SkippedTurnEvent) source);
    }
    if (source instanceof dev.robocode.tankroyale.schema.WonRoundEvent) {
      return map((dev.robocode.tankroyale.schema.WonRoundEvent) source);
    }
    throw new BotException(
        "No mapping exists for event type: " + source.getClass().getSimpleName());
  }

  private static BotDeathEvent map(final dev.robocode.tankroyale.schema.BotDeathEvent source) {
    return new BotDeathEvent(
        source.getTurnNumber(),
        source.getVictimId());
  }

  private static BotHitBotEvent map(final dev.robocode.tankroyale.schema.BotHitBotEvent source) {
    return new BotHitBotEvent(
        source.getTurnNumber(),
        source.getBotId(),
        source.getVictimId(),
        source.getEnergy(),
        source.getX(),
        source.getY(),
        source.getRammed());
  }

  private static BotHitWallEvent map(final dev.robocode.tankroyale.schema.BotHitWallEvent source) {
    return new BotHitWallEvent(
        source.getTurnNumber(),
        source.getVictimId());
  }

  private static BulletFiredEvent map(final dev.robocode.tankroyale.schema.BulletFiredEvent source) {
    return new BulletFiredEvent(
        source.getTurnNumber(),
        BulletStateMapper.map(source.getBullet()));
  }

  private static BulletHitBotEvent map(final dev.robocode.tankroyale.schema.BulletHitBotEvent source) {
    return new BulletHitBotEvent(
        source.getTurnNumber(),
        source.getVictimId(),
        BulletStateMapper.map(source.getBullet()),
        source.getDamage(),
        source.getEnergy());
  }

  private static BulletHitBulletEvent map(final dev.robocode.tankroyale.schema.BulletHitBulletEvent source) {
    return new BulletHitBulletEvent(
        source.getTurnNumber(),
        BulletStateMapper.map(source.getBullet()),
        BulletStateMapper.map(source.getHitBullet()));
  }

  private static BulletHitWallEvent map(final dev.robocode.tankroyale.schema.BulletHitWallEvent source) {
    return new BulletHitWallEvent(
        source.getTurnNumber(),
        BulletStateMapper.map(source.getBullet()));
  }

  private static ScannedBotEvent map(final dev.robocode.tankroyale.schema.ScannedBotEvent source) {
    return new ScannedBotEvent(
        source.getTurnNumber(),
        source.getScannedByBotId(),
        source.getScannedBotId(),
        source.getEnergy(),
        source.getX(),
        source.getY(),
        source.getDirection(),
        source.getSpeed());
  }

  private static SkippedTurnEvent map(final dev.robocode.tankroyale.schema.SkippedTurnEvent source) {
    return new SkippedTurnEvent(source.getTurnNumber());
  }

  private static WonRoundEvent map(final dev.robocode.tankroyale.schema.WonRoundEvent source) {
    return new WonRoundEvent(source.getTurnNumber());
  }
}
