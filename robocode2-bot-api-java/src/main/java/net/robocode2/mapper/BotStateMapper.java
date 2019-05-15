package net.robocode2.mapper;

import lombok.NonNull;
import lombok.experimental.UtilityClass;
import net.robocode2.BotState;

/** Utility class for mapping a bot state */
@UtilityClass
class BotStateMapper {

  BotState map(@NonNull final net.robocode2.schema.BotState source) {
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
