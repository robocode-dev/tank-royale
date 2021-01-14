package dev.robocode.tankroyale.sample.bots;

import dev.robocode.tankroyale.botapi.Bot;
import dev.robocode.tankroyale.botapi.BotInfo;
import dev.robocode.tankroyale.botapi.events.ScannedBotEvent;
import dev.robocode.tankroyale.botapi.events.WonRoundEvent;

import java.io.IOException;

/**
 * TrackFire - a sample bot, original version by Mathew Nelson for Robocode.
 * Modified by Flemming N. Larsen.
 *
 * <p>Sits still. Tracks and fires at the nearest robot it sees.
 */
public class TrackFire extends Bot {

  /** Main method starts our bot */
  public static void main(String[] args) throws IOException {
    new TrackFire().start();
  }

  /** Constructor, which loads the bot settings file */
  protected TrackFire() throws IOException {
    super(BotInfo.fromFile("/trackfire.properties"));
  }

  /** TrackFire's run method */
  @Override
  public void run() {
    // Set colors
    String pink = "#FF69B4";
    setBodyColor(pink);
    setTurretColor(pink);
    setRadarColor(pink);
    setScanColor(pink);
    setBulletColor(pink);

    // Loop while running
    while (isRunning()) {
      turnGunLeft(10); // Scans automatically
    }
  }

  /** onScannedRobot: We have a target. Go get it. */
  @Override
  public void onScannedBot(ScannedBotEvent e) {

    // Calculate direction of the scanned bot and bearing to it for the gun
    double direction = directionTo(e.getX(), e.getY());
    double bearingFromGun = normalizeRelativeAngle(direction - getGunDirection());

    // Turn the gun toward the scanned bot
    turnGunLeft(bearingFromGun);

    // If it is close enough, fire!
    if (Math.abs(bearingFromGun) <= 3) {
      // We check gun heat here, because calling fire() uses a turn,
      // which could cause us to lose track of the other bot.
      if (getGunHeat() == 0) {
        fire(Math.min(3 - Math.abs(bearingFromGun), getEnergy() - .1));
      }
    }
    // Generates another scan event if we see a robot.
    // We only need to call this if the gun (and therefore radar) are not turning
    // as the radar does not scan if it is not being turned.
    if (bearingFromGun == 0) {
      scan();
    }
  }

  /** onWonRound: Do a victory dance! */
  @Override
  public void onWonRound(WonRoundEvent e) {
    // Victory dance turning right 360 degrees 100 times
    turnLeft(36000);
  }
}
