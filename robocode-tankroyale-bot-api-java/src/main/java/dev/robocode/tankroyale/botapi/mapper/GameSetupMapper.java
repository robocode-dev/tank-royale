package dev.robocode.tankroyale.botapi.mapper;

import dev.robocode.tankroyale.botapi.GameSetup;
import lombok.NonNull;
import lombok.experimental.UtilityClass;

/** Utility class for mapping a game setup */
@UtilityClass
public class GameSetupMapper {

  public GameSetup map(@NonNull final dev.robocode.tankroyale.schema.GameSetup source) {
    return GameSetup.builder()
        .gameType(source.getGameType())
        .arenaWidth(source.getArenaWidth())
        .arenaHeight(source.getArenaHeight())
        .numberOfRounds(source.getNumberOfRounds())
        .gunCoolingRate(source.getGunCoolingRate())
        .maxInactivityTurns(source.getMaxInactivityTurns())
        .turnTimeout(source.getTurnTimeout())
        .readyTimeout(source.getReadyTimeout())
        .build();
  }
}
