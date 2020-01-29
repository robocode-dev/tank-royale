package dev.robocode.tankroyale.botapi.events;

/** Event occurring when a bot has collided with another bot */
@SuppressWarnings("unused")
public final class BotHitBotEvent extends Event {

  /** ID of the victim bot that got hit */
  private final int victimId;

  /** ID of the bot that hit another bot */
  private final int botId;

  /** Remaining energy level of the victim bot */
  private final double energy;

  /** X coordinate of victim bot */
  private final double x;

  /** Y coordinate of victim bot */
  private final double y;

  /** Flag specifying, if the victim bot got rammed */
  private final boolean rammed;

  public BotHitBotEvent(
      int turnNumber, int victimId, int botId, double energy, double x, double y, boolean rammed) {
    super(turnNumber);
    this.victimId = victimId;
    this.botId = botId;
    this.energy = energy;
    this.x = x;
    this.y = y;
    this.rammed = rammed;
  }

  /** Returns the ID of the victim bot that got hit */
  public int getVictimId() {
    return victimId;
  }

  /** Returns the ID of the bot that hit another bot */
  public int getBotId() {
    return botId;
  }

  /** Returns the remaining energy level of the victim bot */
  public double getEnergy() {
    return energy;
  }

  /** Returns the X coordinate of victim bot */
  public double getX() {
    return x;
  }

  /** Returns the Y coordinate of victim bot */
  public double getY() {
    return y;
  }

  /** Returns true if the victim bot got rammed; false otherwise */
  public boolean isRammed() {
    return rammed;
  }
}
