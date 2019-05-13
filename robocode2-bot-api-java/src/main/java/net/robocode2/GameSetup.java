package net.robocode2;

import lombok.Builder;
import lombok.Value;

/** Game setup retrieved when game is started. */
@Value
@Builder
public class GameSetup {
  /** Game type, e.g. "melee" */
  String gameType;
  /** Width of the arena measured in pixels */
  int arenaWidth;
  /** Height of the arena measured in pixels */
  int arenaHeight;
  /** number of rounds in a battle */
  int numberOfRounds;
  /**
   * Gun cooling rate. The gun needs to cool down to a gun heat of zero before the gun is able to
   * fire.
   */
  double gunCoolingRate;
  /**
   * Maximum number of inactive turns allowed, where a bot does not take any action before it is
   * zapped by the game
   */
  int inactivityTurns;
  /** Timeout in milliseconds for sending intent after having received 'tick' message */
  int turnTimeout;
  /**
   * Time limit in milliseconds for sending ready message after having received 'new battle' message
   */
  int readyTimeout;
}
