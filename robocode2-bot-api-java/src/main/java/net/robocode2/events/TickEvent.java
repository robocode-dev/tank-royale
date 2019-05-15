package net.robocode2.events;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Value;
import net.robocode2.BotState;
import net.robocode2.BulletState;

import java.util.List;

/** Event occurring whenever a new turn in a round has started */
@Value
@EqualsAndHashCode(callSuper = true)
@Builder
public class TickEvent extends GameEvent {
    /** Current round number */
    int roundNumber;
    /** Current state of this bot */
    BotState botState;
    /** Current state of the bullets fired by this bot */
    List<BulletState> bulletStates;
    /** Events occurring in the turn relevant for this bot */
    List<GameEvent> events;
}
