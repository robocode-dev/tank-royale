import {
  TickEventForBot,
  TickEvent as SchemaTickEvent,
  BotDeathEvent as SchemaBotDeathEvent,
  BotHitBotEvent as SchemaBotHitBotEvent,
  BotHitWallEvent as SchemaBotHitWallEvent,
  BulletFiredEvent as SchemaBulletFiredEvent,
  BulletHitBotEvent as SchemaBulletHitBotEvent,
  BulletHitBulletEvent as SchemaBulletHitBulletEvent,
  BulletHitWallEvent as SchemaBulletHitWallEvent,
  ScannedBotEvent as SchemaScannedBotEvent,
  WonRoundEvent as SchemaWonRoundEvent,
  TeamMessageEvent as SchemaTeamMessageEvent,
  SkippedTurnEvent as SchemaSkippedTurnEvent,
} from "../protocol/schema.js";
import { MessageType } from "../protocol/MessageType.js";
import { BotEvent } from "../events/BotEvent.js";
import { TickEvent } from "../events/TickEvent.js";
import { DeathEvent } from "../events/DeathEvent.js";
import { BotDeathEvent } from "../events/BotDeathEvent.js";
import { HitBotEvent } from "../events/HitBotEvent.js";
import { HitWallEvent } from "../events/HitWallEvent.js";
import { BulletFiredEvent } from "../events/BulletFiredEvent.js";
import { HitByBulletEvent } from "../events/HitByBulletEvent.js";
import { BulletHitBotEvent } from "../events/BulletHitBotEvent.js";
import { BulletHitBulletEvent } from "../events/BulletHitBulletEvent.js";
import { BulletHitWallEvent } from "../events/BulletHitWallEvent.js";
import { ScannedBotEvent } from "../events/ScannedBotEvent.js";
import { WonRoundEvent } from "../events/WonRoundEvent.js";
import { TeamMessageEvent } from "../events/TeamMessageEvent.js";
import { SkippedTurnEvent } from "../events/SkippedTurnEvent.js";
import { BotStateMapper } from "./BotStateMapper.js";
import { BulletStateMapper } from "./BulletStateMapper.js";

/** Maps a schema TickEventForBot to a API TickEvent with all sub-events mapped. */
export class EventMapper {
  static map(tick: TickEventForBot, myBotId: number): TickEvent {
    const events = tick.events.map((e) => EventMapper.mapEvent(e, myBotId)).filter((e): e is BotEvent => e !== null);
    return new TickEvent(
      tick.turnNumber,
      tick.roundNumber,
      BotStateMapper.map(tick.botState),
      BulletStateMapper.mapCollection(tick.bulletStates),
      events,
    );
  }

  private static mapEvent(e: SchemaTickEvent, myBotId: number): BotEvent | null {
    switch (e.type) {
      case MessageType.BotDeathEvent: {
        const ev = e as SchemaBotDeathEvent;
        if (ev.victimId === myBotId) {
          return new DeathEvent(ev.turnNumber);
        }
        return new BotDeathEvent(ev.turnNumber, ev.victimId);
      }
      case MessageType.BotHitBotEvent: {
        const ev = e as SchemaBotHitBotEvent;
        return new HitBotEvent(ev.turnNumber, ev.victimId, ev.energy, ev.x, ev.y, ev.rammed);
      }
      case MessageType.BotHitWallEvent: {
        const ev = e as SchemaBotHitWallEvent;
        return new HitWallEvent(ev.turnNumber);
      }
      case MessageType.BulletFiredEvent: {
        const ev = e as SchemaBulletFiredEvent;
        return new BulletFiredEvent(ev.turnNumber, BulletStateMapper.map(ev.bullet));
      }
      case MessageType.BulletHitBotEvent: {
        const ev = e as SchemaBulletHitBotEvent;
        if (ev.victimId === myBotId) {
          return new HitByBulletEvent(ev.turnNumber, BulletStateMapper.map(ev.bullet), ev.damage, ev.energy);
        }
        return new BulletHitBotEvent(ev.turnNumber, ev.victimId, BulletStateMapper.map(ev.bullet), ev.damage, ev.energy);
      }
      case MessageType.BulletHitBulletEvent: {
        const ev = e as SchemaBulletHitBulletEvent;
        return new BulletHitBulletEvent(ev.turnNumber, BulletStateMapper.map(ev.bullet), BulletStateMapper.map(ev.hitBullet));
      }
      case MessageType.BulletHitWallEvent: {
        const ev = e as SchemaBulletHitWallEvent;
        return new BulletHitWallEvent(ev.turnNumber, BulletStateMapper.map(ev.bullet));
      }
      case MessageType.ScannedBotEvent: {
        const ev = e as SchemaScannedBotEvent;
        return new ScannedBotEvent(ev.turnNumber, ev.scannedByBotId, ev.scannedBotId, ev.energy, ev.x, ev.y, ev.direction, ev.speed);
      }
      case MessageType.WonRoundEvent: {
        const ev = e as SchemaWonRoundEvent;
        return new WonRoundEvent(ev.turnNumber);
      }
      case MessageType.TeamMessageEvent: {
        const ev = e as SchemaTeamMessageEvent;
        return new TeamMessageEvent(ev.turnNumber, ev.message, ev.senderId);
      }
      case MessageType.SkippedTurnEvent: {
        const ev = e as SchemaSkippedTurnEvent;
        return new SkippedTurnEvent(ev.turnNumber);
      }
      default:
        return null;
    }
  }
}
