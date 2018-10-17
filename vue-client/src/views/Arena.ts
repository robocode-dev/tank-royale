import Vue from "vue";
import { Component } from "vue-property-decorator";
import ReconnectingWebSocket from "reconnectingwebsocket";
import { Point } from "@/schemas/Types";
import { BotState } from "@/schemas/States";
import { MessageType } from "@/schemas/Messages";
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
import { ServerHandshake } from "@/schemas/Comm";

import state from "@/store/store";
import { Command } from "@/schemas/Command";

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
  private isRunning: boolean = state.loadIsRunning();
  private isPaused: boolean = state.loadIsPaused();

  private canvas: any;
  private ctx: any;

  private socket: any;
  private clientKey?: string;

  private lastTickEvent?: TickEventForObserver | null = state.loadTickEvent();

  public mounted() {
    this.canvas = document.getElementById("canvas");
    this.ctx = this.canvas.getContext("2d");

    if (this.lastTickEvent) {
      this.draw();
    } else {
      this.clearCanvas();
    }

    const socket = new ReconnectingWebSocket(state.loadServerUrl());
    this.socket = socket;

    const vm = this;

    socket.onmessage = (event: any) => {
      console.log("ws message: " + event.data);

      const message = JSON.parse(event.data);
      switch (message.type) {
        case MessageType.ServerHandshake:
          vm.onServerHandshake(message);
          break;
        case EventType.GameStartedEventForObserver:
          vm.onGameStarted(message);
          break;
        case EventType.TickEventForObserver:
          vm.onTick(message);
          break;
        case EventType.GameAbortedEventForObserver:
          vm.onGameAborted(message);
          break;
        case EventType.GameEndedEventForObserver:
          vm.onGameEnded(message);
          break;
        case EventType.GamePausedEventForObserver:
          vm.onGamePaused(message);
          break;
        case EventType.GameResumedEventForObserver:
          vm.onGameResumed(message);
          break;
      }
      const canvasDiv = document.getElementById("canvas");
    };
  }

  private onServerHandshake(serverHandshake: ServerHandshake) {
    console.log("->serverHandshake");

    this.clientKey = serverHandshake.clientKey;
    this.sendControllerHandshake();

    if (!this.isRunning) {
      this.startGame();
    }
  }

  private sendControllerHandshake() {
    console.log("<-controllerHandshake");

    this.socket.send(
      JSON.stringify({
        clientKey: this.clientKey,
        type: "controllerHandshake",
        name: "Robocode 2 Game Controller",
        version: "0.1.0",
        author: "Flemming N. Larsen <fnl@users.sourceforge.net>",
      }),
    );
  }

  private setRunning(isRunning: boolean) {
    state.saveIsRunning((this.isRunning = isRunning));
  }

  private setPaused(isPaused: boolean) {
    state.saveIsPaused((this.isPaused = isPaused));
  }

  private startGame() {
    console.info("<-startGame");

    this.socket.send(
      JSON.stringify({
        clientKey: this.clientKey,
        type: Command.StartGame,
        gameSetup: state.loadGameSetup(),
        botAddresses: state.loadSelectedBots(),
      }),
    );
  }

  private stopGame() {
    console.info("<-stopGame");

    this.socket.send(
      JSON.stringify({
        clientKey: this.clientKey,
        type: Command.StopGame,
      }),
    );
  }

  private pauseGame() {
    console.info("<-pauseGame");

    this.socket.send(
      JSON.stringify({
        clientKey: this.clientKey,
        type: Command.PauseGame,
      }),
    );
  }

  private resumeGame() {
    console.info("<-resumeGame");

    this.socket.send(
      JSON.stringify({
        clientKey: this.clientKey,
        type: Command.ResumeGame,
      }),
    );
  }

  private onGameStarted(event: GameStartedEventForObserver) {
    this.setRunning(true);

    console.log("->gameStarted");

    state.saveTickEvent((this.lastTickEvent = null));
  }

  private onGameAborted(event: GameAbortedEventForObserver) {
    this.setRunning(false);
    this.setPaused(false);
  }

  private onGameEnded(event: GameEndedEventForObserver) {
    this.setRunning(false);
    this.setPaused(false);
  }

  private onGamePaused(event: GamePausedEventForObserver) {
    this.setPaused(true);
  }

  private onGameResumed(event: GameResumedEventForObserver) {
    this.setPaused(false);
  }

  private onTick(event: TickEventForObserver) {
    console.log("->tickEvent");

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
