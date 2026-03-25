import { InitialPosition as SchemaInitialPosition } from "../protocol/schema.js";
import { InitialPosition } from "../InitialPosition.js";

/** Maps API InitialPosition to schema InitialPosition (for handshake). */
export class InitialPositionMapper {
  static map(pos: InitialPosition | null | undefined): SchemaInitialPosition | null {
    if (pos == null) return null;
    return {
      x: pos.x ?? null,
      y: pos.y ?? null,
      direction: pos.direction ?? null,
    };
  }
}
