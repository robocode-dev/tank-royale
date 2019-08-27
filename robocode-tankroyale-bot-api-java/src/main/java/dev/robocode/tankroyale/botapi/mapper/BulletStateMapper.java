package dev.robocode.tankroyale.botapi.mapper;

import dev.robocode.tankroyale.botapi.BulletState;
import lombok.NonNull;
import lombok.experimental.UtilityClass;
import lombok.val;

import java.util.ArrayList;
import java.util.List;

/** Utility class for mapping a bot state */
@UtilityClass
class BulletStateMapper {

  BulletState map(@NonNull final dev.robocode.tankroyale.schema.BulletState source) {
    return BulletState.builder()
        .bulletId(source.getBulletId())
        .ownerId(source.getOwnerId())
        .power(source.getPower())
        .x(source.getX())
        .y(source.getY())
        .direction(source.getDirection())
        .speed(source.getSpeed())
        .build();
  }

  List<BulletState> map(@NonNull final List<dev.robocode.tankroyale.schema.BulletState> source) {
    val bulletStates = new ArrayList<BulletState>();
    source.forEach(bulletState -> bulletStates.add(map(bulletState)));
    return bulletStates;
  }
}
