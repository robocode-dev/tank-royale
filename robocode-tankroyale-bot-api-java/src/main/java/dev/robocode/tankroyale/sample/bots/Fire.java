package dev.robocode.tankroyale.sample.bots;

import dev.robocode.tankroyale.botapi.Bot;
import dev.robocode.tankroyale.botapi.BotInfo;
import dev.robocode.tankroyale.botapi.events.BulletHitBotEvent;
import dev.robocode.tankroyale.botapi.events.HitBotEvent;
import dev.robocode.tankroyale.botapi.events.ScannedBotEvent;

import java.io.IOException;

/**
 * Fire - a sample bot, original version by Mathew Nelson for Robocode. Modified by Flemming N.
 * Larsen.
 *
 * <p>This bot sits still. Spins gun around. Moves when hit.
 */
public class Fire extends Bot {

  int dist = 50; // Distance to move when we're hit, forward or back

  /** Main method starts our bot */
  public static void main(String[] args) throws IOException {
    new Fire().start();
  }

  /** Constructor, which loads the bot settings file */
  protected Fire() throws IOException {
    super(BotInfo.fromFile("/fire.properties"));
  }

  /** This method runs our bot program, where each command is executed one at a time in a loop. */
  @Override
  public void run() {
    // Set colors
    setBodyColor("#FA0"); // orange
    setGunColor("#F70"); // dark orange
    setTurretColor("#F70"); // dark orange
    setRadarColor("#F00"); // red
    setScanColor("#F00"); // red
    setBulletColor("#08F"); // light blue

    // Spin the gun around slowly... forever
    while (isRunning()) {
      // Turn the gun a bit if the bot if the target speed is 0
      System.out.println(getTurnNumber() + " run.turnGunLeft");
      turnGunLeft(5);
    }
  }

  /** onScannedBot: Fire! */
  @Override
  public void onScannedBot(ScannedBotEvent e) {
    // If the other robot is close by, and we have plenty of life, fire hard!
    double distance = distanceTo(e.getX(), e.getY());
    if (distance < 50 && getEnergy() > 50) {
      System.out.println(getTurnNumber() + " onScannedBot.fire(3)");
      fire(3);
    } else {
      // Otherwise, only fire 1
      System.out.println(getTurnNumber() + " onScannedBot.fire(1)");
      fire(1);
    }
    // Rescan
    System.out.println(getTurnNumber() + " onScannedBot.scan()");
    scan();
  }

  /** onHitByBullet: Turn perpendicular to the bullet, and move a bit. */
  @Override
  public void onHitByBullet(BulletHitBotEvent e) {
    // Turn perpendicular to the bullet direction
    System.out.println(getTurnNumber() + " onHitByBullet.turnLeft()");
    turnLeft(normalizeRelativeAngle(90 - (getDirection() - e.getBullet().getDirection())));

    // Move forward or backward depending if the distance is positive or negative
    System.out.println(getTurnNumber() + " onHitByBullet.forward()");
    forward(dist);
    dist *= -1; // Change distance, meaning forward or backward direction

    // Rescan
    System.out.println(getTurnNumber() + " onHitByBullet.scan()");
    scan();
  }

  /** onHitBot: Aim at target and fire hard. */
  @Override
  public void onHitBot(HitBotEvent e) {
    // Turn gun to the bullet direction
    double direction = directionTo(e.getX(), e.getY());
    double gunBearing = normalizeRelativeAngle(direction - getGunDirection());
    System.out.println(getTurnNumber() + " onHitBot.turnGunLeft()");
    turnGunLeft(gunBearing);

    // Fire hard
    System.out.println(getTurnNumber() + " onHitBot.fire()");
    fire(3);
  }
}
