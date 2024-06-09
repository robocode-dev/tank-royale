package dev.robocode.tankroyale.botapi;

/**
 * Predefined game types.<p>
 * These game types are described <a href="https://robocode-dev.github.io/tank-royale/articles/game_types.html">here</a>.
 */
public final class GameType {

    // Hide constructor to prevent instantiation
    private GameType() {
    }

    /**
     * Classic (standard) battle with a minimum of 2 bots battling each other on an arena size of 800 x 600 units.
     */
    public static final String CLASSIC = "classic";

    /**
     * Melee battle with a minimum of 10 bots battling each other on an arena of 1000 x 1000 units.
     */
    public static final String MELEE = "melee";

    /**
     * One versus one (1-vs-1) battle between exactly two bots alone on an arena of 1000 x 1000 units.
     */
    public static final String ONE_VS_ONE = "1v1";
}
