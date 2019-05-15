package net.robocode2.mapper;

import lombok.NonNull;
import lombok.experimental.UtilityClass;
import net.robocode2.GameSetup;

/** Utility class for mapping a game setup */
@UtilityClass
public class GameSetupMapper {

  public GameSetup map(@NonNull final net.robocode2.schema.GameSetup source) {
    return GameSetup.builder()
        .gameType(source.getGameType())
        .arenaWidth(source.getArenaWidth())
        .arenaHeight(source.getArenaHeight())
        .numberOfRounds(source.getNumberOfRounds())
        .gunCoolingRate(source.getGunCoolingRate())
        .inactivityTurns(source.getInactivityTurns())
        .turnTimeout(source.getTurnTimeout())
        .readyTimeout(source.getReadyTimeout())
        .build();
  }
}
