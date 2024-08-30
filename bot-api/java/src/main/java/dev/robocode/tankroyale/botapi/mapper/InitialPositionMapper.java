package dev.robocode.tankroyale.botapi.mapper;

import dev.robocode.tankroyale.botapi.InitialPosition;

/**
 * Utility class for mapping an initial starting position.
 */
public final class InitialPositionMapper {

    // Hide constructor to prevent instantiation
    private InitialPositionMapper() {
    }

    public static dev.robocode.tankroyale.schema.game.InitialPosition map(final InitialPosition source) {
        if (source == null) return null;

        var initialPosition = new dev.robocode.tankroyale.schema.game.InitialPosition();
        initialPosition.setX(source.getX());
        initialPosition.setY(source.getY());
        initialPosition.setDirection(source.getDirection());
        return initialPosition;
    }
}
