import { GameSetup as SchemaGameSetup } from "../protocol/schema.js";
import { GameSetup } from "../GameSetup.js";

/** Maps schema GameSetup to API GameSetup. */
export class GameSetupMapper {
  static map(s: SchemaGameSetup): GameSetup {
    return new GameSetup(
      s.gameType,
      s.arenaWidth,
      s.arenaHeight,
      s.numberOfRounds,
      s.gunCoolingRate,
      s.maxInactivityTurns,
      s.turnTimeout,
      s.readyTimeout,
    );
  }
}
