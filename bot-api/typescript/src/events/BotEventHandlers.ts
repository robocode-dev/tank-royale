import { EventHandler } from "./EventHandler.js";
import { BotEvent } from "./BotEvent.js";
import { TickEvent } from "./TickEvent.js";
import { ScannedBotEvent } from "./ScannedBotEvent.js";
import { HitBotEvent } from "./HitBotEvent.js";
import { HitByBulletEvent } from "./HitByBulletEvent.js";
import { HitWallEvent } from "./HitWallEvent.js";
import { BulletFiredEvent } from "./BulletFiredEvent.js";
import { BulletHitBotEvent } from "./BulletHitBotEvent.js";
import { BulletHitBulletEvent } from "./BulletHitBulletEvent.js";
import { BulletHitWallEvent } from "./BulletHitWallEvent.js";
import { BotDeathEvent } from "./BotDeathEvent.js";
import { DeathEvent } from "./DeathEvent.js";
import { SkippedTurnEvent } from "./SkippedTurnEvent.js";
import { WonRoundEvent } from "./WonRoundEvent.js";
import { CustomEvent } from "./CustomEvent.js";
import { TeamMessageEvent } from "./TeamMessageEvent.js";
import { ConnectedEvent } from "./ConnectedEvent.js";
import { DisconnectedEvent } from "./DisconnectedEvent.js";
import { ConnectionErrorEvent } from "./ConnectionErrorEvent.js";
import { GameStartedEvent } from "./GameStartedEvent.js";
import { GameEndedEvent } from "./GameEndedEvent.js";
import { RoundStartedEvent } from "./RoundStartedEvent.js";
import { RoundEndedEvent } from "./RoundEndedEvent.js";

/** Holds one EventHandler per event type and dispatches BotEvents by type. */
export class BotEventHandlers {
  readonly onTick = new EventHandler<TickEvent>();
  readonly onScannedBot = new EventHandler<ScannedBotEvent>();
  readonly onHitBot = new EventHandler<HitBotEvent>();
  readonly onHitByBullet = new EventHandler<HitByBulletEvent>();
  readonly onHitWall = new EventHandler<HitWallEvent>();
  readonly onBulletFired = new EventHandler<BulletFiredEvent>();
  readonly onBulletHitBot = new EventHandler<BulletHitBotEvent>();
  readonly onBulletHitBullet = new EventHandler<BulletHitBulletEvent>();
  readonly onBulletHitWall = new EventHandler<BulletHitWallEvent>();
  readonly onBotDeath = new EventHandler<BotDeathEvent>();
  readonly onDeath = new EventHandler<DeathEvent>();
  readonly onSkippedTurn = new EventHandler<SkippedTurnEvent>();
  readonly onWonRound = new EventHandler<WonRoundEvent>();
  readonly onCustomEvent = new EventHandler<CustomEvent>();
  readonly onTeamMessage = new EventHandler<TeamMessageEvent>();
  readonly onConnected = new EventHandler<ConnectedEvent>();
  readonly onDisconnected = new EventHandler<DisconnectedEvent>();
  readonly onConnectionError = new EventHandler<ConnectionErrorEvent>();
  readonly onGameStarted = new EventHandler<GameStartedEvent>();
  readonly onGameEnded = new EventHandler<GameEndedEvent>();
  readonly onRoundStarted = new EventHandler<RoundStartedEvent>();
  readonly onRoundEnded = new EventHandler<RoundEndedEvent>();

  fireEvent(event: BotEvent): void {
    if (event instanceof TickEvent) this.onTick.publish(event);
    else if (event instanceof ScannedBotEvent) this.onScannedBot.publish(event);
    else if (event instanceof HitBotEvent) this.onHitBot.publish(event);
    else if (event instanceof HitByBulletEvent) this.onHitByBullet.publish(event);
    else if (event instanceof HitWallEvent) this.onHitWall.publish(event);
    else if (event instanceof BulletFiredEvent) this.onBulletFired.publish(event);
    else if (event instanceof BulletHitBotEvent) this.onBulletHitBot.publish(event);
    else if (event instanceof BulletHitBulletEvent) this.onBulletHitBullet.publish(event);
    else if (event instanceof BulletHitWallEvent) this.onBulletHitWall.publish(event);
    else if (event instanceof BotDeathEvent) this.onBotDeath.publish(event);
    else if (event instanceof DeathEvent) this.onDeath.publish(event);
    else if (event instanceof SkippedTurnEvent) this.onSkippedTurn.publish(event);
    else if (event instanceof WonRoundEvent) this.onWonRound.publish(event);
    else if (event instanceof CustomEvent) this.onCustomEvent.publish(event);
    else if (event instanceof TeamMessageEvent) this.onTeamMessage.publish(event);
  }
}
