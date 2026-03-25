import { BotState as SchemaBotState } from "../protocol/schema.js";
import { BotState } from "../BotState.js";
import { ColorUtil } from "../util/ColorUtil.js";

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
      ColorUtil.fromHexColor(s.bodyColor ?? null),
      ColorUtil.fromHexColor(s.turretColor ?? null),
      ColorUtil.fromHexColor(s.radarColor ?? null),
      ColorUtil.fromHexColor(s.bulletColor ?? null),
      ColorUtil.fromHexColor(s.scanColor ?? null),
      ColorUtil.fromHexColor(s.tracksColor ?? null),
      ColorUtil.fromHexColor(s.gunColor ?? null),
      s.isDebuggingEnabled,
    );
  }
}
