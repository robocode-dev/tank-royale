import { Point } from "./Types";

export interface BotState {
  id: number;
  energy: number;
  position: Point;
  direction: number;
  gunDirection: number;
  radarDirection: number;
  radarSweep: number;
  speed: number;
}

export interface BulletState {
  ownerId: number;
  power: number;
  position: Point;
  direction: number;
  speed: number;
}
