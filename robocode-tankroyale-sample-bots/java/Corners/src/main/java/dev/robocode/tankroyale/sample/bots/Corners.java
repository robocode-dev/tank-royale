package dev.robocode.tankroyale.sample.bots;

import dev.robocode.tankroyale.botapi.Bot;
import dev.robocode.tankroyale.botapi.BotInfo;
import dev.robocode.tankroyale.botapi.events.BotDeathEvent;
import dev.robocode.tankroyale.botapi.events.ScannedBotEvent;

import java.io.IOException;

public class Corners extends Bot {

  int enemies; // Number of enemy robots in the game
  int corner = 0; // Which corner we are currently using
  volatile boolean stopWhenSeeRobot = false; // See goCorner()

  /** Main method starts our bot */
  public static void main(String[] args) throws IOException {
    new Corners().start();
  }

  /** Constructor, which loads the bot settings file */
  protected Corners() throws IOException {
    super(BotInfo.fromFile("corners.properties"));
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
    // turn to face the wall to the "right" of our desired corner.
    turnLeft(normalizeRelativeDegrees(corner - getDirection()));
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
      // Resume movement
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
  public void onDeath(BotDeathEvent e) {
    // Well, others should never be 0, but better safe than sorry.
    if (enemies == 0) {
      return;
    }

    // If 75% of the robots are still alive when we die, we'll switch corners.
    if ((enemies - getEnemyCount()) / (double) enemies < .75) {
      corner += 90;
      if (corner == 270) {
        corner = -90;
      }
      System.out.println("I died and did poorly... switching corner to " + corner);
    } else {
      System.out.println("I died but did well. I will still use corner " + corner);
    }
  }
}
