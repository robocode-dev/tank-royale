package dev.robocode.tankroyale.sample.bots;

import dev.robocode.tankroyale.botapi.Bot;
import dev.robocode.tankroyale.botapi.BotInfo;
import dev.robocode.tankroyale.botapi.events.HitBotEvent;
import dev.robocode.tankroyale.botapi.events.ScannedBotEvent;

import java.io.IOException;

/**
 * SpinBot - a sample bot, original version by Mathew Nelson for Robocode.
 *
 * <p>Moves in a circle, firing hard when an enemy is detected.
 */
public class SpinBot extends Bot {

  public static void main(String[] args) throws IOException {
    new SpinBot().start();
  }

  protected SpinBot() throws IOException {
    super(BotInfo.fromFile("/SpinBot.json"));
  }

  /** SpinBot's run method - Move in a circle */
  @Override
  public void run() {
    setBodyColor("#00F"); // blue
    setTurretColor("#00F"); // blue
    setRadarColor("#000"); // black
    setScanColor("#FF0"); // yellow

    // Repeat while bot is running
    while (isRunning()) {
      // Tell the game that when we take move, we'll also want to turn right... a lot.
      setTurnLeft(10_000);
      // Limit our speed to 5
      setMaxSpeed(5);
      // Start moving (and turning)
      forward(10_000);
    }
  }

  /** onScannedBot: Fire hard when scanning another bot! */
  @Override
  public void onScannedBot(ScannedBotEvent e) {
    fire(3); // Fire the cannon!
  }

  /**
   * onHitBot: If it's our fault, we'll stop turning and moving, so we need to turn again to keep spinning.
   */
  @Override
  public void onHitBot(HitBotEvent e) {
    double direction = directionTo(e.getX(), e.getY());
    double bearing = calcBearing(direction);
    if (bearing > -10 && bearing < 10) {
      fire(3);
    }
    if (e.isRammed()) {
      turnLeft(10);
    }
  }
}