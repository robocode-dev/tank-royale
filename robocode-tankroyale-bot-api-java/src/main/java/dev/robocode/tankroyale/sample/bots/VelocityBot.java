package dev.robocode.tankroyale.sample.bots;

import dev.robocode.tankroyale.botapi.Bot;
import dev.robocode.tankroyale.botapi.BotInfo;
import dev.robocode.tankroyale.botapi.events.BulletHitBotEvent;
import dev.robocode.tankroyale.botapi.events.HitWallEvent;
import dev.robocode.tankroyale.botapi.events.NextTurnCondition;
import dev.robocode.tankroyale.botapi.events.ScannedBotEvent;

import java.io.IOException;

/**
 * VelocityBot - a sample bot, original version by Joshua Galecki for Robocode. Modified by Flemming
 * N. Larsen.
 *
 * <p>This is a sample of a robot using the turn rates and target speed to move the bot.
 */
public class VelocityBot extends Bot {

  /** Main method starts our bot */
  public static void main(String[] args) throws IOException {
    new VelocityBot().start();
  }

  /** Constructor, which loads the bot settings file */
  protected VelocityBot() throws IOException {
    super(BotInfo.fromFile("/velocitybot.properties"));
  }

  /** VelocityBot's run method */
  @Override
  public void run() {
    setGunTurnRate(15);

    while (isRunning()) {
      if (getTurnNumber() % 64 == 0) {
        // Straighten out, if we were hit by a bullet and are turning
        setTurnRate(0);
        // Go forward with a target speed of 4
        setTargetSpeed(4);
      }
      if (getTurnNumber() % 64 == 32) {
        // Go backwards, faster
        setTargetSpeed(-6);
      }

      // Execute (send commands to server)
      go();
      // Wait for the next turn before continuing the loop
      waitFor(new NextTurnCondition(this));
    }
  }

  @Override
  public void onScannedBot(ScannedBotEvent e) {
    fire(1);
  }

  @Override
  public void onHitByBullet(BulletHitBotEvent e) {
    // Turn to confuse the other robot
    setTurnRate(5);
  }

  @Override
  public void onHitWall(HitWallEvent e) {
    // Move away from the wall
    setTargetSpeed(-1 * getTargetSpeed());
  }
}
