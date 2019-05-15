package net.robocode2.mapper;

import lombok.NonNull;
import lombok.experimental.UtilityClass;
import lombok.val;
import net.robocode2.BotException;
import net.robocode2.events.BotDeathEvent;
import net.robocode2.events.GameEvent;
import net.robocode2.events.TickEvent;

import java.util.ArrayList;
import java.util.List;

/** Utility class for mapping events */
@UtilityClass
public class EventMapper {

  public TickEvent map(@NonNull final net.robocode2.schema.TickEventForBot source) {
    return TickEvent.builder()
        .roundNumber(source.getRoundNumber())
        .botState(BotStateMapper.map(source.getBotState()))
        .bulletStates(BulletStateMapper.map(source.getBulletStates()))
        .events(map(source.getEvents()))
        .build();
  }

  private List<GameEvent> map(@NonNull final List<net.robocode2.schema.Event> source) {
    val gameEvents = new ArrayList<GameEvent>();
    source.forEach(event -> gameEvents.add(map(event)));
    return gameEvents;
  }

  private GameEvent map(@NonNull final net.robocode2.schema.Event source) {
    if (source instanceof net.robocode2.schema.BotDeathEvent) {
      return map((net.robocode2.schema.BotDeathEvent) source);
    }
    throw new BotException(
        "No mapping exists for event type: " + source.getClass().getSimpleName());
  }

  private BotDeathEvent map(@NonNull final net.robocode2.schema.BotDeathEvent source) {
    return BotDeathEvent.builder().victimId(source.getVictimId()).build();
  }
}
