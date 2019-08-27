package dev.robocode.tankroyale.botapi.mapper;

import dev.robocode.tankroyale.botapi.BotState;
import lombok.NonNull;
import lombok.experimental.UtilityClass;

/** Utility class for mapping a bot state */
@UtilityClass
class BotStateMapper {

  BotState map(@NonNull final dev.robocode.tankroyale.schema.BotState source) {
    return BotState.builder()
        .energy(source.getEnergy())
        .x(source.getX())
        .y(source.getY())
        .direction((source.getDirection()))
        .gunDirection(source.getGunDirection())
        .radarDirection(source.getRadarDirection())
        .radarSweep(source.getRadarSweep())
        .speed(source.getSpeed())
        .build();
  }
}
