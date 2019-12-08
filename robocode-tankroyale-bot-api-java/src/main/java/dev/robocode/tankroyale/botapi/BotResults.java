package dev.robocode.tankroyale.botapi;

import lombok.Builder;
import lombok.Value;

/** Individual bot results */
@Value
@Builder
public final class BotResults {
  /** Identifier of the bot used in this battle */
  int id;
  /** Rank/placement of the bot, where 1 is 1st place, 4 is 4th place etc. */
  int rank;
  /** Survival score gained whenever another bot is defeated */
  double survival;
  /** Last survivor score as last survivor in a round */
  double lastSurvivorBonus;
  /** Bullet damage given */
  double bulletDamage;
  /** Bullet kill bonus */
  double bulletKillBonus;
  /** Ram damage given */
  double ramDamage;
  /** Ram kill bonus */
  double ramKillBonus;
  /** Total score */
  double totalScore;
  /** Number of 1st places */
  int firstPlaces;
  /** Number of 2nd places */
  int secondPlaces;
  /** Number of 3rd places */
  int thirdPlaces;
}
