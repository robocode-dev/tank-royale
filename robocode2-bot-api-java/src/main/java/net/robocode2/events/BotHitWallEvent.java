package net.robocode2.events;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Value;

/** Event occurring when the bot has hit a wall */
@Value
@EqualsAndHashCode(callSuper = true)
@Builder
public class BotHitWallEvent extends GameEvent {
    /** ID of the victim bot that hit the wall */
    int victimId;
}
