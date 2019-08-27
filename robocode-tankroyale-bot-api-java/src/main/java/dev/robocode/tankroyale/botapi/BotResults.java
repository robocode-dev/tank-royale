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
  int survival;
  /** Last survivor score as last survivor in a round */
  int lastSurvivorBonus;
  /** Bullet damage given */
  int bulletDamage;
  /** Bullet kill bonus */
  int bulletKillBonus;
  /** Ram damage given */
  int ramDamage;
  /** Ram kill bonus */
  int ramKillBonus;
  /** Total score */
  int totalScore;
  /** Number of 1st places */
  int firstPlaces;
  /** Number of 2nd places */
  int secondPlaces;
  /** Number of 3rd places */
  int thirdPlaces;
}
