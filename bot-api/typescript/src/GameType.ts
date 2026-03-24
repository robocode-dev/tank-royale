/**
 * Predefined game types.
 * These game types are described at https://robocode-dev.github.io/tank-royale/articles/game_types.html
 */
export const GameType = {
  /**
   * Classic (standard) battle with a minimum of 2 bots battling each other on an arena size of 800 x 600 units.
   */
  CLASSIC: "classic",

  /**
   * Melee battle with a minimum of 10 bots battling each other on an arena of 1000 x 1000 units.
   */
  MELEE: "melee",

  /**
   * One versus one (1-vs-1) battle between exactly two bots alone on an arena of 1000 x 1000 units.
   */
  ONE_VS_ONE: "1v1",
} as const;

export type GameType = (typeof GameType)[keyof typeof GameType];
