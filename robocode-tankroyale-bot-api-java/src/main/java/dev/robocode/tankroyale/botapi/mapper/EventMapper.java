package dev.robocode.tankroyale.botapi.mapper;

import dev.robocode.tankroyale.botapi.BotException;
import dev.robocode.tankroyale.botapi.events.*;
import lombok.NonNull;
import lombok.experimental.UtilityClass;
import lombok.val;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/** Utility class for mapping events */
@UtilityClass
public class EventMapper {

  public TickEvent map(@NonNull final dev.robocode.tankroyale.schema.TickEventForBot source) {
    return TickEvent.builder()
        .turnNumber(source.getTurnNumber())
        .roundNumber(source.getRoundNumber())
        .botState(BotStateMapper.map(source.getBotState()))
        .bulletStates(BulletStateMapper.map(source.getBulletStates()))
        .events(map(source.getEvents()))
        .build();
  }

  private Set<Event> map(@NonNull final Collection<? extends dev.robocode.tankroyale.schema.Event> source) {
    val gameEvents = new HashSet<Event>();
    source.forEach(event -> gameEvents.add(map(event)));
    return gameEvents;
  }

  public Event map(@NonNull final dev.robocode.tankroyale.schema.Event source) {
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

  private BotDeathEvent map(@NonNull final dev.robocode.tankroyale.schema.BotDeathEvent source) {
    return BotDeathEvent.builder()
        .turnNumber(source.getTurnNumber())
        .victimId(source.getVictimId())
        .build();
  }

  private BotHitBotEvent map(@NonNull final dev.robocode.tankroyale.schema.BotHitBotEvent source) {
    return BotHitBotEvent.builder()
        .turnNumber(source.getTurnNumber())
        .botId(source.getBotId())
        .victimId(source.getVictimId())
        .energy(source.getEnergy())
        .x(source.getX())
        .y(source.getY())
        .rammed(source.getRammed())
        .build();
  }

  private BotHitWallEvent map(@NonNull final dev.robocode.tankroyale.schema.BotHitWallEvent source) {
    return BotHitWallEvent.builder()
        .turnNumber(source.getTurnNumber())
        .victimId(source.getVictimId())
        .build();
  }

  private BulletFiredEvent map(@NonNull final dev.robocode.tankroyale.schema.BulletFiredEvent source) {
    return BulletFiredEvent.builder()
        .turnNumber(source.getTurnNumber())
        .bullet(BulletStateMapper.map(source.getBullet()))
        .build();
  }

  private BulletHitBotEvent map(@NonNull final dev.robocode.tankroyale.schema.BulletHitBotEvent source) {
    return BulletHitBotEvent.builder()
        .turnNumber(source.getTurnNumber())
        .victimId(source.getVictimId())
        .bullet(BulletStateMapper.map(source.getBullet()))
        .damage((source.getDamage()))
        .energy(source.getEnergy())
        .build();
  }

  private BulletHitBulletEvent map(
      @NonNull final dev.robocode.tankroyale.schema.BulletHitBulletEvent source) {
    return BulletHitBulletEvent.builder()
        .turnNumber(source.getTurnNumber())
        .bullet(BulletStateMapper.map(source.getBullet()))
        .hitBullet(BulletStateMapper.map(source.getHitBullet()))
        .build();
  }

  private BulletHitWallEvent map(@NonNull final dev.robocode.tankroyale.schema.BulletHitWallEvent source) {
    return BulletHitWallEvent.builder()
        .turnNumber(source.getTurnNumber())
        .bullet(BulletStateMapper.map(source.getBullet()))
        .build();
  }

  private ScannedBotEvent map(@NonNull final dev.robocode.tankroyale.schema.ScannedBotEvent source) {
    return ScannedBotEvent.builder()
        .turnNumber(source.getTurnNumber())
        .scannedByBotId(source.getScannedByBotId())
        .scannedBotId(source.getScannedBotId())
        .energy(source.getEnergy())
        .x(source.getX())
        .y(source.getY())
        .direction((source.getDirection()))
        .speed(source.getSpeed())
        .build();
  }

  private SkippedTurnEvent map(@NonNull final dev.robocode.tankroyale.schema.SkippedTurnEvent source) {
    return SkippedTurnEvent.builder().turnNumber(source.getTurnNumber()).build();
  }

  private WonRoundEvent map(@NonNull final dev.robocode.tankroyale.schema.WonRoundEvent source) {
    return WonRoundEvent.builder().turnNumber(source.getTurnNumber()).build();
  }
}
