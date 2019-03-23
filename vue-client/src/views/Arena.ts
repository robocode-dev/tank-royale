import Vue from "vue";
import { Component } from "vue-property-decorator";
import { Server } from "@/server/Server";
import { BotState } from "@/schemas/States";
import {
  EventType,
  GameStartedEventForObserver,
  GameAbortedEventForObserver,
  GameEndedEventForObserver,
  GamePausedEventForObserver,
  GameResumedEventForObserver,
  TickEventForObserver,
  BotDeathEvent,
  BulletHitBotEvent,
  ScannedBotEvent,
} from "@/schemas/Events";
import { Modal } from "bootstrap-vue";
import { BotResultsForObservers } from "@/schemas/BotResults";

class Explosion {
  public x: number;
  public y: number;
  public size: number;

  constructor(x: number, y: number, size: number) {
    this.x = x;
    this.y = y;
    this.size = size;
  }
}

@Component
export default class Arena extends Vue {
  private canvas: any;
  private ctx: any;

  private isRunning: boolean = Server.isGameRunning();
  private isPaused: boolean = Server.isGamePaused();

  private lastTickEvent: TickEventForObserver | null = Server.getLastTickEvent();

  private numberOfPlayedRounds: number = 0;
  private results: any = [];

  public showResultsModal() {
    (this.$refs.resultsModal as Modal).show();
  }

  public hideResultsModal() {
    (this.$refs.resultsModal as Modal).hide();

    this.clearCanvas();
  }

  public getResults(): any {
    return this.results;
  }

  public mounted() {
    this.canvas = document.getElementById("canvas");
    this.ctx = this.canvas.getContext("2d");

    if (this.lastTickEvent) {
      this.draw();
    } else {
      this.clearCanvas();
    }

    const self = this;

    Server.tickEvent.on((event) => {
      self.onTick(event);
    });
    Server.gameStartedEvent.on((event) => {
      self.onGameStarted(event);
    });
    Server.gameAbortedEvent.on((event) => {
      self.onGameAborted(event);
    });
    Server.gameEndedEvent.on((event) => {
      self.onGameEnded(event);
    });
    Server.gamePausedEvent.on((event) => {
      self.onGamePaused(event);
    });
    Server.gameResumedEvent.on((event) => {
      self.onGameResumed(event);
    });
  }

  private startGame() {
    console.info("<-startGame");
    Server.sendStartGame();
  }

  private stopGame() {
    console.info("<-stopGame");
    Server.sendStopGame();
  }

  private pauseGame() {
    console.info("<-pauseGame");
    Server.sendPauseGame();
  }

  private resumeGame() {
    console.info("<-resumeGame");
    Server.sendResumeGame();
  }

  private onGameStarted(event: GameStartedEventForObserver) {
    console.log("->gameStarted");
    this.updateRunningAndPausedStates();
  }

  private onGameAborted(event: GameAbortedEventForObserver) {
    console.log("->gameAborted");
    this.updateRunningAndPausedStates();
  }

  private onGameEnded(event: GameEndedEventForObserver) {
    console.log("->gameEnded");
    this.updateRunningAndPausedStates();

    this.numberOfPlayedRounds = event.numberOfRounds;
    this.results = [];

    const botResults: BotResultsForObservers[] = event.results;
    if (botResults) {
      let rank = 1;
      botResults.forEach((r) => {
        this.results.push({
          "Rank": rank++,
          "Robot Name": r.name + (r.version ? " " + r.version : ""),
          "Total Score": r.totalScore,
          "Survival": r.survival,
          "Surv Bonus": r.lastSurvivorBonus,
          "Bullet Dmg": r.bulletDamage,
          "Bullet Bonus": r.bulletKillBonus,
          "Ram Damage": r.ramDamage,
          "Ram Bonus": r.ramKillBonus,
          "#1": r.firstPlaces,
          "#2": r.secondPlaces,
          "#3": r.thirdPlaces,
        });
      });
    }

    this.showResultsModal();
  }

  private onGamePaused(event: GamePausedEventForObserver) {
    console.log("->gamePaused");
    this.updateRunningAndPausedStates();
  }

  private onGameResumed(event: GameResumedEventForObserver) {
    console.log("->gameResumed");
    this.updateRunningAndPausedStates();
  }

  private updateRunningAndPausedStates() {
    this.isRunning = Server.isGameRunning();
    this.isPaused = Server.isGamePaused();
  }

  private onTick(event: TickEventForObserver) {
    console.log("->tick");

    this.lastTickEvent = event;

    const botXs: number[] = [];
    const botYs: number[] = [];
    const explosions: Explosion[] = [];

    if (this.lastTickEvent.botStates) {
      this.lastTickEvent.botStates.forEach((bot) => {
        if (bot.id && botXs && botYs) {
          botXs[bot.id] = bot.x;
          botYs[bot.id] = bot.x;
        }
      });
    }

    if (this.lastTickEvent.events) {
      this.lastTickEvent.events.forEach((evt) => {
        switch (evt.type) {
          case EventType.BotDeathEvent:
            const victimId = (evt as BotDeathEvent).victimId;
            explosions.push(new Explosion(botXs[victimId], botYs[victimId], 40));
            break;
          case EventType.BulletHitBotEvent:
            const bulletHitBotEvent = evt as BulletHitBotEvent;
            if (bulletHitBotEvent.bullet) {
              explosions.push(new Explosion(bulletHitBotEvent.bullet.x, bulletHitBotEvent.bullet.y, 15));
            }
            break;
          case EventType.ScannedBotEvent:
            break;
          default:
            console.error("Unknown event type: " + evt.type);
        }
      });
    }

    this.lastTickEvent.explosions = explosions;

    this.draw();

    try {
      explosions.forEach((explosion) => {
        explosion.size -= 5;
        if (explosion.size <= 0) {
          explosions.splice(explosions.indexOf(explosion), 1);
        }
      });
    } catch (err) {
      console.error(err);
    }
  }

  private draw() {
    this.clearCanvas();

    if (this.lastTickEvent) {
      if (this.lastTickEvent.bulletStates) {
        this.lastTickEvent.bulletStates.forEach((bullet) => {
          if (bullet.power) {
            this.drawBullet(bullet.x, bullet.y, bullet.power);
          }
        });
      }

      if (this.lastTickEvent.botStates) {
        this.lastTickEvent.botStates.forEach((bot) => {
          this.drawBot(bot.x, bot.y, bot);
        });
      }

      if (this.lastTickEvent.events) {
        this.lastTickEvent.events.filter((event) => event.type === EventType.ScannedBotEvent).forEach((scanEvent) => {
          const scannedBotEvent = scanEvent as ScannedBotEvent;
          this.fillCircle(scannedBotEvent.x, scannedBotEvent.y, 18, "rgba(255, 255, 0, 1.0)");
        });
      }

      if (this.lastTickEvent.explosions) {
        this.lastTickEvent.explosions.forEach((explosion) => {
          const pos = explosion.position;
          if (pos) {
            this.fillCircle(pos.x, pos.y, explosion.size, "red");
          }
        });
      }
    }
  }

  private clearCanvas() {
    this.ctx.fillStyle = "black";
    this.ctx.fillRect(0, 0, this.canvas.width, this.canvas.height);
  }

  private drawBullet(x: number, y: number, power: number) {
    const size = Math.max(Math.sqrt(5 * power), 1);
    this.fillCircle(x, y, size, "white");
  }

  private drawBot(x: number, y: number, bot: BotState) {
    this.drawBotBody(x, y, bot.direction);
    this.drawGun(x, y, bot.gunDirection);
    this.drawRadar(x, y, bot.radarDirection);
    this.drawScanField(x, y, bot.radarDirection, bot.radarSweep);
    this.drawLabels(x, y, bot.id, bot.energy);
  }

  private drawBotBody(x: number, y: number, direction: number) {
    const ctx = this.ctx;
    ctx.save();

    ctx.translate(x, y);
    ctx.rotate(this.toRad(direction));

    ctx.fillStyle = "blue";
    ctx.beginPath();
    ctx.fillRect(-18, -18 + 1 + 6, 36, 36 - 2 * 7);

    ctx.fillStyle = "gray";
    ctx.beginPath();
    ctx.fillRect(-18, -18, 36, 6);
    ctx.fillRect(-18, 18 - 6, 36, 6);

    ctx.restore();
  }

  private drawGun(x: number, y: number, direction: number) {
    const ctx = this.ctx;
    ctx.save();

    ctx.translate(x, y);

    ctx.fillStyle = "lightgray";
    ctx.beginPath();
    ctx.arc(0, 0, 10, 0, this.toRad(360));
    ctx.fill();

    ctx.beginPath();
    ctx.rotate(this.toRad(direction));
    ctx.rect(10, -2, 14, 4);
    ctx.fill();

    ctx.restore();
  }

  private drawRadar(x: number, y: number, direction: number) {
    const ctx = this.ctx;
    ctx.save();

    ctx.translate(x, y);
    ctx.rotate(this.toRad(direction));

    ctx.fillStyle = "red";
    ctx.beginPath();
    ctx.arc(10, 0, 15, (7 * Math.PI) / 10, Math.PI * 2 - (7 * Math.PI) / 10, false);
    ctx.arc(12, 0, 13, Math.PI * 2 - (7 * Math.PI) / 10, (7 * Math.PI) / 10, true);
    ctx.fill();

    ctx.beginPath();
    ctx.arc(0, 0, 4, 0, 2 * Math.PI);
    ctx.fill();

    ctx.restore();
  }

  private drawScanField(x: number, y: number, direction: number, spreadAngle: number) {
    const angle = this.toRad(spreadAngle);

    const color = "rgba(0, 255, 255, 0.5)";

    const ctx = this.ctx;
    ctx.save();
    ctx.translate(x, y);
    ctx.rotate(this.toRad(direction));

    if (Math.abs(angle) < 0.0001) {
      ctx.strokeStyle = color;
      ctx.lineTo(1200, 0);
      ctx.stroke();
    } else {
      ctx.fillStyle = color;
      ctx.beginPath();
      ctx.moveTo(0, 0);
      ctx.arc(0, 0, 1200, 0, angle, angle < 0);
      ctx.lineTo(0, 0);
      ctx.fill();
    }

    ctx.restore();
  }

  private drawLabels(x: number, y: number, botId: number, energy: number) {
    const ctx = this.ctx;
    ctx.save();
    ctx.fillStyle = "white";
    ctx.font = "10px Arial";

    const idStr = "" + botId;
    const energyStr = energy.toFixed(1);
    const idWidth = ctx.measureText(idStr).width;
    const energyWidth = ctx.measureText(energyStr).width;

    ctx.fillText(idStr, x - idWidth / 2, y + 30 + 10);
    ctx.fillText(energyStr, x - energyWidth / 2, y - 30);

    ctx.restore();
  }

  private fillCircle(x: number, y: number, radius: number, color: string) {
    const ctx = this.ctx;
    ctx.fillStyle = color;
    ctx.beginPath();
    ctx.arc(x, y, radius, 0, 2 * Math.PI);
    ctx.fill();
  }

  private toRad(degrees: number): number {
    return (degrees * Math.PI) / 180;
  }
}
