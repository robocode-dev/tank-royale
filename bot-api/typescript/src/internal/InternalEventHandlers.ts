import { EventHandler } from "../events/EventHandler.js";
import { TickEvent } from "../events/TickEvent.js";
import { DisconnectedEvent } from "../events/DisconnectedEvent.js";
import { GameEndedEvent } from "../events/GameEndedEvent.js";
import { RoundStartedEvent } from "../events/RoundStartedEvent.js";
import { RoundEndedEvent } from "../events/RoundEndedEvent.js";
import { DeathEvent } from "../events/DeathEvent.js";
import { HitBotEvent } from "../events/HitBotEvent.js";
import { HitWallEvent } from "../events/HitWallEvent.js";
import { BulletFiredEvent } from "../events/BulletFiredEvent.js";
import { BotEvent } from "../events/BotEvent.js";

/** Minimal marker for internal-only virtual events */
export interface IInternalEvent {}

/** Virtual event fired each tick to signal "next turn" to internal subscribers */
export class NextTurnEvent implements IInternalEvent {
  constructor(readonly tickEvent: TickEvent) {}
}

/** Virtual event fired when the game is aborted */
export class GameAbortedInternalEvent implements IInternalEvent {}

/**
 * Internal event handlers used only by the API internals layer (not exposed to bot authors).
 * Mirrors Java's InternalEventHandlers class.
 */
export class InternalEventHandlers {
  readonly onNextTurn = new EventHandler<NextTurnEvent>();
  readonly onDisconnected = new EventHandler<DisconnectedEvent>();
  readonly onGameEnded = new EventHandler<GameEndedEvent>();
  readonly onGameAborted = new EventHandler<GameAbortedInternalEvent>();
  readonly onRoundStarted = new EventHandler<RoundStartedEvent>();
  readonly onRoundEnded = new EventHandler<RoundEndedEvent>();
  readonly onDeath = new EventHandler<DeathEvent>();
  readonly onHitBot = new EventHandler<HitBotEvent>();
  readonly onHitWall = new EventHandler<HitWallEvent>();
  readonly onBulletFired = new EventHandler<BulletFiredEvent>();

  fireEvent(event: BotEvent): void {
    if (event instanceof TickEvent) {
      this.onNextTurn.publish(new NextTurnEvent(event));
    } else if (event instanceof DisconnectedEvent) {
      this.onDisconnected.publish(event);
    } else if (event instanceof GameEndedEvent) {
      this.onGameEnded.publish(event);
    } else if (event instanceof RoundStartedEvent) {
      this.onRoundStarted.publish(event);
    } else if (event instanceof RoundEndedEvent) {
      this.onRoundEnded.publish(event);
    } else if (event instanceof DeathEvent) {
      this.onDeath.publish(event);
    } else if (event instanceof HitBotEvent) {
      this.onHitBot.publish(event);
    } else if (event instanceof HitWallEvent) {
      this.onHitWall.publish(event);
    } else if (event instanceof BulletFiredEvent) {
      this.onBulletFired.publish(event);
    }
  }

  fireGameAborted(): void {
    this.onGameAborted.publish(new GameAbortedInternalEvent());
  }
}
