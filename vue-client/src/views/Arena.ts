import Vue from "vue";
import { Component } from "vue-property-decorator";
import { Server } from "@/server/Server";
import { Point } from "@/schemas/Types";
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

import state from "@/store/store";

class Explosion {
  public position: Point;
  public size: number;

  constructor(position: Point, size: number) {
    this.position = position;
    this.size = size;
  }
}

@Component
export default class Arena extends Vue {
  private server: Server = Server.instance();

  private canvas: any;
  private ctx: any;

  private isRunning: boolean = state.loadIsRunning();
  private isPaused: boolean = state.loadIsPaused();

  private lastTickEvent?: TickEventForObserver | null = state.loadTickEvent();

  public mounted() {
    this.canvas = document.getElementById("canvas");
    this.ctx = this.canvas.getContext("2d");

    if (this.lastTickEvent) {
      this.draw();
    } else {
      this.clearCanvas();
    }

    const server = this.server;
    const self = this;

    server.tickEvent.on((event) => {
      self.onTick(event);
    });
    server.gameStartedEvent.on((event) => {
      self.onGameStarted(event);
    });
    server.gameAbortedEvent.on((event) => {
      self.onGameAborted(event);
    });
    server.gameEndedEvent.on((event) => {
      self.onGameEnded(event);
    });
    server.gamePausedEvent.on((event) => {
      self.onGamePaused(event);
    });
    server.gameResumedEvent.on((event) => {
      self.onGameResumed(event);
    });
  }

  private setRunning(isRunning: boolean) {
    state.saveIsRunning((this.isRunning = isRunning));
  }

  private setPaused(isPaused: boolean) {
    state.saveIsPaused((this.isPaused = isPaused));
  }

  private startGame() {
    console.info("<-startGame");
    const gameSetup = state.loadGameSetup();
    if (gameSetup === null) {
      throw new Error("startGame: No game setup");
    }
    this.server.sendStartGame(gameSetup, state.loadSelectedBots());
  }

  private stopGame() {
    console.info("<-stopGame");
    this.server.sendStopGame();
  }

  private pauseGame() {
    console.info("<-pauseGame");
    this.server.sendPauseGame();
  }

  private resumeGame() {
    console.info("<-resumeGame");
    this.server.sendResumeGame();
  }

  private onGameStarted(event: GameStartedEventForObserver) {
    console.log("->gameStarted");
    this.setRunning(true);
    state.saveTickEvent((this.lastTickEvent = null));
  }

  private onGameAborted(event: GameAbortedEventForObserver) {
    console.log("->gameAborted");
    this.setRunning(false);
    this.setPaused(false);
  }

  private onGameEnded(event: GameEndedEventForObserver) {
    console.log("->gameEnded");
    this.setRunning(false);
    this.setPaused(false);
  }

  private onGamePaused(event: GamePausedEventForObserver) {
    console.log("->gamePaused");
    this.setPaused(true);
  }

  private onGameResumed(event: GameResumedEventForObserver) {
    console.log("->gameResumed");
    this.setPaused(false);
  }

  private onTick(event: TickEventForObserver) {
    console.log("->tick");

    this.lastTickEvent = event;

    const botPositions: Point[] = [];
    const explosions: Explosion[] = [];

    if (this.lastTickEvent.botStates) {
      this.lastTickEvent.botStates.forEach((bot) => {
        if (bot.id && bot.position) {
          botPositions[bot.id] = bot.position;
        }
      });
    }

    if (this.lastTickEvent.events) {
      this.lastTickEvent.events.forEach((evt) => {
        switch (evt.type) {
          case EventType.BotDeathEvent:
            explosions.push(
              new Explosion(botPositions[(evt as BotDeathEvent).victimId], 40),
            );
            break;
          case EventType.BulletHitBotEvent:
            const bulletHitBotEvent = evt as BulletHitBotEvent;
            if (bulletHitBotEvent.bullet && bulletHitBotEvent.bullet.position) {
              explosions.push(
                new Explosion(bulletHitBotEvent.bullet.position, 15),
              );
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

    state.saveTickEvent(this.lastTickEvent);

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
          const pos = bullet.position;
          if (pos && bullet.power) {
            this.drawBullet(pos.x, pos.y, bullet.power);
          }
        });
      }

      if (this.lastTickEvent.botStates) {
        this.lastTickEvent.botStates.forEach((bot) => {
          const pos = bot.position;
          if (pos) {
            this.drawBot(pos.x, pos.y, bot);
          }
        });
      }

      if (this.lastTickEvent.events) {
        this.lastTickEvent.events
          .filter((event) => event.type === EventType.ScannedBotEvent)
          .forEach((scanEvent) => {
            const pos = (scanEvent as ScannedBotEvent).position;
            if (pos) {
              this.fillCircle(pos.x, pos.y, 18, "rgba(255, 255, 0, 1.0)");
            }
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
    ctx.arc(
      10,
      0,
      15,
      (7 * Math.PI) / 10,
      Math.PI * 2 - (7 * Math.PI) / 10,
      false,
    );
    ctx.arc(
      12,
      0,
      13,
      Math.PI * 2 - (7 * Math.PI) / 10,
      (7 * Math.PI) / 10,
      true,
    );
    ctx.fill();

    ctx.beginPath();
    ctx.arc(0, 0, 4, 0, 2 * Math.PI);
    ctx.fill();

    ctx.restore();
  }

  private drawScanField(
    x: number,
    y: number,
    direction: number,
    spreadAngle: number,
  ) {
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
