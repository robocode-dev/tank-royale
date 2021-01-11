package dev.robocode.tankroyale.sample.bots;

import dev.robocode.tankroyale.botapi.Bot;
import dev.robocode.tankroyale.botapi.BotInfo;
import dev.robocode.tankroyale.botapi.events.HitBotEvent;
import dev.robocode.tankroyale.botapi.events.ScannedBotEvent;

import java.io.IOException;

/**
 * RamFire - a sample bot, original version by Mathew Nelson for Robocode. Modified by Flemming N.
 * Larsen.
 *
 * <p>Drives at robots trying to ram them. Fires when it hits them.
 */
public class RamFire extends Bot {
  int turnDirection = 1; // Clockwise (-1) or counterclockwise (1)

  /** Main method starts our bot */
  public static void main(String[] args) throws IOException {
    new RamFire().start();
  }

  /** Constructor, which loads the bot settings file */
  protected RamFire() throws IOException {
    super(BotInfo.fromFile("/ramfire.properties"));
  }

  /** run: Spin around looking for a target */
  @Override
  public void run() {
    // Set colors
    setBodyColor("#999"); // gray
    setTurretColor("#888"); // gray
    setRadarColor("#666"); // dark gray

    while (isRunning()) {
      if (getDistanceRemaining() == 0) {
        turnLeft(5 * turnDirection);
      }
    }
  }

  /** onScannedRobot: We have a target. Go ram it. */
  @Override
  public void onScannedBot(ScannedBotEvent e) {
    turnToFaceTarget(e.getX(), e.getY());

    double distance = distanceTo(e.getX(), e.getY());
    forward(distance + 5);
  }

  /** onHitBot: Turn to face robot, fire hard, and ram him again! */
  @Override
  public void onHitBot(HitBotEvent e) {
    turnToFaceTarget(e.getX(), e.getY());

    // Determine a shot that won't kill the robot...
    // We want to ram him instead for bonus points
    if (e.getEnergy() > 16) {
      fire(3);
    } else if (e.getEnergy() > 10) {
      fire(2);
    } else if (e.getEnergy() > 4) {
      fire(1);
    } else if (e.getEnergy() > 2) {
      fire(.5);
    } else if (e.getEnergy() > .4) {
      fire(.1);
    }
    forward(40); // Ram him again!
  }

  /**
   * turnToFaceTarget: Method that turns the bot to face the target at coordinate x,y, but also sets
   * the default turn direction used if no bot is being scanned within in the run() method.
   */
  private void turnToFaceTarget(double x, double y) {
    double bearing = bearingTo(x, y);
    if (bearing >= 0) {
      turnDirection = 1;
    } else {
      turnDirection = -1;
    }
    turnLeft(bearing);
  }
}
