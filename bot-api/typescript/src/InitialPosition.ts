/**
 * Initial starting position containing a start coordinate (x,y) and the shared direction of the body, gun, and radar.
 *
 * The initial position is only used when debugging to request the server to let a bot start at a specific position.
 * Note that initial starting positions must be enabled at the server-side; otherwise the initial starting position
 * is ignored.
 */
export class InitialPosition {
  readonly x: number | null;
  readonly y: number | null;
  readonly direction: number | null;

  constructor(x: number | null, y: number | null, direction: number | null) {
    this.x = x;
    this.y = y;
    this.direction = direction;
  }

  toString(): string {
    if (this.x === null && this.y === null && this.direction === null) return "";
    const strX = this.x === null ? "" : this.x;
    const strY = this.y === null ? "" : this.y;
    const strDirection = this.direction === null ? "" : this.direction;
    return `${strX},${strY},${strDirection}`;
  }

  static fromString(initialPosition: string | null | undefined): InitialPosition | null {
    if (initialPosition == null || initialPosition.trim() === "") return null;
    const trimmed = initialPosition.trim();
    if (trimmed.replace(/[,\s]/g, "") === "") return null;
    const values = trimmed.split(/\s*,\s*|\s+/);
    return InitialPosition.parseInitialPosition(values);
  }

  private static parseInitialPosition(values: string[]): InitialPosition | null {
    if (values.length < 1) return null;
    const x = InitialPosition.parseDouble(values[0]);
    if (values.length < 2) {
      return new InitialPosition(x, null, null);
    }
    const y = InitialPosition.parseDouble(values[1]);
    let direction: number | null = null;
    if (values.length >= 3) {
      direction = InitialPosition.parseDouble(values[2]);
    }
    return new InitialPosition(x, y, direction);
  }

  private static parseDouble(str: string | null | undefined): number | null {
    if (str == null) return null;
    const trimmed = str.trim();
    if (trimmed === "") return null;
    const val = Number(trimmed);
    return isNaN(val) ? null : val;
  }
}
