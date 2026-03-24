/**
 * Represents the state of a bullet at a specific turn.
 */
export class BulletState {
  readonly bulletId: number;
  readonly ownerId: number;
  readonly power: number;
  readonly x: number;
  readonly y: number;
  readonly direction: number;
  readonly color: string | null;

  constructor(
    bulletId: number,
    ownerId: number,
    power: number,
    x: number,
    y: number,
    direction: number,
    color: string | null,
  ) {
    this.bulletId = bulletId;
    this.ownerId = ownerId;
    this.power = power;
    this.x = x;
    this.y = y;
    this.direction = direction;
    this.color = color;
  }

  /** Returns the speed of the bullet measured in units per turn. */
  get speed(): number {
    return 20 - 3 * this.power;
  }
}
