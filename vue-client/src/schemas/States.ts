import { Point } from "./Types";

export class BotState {
  public id: number;
  public energy: number;
  public position: Point;
  public direction: number;
  public gunDirection: number;
  public radarDirection: number;
  public radarSweep: number;
  public speed: number;

  constructor(
    id: number,
    energy: number,
    position: Point,
    direction: number,
    gunDirection: number,
    radarDirection: number,
    radarSweep: number,
    speed: number,
  ) {
    this.id = id;
    this.energy = energy;
    this.position = position;
    this.direction = direction;
    this.gunDirection = gunDirection;
    this.radarDirection = radarDirection;
    this.radarSweep = radarSweep;
    this.speed = speed;
  }
}

export class BulletState {
  public ownerId?: number;
  public power?: number;
  public position?: Point;
  public direction?: number;
  public speed?: number;
}

export class Explosion {
  public position: Point;
  public size: number;

  constructor(position: Point, size: number) {
    this.position = position;
    this.size = size;
  }
}
