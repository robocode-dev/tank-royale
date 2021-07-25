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

  boolean isScanning; // flag set when scanning

  /** Main method starts our bot */
  public static void main(String[] args) throws IOException {
    new TrackFire().start();
  }

  /** Constructor, which loads the bot settings file */
  protected TrackFire() throws IOException {
    super(BotInfo.fromFile("TrackFire.json"));
  }

  /** TrackFire's run method */
  @Override
  public void run() {
    isScanning = false; // reset scanning flag

    // Set colors
    String pink = "#FF69B4";
    setBodyColor(pink);
    setTurretColor(pink);
    setRadarColor(pink);
    setScanColor(pink);
    setBulletColor(pink);

    // Loop while running
    while (isRunning()) {
      if (isScanning) {
        go(); // skip turn if we a scanning
      } else {
        turnGunLeft(10); // Scans automatically as radar is mounted on gun
      }
    }
  }

  /** onScannedBot: We have a target. Go get it. */
  @Override
  public void onScannedBot(ScannedBotEvent e) {
    isScanning = true; // we started scanning

    // Calculate direction of the scanned bot and bearing to it for the gun
    double bearingFromGun = gunBearingTo(e.getX(), e.getY());

    // Turn the gun toward the scanned bot
    turnGunLeft(bearingFromGun);

    // If it is close enough, fire!
    if (Math.abs(bearingFromGun) <= 3 && getGunHeat() == 0) {
      fire(Math.min(3 - Math.abs(bearingFromGun), getEnergy() - .1));
    }

    // Generates another scan event if we see a robot.
    // We only need to call this if the gun (and therefore radar)
    // are not turning. Otherwise, scan is called automatically.
    if (bearingFromGun < 5) {
      scan();
    }

    isScanning = false; // we stopped scanning
  }

  /** onWonRound: Do a victory dance! */
  @Override
  public void onWonRound(WonRoundEvent e) {
    // Victory dance turning right 360 degrees 100 times
    turnLeft(36_000);
  }
}
