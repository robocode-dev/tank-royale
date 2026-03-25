import { BotState as SchemaBotState } from "../protocol/schema.js";
import { BotState } from "../BotState.js";

/** Maps schema BotState to API BotState. */
export class BotStateMapper {
  static map(s: SchemaBotState): BotState {
    return new BotState(
      s.isDroid,
      s.energy,
      s.x,
      s.y,
      s.direction,
      s.gunDirection,
      s.radarDirection,
      s.radarSweep,
      s.speed,
      s.turnRate,
      s.gunTurnRate,
      s.radarTurnRate,
      s.gunHeat,
      s.enemyCount,
      s.bodyColor ?? null,
      s.turretColor ?? null,
      s.radarColor ?? null,
      s.bulletColor ?? null,
      s.scanColor ?? null,
      s.tracksColor ?? null,
      s.gunColor ?? null,
      s.isDebuggingEnabled,
    );
  }
}
