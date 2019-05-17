package net.robocode2.events;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Value;

/** Event occurring when a bot has collided with another bot */
@Value
@EqualsAndHashCode(callSuper = true)
@Builder
public class BotHitBotEvent extends GameEvent {
    /** ID of the victim bot that got hit */
    int victimId;
    /** ID of the bot that hit another bot */
    int botId;
    /** Remaining energy level of the victim bot */
    double energy;
    /** X coordinate of victim bot */
    double x;
    /** Y coordinate of victim bot */
    double y;
    /** Flag specifying, if the victim bot got rammed */
    boolean rammed;
}
