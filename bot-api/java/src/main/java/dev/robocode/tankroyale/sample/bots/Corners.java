package dev.robocode.tankroyale.sample.bots;

import dev.robocode.tankroyale.botapi.Bot;
import dev.robocode.tankroyale.botapi.BotInfo;
import dev.robocode.tankroyale.botapi.events.DeathEvent;
import dev.robocode.tankroyale.botapi.events.ScannedBotEvent;

import java.io.IOException;

/**
 * Corners - a sample bot, original version by Mathew Nelson for Robocode.
 *
 * <p>This bot moves to a corner, then swings the gun back and forth. If it dies, it tries a new
 * corner in the next round.
 */
public class Corners extends Bot {

  int enemies; // Number of enemy robots in the game
  static int corner = 0; // Which corner we are currently using
  boolean stopWhenSeeRobot = false; // See goCorner()

  /** Main method starts our bot */
  public static void main(String[] args) throws IOException {
    new Corners().start();
  }

  /** Constructor, which loads the bot settings file */
  Corners() throws IOException {
    super(BotInfo.fromJsonFile("/Corners.json"));
  }

  /** This method runs our bot program, where each command is executed one at a time in a loop. */
  @Override
  public void run() {
    // Set colors
    setBodyColor("#F00"); // red
    setGunColor("#000"); // black
    setRadarColor("#FF0"); // yellow
    setBulletColor("#0F0"); // green
    setScanColor("#0F0"); // green

    // Save # of other bots
    enemies = getEnemyCount();

    // Move to a corner
    goCorner();

    // Initialize gun turn speed to 3
    int gunIncrement = 3;

    // Spin gun back and forth
    while (isRunning()) {
      for (int i = 0; i < 30; i++) {
        turnGunRight(gunIncrement);
      }
      gunIncrement *= -1;
    }
  }

  /** A very inefficient way to get to a corner. Can you do better? */
  private void goCorner() {
    // We don't want to stop when we're just turning...
    stopWhenSeeRobot = false;
    // Turn to face the wall towards our desired corner
    turnLeft(calcBearing(corner));
    // Ok, now we don't want to crash into any robot in our way...
    stopWhenSeeRobot = true;
    // Move to that wall
    forward(5000);
    // Turn to face the corner
    turnRight(90);
    // Move to the corner
    forward(5000);
    // Turn gun to starting point
    turnGunRight(90);
  }

  /** We saw another bot. Stop and fire! */
  @Override
  public void onScannedBot(ScannedBotEvent e) {
    double distance = distanceTo(e.getX(), e.getY());

    // Should we stop, or just fire?
    if (stopWhenSeeRobot) {
      // Stop movement
      stop();
      // Call our custom firing method
      smartFire(distance);
      // Rescan for another robot
      scan();
      // We won't get here if we saw another robot.
      // Okay, we didn't see another robot... start moving or turning again.
      resume();
    } else {
      smartFire(distance);
    }
  }

  /**
   * Custom fire method that determines firepower based on distance.
   *
   * @param distance the distance to the robot to fire at
   */
  private void smartFire(double distance) {
    if (distance > 200 || getEnergy() < 15) {
      fire(1);
    } else if (distance > 50) {
      fire(2);
    } else {
      fire(3);
    }
  }

  /** We died. Figure out if we need to switch to another corner. */
  @Override
  public void onDeath(DeathEvent e) {
    // Well, others should never be 0, but better safe than sorry.
    if (enemies == 0) {
      return;
    }

    // If 75% of the robots are still alive when we die, we'll switch corners.
    if ((enemies - getEnemyCount()) / (double) enemies < .75) {
      if (corner != 270) {
        corner += 90;
      }
      System.out.println("I died and did poorly... switching corner to " + corner);
    } else {
      System.out.println("I died but did well. I will still use corner " + corner);
    }
  }
}
