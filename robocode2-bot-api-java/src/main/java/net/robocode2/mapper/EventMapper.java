package net.robocode2.mapper;

import lombok.NonNull;
import lombok.experimental.UtilityClass;
import lombok.val;
import net.robocode2.BotException;
import net.robocode2.events.*;

import java.util.ArrayList;
import java.util.List;

/** Utility class for mapping events */
@UtilityClass
public class EventMapper {

  public TickEvent map(@NonNull final net.robocode2.schema.TickEventForBot source) {
    return TickEvent.builder()
        .turnNumber(source.getTurnNumber())
        .roundNumber(source.getRoundNumber())
        .botState(BotStateMapper.map(source.getBotState()))
        .bulletStates(BulletStateMapper.map(source.getBulletStates()))
        .events(map(source.getEvents()))
        .build();
  }

  private List<GameEvent> map(@NonNull final List<net.robocode2.schema.Event> source) {
    val gameEvents = new ArrayList<GameEvent>();
    source.forEach(event -> gameEvents.add(map(event)));
    return gameEvents;
  }

  private GameEvent map(@NonNull final net.robocode2.schema.Event source) {
    if (source instanceof net.robocode2.schema.BotDeathEvent) {
      return map((net.robocode2.schema.BotDeathEvent) source);
    }
    if (source instanceof net.robocode2.schema.BotHitBotEvent) {
      return map((net.robocode2.schema.BotHitBotEvent) source);
    }
    if (source instanceof net.robocode2.schema.BotHitWallEvent) {
      return map((net.robocode2.schema.BotHitWallEvent) source);
    }
    if (source instanceof net.robocode2.schema.BulletFiredEvent) {
      return map((net.robocode2.schema.BulletFiredEvent) source);
    }
    if (source instanceof net.robocode2.schema.BulletHitBotEvent) {
      return map((net.robocode2.schema.BulletHitBotEvent) source);
    }
    if (source instanceof net.robocode2.schema.BulletHitBulletEvent) {
      return map((net.robocode2.schema.BulletHitBulletEvent) source);
    }
    if (source instanceof net.robocode2.schema.BulletHitWallEvent) {
      return map((net.robocode2.schema.BulletHitWallEvent) source);
    }
    if (source instanceof net.robocode2.schema.ScannedBotEvent) {
      return map((net.robocode2.schema.ScannedBotEvent) source);
    }
    if (source instanceof net.robocode2.schema.SkippedTurnEvent) {
      return map((net.robocode2.schema.SkippedTurnEvent) source);
    }
    if (source instanceof net.robocode2.schema.WonRoundEvent) {
      return map((net.robocode2.schema.WonRoundEvent) source);
    }
    throw new BotException(
        "No mapping exists for event type: " + source.getClass().getSimpleName());
  }

  private BotDeathEvent map(@NonNull final net.robocode2.schema.BotDeathEvent source) {
    return BotDeathEvent.builder()
        .turnNumber(source.getTurnNumber())
        .victimId(source.getVictimId())
        .build();
  }

  private BotHitBotEvent map(@NonNull final net.robocode2.schema.BotHitBotEvent source) {
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

  private BotHitWallEvent map(@NonNull final net.robocode2.schema.BotHitWallEvent source) {
    return BotHitWallEvent.builder()
        .turnNumber(source.getTurnNumber())
        .victimId(source.getVictimId())
        .build();
  }

  private BulletFiredEvent map(@NonNull final net.robocode2.schema.BulletFiredEvent source) {
    return BulletFiredEvent.builder()
        .turnNumber(source.getTurnNumber())
        .bullet(BulletStateMapper.map(source.getBullet()))
        .build();
  }

  private BulletHitBotEvent map(@NonNull final net.robocode2.schema.BulletHitBotEvent source) {
    return BulletHitBotEvent.builder()
        .turnNumber(source.getTurnNumber())
        .victimId(source.getVictimId())
        .bullet(BulletStateMapper.map(source.getBullet()))
        .damage((source.getDamage()))
        .energy(source.getEnergy())
        .build();
  }

  private BulletHitBulletEvent map(
      @NonNull final net.robocode2.schema.BulletHitBulletEvent source) {
    return BulletHitBulletEvent.builder()
        .turnNumber(source.getTurnNumber())
        .bullet(BulletStateMapper.map(source.getBullet()))
        .hitBullet(BulletStateMapper.map(source.getHitBullet()))
        .build();
  }

  private BulletHitWallEvent map(@NonNull final net.robocode2.schema.BulletHitWallEvent source) {
    return BulletHitWallEvent.builder()
        .turnNumber(source.getTurnNumber())
        .bullet(BulletStateMapper.map(source.getBullet()))
        .build();
  }

  private ScannedBotEvent map(@NonNull final net.robocode2.schema.ScannedBotEvent source) {
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

  private SkippedTurnEvent map(@NonNull final net.robocode2.schema.SkippedTurnEvent source) {
    return SkippedTurnEvent.builder().turnNumber(source.getTurnNumber()).build();
  }

  private WonRoundEvent map(@NonNull final net.robocode2.schema.WonRoundEvent source) {
    return WonRoundEvent.builder().turnNumber(source.getTurnNumber()).build();
  }
}
