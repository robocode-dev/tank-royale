package dev.robocode.tankroyale.sample.bots;

import dev.robocode.tankroyale.botapi.Bot;
import dev.robocode.tankroyale.botapi.BotInfo;
import dev.robocode.tankroyale.botapi.events.BulletHitBotEvent;
import dev.robocode.tankroyale.botapi.events.ScannedBotEvent;

import java.io.IOException;

/**
 * Fire - a sample bot, original version by Mathew Nelson for Robocode.
 * Modified by Flemming N. Larsen.
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
      turnGunRight(5);
    }
  }

  /** onScannedBot: Fire! */
  @Override
  public void onScannedBot(ScannedBotEvent e) {
    // Set bot to stop movement (executed with next command - fire)
    setStop();

    // If the other robot is close by, and we have plenty of life, fire hard!
    double distance = distanceTo(e.getX(), e.getY());
    if (distance < 50 && getEnergy() > 50) {
      fire(3);
    } else {
      // Otherwise, only fire 1
      fire(1);
    }
    // Scan, and resume movement if we did not scan anything
    if (!scan()) {
      setResume();
    }
  }

  /** onHitByBullet: Turn perpendicular to the bullet, and move a bit. */
  @Override
  public void onHitByBullet(BulletHitBotEvent e) {
    // Set bot to resume movement, if it was stopped
    setResume();

    // Turn perpendicular to the bullet direction
    double direction = directionTo(e.getBullet().getX(), e.getBullet().getY());
    turnLeft(normalizeRelativeAngle(90 - (getDirection() - direction)));

    // Move forward or backward depending if the distance is positive or negative
    forward(dist);
    dist *= -1; // Change distance, meaning forward or backward direction

    // Rescan
    setScan();
  }

  /** onBulletHit: Aim at target (where bullet came from) and fire hard. */
  @Override
  public void onBulletHit(BulletHitBotEvent e) {
    System.out.println("onBulletHit: " + getTurnNumber());

    // Set bot to resume movement, if it was stopped
    setResume();

    // Turn gun to the bullet direction
    double direction = directionTo(e.getBullet().getX(), e.getBullet().getY());
    double gunBearing = normalizeRelativeAngle(direction - getGunDirection());
    turnGunLeft(gunBearing);

    // Check that radar is locked (by stopping movement in onScannedBot)
    if (isStopped()) {
      // Fire hard
      fire(3);
    }
  }
}
