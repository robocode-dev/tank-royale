export interface BotState {
  id: number;
  energy: number;
  x: number;
  y: number;
  direction: number;
  gunDirection: number;
  radarDirection: number;
  radarSweep: number;
  speed: number;
}

export interface BulletState {
  ownerId: number;
  power: number;
  x: number;
  y: number;
  direction: number;
  speed: number;
}
