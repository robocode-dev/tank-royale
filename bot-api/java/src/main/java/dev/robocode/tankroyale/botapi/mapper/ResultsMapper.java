package dev.robocode.tankroyale.botapi.mapper;

import dev.robocode.tankroyale.botapi.BotResults;

/**
 * Utility class for mapping bot results.
 */
public class ResultsMapper {

    // Hide constructor to prevent instantiation
    private ResultsMapper() {
    }

    public static BotResults map(final dev.robocode.tankroyale.schema.game.ResultsForBot source) {
        return new BotResults(
                source.getRank(),
                source.getSurvival(),
                source.getLastSurvivorBonus(),
                source.getBulletDamage(),
                source.getBulletKillBonus(),
                source.getRamDamage(),
                source.getRamKillBonus(),
                source.getTotalScore(),
                source.getFirstPlaces(),
                source.getSecondPlaces(),
                source.getThirdPlaces());
    }
}
