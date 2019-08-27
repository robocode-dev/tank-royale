package dev.robocode.tankroyale.botapi.mapper;

import dev.robocode.tankroyale.botapi.BotResults;
import lombok.NonNull;
import lombok.experimental.UtilityClass;
import lombok.val;

import java.util.ArrayList;
import java.util.List;

/** Utility class for mapping bot results */
@UtilityClass
public class ResultsMapper {

  public List<BotResults> map(@NonNull final List<dev.robocode.tankroyale.schema.BotResultsForBot> source) {
    val botResultsList = new ArrayList<BotResults>();
    source.forEach(botResults -> botResultsList.add(map(botResults)));
    return botResultsList;
  }

  private BotResults map(@NonNull final dev.robocode.tankroyale.schema.BotResultsForBot source) {
    return BotResults.builder()
        .id(source.getId())
        .rank(source.getRank())
        .survival(source.getSurvival())
        .lastSurvivorBonus(source.getLastSurvivorBonus())
        .bulletDamage(source.getBulletDamage())
        .bulletKillBonus(source.getBulletKillBonus())
        .ramDamage(source.getRamDamage())
        .ramKillBonus(source.getRamKillBonus())
        .totalScore(source.getTotalScore())
        .firstPlaces(source.getFirstPlaces())
        .secondPlaces(source.getSecondPlaces())
        .thirdPlaces(source.getThirdPlaces())
        .build();
  }
}
