import { BulletState as SchemaBulletState } from "../protocol/schema.js";
import { BulletState } from "../BulletState.js";

/** Maps schema BulletState to API BulletState. */
export class BulletStateMapper {
  static map(s: SchemaBulletState): BulletState {
    return new BulletState(s.bulletId, s.ownerId, s.power, s.x, s.y, s.direction, s.color ?? null);
  }

  static mapCollection(states: SchemaBulletState[]): BulletState[] {
    return states.map(BulletStateMapper.map);
  }
}
