import { BaseBotInternals, IStopResumeListener } from "./BaseBotInternals.js";
import { BotStoppedException } from "./BotStoppedException.js";
import { NextTurnEvent } from "./InternalEventHandlers.js";
import { HitBotEvent } from "../events/HitBotEvent.js";
import { MathUtil } from "../util/MathUtil.js";
import type { IBot } from "../IBot.js";

const MAX_SPEED = 8;

/**
 * Internal implementation for Bot — tracks remaining movement/turn values and
 * implements Nat Pavasant's optimal velocity algorithm.
 * Mirrors Java's BotInternals class.
 */
export class BotInternals implements IStopResumeListener {
  private readonly bot: IBot;
  private readonly base: BaseBotInternals;

  // Remaining movement tracking
  distanceRemaining = 0;
  turnRemaining = 0;
  gunTurnRemaining = 0;
  radarTurnRemaining = 0;

  // Previous directions (for delta calculation)
  private previousDirection = 0;
  private previousGunDirection = 0;
  private previousRadarDirection = 0;

  // Saved state for stop/resume
  private savedDistanceRemaining = 0;
  private savedTurnRemaining = 0;
  private savedGunTurnRemaining = 0;
  private savedRadarTurnRemaining = 0;
  private savedPreviousDirection = 0;
  private savedPreviousGunDirection = 0;
  private savedPreviousRadarDirection = 0;

  // Override flags — true when Bot.setForward/setTurnLeft etc. have been called
  overrideTurnRate = false;
  overrideGunTurnRate = false;
  overrideRadarTurnRate = false;
  overrideTargetSpeed = false;

  private isOverDriving = false;

  constructor(bot: IBot, base: BaseBotInternals) {
    this.bot = bot;
    this.base = base;
    base.setStopResumeListener(this);

    const ih = base.internalEventHandlers;
    // Priority 110 — runs BEFORE BaseBotInternals.onNextTurn (priority 100) which notifies worker
    ih.onNextTurn.subscribe((e: NextTurnEvent) => this.onNextTurn(e), 110);
    ih.onGameAborted.subscribe(() => base.stopThread(), 100);
    ih.onRoundEnded.subscribe(() => base.stopThread(), 90);
    ih.onGameEnded.subscribe(() => base.stopThread(), 90);
    ih.onDisconnected.subscribe(() => base.stopThread(), 90);
    ih.onDeath.subscribe(() => base.stopThread(), 90);
    ih.onHitWall.subscribe(() => { this.distanceRemaining = 0; }, 90);
    ih.onHitBot.subscribe((e) => {
      if ((e as unknown as HitBotEvent).isRammed) this.distanceRemaining = 0;
    }, 90);
  }

  private onNextTurn(e: NextTurnEvent): void {
    if (e.tickEvent.turnNumber === 1) {
      this.onFirstTurn();
    }
    this.processTurn();
  }

  private onFirstTurn(): void {
    this.base.stopThread();
    this.clearRemaining();
    this.base.startThread(this.bot);
  }

  private clearRemaining(): void {
    this.distanceRemaining = 0;
    this.turnRemaining = 0;
    this.gunTurnRemaining = 0;
    this.radarTurnRemaining = 0;
    this.previousDirection = this.bot.getDirection();
    this.previousGunDirection = this.bot.getGunDirection();
    this.previousRadarDirection = this.bot.getRadarDirection();
  }

  private processTurn(): void {
    if (this.bot.isDisabled()) {
      this.clearRemaining();
    } else {
      this.updateTurnRemaining();
      this.updateGunTurnRemaining();
      this.updateRadarTurnRemaining();
      this.updateMovement();
    }
  }

  private updateTurnRemaining(): void {
    const delta = this.bot.calcDeltaAngle(this.bot.getDirection(), this.previousDirection);
    this.previousDirection = this.bot.getDirection();
    if (!this.overrideTurnRate) return;
    if (Math.abs(this.turnRemaining) <= Math.abs(delta)) {
      this.turnRemaining = 0;
    } else {
      this.turnRemaining -= delta;
      if (this.isNearZero(this.turnRemaining)) this.turnRemaining = 0;
    }
    this.base.setTurnRate(this.turnRemaining);
  }

  private updateGunTurnRemaining(): void {
    const delta = this.bot.calcDeltaAngle(this.bot.getGunDirection(), this.previousGunDirection);
    this.previousGunDirection = this.bot.getGunDirection();
    if (!this.overrideGunTurnRate) return;
    if (Math.abs(this.gunTurnRemaining) <= Math.abs(delta)) {
      this.gunTurnRemaining = 0;
    } else {
      this.gunTurnRemaining -= delta;
      if (this.isNearZero(this.gunTurnRemaining)) this.gunTurnRemaining = 0;
    }
    this.base.setGunTurnRate(this.gunTurnRemaining);
  }

  private updateRadarTurnRemaining(): void {
    const delta = this.bot.calcDeltaAngle(this.bot.getRadarDirection(), this.previousRadarDirection);
    this.previousRadarDirection = this.bot.getRadarDirection();
    if (!this.overrideRadarTurnRate) return;
    if (Math.abs(this.radarTurnRemaining) <= Math.abs(delta)) {
      this.radarTurnRemaining = 0;
    } else {
      this.radarTurnRemaining -= delta;
      if (this.isNearZero(this.radarTurnRemaining)) this.radarTurnRemaining = 0;
    }
    this.base.setRadarTurnRate(this.radarTurnRemaining);
  }

  private updateMovement(): void {
    if (!this.overrideTargetSpeed) {
      if (Math.abs(this.distanceRemaining) < Math.abs(this.bot.getSpeed())) {
        this.distanceRemaining = 0;
      } else {
        this.distanceRemaining -= this.bot.getSpeed();
      }
    } else if (!isFinite(this.distanceRemaining)) {
      this.base.setTargetSpeed(this.distanceRemaining > 0 ? MAX_SPEED : -MAX_SPEED);
    } else {
      let distance = this.distanceRemaining;
      // Nat Pavasant's optimal velocity algorithm
      const newSpeed = this.getAndSetNewTargetSpeed(distance);
      if (this.isNearZero(newSpeed) && this.isOverDriving) {
        distance = 0;
        this.isOverDriving = false;
      }
      if (Math.sign(distance * newSpeed) !== -1) {
        this.isOverDriving = this.base.getDistanceTraveledUntilStop(newSpeed) > Math.abs(distance);
      }
      this.distanceRemaining = distance - newSpeed;
    }
  }

  private getAndSetNewTargetSpeed(distance: number): number {
    const speed = this.base.getNewTargetSpeed(this.bot.getSpeed(), distance);
    this.base.setTargetSpeed(speed);
    return speed;
  }

  // ---------------------------------------------------------------------------
  // Setters called by Bot (override the BaseBotInternals simple setters)
  // ---------------------------------------------------------------------------

  setTurnRate(turnRate: number): void {
    this.overrideTurnRate = false;
    this.turnRemaining = toInfiniteValue(turnRate);
    this.base.setTurnRate(turnRate);
  }

  setGunTurnRate(gunTurnRate: number): void {
    this.overrideGunTurnRate = false;
    this.gunTurnRemaining = toInfiniteValue(gunTurnRate);
    this.base.setGunTurnRate(gunTurnRate);
  }

  setRadarTurnRate(radarTurnRate: number): void {
    this.overrideRadarTurnRate = false;
    this.radarTurnRemaining = toInfiniteValue(radarTurnRate);
    this.base.setRadarTurnRate(radarTurnRate);
  }

  setTargetSpeed(targetSpeed: number): void {
    this.overrideTargetSpeed = false;
    if (targetSpeed > 0) {
      this.distanceRemaining = Infinity;
    } else if (targetSpeed < 0) {
      this.distanceRemaining = -Infinity;
    } else {
      this.distanceRemaining = 0;
    }
    this.base.setTargetSpeed(targetSpeed);
  }

  setForward(distance: number): void {
    if (isNaN(distance)) throw new Error("'distance' cannot be NaN");
    this.overrideTargetSpeed = true;
    this.getAndSetNewTargetSpeed(distance);
    this.distanceRemaining = distance;
  }

  setTurnLeft(degrees: number): void {
    this.overrideTurnRate = true;
    this.turnRemaining = degrees;
    this.base.setTurnRate(degrees);
  }

  setTurnGunLeft(degrees: number): void {
    this.overrideGunTurnRate = true;
    this.gunTurnRemaining = degrees;
    this.base.setGunTurnRate(degrees);
  }

  setTurnRadarLeft(degrees: number): void {
    this.overrideRadarTurnRate = true;
    this.radarTurnRemaining = degrees;
    this.base.setRadarTurnRate(degrees);
  }

  // ---------------------------------------------------------------------------
  // IStopResumeListener
  // ---------------------------------------------------------------------------

  onStop(): void {
    this.savedPreviousDirection = this.previousDirection;
    this.savedPreviousGunDirection = this.previousGunDirection;
    this.savedPreviousRadarDirection = this.previousRadarDirection;
    this.savedDistanceRemaining = this.distanceRemaining;
    this.savedTurnRemaining = this.turnRemaining;
    this.savedGunTurnRemaining = this.gunTurnRemaining;
    this.savedRadarTurnRemaining = this.radarTurnRemaining;
  }

  onResume(): void {
    this.previousDirection = this.savedPreviousDirection;
    this.previousGunDirection = this.savedPreviousGunDirection;
    this.previousRadarDirection = this.savedPreviousRadarDirection;
    this.distanceRemaining = this.savedDistanceRemaining;
    this.turnRemaining = this.savedTurnRemaining;
    this.gunTurnRemaining = this.savedGunTurnRemaining;
    this.radarTurnRemaining = this.savedRadarTurnRemaining;
  }

  // ---------------------------------------------------------------------------
  // Blocking movement helpers (called by Bot)
  // ---------------------------------------------------------------------------

  forward(distance: number): void {
    if (this.bot.isStopped()) {
      this.bot.go();
    } else {
      this.setForward(distance);
      this.waitFor(() => this.distanceRemaining === 0 && this.bot.getSpeed() === 0);
    }
  }

  turnLeft(degrees: number): void {
    if (this.bot.isStopped()) {
      this.bot.go();
    } else {
      this.setTurnLeft(degrees);
      this.waitFor(() => this.turnRemaining === 0);
    }
  }

  turnGunLeft(degrees: number): void {
    if (this.bot.isStopped()) {
      this.bot.go();
    } else {
      this.setTurnGunLeft(degrees);
      this.waitFor(() => this.gunTurnRemaining === 0);
    }
  }

  turnRadarLeft(degrees: number): void {
    if (this.bot.isStopped()) {
      this.bot.go();
    } else {
      this.setTurnRadarLeft(degrees);
      this.waitFor(() => this.radarTurnRemaining === 0);
    }
  }

  fire(firepower: number): void {
    this.bot.setFire(firepower);
    this.bot.go();
  }

  rescan(): void {
    this.base.eventInterruption.setInterruptible("ScannedBotEvent", true);
    this.base.setRescan();
    this.bot.go();
  }

  stop(overwrite?: boolean): void {
    this.base.setStop(overwrite);
    this.bot.go();
  }

  resume(): void {
    this.base.setResume();
    this.bot.go();
  }

  waitFor(condition: () => boolean): void {
    do {
      this.bot.go();
    } while (this.base.isRunning() && !condition());
  }

  // ---------------------------------------------------------------------------
  // Helpers
  // ---------------------------------------------------------------------------

  private isNearZero(value: number): boolean {
    return Math.abs(value) < 0.00001;
  }
}

function toInfiniteValue(rate: number): number {
  if (rate > 0) return Infinity;
  if (rate < 0) return -Infinity;
  return 0;
}
