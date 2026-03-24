/**
 * Represents an ordered pair of x and y coordinates that define a point in a two-dimensional plane.
 */
export class Point {
  readonly x: number;
  readonly y: number;

  constructor(x: number, y: number) {
    this.x = x;
    this.y = y;
  }

  equals(other: unknown): boolean {
    if (this === other) return true;
    if (!(other instanceof Point)) return false;
    return this.x === other.x && this.y === other.y;
  }

  toString(): string {
    return `(x=${this.x}, y=${this.y})`;
  }
}
