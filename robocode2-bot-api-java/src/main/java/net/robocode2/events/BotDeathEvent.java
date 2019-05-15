package net.robocode2.events;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Value;
import net.robocode2.BotState;
import net.robocode2.BulletState;

import java.util.List;

/** Event occurring when another bot has died */
@Value
@EqualsAndHashCode(callSuper = true)
@Builder
public class BotDeathEvent extends GameEvent {
    /** ID of the bot that has died */
    int victimId;
}
