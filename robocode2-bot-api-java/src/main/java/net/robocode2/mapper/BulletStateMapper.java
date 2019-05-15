package net.robocode2.mapper;

import lombok.NonNull;
import lombok.experimental.UtilityClass;
import lombok.val;
import net.robocode2.BulletState;

import java.util.ArrayList;
import java.util.List;

/** Utility class for mapping a bot state */
@UtilityClass
class BulletStateMapper {

  BulletState map(@NonNull final net.robocode2.schema.BulletState source) {
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

  List<BulletState> map(@NonNull final List<net.robocode2.schema.BulletState> source) {
    val bulletStates = new ArrayList<BulletState>();
    source.forEach(bulletState -> bulletStates.add(map(bulletState)));
    return bulletStates;
  }
}
