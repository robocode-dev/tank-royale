package dev.robocode.tankroyale.botapi.mapper;

import dev.robocode.tankroyale.botapi.BotException;
import dev.robocode.tankroyale.botapi.BulletState;
import dev.robocode.tankroyale.botapi.events.*;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * Utility class for mapping events.
 */
public final class EventMapper {

    public static TickEvent map(final dev.robocode.tankroyale.schema.TickEventForBot source, int myBotId) {
        return new TickEvent(
                source.getTurnNumber(),
                source.getRoundNumber(),
                source.getEnemyCount(),
                BotStateMapper.map(source.getBotState()),
                BulletStateMapper.map(source.getBulletStates()),
                map(source.getEvents(), myBotId));
    }

    private static Set<BotEvent> map(final Collection<dev.robocode.tankroyale.schema.Event> source, int myBotId) {
        Set<BotEvent> gameBotEvents = new HashSet<>();
        source.forEach(event -> gameBotEvents.add(map(event, myBotId)));
        return gameBotEvents;
    }

    public static BotEvent map(final dev.robocode.tankroyale.schema.Event source, int myBotId) {
        if (source instanceof dev.robocode.tankroyale.schema.BotDeathEvent) {
            return map((dev.robocode.tankroyale.schema.BotDeathEvent) source, myBotId);
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
            return map((dev.robocode.tankroyale.schema.BulletHitBotEvent) source, myBotId);
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

    private static BotEvent map(final dev.robocode.tankroyale.schema.BotDeathEvent source, int myBotId) {
        if (source.getVictimId() == myBotId) {
            return new DeathEvent(source.getTurnNumber());
        }
        return new BotDeathEvent(source.getTurnNumber(), source.getVictimId());
    }

    private static HitBotEvent map(final dev.robocode.tankroyale.schema.BotHitBotEvent source) {
        return new HitBotEvent(
                source.getTurnNumber(),
                source.getVictimId(),
                source.getEnergy(),
                source.getX(),
                source.getY(),
                source.getRammed());
    }

    private static HitWallEvent map(final dev.robocode.tankroyale.schema.BotHitWallEvent source) {
        return new HitWallEvent(source.getTurnNumber());
    }

    private static BulletFiredEvent map(final dev.robocode.tankroyale.schema.BulletFiredEvent source) {
        return new BulletFiredEvent(
                source.getTurnNumber(),
                BulletStateMapper.map(source.getBullet()));
    }

    private static BotEvent map(final dev.robocode.tankroyale.schema.BulletHitBotEvent source, int myBotId) {
        BulletState bullet = BulletStateMapper.map(source.getBullet());
        if (source.getVictimId() == myBotId) {
            return new HitByBulletEvent(
                    source.getTurnNumber(),
                    bullet,
                    source.getDamage(),
                    source.getEnergy());
        }
        return new BulletHitBotEvent(
                source.getTurnNumber(),
                source.getVictimId(),
                bullet,
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
