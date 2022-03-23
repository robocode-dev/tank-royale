package dev.robocode.tankroyale.botapi.mapper;

import dev.robocode.tankroyale.botapi.InitialPosition;

/**
 * Utility class for mapping an initial starting position.
 */
public final class InitialPositionMapper {

    public static dev.robocode.tankroyale.schema.InitialPosition map(final InitialPosition source) {
        if (source == null) return null;

        var initialPosition = new dev.robocode.tankroyale.schema.InitialPosition();
        initialPosition.setX(source.getX());
        initialPosition.setY(source.getY());
        initialPosition.setAngle(source.getAngle());
        return initialPosition;
    }
}
