package dev.robocode.tankroyale.sample.bots;

import dev.robocode.tankroyale.botapi.Bot;
import dev.robocode.tankroyale.botapi.BotInfo;
import dev.robocode.tankroyale.botapi.events.BulletHitBotEvent;
import dev.robocode.tankroyale.botapi.events.ScannedBotEvent;
import dev.robocode.tankroyale.botapi.events.TickEvent;

import java.io.IOException;

/**
 * Fire - a sample bot, original version by Mathew Nelson for Robocode.
 * Modified by Flemming N. Larsen.
 *
 * <p>This bot sits still. Spins gun around. Moves when hit.
 */
public class Fire extends Bot {

  int dist = 50; // Distance to move when we're hit, forward or back

  boolean interrupt; // flag for stop turning the gun temporarily

  /** Main method starts our bot */
  public static void main(String[] args) throws IOException {
    new Fire().start();
  }

  /** Constructor, which loads the bot settings file */
  protected Fire() throws IOException {
    super(BotInfo.fromFile("fire.properties"));
  }

  /** This method runs our bot program, where each command is executed one at a time in a loop. */
  @Override
  public void run() {
    // Set colors
    setBodyColor("#FA0"); // orange
    setGunColor("##FA0"); // orange
    setRadarColor("#F00"); // red
    setBulletColor("#F00"); // red
    setScanColor("#F00"); // red

    // Spin the gun around slowly... forever
    while (isRunning()) {
      if (interrupt) {
        // Stop turning gun if interrupted
        turnGunRight(0);
      } else {
        // Else turn gun 5 more degrees to the right
        turnGunRight(5);
      }
    }
  }

  /** onTick: Every new turn, reset/remove the interrupt */
  public void onTick(TickEvent e) {
    interrupt = false; // no interrupt means that the gun will turn 5 degrees to the right in the run() method
  }

  /** onScannedBot: Fire! */
  @Override
  public void onScannedBot(ScannedBotEvent e) {
    // If the other robot is close by, and we have plenty of life, fire hard!
    double distance = distanceTo(e.getX(), e.getY());
    if (distance < 50 && getEnergy() > 50) {
      fire(3);
    } else {
      // Otherwise, only fire 1
      fire(1);
    }
    interrupt = true; // interrupt/stop turning the gun in the main loop in the run() method
  }

  /** onHitByBullet: Turn perpendicular to the bullet, and move a bit. */
  @Override
  public void onHitByBullet(BulletHitBotEvent e) {
    // Turn perpendicular to the bullet direction
    turnRight(normalizeRelativeDegrees(90 - (e.getBullet().getDirection() - getDirection())));

    // Move forward or backward depending if the distance is positive or negative
    forward(dist);
    dist *= -1; // Change distance, meaning forward or backward direction

    interrupt = true; // interrupt/stop turning the gun in the main loop in the run() method
  }

  /** onBulletHit: Aim at target (where bullet came from) and fire hard. */
  @Override
  public void onBulletHit(BulletHitBotEvent e) {
    // Turn gun to the bullet direction
    double gunBearing = normalizeRelativeDegrees(e.getBullet().getDirection() - getGunDirection());
    turnGunLeft(gunBearing);

    // Fire hard
    fire(3);
  }
}
